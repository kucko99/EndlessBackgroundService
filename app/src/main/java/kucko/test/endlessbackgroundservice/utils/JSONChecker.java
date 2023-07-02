package kucko.test.endlessbackgroundservice.utils;

import static kucko.test.endlessbackgroundservice.MainActivity.LOG_TAG;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.webkit.URLUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import kucko.test.endlessbackgroundservice.R;
import kucko.test.endlessbackgroundservice.dialog.CustomAlertDialog;

public class JSONChecker
{
    private static final String TAG_CLASS = "JSONChecker::";

    private SharedPreferences sp;

    private final Context context;

    private final String JSON;
    private static String fileName;
    private static String fileSize;
    private static String newVersionNumber;

    public JSONChecker(Context context, String JSON )
    {
        this.context = context;
        this.JSON = JSON;
        this.sp = new SharedPreferences( context );
    }

    public boolean isUpdateURLInJSON()
    {
        if( JSON != null )
        {
            try
            {
                JSONObject JSONObject = new JSONObject( JSON );
                if( JSONObject != null )
                {
                    String updateDownloadURL = (String) JSONObject.get("url");

                    JSONArray versionNumbers = (JSONArray) JSONObject.get( "ecr_version" );
                    newVersionNumber = versionNumbers.getString(0) + "." +
                            versionNumbers.getString(1) + "." +
                            versionNumbers.getString(2);

                    if( updateDownloadURL != null && URLUtil.isValidUrl( updateDownloadURL ) )
                    {
                        try{
                            URL url = new URL( updateDownloadURL );
                            url.toURI();

                            URLConnection conn = url.openConnection();
                            conn.connect();

                            getFileName( updateDownloadURL );

                            sp.saveData( SharedPreferences.KEYS.UPDATE_URL, updateDownloadURL );
                            return true;
                        }
                        catch( MalformedURLException e)
                        {
                            String message = context.getResources().getString( R.string.unavailableUrl)
                                    + ": " + updateDownloadURL;
                            showErrorDialog( message );
                            e.printStackTrace();
                            return false;
                        }
                        catch( URISyntaxException e )
                        {
                            String message = context.getResources().getString( R.string.unavailableUrl)
                                    + ": " + updateDownloadURL;
                            showErrorDialog( message );
                            e.printStackTrace();
                            return false;
                        }
                        catch ( IOException e )
                        {
                            String message = context.getResources().getString( R.string.unavailableUrl )
                                    + ": " + updateDownloadURL;
                            showErrorDialog( message );
                            e.printStackTrace();
                            return false;
                        }
                    }
                    else
                    {
                        String message = context.getResources().getString( R.string.unavailableUrl )
                                + ": " + updateDownloadURL;
                        showErrorDialog( message );
                        Log.e( LOG_TAG, TAG_CLASS + " null or not validate URL." );
                    }
                }
            }
            catch( JSONException e )
            {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    private void getFileName( String updateURL )
    {
        URL url;
        try {
            url = new URL( updateURL );
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if ( connection.getResponseCode() == HttpURLConnection.HTTP_OK )
            {
                String disposition = connection.getHeaderField( "Content-Disposition" );
                if( disposition != null )
                {
                    int index = disposition.indexOf( "filename=" );
                    if( index > 0 )
                    {
                        fileName = disposition.substring( index + 9, disposition.length() );
                    }
                    checkTwoFilenamesAttr();
                }
                else
                {
                    fileName = updateURL.substring( updateURL.lastIndexOf('/')+1, updateURL.length() );
                }

                String length = connection.getHeaderField( "Content-Length" );
                if( length != null )
                {
                    fileSize = length;
                    sp.saveData( SharedPreferences.KEYS.FILE_SIZE, fileSize );
                }

                sp.saveData( SharedPreferences.KEYS.FILE_NAME, fileName );
                sp.saveData( SharedPreferences.KEYS.UPDATE_VERSION, newVersionNumber );

                Log.d( LOG_TAG, TAG_CLASS + " File name: "+ fileName +" | File size: "+fileSize +"B | New version: "+newVersionNumber);
            }
        }
        catch( MalformedURLException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    private void checkTwoFilenamesAttr()
    {
        if( fileName.contains( ";" ) )
        {
            fileName = fileName.split(";" )[ 0 ];
        }

        if( fileName.contains( "\"" ) )
        {
            fileName = fileName.replaceAll( "\"", "" );
        }
    }

    private void showErrorDialog( String message )
    {
        ((Activity) context).runOnUiThread(() -> {
            CustomAlertDialog alertDialog = new CustomAlertDialog();
            alertDialog.showError( context, message, false,
                    (dialog, which) -> ((Activity) context).finish()
            );
        });
    }
}
