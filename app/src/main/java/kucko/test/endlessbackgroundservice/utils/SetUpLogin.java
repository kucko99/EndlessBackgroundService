package kucko.test.endlessbackgroundservice.utils;

import static kucko.test.endlessbackgroundservice.MainActivity.LOG_TAG;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

import kucko.test.endlessbackgroundservice.BroadcastMsgReceiver;
import kucko.test.endlessbackgroundservice.R;
import kucko.test.endlessbackgroundservice.services.CheckUpdateTimer;
import kucko.test.endlessbackgroundservice.services.UpdateOSService;
import kucko.test.endlessbackgroundservice.utils.GetDeviceInfo;
import kucko.test.endlessbackgroundservice.utils.GetRequestToCloud;
import kucko.test.endlessbackgroundservice.utils.PostRequestToCloud;

public class SetUpLogin
{
    private static final String TAG_CLASS           = "SetUpLogin::";
    private static final String SmVersionString     = "/?Sm_Version=";
    private static final String IcmVersionString     = "/?Icm_Version=";
    private static final String EcrVersionString    = "&Ecr_Version=";

    public static final String GET_REQUEST       = "GET_REQUEST";
    public static final String POST_REQUEST      = "POST_REQUEST";

    private final GetDeviceInfo getDeviceInfo;
    private final Context context;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SetUpLogin( Context context )
    {
        this.context = context;
        this.getDeviceInfo = new GetDeviceInfo();
    }

    /*
        login: ECRSN_ECRHW_NFUHW_DEVID/?Sm_Version=X.XX.X&Ecr_Version=X.XX.X
        example: CZCX00007W_1_1_SK-SK-userdebug/?Sm_Version=0.15.0&Ecr_Version=1.15.1
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public String setUpLogin( String whichRequest ) throws IOException
    {
        String login = "";
        String checkCommWithNFU = getDeviceInfo.getDeviceInfoExByIndex( 2 );
        if ( checkCommWithNFU.contains( "Error" ) )
        {
            if ( context instanceof Activity)
            {
                Intent serverError = new Intent();
                //serverError.setAction( BroadcastMsgReceiver.UPDATE_OS_REQUEST_TO_CLOUD_ERROR );
                serverError.putExtra( "message", context.getResources().getString( R.string.errorCommunicationNFU ) );
                context.sendBroadcast( serverError );
            }
            else if ( context instanceof Service )
            {
                Log.d( LOG_TAG, TAG_CLASS + " " + context.getResources().getString(R.string.errorCommunicationNFU) + ". (" + whichRequest + ")" );
                //context.sendBroadcast( new Intent().setAction( UPDATE_OS_SERVICE_NO_UPDATE ) );
            }
            Log.d( LOG_TAG, TAG_CLASS + " errorCommunicationNFU (" + whichRequest + ")" );

            if ( whichRequest.equals( GET_REQUEST ) )
            {
                UpdateOSService.failCheckNum++;
                Log.d( LOG_TAG, TAG_CLASS + " fail check num: " + UpdateOSService.failCheckNum );

                if ( UpdateOSService.failCheckNum == 4 )
                {
                    UpdateOSService.checkUpdateTimer.setAlarm( context, CheckUpdateTimer.PERIODIC_CHECK_INTERVAL );
                    UpdateOSService.failCheckNum = 0;
                }
            }
        }
        else
        {
            Log.d( LOG_TAG, TAG_CLASS + " NFUHW: " + UpdateOSService.NFUHW + " | NFUFW: " + UpdateOSService.NFUFW + " | ECRFW: " + UpdateOSService.ECRFW );
            if ( UpdateOSService.NFUHW.isEmpty() || UpdateOSService.NFUFW.isEmpty() || UpdateOSService.ECRFW.isEmpty() )
            {
                UpdateOSService.NFUHW = getNFUHW();
                UpdateOSService.NFUFW = getNFUFW();
                UpdateOSService.ECRFW = getECRFW();
            }

            if ( whichRequest.equals( GET_REQUEST ) )
            {
                login = getECRSN() + "_" + getECRHW() + "_" + UpdateOSService.NFUHW + "_" + getDEVID()
                        + SmVersionString + UpdateOSService.NFUFW + EcrVersionString + UpdateOSService.ECRFW;
            }
            else if ( whichRequest.equals( POST_REQUEST ) )
            {
                login = getECRSN() + "_" + getECRHW() + "_" + UpdateOSService.NFUHW + "_" + getDEVID()
                        + IcmVersionString + UpdateOSService.NFUFW + EcrVersionString + UpdateOSService.ECRFW;
            }

            if ( UpdateOSService.failCheckNum > 1 )
            {
                UpdateOSService.checkUpdateTimer.setAlarm( context, CheckUpdateTimer.PERIODIC_CHECK_INTERVAL );
                UpdateOSService.failCheckNum = 0;
                Log.d( LOG_TAG, TAG_CLASS + " setting alarm to PERIODIC_CHECK_INTERVAL." );
            }
        }
        Log.d( LOG_TAG, TAG_CLASS + " (" + whichRequest + ") Login: " + login );
        return login;
    }

    private String getECRSN()
    {
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O )
        {
            try
            {
                return Build.getSerial();
            }
            catch ( SecurityException e )
            {
                e.printStackTrace();
                return "null";
            }
        }
        else
        {
            return Build.SERIAL;
        }
    }

    private int getECRHW()
    {
        String[] segments = Build.DISPLAY.split( "-" );
        String hwRevision = segments[ 0 ];

        return Integer.parseInt( hwRevision.substring( hwRevision.length()-2 ) );
    }

    private String getNFUHW()
    {
        return getDeviceInfo.getDeviceInfoExByIndex( 3 ).substring( 0,1 );
    }

    // DEVID: <region>-<language>-<buildVariant>
    private String getDEVID() throws IOException
    {
        return getRegion() + "-" + getLanguage() + "-" + getBuildVariant();
    }
    private String getRegion()
    {
        String[] segments = Build.MODEL.split( "_" );
        return segments[1].substring( 0, 2 );
    }
    private String getLanguage() throws IOException {
        Process process = Runtime.getRuntime().exec( "getprop" );

        BufferedReader stdInput = new BufferedReader( new
                InputStreamReader( process.getInputStream() ) );

        String s;
        StringBuilder output = new StringBuilder();
        while ( ( s = stdInput.readLine() ) != null )
        {
            if ( s.contains( "locale.language" ) )
            {
                output.append( s );
            }
        }
        String[] segments = output.toString().split( ":" );
        String language = segments[ 1 ].substring( 2,4 );

        return language.toUpperCase( Locale.ROOT );
    }
    private String getBuildVariant()
    {
        return Build.TYPE;
    }

    private String getNFUFW()
    {
        String result = "";

        String smVersionRaw = getDeviceInfo.getDeviceInfoExByIndex( 2 );
        String[] stringSegments = smVersionRaw.split("\\.");
        int[] intSegments = new int[ stringSegments.length ];

        for( int i=0; i<stringSegments.length; i++ )
        {
            intSegments[i] = Integer.parseInt( stringSegments[i] );
        }

        if( stringSegments.length == 2 )
        {
            result = intSegments[0] + "." + intSegments[1] + ".0";
        }
        else if( stringSegments.length == 3 )
        {
            result = intSegments[0] + "." + intSegments[1] + "." + intSegments[2];
        }

        return result;
    }

    private String getECRFW()
    {
        String result = "";

        String buildID = Build.ID;
        String ecrVersion = buildID.substring(0, buildID.indexOf("-"));

        String[] stringSegments = ecrVersion.split("\\.");
        int[] intSegments = new int[ stringSegments.length ];

        for( int i=0; i<stringSegments.length; i++ )
        {
            intSegments[i] = Integer.parseInt( stringSegments[i] );
        }

        result += intSegments[0] + "." + intSegments[1] + "." + intSegments[2];

        return result;
    }
}
