package com.whueric.dataonoff;

/**
 * Created by echunwu on 2/19/14.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootBroadcastReceiver extends BroadcastReceiver
{
    static final String action_boot = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(action_boot))
        {
            //Toast.makeText(context, "onReceived", Toast.LENGTH_LONG).show();
            Intent startServiceIntent = new Intent(context, AutoOffOnService.class);
            startServiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            SharedPreferences settings = context.getSharedPreferences("Config", Context.MODE_PRIVATE);
            boolean isEnabled = settings.getBoolean("isEnabledService", true);
            if (isEnabled)
                context.startService(startServiceIntent);
        }
    }

}
