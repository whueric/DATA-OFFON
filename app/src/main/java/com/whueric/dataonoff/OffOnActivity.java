package com.whueric.dataonoff;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Switch;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author whueric@gmail.com
 * @version 0.1
 */

public class OffOnActivity extends Activity
{
    private static final int REQ_SYSTEM_SETTINGS = 0;
    private MobileDataAdmin mda = null;
    private WiFiAdmin wfa = null;
    private Switch switchEnable = null;
    private TextView textLog = null;
    private ServiceManager service;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);

        try
        {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null)
            {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        }
        catch (Exception ex)
        {
            // Ignore
        }

        switchEnable = (Switch) findViewById(R.id.switchEnable);

        textLog = (TextView) findViewById(R.id.textView_status);
        textLog.setMovementMethod(new ScrollingMovementMethod());

        mda = new MobileDataAdmin(this.getApplicationContext());
        wfa = new WiFiAdmin(this.getApplicationContext());

        addMessage(getResources().getString(R.string.app_ins));
        if (mda.isDataEnabled())
            addMessage(getResources().getString(R.string.status_data_on));
        else
            addMessage(getResources().getString(R.string.status_data_off));

        if (wfa.isWiFiEnabled())
            addMessage(getResources().getString(R.string.status_wifi_on));
        else
            addMessage(getResources().getString(R.string.status_wifi_off));


        this.service = new ServiceManager(this, AutoOffOnService.class, new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                switch (msg.what)
                {
                    case AutoOffOnService.MSG_NETWORK:
                        addMessage(msg.getData().getString("status"));
                        break;

                    default:
                        super.handleMessage(msg);
                }
            }
        });


        /*
        Display display = getWindowManager().getDefaultDisplay();
        int width = 0, height = 0;
        if(android.os.Build.VERSION.SDK_INT < 13)
        {
            height = display.getHeight();
            width = display.getWidth();
        }
        else
        {
            Point size = new Point();
            display.getSize(size);
            width = size.x;
            height = size.y;
        }

        if(height > 800)
        {
            displayConnectInfo();
        }
        else
        {
            hideConnectInfo();
        }
        */

        //service_intent = new Intent(OffOnActivity.this, AutoOffOnService.class);

        if (isFirstRun())
        {
            switchEnable.setChecked(true);
            service.start();
            setFirstRun(false);
            setEnabledService(true);
        }
        else
        {
            if (isEnabledService())
            {
                switchEnable.setChecked(true);
                if (service.isRunning())
                    service.stop();
                service.start();
            }
            else
            {
                switchEnable.setChecked(false);
                if (service.isRunning())
                    service.stop();
            }
        }

        if (isShowNotification())
        {
            addNotification();
        }
        else
        {
            removeNotification();
        }

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        switchEnable.setChecked(isEnabledService());
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        try
        {
            service.unbind();
        }
        catch (Throwable t)
        {
            Log.e("MainActivity", "Failed to unbind from the service", t);
        }
    }

    /*
    private void displayConnectInfo()
    {
        String connInfo = "";
        if (wfa.isConnected())
            connInfo = getString(R.string.wifi_content) + wfa.getSSID();
        connInfo += "\n" + getString(R.string.connInfo) + mda.getNetworkType();

        connInfo_tv.setText(connInfo);
    }

    private void hideConnectInfo()
    {
        connInfo_tv.setVisibility(View.INVISIBLE);
    }
    */

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            //land
        }
        else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            //port
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            startActivityForResult(new Intent(this, SetPreferenceActivity.class), REQ_SYSTEM_SETTINGS);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQ_SYSTEM_SETTINGS)
        {
            //取得属于整个应用程序的SharedPreferences
            if (isShowNotification())
            {
                addNotification();
                //notification_chkbox.setChecked(true);
            }
            else
            {
                removeNotification();
                //notification_chkbox.setChecked(false);
            }
        }
        else
        {
            //其他Intent返回的结果
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // function to append a string to a TextView as a new line
    // and scroll to the bottom if needed
    private void addMessage(String msg)
    {
        if (textLog != null)
        {
            String format = "HH:mm:ss";
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            String timestamp = sdf.format(new Date()) + " ";

            textLog.append(timestamp + msg + "\n");
            final Layout layout = textLog.getLayout();
            if (layout != null)
            {
                int scrollDelta = layout.getLineBottom(textLog.getLineCount() - 1)
                        - textLog.getScrollY() - textLog.getHeight();
                if (scrollDelta > 0)
                    textLog.scrollBy(0, scrollDelta);
            }
        }
    }

    private void addNotification()
    {
        Utility.getInstance().addNotification(this);
    }

    private void removeNotification()
    {
        Utility.getInstance().removeNotification(this);
    }


    private boolean isShowNotification()
    {
        SharedPreferences settings = this.getSharedPreferences(this.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
        return settings.getBoolean("show_notification", false);
    }

    private boolean isFirstRun()
    {
        SharedPreferences settings = this.getSharedPreferences(this.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
        return settings.getBoolean("isFirstRun", true);
    }

    private void setFirstRun(boolean flag)
    {
        SharedPreferences settings = this.getSharedPreferences(this.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor edit = settings.edit();
        edit.putBoolean("isFirstRun", flag);
        edit.commit();
    }

    private boolean isEnabledService()
    {
        SharedPreferences settings = this.getSharedPreferences(this.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
        return settings.getBoolean("isEnabledService", true);
    }

    private void setEnabledService(boolean flag)
    {
        SharedPreferences settings = this.getSharedPreferences(this.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor edit = settings.edit();
        edit.putBoolean("isEnabledService", flag);
        edit.commit();
    }

    public void onSwitchClicked(View view)
    {
        // Is the toggle on?
        boolean on = ((Switch) view).isChecked();

        if (on)
        {
            // Enable
            setEnabledService(true);
            addMessage(getResources().getString(R.string.app_ins));
            if (service.isRunning())
                service.stop();
            service.start();
            if (isShowNotification())
                addNotification();
        }
        else
        {
            // Disable
            addMessage(getResources().getString(R.string.app_oos));
            setEnabledService(false);
            if (service.isRunning())
                service.stop();
            removeNotification();
        }
    }
}