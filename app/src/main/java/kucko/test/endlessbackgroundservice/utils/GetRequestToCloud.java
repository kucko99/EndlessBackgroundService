package kucko.test.endlessbackgroundservice.utils;

import static kucko.test.endlessbackgroundservice.BroadcastMsgReceiver.UPDATE_OS_NEW_UPDATE;
import static kucko.test.endlessbackgroundservice.BroadcastMsgReceiver.UPDATE_OS_UPDATED_DEVICE;
import static kucko.test.endlessbackgroundservice.MainActivity.LOG_TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import kucko.test.endlessbackgroundservice.BroadcastMsgReceiver;
import kucko.test.endlessbackgroundservice.R;
import kucko.test.endlessbackgroundservice.UpdateOS;
import kucko.test.endlessbackgroundservice.notification.UpdateNotification;
import kucko.test.endlessbackgroundservice.services.UpdateOSService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class GetRequestToCloud
{
    private static final String TAG_CLASS = "GetRequestToCloud::";

    private static final String request_url = "https://api.elcom.cloud/otaupdate/";
    private final String login;
    private final Context context;

    private final SharedPreferences sp;
    private final UpdateNotification updateNotification;

    private String outputJSON;

    private boolean newUpdate;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GetRequestToCloud( String login, Context context )
    {
        this.context = context;
        this.login = login;
        this.sp = new SharedPreferences( context );
        this.updateNotification = new UpdateNotification( UpdateNotification.STATE_NOTIFICATION, context );
    }

    public void checkNewUpdateOnCloud()
    {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout( 20, TimeUnit.SECONDS )
                .readTimeout( 20, TimeUnit.SECONDS )
                .build();

        Request request = new Request.Builder()
                .url( request_url + login )
                .build();

        client.newCall( request ).enqueue( new Callback()
        {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onResponse( @NonNull Call call, @NonNull Response response ) throws IOException
            {
                if ( !response.isSuccessful() )
                {
                    sp.saveData( SharedPreferences.KEYS.IF_NEW_UPDATE, String.valueOf( false ) );
                    //context.sendBroadcast( new Intent( BroadcastMsgReceiver.UPDATE_OS_REQUEST_TO_CLOUD_END ) );

                    if( response.code() != 404 )
                    {
                        Intent serverError = new Intent();
                        //serverError.setAction( BroadcastMsgReceiver.UPDATE_OS_REQUEST_TO_CLOUD_ERROR);
                        //serverError.putExtra( "message", context.getString(R.string.errorCommunicationCloud) + "\n" + response.message() );
                        context.sendBroadcast( serverError );
                    }

                    throw new IOException( "Unexpected code " + response );
                }

                if ( response.body() != null )
                {
                    outputJSON = response.body().string();
                }

                JSONChecker JSONChecker = new JSONChecker( context, outputJSON );

                if( JSONChecker.isUpdateURLInJSON() )
                {
                    newUpdate = true;

                    if( !isUpdateDownload() )
                    {
                        context.sendBroadcast( new Intent().setAction( BroadcastMsgReceiver.UPDATE_OS_NEW_UPDATE ) );
                    }

                    if ( Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 )
                    {
                        new IconNotifBadge( context ).showIconBadge();
                    }
                    else
                    {
                        Log.d( LOG_TAG, TAG_CLASS + " checkUpdateOnCloud: don't show icon badge - low API level" );
                    }
                }
                else
                {
                    newUpdate = false;
                    context.sendBroadcast( new Intent().setAction( UPDATE_OS_UPDATED_DEVICE ) );

                    if ( Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 )
                    {
                        new IconNotifBadge( context ).hideIconBadge();
                    }
                    else
                    {
                        Log.d( LOG_TAG, TAG_CLASS + " checkUpdateOnCloud: don't hide icon badge - low API level." );
                    }
                }

                sp.saveData( SharedPreferences.KEYS.IF_NEW_UPDATE, String.valueOf( newUpdate ) );
                Log.d( LOG_TAG, TAG_CLASS + " checkUpdateOnCloud: is new update? :" + ( newUpdate ? "Yes." : "No." ) );
                Log.d( LOG_TAG, TAG_CLASS + " json: "+ outputJSON );
                checkNewerUpdateOnCloud();
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onFailure( @NonNull Call call, @NonNull IOException e )
            {
                newUpdate = false;
                Intent failureIntent = new Intent();
                //failureIntent.setAction( BroadcastMsgReceiver.UPDATE_OS_REQUEST_TO_CLOUD_ERROR);

                String exception = e.toString();
                if( exception.contains( "java.net.UnknownHostException: Unable to resolve host" ) )
                {
                    failureIntent.putExtra( "message", context.getResources().getString( R.string.noInternetConnection ) );
                    context.sendBroadcast( failureIntent );
                }
                else if( exception.contains( "java.security.cert.CertPathValidatorException: Trust anchor for certification path not found." ) )
                {
                    String dialogMessage = context.getResources().getString( R.string.noRootCertificationError ) + "\n\n";
                    dialogMessage += exception;
                    failureIntent.putExtra( "message", dialogMessage );
                    context.sendBroadcast( failureIntent );
                }
                else if( exception.contains( "java.net.SocketTimeoutException" ) )
                {
                    String dialogMessage = context.getResources().getString( R.string.timeoutError );
                    failureIntent.putExtra( "message", dialogMessage );
                    context.sendBroadcast( failureIntent );
                }

                new IconNotifBadge( context ).hideIconBadge();
                sp.saveData( SharedPreferences.KEYS.IF_NEW_UPDATE, String.valueOf( newUpdate ) );

                Log.d( LOG_TAG, TAG_CLASS + " checkUpdateOnCloud: onFailure()" );
                e.printStackTrace();
            }
        });
    }

    private void checkNewerUpdateOnCloud()
    {
        boolean isNewerUpdateOnCloud = false;
        String[] downloadedVersionNumStr    = sp.loadData( SharedPreferences.KEYS.SAVED_UPDATE_VERSION ).split( "\\." );
        String[] cloudVersionNumStr         = sp.loadData( SharedPreferences.KEYS.UPDATE_VERSION ).split( "\\." );
        if( downloadedVersionNumStr.length == 3 )
        {
            for( int i = 0; i < downloadedVersionNumStr.length; i++ )
            {
                int downloadedVersionNum = Integer.parseInt( downloadedVersionNumStr[i] );
                int cloudVersionNum      = Integer.parseInt( cloudVersionNumStr[i] );
                if( downloadedVersionNum < cloudVersionNum )
                {
                    isNewerUpdateOnCloud = true;
                    break;
                }
            }
        }
        //context.sendBroadcast( new Intent( BroadcastMsgReceiver.UPDATE_OS_REQUEST_TO_CLOUD_END ) );
        Log.d( LOG_TAG, TAG_CLASS + " isNewerUpdateOnCloud() - Is newer version on Cloud = " + isNewerUpdateOnCloud );
        if( isNewerUpdateOnCloud )
        {
            //UpdateOS.deleteOlderDownloadedVersion();
            sp.saveData( SharedPreferences.KEYS.SAVED_UPDATE_VERSION, "" );
        }
    }

    private boolean isUpdateDownload()
    {
        String fileNameInSP = sp.loadData( SharedPreferences.KEYS.FILE_NAME );
        String fileSizeInSP_String = sp.loadData( SharedPreferences.KEYS.FILE_SIZE );

        File file = UpdateOS.findFileInCache( fileNameInSP );
        if( file != null )
        {
            int fileSizeInSP_int = getIntValueOfFileSize( fileSizeInSP_String );
            if( file.length() > 0 && file.length() == fileSizeInSP_int )
            {
                updateNotification.showUpdateStateNotificationDownloadedUpdate();
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
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
}
