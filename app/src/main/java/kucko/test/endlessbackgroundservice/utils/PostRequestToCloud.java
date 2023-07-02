package kucko.test.endlessbackgroundservice.utils;

import static kucko.test.endlessbackgroundservice.MainActivity.LOG_TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import kucko.test.endlessbackgroundservice.R;
import kucko.test.endlessbackgroundservice.services.UpdateOSService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostRequestToCloud
{
    private static final String TAG_CLASS           = "PostRequestToCloud::";

    private static final String request_url = "https://api.elcom.cloud/otaupdate/";
    private final String login;
    private final Context context;

    private final SharedPreferences sp;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PostRequestToCloud( String login, Context context )
    {
        this.context = context;
        this.login = login;
        this.sp = new SharedPreferences( context );
    }

    public void sendNFUAndECRFWVersionsToCloud()
    {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout( 20, TimeUnit.SECONDS )
                .readTimeout( 20, TimeUnit.SECONDS )
                .build();

        RequestBody requestBody = RequestBody.create( null, new byte[0] );

        Request request = new Request.Builder()
                .url( request_url + login )
                .post( requestBody )
                .build();

        client.newCall( request ).enqueue( new Callback()
        {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response ) throws IOException
            {
                if ( !response.isSuccessful() )
                {
                    if( response.code() != 404 )
                    {
                        Intent serverError = new Intent();
                        //serverError.setAction( BroadcastMsgReceiver.UPDATE_OS_REQUEST_TO_CLOUD_ERROR);
                        serverError.putExtra( "message", context.getString(R.string.errorCommunicationCloud) + "\n" + response.message() );
                        context.sendBroadcast( serverError );
                    }

                    Log.d( LOG_TAG, TAG_CLASS + " postSmAndEcrVersion: isNotSuccessful" );
                    throw new IOException( "Unexpected code " + response );
                }
                else
                {
                    sp.saveData( SharedPreferences.KEYS.PR_NFU_HW_VERSION, UpdateOSService.NFUHW );
                    sp.saveData( SharedPreferences.KEYS.PR_NFU_FW_VERSION, UpdateOSService.NFUFW );
                    sp.saveData( SharedPreferences.KEYS.PR_ECR_FW_VERSION, UpdateOSService.ECRFW );
                    Log.d( LOG_TAG, TAG_CLASS + " postSmAndEcrVersion: isSuccessful" );
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onFailure( @NonNull Call call, @NonNull IOException e )
            {
                String exception = e.toString();
                Log.d( LOG_TAG, TAG_CLASS + " " + exception );
                Log.d( LOG_TAG, TAG_CLASS + " postSmAndEcrVersion: onFailure()" );
                e.printStackTrace();
            }
        });
    }
}
