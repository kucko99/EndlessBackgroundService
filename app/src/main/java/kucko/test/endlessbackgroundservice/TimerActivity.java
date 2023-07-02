package kucko.test.endlessbackgroundservice;

import static kucko.test.endlessbackgroundservice.MainActivity.LOG_TAG;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import kucko.test.endlessbackgroundservice.services.CheckingCloudService;

public class TimerActivity extends AppCompatActivity {
    private static final String TAG_CLASS = "TimerActivity::";

    private final long interval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer_activity);

        intent = new Intent(this, CheckingCloudService.class);

        Log.d( LOG_TAG, TAG_CLASS + " created Timer Activity." );
    }

    public void startTimerMethode( View view )
    {
        AlarmManager am = ( AlarmManager ) getSystemService( Context.ALARM_SERVICE );
        PendingIntent pendingIntent = PendingIntent.getBroadcast( this, 0, intent, 0 );

        am.setRepeating( AlarmManager.RTC, System.currentTimeMillis(), interval, pendingIntent );
        Log.d( LOG_TAG, TAG_CLASS + " startTimerMethode(): starting Timer." );
    }

    public void restartTimerMethode( View view )
    {
        AlarmManager am = ( AlarmManager ) getSystemService( Context.ALARM_SERVICE );
        PendingIntent pendingIntent = PendingIntent.getBroadcast( this, 0, intent, 0 );

        am.cancel( pendingIntent );

        am.setRepeating( AlarmManager.RTC, System.currentTimeMillis(), interval, pendingIntent );
        Log.d( LOG_TAG, TAG_CLASS + " restartTimerMethode(): restarting Timer." );
    }
}