package com.whueric.dataonoff;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by echunwu on 2/23/14.
 */

public final class Utility
{

    private static final Utility INSTANCE = new Utility();
    public static String WHEN_SCREEN_ON = "0";
    public static String WHEN_DEVICE_UNLOCKED = "1";
    public static String TURNOFF_IMMMEDIATELY = "0";
    public static String ONOFF_FOR_DATA = "0";
    public static String ONOFF_FOR_WIFI = "1";
    public static String ONOFF_FOR_BOTH = "2";
    static int NOTIFICATION_ID_ICON = 1;

    private Utility()
    {
        if (INSTANCE != null)
        {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static Utility getInstance()
    {
        return INSTANCE;
    }

    public void addNotification(Context context)
    {
        Intent notificationIntent = new Intent(context, OffOnActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        Bitmap icon = BitmapFactory.decodeResource(context.getApplicationContext().getResources(), R.drawable.ic_launcher);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        Notification notification = new Notification.Builder(context.getApplicationContext()).setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.notification_content))
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(icon)
                        //.addAction(R.drawable.disable, "Disable", pi)
                .build();


        // 将此通知放到通知栏的"Ongoing"即"正在运行"组中
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        // 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用
        notification.flags |= Notification.FLAG_NO_CLEAR;
        //n.flags |= Notification.FLAG_AUTO_CANCEL;

        notification.contentIntent = pi;
        //notification.setLatestEventInfo(this, "FloatsWindow", "start!", pi);
        nm.notify(NOTIFICATION_ID_ICON, notification);

    }

    public void removeNotification(Context context)
    {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID_ICON);
    }
}
