package kucko.test.endlessbackgroundservice;

import static kucko.test.endlessbackgroundservice.MainActivity.LOG_TAG;
import static kucko.test.endlessbackgroundservice.UpdateOSServiceState.setServiceState;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class UpdateOSService extends Service
{
    public enum ACTIONS
    {
        START_SERVICE( "START_SERVICE" ),
        STOP_SERVICE( "STOP_SERVICE" ),
        DOWNLOAD_UPDATE( "DOWNLOAD_UPDATE" ),
        ;

        private final String text;
        ACTIONS( final String text )
        {
            this.text = text;
        }

        @NonNull
        @Override
        public String toString()
        {
            return text;
        }
    }

    private static final String TAG_CLASS = "UpdateOSService::";

    public static String NFUHW;
    public static String NFUFW;
    public static String ECRFW;

    private PowerManager.WakeLock wakeLock;
    private boolean isServiceStarted = false;

    private UpdateNotification updateNotification;
    private IntentFilter broadcastFilter;

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d( LOG_TAG, TAG_CLASS + " the service has been created." );

        createBroadcastReceiver();
        registerReceiver( broadcastReceiver, broadcastFilter );

        updatedDevice();
    }

    private void createBroadcastReceiver()
    {
        broadcastFilter = new IntentFilter();
        broadcastFilter.addAction( UPDATE_OS_NEW_UPDATE );
        broadcastFilter.addAction( UPDATE_OS_END_SERVICE );
        broadcastFilter.addAction( UPDATE_OS_DOWNLOAD_UPDATE );
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId )
    {
        Log.d( LOG_TAG, TAG_CLASS + " the service executed with startID: " + startId );

        if ( intent != null && !intent.getAction().isEmpty() )
        {
            String action = intent.getAction();
            Log.d( LOG_TAG, TAG_CLASS + " using an intent with action " + action );

            if ( action.equals( ACTIONS.START_SERVICE.text ) )
            {
                startService();
            }
            else
            {
                Log.d( LOG_TAG, TAG_CLASS + " no action in the received intent." );
            }
        }
        else
        {
            Log.d( LOG_TAG, TAG_CLASS + " with a null intent. It has been probably restarted by the system." );
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver( broadcastReceiver );
        isServiceStarted = false;
        Log.d( LOG_TAG, TAG_CLASS + " the service has been destroyed." );

        BootBroadcastReceiver.startService( this );
    }

    @Override
    public void onTaskRemoved( Intent rootIntent )
    {
        super.onTaskRemoved( rootIntent );
    }

    private void startService()
    {
        if ( isServiceStarted )
            return;

        isServiceStarted = true;
        setServiceState( this, UpdateOSServiceState.ServiceState.STARTED );
//        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        if (powerManager != null) {
//            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "UpdateOS::WakeLock");
//            wakeLock.acquire();
//        }

        SetUpLogin setUp = new SetUpLogin();
        String login = "";
        try {
            login = setUp.setUpLogin( SetUpLogin.GET_REQUEST );
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent loginIntent = new Intent( UPDATE_OS_LOGIN );
        loginIntent.putExtra("login", login);
        sendBroadcast( loginIntent );
        Log.d( LOG_TAG, TAG_CLASS + " sending login broadcast." );

        Log.d( LOG_TAG, TAG_CLASS + " starting the foreground service." );
    }

    private void stopService()
    {
        try {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
            stopForeground(true);
            stopSelf();
        } catch (Exception e) {
            Log.d( LOG_TAG, TAG_CLASS + " service stopped without being started: " + e.getMessage() );
        }
        isServiceStarted = false;
        setServiceState( this, UpdateOSServiceState.ServiceState.STOPPED );

        Log.d( LOG_TAG, TAG_CLASS + " stopping the foreground service." );
    }

    private void updatedDevice()
    {
        updateNotification = new UpdateNotification( UpdateNotification.STATE_NOTIFICATION, this, this );
        updateNotification.showUpdateStateNotificationNoAvailableUpdate();
        startForeground( UpdateNotification.STATE_NOTIFICATION, updateNotification.getM_NotificationBuilder().build() );
    }

    private void newUpdate()
    {
        updateNotification = new UpdateNotification( UpdateNotification.STATE_NOTIFICATION, this, this );
        updateNotification.showUpdateStateNotificationAvailableUpdate();
        startForeground( UpdateNotification.STATE_NOTIFICATION, updateNotification.getM_NotificationBuilder().build() );
    }

    private void downloadUpdate()
    {
        updateNotification = new UpdateNotification( UpdateNotification.DOWNLOAD_NOTIFICATION, this, this );
        updateNotification.showUpdateStateNotificationDownloadedUpdate();
        startForeground( UpdateNotification.DOWNLOAD_NOTIFICATION, updateNotification.getM_NotificationBuilder().build() );
        updateNotification.setProgress( 30 );
    }

    @Nullable
    @Override
    public IBinder onBind( Intent intent )
    {
        return null;
    }

    public static final String UPDATE_OS_LOGIN = "UPDATE_OS_LOGIN";
    public static final String UPDATE_OS_END_SERVICE = "UPDATE_OS_END_SERVICE";
    public static final String UPDATE_OS_NEW_UPDATE = "UPDATE_OS_NEW_UPDATE";
    public static final String UPDATE_OS_DOWNLOAD_UPDATE = "UPDATE_OS_DOWNLOAD_UPDATE";

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d( LOG_TAG, TAG_CLASS + " getting broadcast: " + intent.getAction() );
            if ( intent.getAction().equals( UPDATE_OS_DOWNLOAD_UPDATE ) )
            {
                downloadUpdate();
            }
            else if ( intent.getAction().equals( UPDATE_OS_NEW_UPDATE ) )
            {
                newUpdate();
            }
            else if ( intent.getAction().equals( UPDATE_OS_END_SERVICE ) )
            {
                stopService();
            }
        }
    };
}