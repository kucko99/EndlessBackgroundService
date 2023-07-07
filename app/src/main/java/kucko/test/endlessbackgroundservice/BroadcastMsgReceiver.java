package kucko.test.endlessbackgroundservice;

import static kucko.test.endlessbackgroundservice.MainActivity.LOG_TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import kucko.test.endlessbackgroundservice.services.UpdateOSService;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BroadcastMsgReceiver extends BroadcastReceiver {
    private static final String TAG_CLASS = "BroadcastMsgReceiver::";

    public static final String UPDATE_OS_LOGIN = "UPDATE_OS_LOGIN";
    public static final String UPDATE_OS_END_SERVICE = "UPDATE_OS_END_SERVICE";
    public static final String UPDATE_OS_NEW_UPDATE = "UPDATE_OS_NEW_UPDATE";
    public static final String UPDATE_OS_DOWNLOAD_UPDATE = "UPDATE_OS_DOWNLOAD_UPDATE";

    public static final String UPDATE_OS_START_DOWNLOAD_UPDATE = "UPDATE_OS_START_DOWNLOAD_UPDATE";
    public static final String UPDATE_OS_STOP_DOWNLOAD_UPDATE = "UPDATE_OS_STOP_DOWNLOAD_UPDATE";
    public static final String UPDATE_OS_UPDATED_DEVICE = "UPDATE_OS_UPDATED_DEVICE";

    public static final String UPDATE_OS_UPDATE_DOWNLOADING = "eu.elcom.updateos.BroadcastMsgReceiver_Update_Downloading";

    private final Object m_Object;

    public BroadcastMsgReceiver(Object object) {
        m_Object = object;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(LOG_TAG, TAG_CLASS + " getting broadcast: " + intent.getAction());
        if (intent.getAction().equals(UPDATE_OS_DOWNLOAD_UPDATE))
        {
            if (m_Object instanceof UpdateOSService)
            {
                ((UpdateOSService) m_Object).downloadUpdate();
            }
        } else if (intent.getAction().equals(UPDATE_OS_NEW_UPDATE))
        {
            if (m_Object instanceof UpdateOSService)
            {
                ((UpdateOSService) m_Object).newUpdate();
            }
        } else if (intent.getAction().equals(UPDATE_OS_END_SERVICE))
        {
            if (m_Object instanceof UpdateOSService)
            {
                ((UpdateOSService) m_Object).stopService();
            }
        } else if (intent.getAction().equals(UPDATE_OS_UPDATED_DEVICE))
        {
            if (m_Object instanceof UpdateOSService)
            {
                ((UpdateOSService) m_Object).updatedDevice();
            }
        } else if (intent.getAction().equals(UPDATE_OS_START_DOWNLOAD_UPDATE))
        {
            if (m_Object instanceof UpdateOSService)
            {
                ((UpdateOSService) m_Object).startDownloadUpdate();
            }
        } else if (intent.getAction().equals(UPDATE_OS_STOP_DOWNLOAD_UPDATE))
        {
            if (m_Object instanceof UpdateOSService)
            {
                ((UpdateOSService) m_Object).stopDownloadUpdate();
            }
        }
        else if ( intent.getAction().equals( UPDATE_OS_UPDATE_DOWNLOADING ) )
        {
            if( m_Object instanceof MainActivity )
            {
                ( (MainActivity) m_Object ).showProgressDialog();
            }
        }
    }

}