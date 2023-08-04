package kucko.test.endlessbackgroundservice.services;

import static kucko.test.endlessbackgroundservice.BroadcastMsgReceiver.UPDATE_OS_DOWNLOAD_UPDATE;
import static kucko.test.endlessbackgroundservice.BroadcastMsgReceiver.UPDATE_OS_END_SERVICE;
import static kucko.test.endlessbackgroundservice.BroadcastMsgReceiver.UPDATE_OS_NEW_UPDATE;
import static kucko.test.endlessbackgroundservice.BroadcastMsgReceiver.UPDATE_OS_START_DOWNLOAD_UPDATE;
import static kucko.test.endlessbackgroundservice.BroadcastMsgReceiver.UPDATE_OS_STOP_DOWNLOAD_UPDATE;
import static kucko.test.endlessbackgroundservice.MainActivity.DESTINATION_DIR_CONST;
import static kucko.test.endlessbackgroundservice.MainActivity.LOG_TAG;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;

import kucko.test.endlessbackgroundservice.BroadcastMsgReceiver;
import kucko.test.endlessbackgroundservice.MainActivity;
import kucko.test.endlessbackgroundservice.R;
import kucko.test.endlessbackgroundservice.UpdateOS;
import kucko.test.endlessbackgroundservice.asynctask.DownloadOTAfileAsyncTask;
import kucko.test.endlessbackgroundservice.utils.GetRequestToCloud;
import kucko.test.endlessbackgroundservice.utils.PostRequestToCloud;
import kucko.test.endlessbackgroundservice.utils.SetUpLogin;
import kucko.test.endlessbackgroundservice.notification.UpdateNotification;
import kucko.test.endlessbackgroundservice.utils.SharedPreferences;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class UpdateOSService extends Service
{
    public enum ACTIONS
    {
        START_SERVICE( "START_SERVICE" )
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

    private String FILE_NAME;
    private String FILE_SIZE;

    private static Context context;
    public static CheckUpdateTimer checkUpdateTimer;
    public static int failCheckNum;

    private SharedPreferences sp;
    private static DownloadOTAfileAsyncTask downloadOTAfileAsyncTask;

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    private BroadcastMsgReceiver m_BroadcastMsgReceiver;
    private IntentFilter m_BroadcastMsgFilter;
    private UpdateNotification updateNotification;

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d( LOG_TAG, TAG_CLASS + " the service has been created." );

        context = this;
        failCheckNum = 0;

        createBroadcastReceiver();
        registerReceiver( m_BroadcastMsgReceiver, m_BroadcastMsgFilter );

        checkUpdateTimer = new CheckUpdateTimer();
        sp = new SharedPreferences( this );
        sp.removeUnusedData();

        SetUpLogin setUp = new SetUpLogin( this );
        try
        {
            postReqLogin = setUp.setUpLogin( SetUpLogin.POST_REQUEST );
            getReqLogin = setUp.setUpLogin( SetUpLogin.GET_REQUEST );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    private void createBroadcastReceiver()
    {
        m_BroadcastMsgReceiver = new BroadcastMsgReceiver( this );
        m_BroadcastMsgFilter = new IntentFilter();
        m_BroadcastMsgFilter.addAction( UPDATE_OS_NEW_UPDATE );
        m_BroadcastMsgFilter.addAction( UPDATE_OS_END_SERVICE );
        m_BroadcastMsgFilter.addAction( UPDATE_OS_DOWNLOAD_UPDATE );

        m_BroadcastMsgFilter.addAction( UPDATE_OS_START_DOWNLOAD_UPDATE );
        m_BroadcastMsgFilter.addAction( UPDATE_OS_STOP_DOWNLOAD_UPDATE );
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
                if ( !isServiceStarted )
                {
                    if ( intent.hasExtra( "NFUHW" ) && intent.hasExtra( "NFUFW" ) && intent.hasExtra( "ECRFW" ) )
                    {
                        NFUHW = intent.getStringExtra( "NFUHW" );
                        NFUFW = intent.getStringExtra( "NFUFW" );
                        ECRFW = intent.getStringExtra( "ECRFW" );
                    }
                    waitForInternetConn();
                }
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
        unregisterReceiver( m_BroadcastMsgReceiver );
        isServiceStarted = false;
        Log.d( LOG_TAG, TAG_CLASS + " the service has been destroyed." );

        BootBroadcastReceiver.startService( this, NFUHW, NFUFW, ECRFW );
    }

    @Override
    public void onTaskRemoved( Intent rootIntent )
    {
        super.onTaskRemoved( rootIntent );
    }

    private void waitForInternetConn()
    {
        connectivityManager = (ConnectivityManager) getSystemService( Context.CONNECTIVITY_SERVICE );
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable( Network network ) {
                // Spustenie úloh, keď je dostupné pripojenie na internet
                communicateWithServer();
            }

            @Override
            public void onLost(Network network) {
                // Spracovanie straty pripojenia na internet (ak je to relevantné pre vášu aplikáciu)
            }
        };

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N )
        {
            connectivityManager.registerDefaultNetworkCallback( networkCallback );
        }
        else
        {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability( NetworkCapabilities.NET_CAPABILITY_INTERNET )
                    .build();
            connectivityManager.registerNetworkCallback( request, networkCallback );
        }
    }

    private void communicateWithServer()
    {
        if ( isServiceStarted )
            return;

        if ( !hasInternetConnection() )
            return;

        isServiceStarted = true;

        if ( postReqLogin.equals( "" ) || getReqLogin.equals( "" ) )
        {
            checkUpdateTimer.setAlarm( this, CheckUpdateTimer.FAIL_CHECK_INTERVAL );
        }
        else
        {
            new PostRequestToCloud( postReqLogin, this ).sendNFUAndECRFWVersionsToCloud();
            new GetRequestToCloud( getReqLogin, this ).checkNewUpdateOnCloud();

            checkUpdateTimer.setAlarm( this, CheckUpdateTimer.PERIODIC_CHECK_INTERVAL );
        }

        connectivityManager.unregisterNetworkCallback( networkCallback );

        Log.d( LOG_TAG, TAG_CLASS + " starting the foreground service." );
    }

    public void stopService()
    {
        try {
            stopForeground(true);
            stopSelf();
        } catch (Exception e) {
            Log.d( LOG_TAG, TAG_CLASS + " service stopped without being started: " + e.getMessage() );
        }
        isServiceStarted = false;

        Log.d( LOG_TAG, TAG_CLASS + " stopping the foreground service." );
    }

    public void noUpdateOnCloudNotification()
    {
        updateNotification = new UpdateNotification( UpdateNotification.ID_STATE_NOTIFICATION, UpdateNotification.TYPE_NO_UPDATE_ON_CLOUD, this );
        updateNotification.showUpdateStateNotificationNoAvailableUpdate();
        updateNotification.getM_NotificationBuilder().setPriority( Notification.PRIORITY_MIN );
        startForeground( UpdateNotification.ID_STATE_NOTIFICATION, updateNotification.getM_NotificationBuilder().build() );
    }

    public void updatedDevice()
    {
        updateNotification = new UpdateNotification( UpdateNotification.ID_STATE_NOTIFICATION, UpdateNotification.TYPE_UPDATE_ON_CLOUD,this );
        updateNotification.showUpdateStateNotificationAvailableUpdate();
        startForeground( UpdateNotification.ID_STATE_NOTIFICATION, updateNotification.getM_NotificationBuilder().build() );
    }

    public void newUpdate()
    {
        updateNotification = new UpdateNotification( UpdateNotification.ID_STATE_NOTIFICATION, UpdateNotification.TYPE_UPDATE_DOWNLOADED, this );
        updateNotification.showUpdateStateNotificationDownloadedUpdate();
        startForeground( UpdateNotification.ID_STATE_NOTIFICATION, updateNotification.getM_NotificationBuilder().build() );
    }

    public void failedDownloadUpdate()
    {
        updateNotification = new UpdateNotification( UpdateNotification.ID_STATE_NOTIFICATION, UpdateNotification.TYPE_UPDATE_DOWNLOAD_FAILED, this );
        updateNotification.showUpdateStateNotificationDownloadFailed();
        startForeground( UpdateNotification.ID_STATE_NOTIFICATION, updateNotification.getM_NotificationBuilder().build() );
    }

    public void downloadUpdate()
    {
        updateNotification = new UpdateNotification( UpdateNotification.ID_DOWNLOAD_NOTIFICATION, 0, this );
        updateNotification.initializeUpdateOSNotification(
                getResources().getString( R.string.notification_title ),
                getResources().getString( R.string.notification_text_downloading ),
                R.drawable.ic_info,
                R.drawable.ic_launcher,
                false
        );
    }

    public void startDownloadUpdate()
    {
        String UPDATE_URL  = sp.loadData(SharedPreferences.KEYS.UPDATE_URL);
        FILE_NAME   = sp.loadData( SharedPreferences.KEYS.FILE_NAME );
        FILE_SIZE   = sp.loadData( SharedPreferences.KEYS.FILE_SIZE );

        downloadUpdate();
        downloadOTAfileAsyncTask = new DownloadOTAfileAsyncTask( this, updateNotification );

        if( !isUpdateDownload() )
        {
            startForeground( UpdateNotification.ID_DOWNLOAD_NOTIFICATION, updateNotification.getM_NotificationBuilder().build() );

            MainActivity.actualProgress = 0;
            downloadOTAfileAsyncTask.execute(
                    UPDATE_URL,
                    FILE_NAME,
                    DESTINATION_DIR_CONST
            );
        }
    }

    public void stopDownloadUpdate()
    {
        downloadOTAfileAsyncTask.cancel( true );
        this.sendBroadcast( new Intent( BroadcastMsgReceiver.UPDATE_OS_STOP_DOWNLOAD_UPDATE ) );
        MainActivity.isUpdateCurrentlyDownloading = false;
    }

    private boolean hasInternetConnection()
    {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private boolean isUpdateDownload()
    {
        File file = UpdateOS.findFileInCache( FILE_NAME );
        if(file == null)
        {
            return false;
        }
        else {
            int fileSizeInSP_int = getIntValueOfFileSize( FILE_SIZE );
            return file.length() == fileSizeInSP_int;
        }
    }
    private int getIntValueOfFileSize( String fileSizeInSP_String )
    {
        if( fileSizeInSP_String.isEmpty() )
        {
            return 0;
        }
        else
        {
            return Integer.parseInt( fileSizeInSP_String );
        }
    }


    @Nullable
    @Override
    public IBinder onBind( Intent intent )
    {
        return null;
    }

    @Override
    public void onConfigurationChanged( Configuration newConfig )
    {
        super.onConfigurationChanged( newConfig );
        switch ( updateNotification.NOTIFICATION_TYPE )
        {
            case 11:
                updatedDevice();
                break;
            case 12:
                newUpdate();
                break;
            case 13:
                downloadUpdate();
                break;
            case 14:

                break;
        }
    }
}
