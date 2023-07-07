package kucko.test.endlessbackgroundservice;

import static kucko.test.endlessbackgroundservice.BroadcastMsgReceiver.UPDATE_OS_START_DOWNLOAD_UPDATE;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import kucko.test.endlessbackgroundservice.dialog.CustomProgressDialog;
import kucko.test.endlessbackgroundservice.services.BootBroadcastReceiver;
import kucko.test.endlessbackgroundservice.services.UpdateOSService;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity
{
    public static final String LOG_TAG = "EndlessBackgrSer";
    private static final String TAG_CLASS = "MainActivity::";

    public static final String DESTINATION_DIR_CONST           = /*"/cache/update";*/"/storage/emulated/legacy/update";

    public static boolean isUpdateCurrentlyDownloading = false;
    public static int actualProgress = 0;
    public static Handler handler;
    private CustomProgressDialog mPDialog;

    private TextView text;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        text = ( TextView ) findViewById( R.id.login );

        boolean isMyServiceRunning = isServiceRunning( this, UpdateOSService.class );
        Log.d( LOG_TAG, TAG_CLASS + " is service running? - " + ( isMyServiceRunning ? "YES" : "NO" ));

        if( !isMyServiceRunning )
        {
            BootBroadcastReceiver.startService( this, UpdateOSService.NFUHW, UpdateOSService.NFUFW, UpdateOSService.ECRFW );
        }

        progressDialog();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mPDialog.setProgress( MainActivity.actualProgress );
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void progressDialog()
    {
        mPDialog = new CustomProgressDialog( this, MainActivity.actualProgress );

        View.OnClickListener onClickListener = v -> {
//            downloadOTAfileAsyncTask.cancel( true );
            MainActivity.isUpdateCurrentlyDownloading = false;
        };

        mPDialog.setButton( onClickListener );

        if( isUpdateCurrentlyDownloading )
        {
            showProgressDialog();
        }
    }

    public void showProgressDialog()
    {
        if( (mPDialog.dialog != null) && !mPDialog.dialog.isShowing() )
        {
            mPDialog.setProgress( actualProgress );
            try
            {
                mPDialog.dialog.show();
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
    }
    public void hideProgressDialog()
    {
        if( (mPDialog.dialog != null) && mPDialog.dialog.isShowing() )
        {
            try
            {
                mPDialog.dialog.dismiss();
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    private void readLoginAndRefreshUI( String loginFromService )
    {
        text.setText( loginFromService );
    }

    public void updatedDevice(View view) {
        //sendBroadcast( new Intent().setAction( UPDATE_OS_UPDATED_DEVICE ) );
        Log.d( LOG_TAG, TAG_CLASS + " sending device is updated broadcast." );
    }

    public void downloadUpdate( View view )
    {
        //sendBroadcast( new Intent().setAction( UPDATE_OS_DOWNLOAD_UPDATE ) );
        Log.d( LOG_TAG, TAG_CLASS + " sending download broadcast." );
    }

    public void newUpdate( View view )
    {
        //sendBroadcast( new Intent().setAction( UPDATE_OS_NEW_UPDATE ) );
        Log.d( LOG_TAG, TAG_CLASS + " sending new update broadcast." );
    }

    public void endService( View view )
    {
        //sendBroadcast( new Intent().setAction( UPDATE_OS_END_SERVICE ) );
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

    public void startDownload( View view )
    {
        sendBroadcast( new Intent().setAction( UPDATE_OS_START_DOWNLOAD_UPDATE ) );
    }

    public void timerTest( View view )
    {
        Intent timerActivityIntent = new Intent( this, TimerActivity.class );
        startActivity( timerActivityIntent );
    }
}