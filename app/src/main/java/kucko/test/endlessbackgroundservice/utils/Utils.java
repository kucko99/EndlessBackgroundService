package kucko.test.endlessbackgroundservice.utils;

import static kucko.test.endlessbackgroundservice.MainActivity.LOG_TAG;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

public class Utils
{
    private static final String TAG_CLASS = "Utils::";

    public Utils()
    {
    }

    public static int getBatteryPercentage( Context context )
    {
        IntentFilter ifilter = new IntentFilter( Intent.ACTION_BATTERY_CHANGED );
        Intent batteryStatus = context.registerReceiver( null, ifilter );
        int level = batteryStatus.getIntExtra( BatteryManager.EXTRA_LEVEL, -1 );
        int scale = batteryStatus.getIntExtra( BatteryManager.EXTRA_SCALE, -1 );
        float batteryPct = level / (float)scale;
        int percentage = (int)(batteryPct * 100);

        Log.d( LOG_TAG, TAG_CLASS + "getBatteryPercentage() - Battery percentage = " + String.valueOf( percentage ) );
        return( percentage );
    }
}
