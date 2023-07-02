package kucko.test.endlessbackgroundservice.services;

import static kucko.test.endlessbackgroundservice.MainActivity.LOG_TAG;
import static kucko.test.endlessbackgroundservice.services.UpdateOSServiceState.setServiceState;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.IOException;

import kucko.test.endlessbackgroundservice.utils.PostRequestToCloud;
import kucko.test.endlessbackgroundservice.utils.SetUpLogin;
import kucko.test.endlessbackgroundservice.notification.UpdateNotification;

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

    public static String NFUHW = "";
    public static String NFUFW = "";
    public static String ECRFW = "";

    public static String getReqLogin = "";
    public static String postReqLogin = "";

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
                if( intent.hasExtra( "NFUHW" ) && intent.hasExtra( "NFUFW" ) && intent.hasExtra( "ECRFW" ) )
                {
                    NFUHW = intent.getStringExtra( "NFUHW" );
                    NFUFW = intent.getStringExtra( "NFUFW" );
                    ECRFW = intent.getStringExtra( "ECRFW" );
                }
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

        BootBroadcastReceiver.startService( this, NFUHW, NFUFW, ECRFW );
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

        SetUpLogin setUp = new SetUpLogin( this );
        try {
            getReqLogin = setUp.setUpLogin( SetUpLogin.GET_REQUEST );
            postReqLogin = setUp.setUpLogin( SetUpLogin.POST_REQUEST );
        } catch (IOException e) {
            e.printStackTrace();
        }

        new PostRequestToCloud( postReqLogin, this );

        Intent loginIntent = new Intent( UPDATE_OS_LOGIN );
        loginIntent.putExtra("login", getReqLogin);
        sendBroadcast( loginIntent );
        Log.d( LOG_TAG, TAG_CLASS + " sending login broadcast." );

        TwentyFourHoursTimer twentyFourHoursTimer = new TwentyFourHoursTimer();
        twentyFourHoursTimer.setUpAlarm( this, false );

        Log.d( LOG_TAG, TAG_CLASS + " starting the foreground service." );
    }

    private void stopService()
    {
        try {
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
        updateNotification = new UpdateNotification( UpdateNotification.STATE_NOTIFICATION, this );
        updateNotification.showUpdateStateNotificationNoAvailableUpdate();
        updateNotification.getM_NotificationBuilder().setPriority( Notification.PRIORITY_MIN );
        startForeground( UpdateNotification.STATE_NOTIFICATION, updateNotification.getM_NotificationBuilder().build() );
    }

    private void newUpdate()
    {
        updateNotification = new UpdateNotification( UpdateNotification.STATE_NOTIFICATION, this );
        updateNotification.showUpdateStateNotificationAvailableUpdate();
        startForeground( UpdateNotification.STATE_NOTIFICATION, updateNotification.getM_NotificationBuilder().build() );
    }

    private void downloadUpdate()
    {
        updateNotification = new UpdateNotification( UpdateNotification.DOWNLOAD_NOTIFICATION, this );
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
    public static final String UPDATE_OS_UPDATED_DEVICE = "UPDATE_OS_UPDATED_DEVICE";

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
            else if ( intent.getAction().equals( UPDATE_OS_UPDATED_DEVICE ) )
            {
                updatedDevice();
            }
        }
    };
}
