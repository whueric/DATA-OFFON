package com.whueric.dataonoff;

/**
 * Created by echunwu on 2/19/14.
 */

import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class AutoOffOnService extends AbstractService
{
    public static final int MSG_NETWORK = 1;

    private final int TURN_OFF = 1;
    private ScreenObserver mScreenObserver;
    private DeviceStateObserver mDeviceStateObserver;
    private MobileDataAdmin mda = null;
    private WiFiAdmin wfa = null;
    private boolean wifiDisabledByMe = false;
    private boolean dataDisabledByMe = false;
    private boolean isTimeStart = false;

    private Handler handler;
    private Timer timer;
    //private String on_when = Utility.WHEN_SCREEN_ON;  //when screen is on
    //private String off_latency = Utility.TURNOFF_IMMMEDIATELY;   //immediately
    //private boolean show_notification = false;

    @Override
    public void onCreate()
    {
        super.onCreate();

        mda = new MobileDataAdmin(this.getApplicationContext());
        wfa = new WiFiAdmin(this.getApplicationContext());

        mScreenObserver = new ScreenObserver(this);
        mScreenObserver.requestScreenStateUpdate(new ScreenObserver.ScreenStateListener()
        {
            @Override
            public void onScreenOn()
            {
                ActionOnScreenOn();
            }

            @Override
            public void onScreenOff()
            {
                ActionOnScreenOff();
            }
        });

        mDeviceStateObserver = new DeviceStateObserver(this);
        mDeviceStateObserver.requestDeviceStateUpdate(new DeviceStateObserver.DeviceStateListener()
        {
            @Override
            public void onDeviceUnlocked()
            {
                ActionOnDeviceUnlocked();
            }

            @Override
            public void onDeviceLocked()
            {
                ActionOnDeviceLocked();
            }

        });


        handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                switch (msg.what)
                {
                    case TURN_OFF:
                        turnOff();
                        break;
                }
                super.handleMessage(msg);
            }

        };

        Log.d("AutoOffOnService", "OnCreate");
        //android.os.Debug.waitForDebugger();
    }

    @Override
    public void onStartService()
    {
        if (isShowNotification())
            Utility.getInstance().addNotification(this.getApplicationContext());
    }

    @Override
    public void onStopService()
    {
        sendTextMsg(getResources().getString(R.string.app_oos));
        mScreenObserver.stopScreenStateUpdate();
        mDeviceStateObserver.stopDeviceStateUpdate();

        Utility.getInstance().removeNotification(this.getApplicationContext());
    }

    @Override
    public void onReceiveMessage(Message msg)
    {

    }

    private void ActionOnDeviceLocked()
    {
        sendTextMsg(getResources().getString(R.string.device_is_locked));
    }

    private void ActionOnDeviceUnlocked()
    {
        sendTextMsg(getResources().getString(R.string.device_unlocked));

        if (getOnWhen().equals(Utility.WHEN_DEVICE_UNLOCKED))
        {
            turnOn();
        }
    }

    private void ActionOnScreenOn()
    {
        sendTextMsg(getResources().getString(R.string.device_screen_on));
        sendTextMsg(getResources().getString(R.string.stop_timer));
        stopOffTimer();

        //if device already get unlocked, turn on
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        boolean locked = km.inKeyguardRestrictedInputMode();
        if (false == locked)
            turnOn();
        else if (getOnWhen().equals(Utility.WHEN_DEVICE_UNLOCKED))
            return;
        else
            turnOn();
    }

    private void ActionOnScreenOff()
    {
        sendTextMsg(getResources().getString(R.string.device_screen_off));

        if (getOffLatency().equals(Utility.TURNOFF_IMMMEDIATELY))
        {
            turnOff();
        }
        else        //turn off latency
        {
            sendTextMsg(getResources().getString(R.string.start_timer));
            startOffTimer();
        }

    }

    private void startOffTimer()
    {
        if (!isTimeStart)
        {
            // 创建定时器
            TimerTask task = new TimerTask()
            {
                @Override
                public void run()
                {
                    Message message = new Message();
                    message.what = TURN_OFF;
                    handler.sendMessage(message);
                }
            };
            timer = new Timer(true);
            timer.schedule(task, Integer.valueOf(getOffLatency()) * 60 * 1000); //off_latency in minutes
        }

        isTimeStart = true;// 修改标记为true，防止第二次开启
    }

    private void stopOffTimer()
    {
        if (isTimeStart)
        {
            timer.cancel();
            timer.purge();
            timer = null;
            isTimeStart = false;
        }
    }


    private void turnOn()
    {

        if (getOnOffFor().equals(Utility.ONOFF_FOR_WIFI) || getOnOffFor().equals(Utility.ONOFF_FOR_BOTH))
        {
            if (wifiDisabledByMe && wfa.isWiFiEnabled() == false)
            {
                wfa.openWifi();
                sendTextMsg(getResources().getString(R.string.turn_on_wifi));
            }
        }

        if (getOnOffFor().equals(Utility.ONOFF_FOR_DATA) || getOnOffFor().equals(Utility.ONOFF_FOR_BOTH))
        {
            if (dataDisabledByMe && mda.isDataEnabled() == false)
            {
                sendTextMsg(getResources().getString(R.string.turn_on_data));
                mda.setMobileDataEnabled2(true);
            }
        }
    }

    private void turnOff()
    {
        if (getOnOffFor().equals(Utility.ONOFF_FOR_WIFI) || getOnOffFor().equals(Utility.ONOFF_FOR_BOTH))
        {
            if (wfa.isWiFiEnabled())
            {
                wifiDisabledByMe = true;
                wfa.closeWifi();
                sendTextMsg(getResources().getString(R.string.turn_off_wifi));
            }
            else
                wifiDisabledByMe = false;
        }

        if (getOnOffFor().equals(Utility.ONOFF_FOR_DATA) || getOnOffFor().equals(Utility.ONOFF_FOR_BOTH))
        {
            if (mda.isDataEnabled())
            {
                dataDisabledByMe = true;
                mda.setMobileDataEnabled2(false);
                sendTextMsg(getResources().getString(R.string.turn_off_data));
            }
            else
                dataDisabledByMe = false;
        }
    }

    private String getOnWhen()
    {
        SharedPreferences settings = this.getSharedPreferences(this.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
        return settings.getString("on_when", Utility.WHEN_SCREEN_ON);
    }

    private String getOffLatency()
    {
        SharedPreferences settings = this.getSharedPreferences(this.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
        return settings.getString("off_latency", Utility.TURNOFF_IMMMEDIATELY);

    }

    private String getOnOffFor()
    {
        SharedPreferences settings = this.getSharedPreferences(this.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
        return settings.getString("onoff_for", Utility.ONOFF_FOR_BOTH);

    }

    private boolean isShowNotification()
    {
        SharedPreferences settings = this.getSharedPreferences(this.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
        return settings.getBoolean("show_notification", false);
    }
}
