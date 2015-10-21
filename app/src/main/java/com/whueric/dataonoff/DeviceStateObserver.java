package com.whueric.dataonoff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.lang.reflect.Method;

public class DeviceStateObserver
{
    private static String TAG = "DeviceStateObserver";
    private static Method mReflectScreenState;
    private Context mContext;
    private DeviceStateBroadcastReceiver mDeviceReceiver;
    private DeviceStateListener mDeviceStateListener;

    public DeviceStateObserver(Context context)
    {
        mContext = context;
        mDeviceReceiver = new DeviceStateBroadcastReceiver();
    }

    /**
     * 请求screen状态更新
     *
     * @param listener
     */
    public void requestDeviceStateUpdate(DeviceStateListener listener)
    {
        mDeviceStateListener = listener;
        startDeviceStateBroadcastReceiver();
        firstGetDeviceState();
    }

    /**
     * 第一次请求screen状态
     */
    private void firstGetDeviceState()
    {
    }

    /**
     * 停止screen状态更新
     */
    public void stopDeviceStateUpdate()
    {
        mContext.unregisterReceiver(mDeviceReceiver);
    }

    /**
     * 启动screen状态广播接收器
     */
    private void startDeviceStateBroadcastReceiver()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);
        //filter.addAction(Intent.ACTION_USER_BACKGROUND);
        mContext.registerReceiver(mDeviceReceiver, filter);
    }

    public interface DeviceStateListener
    {
        public void onDeviceUnlocked();

        public void onDeviceLocked();
    }

    public class DeviceStateBroadcastReceiver extends BroadcastReceiver
    {
        public DeviceStateBroadcastReceiver()
        {
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            //device gets unlocked
            if (intent != null && Intent.ACTION_USER_PRESENT.equals(intent.getAction()))
            {
                mDeviceStateListener.onDeviceUnlocked();
            }
        }
    }
}


