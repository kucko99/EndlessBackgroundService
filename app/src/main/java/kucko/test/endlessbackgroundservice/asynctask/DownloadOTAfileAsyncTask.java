package kucko.test.endlessbackgroundservice.asynctask;

import static kucko.test.endlessbackgroundservice.MainActivity.LOG_TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import kucko.test.endlessbackgroundservice.MainActivity;
import kucko.test.endlessbackgroundservice.R;
import kucko.test.endlessbackgroundservice.MainActivity;
import kucko.test.endlessbackgroundservice.UpdateOS;
import kucko.test.endlessbackgroundservice.notification.UpdateNotification;
import kucko.test.endlessbackgroundservice.utils.SharedPreferences;

//async task to download OTA file from Cloud
public class DownloadOTAfileAsyncTask extends AsyncTask< String, Integer, Boolean >
{
    private static final String TAG_CLASS = "DownloadOTAfileAsyncTask::";

    @SuppressLint("StaticFieldLeak")
    private final Context               context;
    private boolean                     isInterruptedDownloadWithInternet = false;

    private PowerManager.WakeLock       mWakeLock;

    private final UpdateNotification    updateProgressNotification;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DownloadOTAfileAsyncTask(Context context, UpdateNotification updateProgressNotification )
    {
        Log.d( LOG_TAG, TAG_CLASS + "DownloadOTAfileAsyncTask()" );

        this.context = context;
        this.updateProgressNotification = updateProgressNotification;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onPreExecute()
    {
        Log.d( LOG_TAG, TAG_CLASS + "onPreExecute()" );

        UpdateOS.deleteOlderDownloadedVersion();

        PowerManager pm = (PowerManager) context.getSystemService( Context.POWER_SERVICE );
        mWakeLock = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, getClass().getName() );
        mWakeLock.acquire();

        super.onPreExecute();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected Boolean doInBackground( String... strings )
    {
        boolean bRetVal;

        Log.d( LOG_TAG, TAG_CLASS + "doInBackground(): " + strings[0] + " | " +strings[1] + " | " + strings[2] );

        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        try
        {
            URL url = new URL( strings[ 0 ] );
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if ( connection.getResponseCode() != HttpURLConnection.HTTP_OK )
            {
                return false;
            }

            Log.i("DownloadTask","Response " + connection.getResponseCode());

            int fileLength = connection.getContentLength();
            String fileName = strings[1];

            File file = new File( strings[2] + "/" + fileName );
            Log.d( LOG_TAG, TAG_CLASS + " file path: " + file.getPath() );
            input = connection.getInputStream();
            output = new FileOutputStream( file, false );

            byte[] data = new byte[ 8192 ];
            long total = 0;
            int previousProgress = 0;
            int count;

            MainActivity.isUpdateCurrentlyDownloading = true;
            //context.sendBroadcast( new Intent( BroadcastMsgReceiver.UPDATE_OS_UPDATE_DOWNLOADING ) );

            while ( ( count = input.read( data ) ) != -1 )
            {
                if ( isCancelled() )
                {
                    Log.i( LOG_TAG,TAG_CLASS + " Download cancelled." );
                    MainActivity.isUpdateCurrentlyDownloading = false;
                    input.close();
                    return null;
                }

                total += count;
                int progress = (int) (total * 100 / fileLength);

                if ( progress > previousProgress )
                {
                    previousProgress = progress;
                    publishProgress( progress );
                }

                output.write( data, 0, count );
            }

            bRetVal = true;
        }
        catch ( SocketTimeoutException e )
        {
            e.printStackTrace();
            bRetVal = false;
        }
        catch ( MalformedURLException e )
        {
            e.printStackTrace();
            bRetVal = false;
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
            bRetVal = false;
        } catch ( UnknownHostException e )
        {
            e.printStackTrace();
            bRetVal = false;
            //context.sendBroadcast( new Intent( BroadcastMsgReceiver.UPDATE_OS_INTERNET_CONN_ERROR ) );
            //context.sendBroadcast( new Intent( BroadcastMsgReceiver.UPDATE_OS_DOWNLOAD_ERROR ) );
        }
        catch (IOException e)
        {
            e.printStackTrace();
            String message = e.getMessage();
            if ( message.contains( "Connection timed out" ) )
            {
                isInterruptedDownloadWithInternet = true;
            }
            bRetVal = false;
        } finally
        {
            try
            {
                if ( output != null )
                    output.close();
                if ( input != null )
                    input.close();
            }
            catch ( IOException ignored )
            { }
            if ( connection != null )
                connection.disconnect();
        }

        return bRetVal;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onProgressUpdate( Integer... progress )
    {
        MainActivity.actualProgress = progress[0];

        if( MainActivity.handler != null )
        {
            MainActivity.handler.sendMessage( MainActivity.handler.obtainMessage() );
        }
        updateProgressNotification.setProgress( progress[0] );

        super.onProgressUpdate( progress );
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onPostExecute( Boolean bResult )
    {
        Log.d( LOG_TAG, TAG_CLASS + "onPostExecute() - Download file (bResult = " + bResult.toString() + ")" );

        mWakeLock.release();

        MainActivity.isUpdateCurrentlyDownloading = false;
        MainActivity.actualProgress = 0;

        updateProgressNotification.getM_NotificationManager().cancel( UpdateNotification.DOWNLOAD_NOTIFICATION );

        if( bResult )
        {
            //context.sendBroadcast( new Intent( BroadcastMsgReceiver.UPDATE_OS_SERVICE_UPDATE_DOWNLOADED ) );
            //context.sendBroadcast( new Intent( BroadcastMsgReceiver.UPDATE_OS_UPDATE_DOWNLOADED ) );
            SharedPreferences sp = new SharedPreferences(context);
            sp.saveData( SharedPreferences.KEYS.SAVED_UPDATE_VERSION, sp.loadData( SharedPreferences.KEYS.UPDATE_VERSION ) );
        }
        else
        {
            //context.sendBroadcast( new Intent( BroadcastMsgReceiver.UPDATE_OS_SERVICE_UPDATE_DOWNLOAD_FAILED ) );
            //context.sendBroadcast( new Intent( BroadcastMsgReceiver.UPDATE_OS_DOWNLOAD_ERROR ) );
            Intent serverError = new Intent();
            //serverError.setAction( BroadcastMsgReceiver.UPDATE_OS_REQUEST_TO_CLOUD_ERROR);
            serverError.putExtra( "message", context.getString(R.string.downloadOTAErr) );
            context.sendBroadcast( serverError );
        }

        if( !bResult && isInterruptedDownloadWithInternet )
        {
            //context.sendBroadcast( new Intent( BroadcastMsgReceiver.UPDATE_OS_INTERNET_CONN_ERROR ) );
            //context.sendBroadcast( new Intent( BroadcastMsgReceiver.UPDATE_OS_DOWNLOAD_ERROR ) );
        }

        //context.sendBroadcast( new Intent( BroadcastMsgReceiver.UPDATE_OS_SERVICE_STOP_DOWNLOAD_UPDATE ) );

        super.onPostExecute( bResult );
    }
}
