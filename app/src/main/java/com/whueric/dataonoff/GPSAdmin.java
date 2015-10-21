package com.whueric.dataonoff;

import android.content.Context;

/**
 * Created by echunwu on 1/21/14.
 */
public class GPSAdmin
{
    private Context m_context = null;

    public GPSAdmin(Context context)
    {
        this.m_context = context;
    }
    /*
    private void toggleGPS()
	{
		Intent gpsIntent = new Intent();
		gpsIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
		gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
		gpsIntent.setData(Uri.parse("custom:3"));
		try
		{
			PendingIntent.getBroadcast(m_context, 0, gpsIntent, 0).send();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
    */
}
