package com.whueric.dataonoff;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by echunwu on 12/4/13.
 */
public class MobileDataAdmin extends NetworkAdmin
{
    private ConnectivityManager m_connManager = null;

    private TelephonyManager m_telemanager = null;

    public MobileDataAdmin(Context context)
    {
        super(context);
        m_connManager = (ConnectivityManager) m_context.getSystemService(Context.CONNECTIVITY_SERVICE);
        m_telemanager = (TelephonyManager) m_context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    // 打开或关闭GPRS
    public boolean setMobileDataEnabled2(boolean bEnable)
    {
        boolean isOpen = isGprsOpen();
        if (isOpen == !bEnable)
        {
            //setGprsEnabled("setMobileDataEnabled", bEnable);
            setMobileDataEnabled(bEnable);
        }

        return isOpen;
    }

    public boolean setMobileDataEnabled3(boolean bEnable)
    {
        // m_connManager.setMobileDataEnabled(bEnable);
        return true;
    }

    // 检测GPRS是否打开
    private boolean isGprsOpen()
    {
        Class cmClass = m_connManager.getClass();
        Class[] argClasses = null;
        Object[] argObject = null;

        Boolean isOpen = false;
        try
        {
            Method method = cmClass.getMethod("getMobileDataEnabled", argClasses);
            isOpen = (Boolean) method.invoke(m_connManager, argObject);
        }
        catch (Exception e)
        {
            Log.e("isGprsOpen", e.getMessage());
        }

        return isOpen;
    }

    // 开启/关闭GPRS
    private void setGprsEnabled(String methodName, boolean isEnable)
    {
        Class cmClass = m_connManager.getClass();
        Class[] argClasses = new Class[1];
        argClasses[0] = boolean.class;

        try
        {
            Method method = cmClass.getMethod(methodName, argClasses);
            method.invoke(m_connManager, isEnable);
        }
        catch (Exception e)
        {
            Log.e("setGprsEnabled", e.getMessage());
        }
    }

    public void setMobileDataState(boolean mobileDataEnabled)
    {
        try
        {
            TelephonyManager telephonyService = (TelephonyManager) m_context.getSystemService(Context.TELEPHONY_SERVICE);

            Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);

            if (null != setMobileDataEnabledMethod)
            {
                setMobileDataEnabledMethod.invoke(telephonyService, mobileDataEnabled);
            }
        }
        catch (Exception ex)
        {
            Log.e("setMobileDataState", "Error setting mobile data state", ex);
        }
    }

    public void setMobileDataEnabled(boolean enabled)
    {
        if (Build.VERSION.SDK_INT >= 21)
        {
            setMobileDataState(enabled);
        }
        else
        if (android.os.Build.VERSION.SDK_INT >= 9)
        {
            try
            {
                final Class conmanClass = Class.forName(m_connManager.getClass().getName());
                final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
                iConnectivityManagerField.setAccessible(true);

                final Object iConnectivityManager = iConnectivityManagerField.get(m_connManager);
                final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
                final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
                setMobileDataEnabledMethod.setAccessible(true);

                setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
            }
            catch (Exception e)
            {
                Log.e("setMobileDataEnabled", e.getMessage());
            }
        }
        else
        {

            Method dataConnSwitchmethod;
            Class telephonyManagerClass;
            Object ITelephonyStub;
            Class ITelephonyClass;

            try
            {
                telephonyManagerClass = Class.forName(m_telemanager.getClass().getName());
                Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
                getITelephonyMethod.setAccessible(true);
                ITelephonyStub = getITelephonyMethod.invoke(m_telemanager);
                ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());

                if (enabled)
                {
                    dataConnSwitchmethod = ITelephonyClass.getDeclaredMethod("enableDataConnectivity");
                }
                else
                {
                    dataConnSwitchmethod = ITelephonyClass.getDeclaredMethod("disableDataConnectivity");
                }
                dataConnSwitchmethod.setAccessible(true);
                dataConnSwitchmethod.invoke(ITelephonyStub);
            }
            catch (Exception e)
            {
                Log.e("setMobileDataEnabled", e.getMessage());
            }
        }
    }

    public boolean isDataEnabled()
    {
        boolean mobileDataEnabled = false; // Assume disabled
        if (android.os.Build.VERSION.SDK_INT >= 9)
        {
            try
            {
                Class cmClass = Class.forName(m_connManager.getClass().getName());
                Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
                method.setAccessible(true); // Make the method callable
                // get the setting for "mobile data"
                mobileDataEnabled = (Boolean) method.invoke(m_connManager);
            }
            catch (Exception e)
            {
                Log.e("isDataEnabled", e.getMessage());
            }
        }
        else
        {
            if (m_telemanager.getDataState() == TelephonyManager.DATA_CONNECTED)
            {
                mobileDataEnabled = true;
            }
            else
            {
                mobileDataEnabled = false;
            }
        }
        return mobileDataEnabled;
    }

    public String getNetworkType()
    {
        switch (m_telemanager.getNetworkType())
        {

            case TelephonyManager.NETWORK_TYPE_1xRTT: // Current network is 1xRTT
            case TelephonyManager.NETWORK_TYPE_CDMA: // Current network is CDMA: Either IS95A or IS95B
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EDGE: // Current network is EDGE
            case TelephonyManager.NETWORK_TYPE_GPRS: // Current network is GPRS
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_EHRPD: // Current network is eHRPD
                return "eHRPD";
            case TelephonyManager.NETWORK_TYPE_EVDO_0: // Current network is EVDO revision 0
            case TelephonyManager.NETWORK_TYPE_EVDO_A: // Current network is EVDO revision A
            case TelephonyManager.NETWORK_TYPE_EVDO_B: // Current network is EVDO revision B
                return "EVDO";
            case TelephonyManager.NETWORK_TYPE_HSDPA: // Current network is HSDPA
            case TelephonyManager.NETWORK_TYPE_HSPA: // Current network is HSPA
            case TelephonyManager.NETWORK_TYPE_HSPAP: // Current network is HSPA+
            case TelephonyManager.NETWORK_TYPE_HSUPA: // Current network is HSUPA
                return "WCDMA";
            case TelephonyManager.NETWORK_TYPE_IDEN: // Current network is iDen
                return "iDen";
            case TelephonyManager.NETWORK_TYPE_LTE: // Current network is LTE
            case TelephonyManager.NETWORK_TYPE_UMTS: // Current network is UMTS
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN: // Network type is unknown
                return "Data";
        }
        return "Data";
    }


}
