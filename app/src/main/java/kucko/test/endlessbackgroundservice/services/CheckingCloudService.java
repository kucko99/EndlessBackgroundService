package kucko.test.endlessbackgroundservice.services;

import static kucko.test.endlessbackgroundservice.MainActivity.LOG_TAG;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import kucko.test.endlessbackgroundservice.utils.GetRequestToCloud;

public class CheckingCloudService extends Service {
    private static final String TAG_CLASS = "CheckingCloudService::";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, TAG_CLASS + " onStartCommand() - Starting Get request.");

        new GetRequestToCloud( UpdateOSService.getReqLogin, this ).checkNewUpdateOnCloud();

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}