package kucko.test.endlessbackgroundservice;

import static kucko.test.endlessbackgroundservice.MainActivity.LOG_TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BootBroadcastReceiver extends BroadcastReceiver
{
    private static final String TAG_CLASS = "BootBroadcastReceiver::";

    public void onReceive( Context context, Intent intent )
    {
        if( intent.getAction().equals( Intent.ACTION_BOOT_COMPLETED ) )
        {
            Log.d( LOG_TAG, TAG_CLASS + " onReceive() - Init and read versions." );
            SetUpLogin setUp = new SetUpLogin();
            try {
                String getRequestLogin = setUp.setUpLogin( SetUpLogin.GET_REQUEST );
                String postRequestLogin = setUp.setUpLogin( SetUpLogin.POST_REQUEST );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
