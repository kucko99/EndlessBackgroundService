package kucko.test.endlessbackgroundservice.services;

import static kucko.test.endlessbackgroundservice.MainActivity.LOG_TAG;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TwentyFourHoursTimer extends BroadcastReceiver
{
    private static final String TAG_CLASS = "TwentyFourHoursTimer::";

    @Override
    public void onReceive( Context context, Intent intent )
    {
        Intent checkUpdateService = new Intent( context, CheckingCloudService.class );
        Log.d( LOG_TAG,TAG_CLASS + " onReceive() - Starting service to check update on Cloud." );

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O )
        {
            context.startForegroundService( checkUpdateService );
        }
        else
        {
            context.startService( checkUpdateService );
        }
    }

    public void setUpAlarm( Context context, boolean restart )
    {
        AlarmManager am = ( AlarmManager ) context.getSystemService( Context.ALARM_SERVICE );
        Intent intent = new Intent( context, TwentyFourHoursTimer.class );
        PendingIntent pendingIntent = PendingIntent.getBroadcast( context, 0, intent, 0 );

        if ( restart )
        {
            am.cancel( pendingIntent );
        }

        am.setRepeating( AlarmManager.RTC, System.currentTimeMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent );
    }
}
