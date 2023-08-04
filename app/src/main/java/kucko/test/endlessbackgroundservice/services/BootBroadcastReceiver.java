package kucko.test.endlessbackgroundservice.services;

import static kucko.test.endlessbackgroundservice.MainActivity.LOG_TAG;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BootBroadcastReceiver extends BroadcastReceiver
{
    private static final String TAG_CLASS = "BootBroadcastReceiver::";

    public void onReceive( Context context, Intent intent )
    {
        if( intent.getAction().equals( Intent.ACTION_BOOT_COMPLETED ) )
        {
            Log.d( LOG_TAG, TAG_CLASS + " onReceive() - Init and start service." );
            startService( context, "", "", "" );
        }
    }

    public static void startService( Context context, String NFUHW, String NFUFW, String ECRFW )
    {
        Log.d( LOG_TAG, TAG_CLASS + " startService() - starting service." );
        Intent updateOSService = new Intent( context, UpdateOSService.class );
        updateOSService.setAction( UpdateOSService.ACTIONS.START_SERVICE.toString() );

        if( !NFUHW.isEmpty() || !NFUFW.isEmpty() || !ECRFW.isEmpty() )
        {
            updateOSService.putExtra( "NFUHW", NFUHW );
            updateOSService.putExtra( "NFUFW", NFUFW );
            updateOSService.putExtra( "ECRFW", ECRFW );
        }

        if ( !isServiceRunning( context, UpdateOSService.class ) )
        {
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O )
            {
                context.startForegroundService( updateOSService );
            }
            else
            {
                context.startService( updateOSService );
            }
        }
    }

    public static boolean isServiceRunning( Context context, Class<?> serviceClass )
    {
        ActivityManager manager = ( ActivityManager ) context.getSystemService( Context.ACTIVITY_SERVICE );
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices( Integer.MAX_VALUE );

        for ( ActivityManager.RunningServiceInfo serviceInfo : runningServices )
        {
            if ( serviceInfo.service.getClassName().equals( serviceClass.getName() ) )
            {
                return true;
            }
        }
        return false;
    }
}
