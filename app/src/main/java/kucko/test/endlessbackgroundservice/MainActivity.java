package kucko.test.endlessbackgroundservice;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity
{
    public static final String LOG_TAG = "EndlessBackgrSer";
    private static final String TAG_CLASS = "MainActivity::";

    private TextView text;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        IntentFilter filter = new IntentFilter();
        filter.addAction( UpdateOSService.UPDATE_OS_LOGIN );
        registerReceiver( broadcastReceiver, filter );

        text = ( TextView ) findViewById( R.id.login );

        boolean isMyServiceRunning = isServiceRunning( this, UpdateOSService.class );
        Log.d( LOG_TAG, TAG_CLASS + " is service running? - " + ( isMyServiceRunning ? "YES" : "NO" ));

        if( !isMyServiceRunning )
        {
            BootBroadcastReceiver.startService( this, UpdateOSService.NFUHW, UpdateOSService.NFUFW, UpdateOSService.ECRFW );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver );
    }

    private void readLoginAndRefreshUI( String loginFromService )
    {
        text.setText( loginFromService );
    }

    public void updatedDevice(View view) {
        sendBroadcast( new Intent().setAction( UpdateOSService.UPDATE_OS_UPDATED_DEVICE ) );
        Log.d( LOG_TAG, TAG_CLASS + " sending device is updated broadcast." );
    }

    public void downloadUpdate( View view )
    {
        sendBroadcast( new Intent().setAction( UpdateOSService.UPDATE_OS_DOWNLOAD_UPDATE ) );
        Log.d( LOG_TAG, TAG_CLASS + " sending download broadcast." );
    }

    public void newUpdate( View view )
    {
        sendBroadcast( new Intent().setAction( UpdateOSService.UPDATE_OS_NEW_UPDATE ) );
        Log.d( LOG_TAG, TAG_CLASS + " sending new update broadcast." );
    }

    public void endService( View view )
    {
        sendBroadcast( new Intent().setAction( UpdateOSService.UPDATE_OS_END_SERVICE ) );
        Log.d( LOG_TAG, TAG_CLASS + " sending end service broadcast." );
    }

    public void readVersions( View view )
    {
        String result = "NULL";
        if ( UpdateOSService.NFUHW != null )
        {
            result = "NFUHW: " + UpdateOSService.NFUHW + "\n";
            result += "NFUFW: " + UpdateOSService.NFUFW + "\n";
            result += "ECRFW: " + UpdateOSService.ECRFW;
        }
        text.setText( result );
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d( LOG_TAG, TAG_CLASS + " getting broadcast: " + intent.getAction() );
            if ( intent.getAction().equals( UpdateOSService.UPDATE_OS_LOGIN ) )
            {
                String loginFromService = intent.getStringExtra( "login" );
                readLoginAndRefreshUI( loginFromService );
            }
        }
    };

    public void threadsTest( View view )
    {
        Intent threadsActivityIntent = new Intent( this, ThreadsActivity.class );
        startActivity( threadsActivityIntent );
    }

    public boolean isServiceRunning( Context context, Class<?> serviceClass )
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