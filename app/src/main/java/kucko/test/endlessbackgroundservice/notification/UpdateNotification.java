package kucko.test.endlessbackgroundservice.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import kucko.test.endlessbackgroundservice.MainActivity;
import kucko.test.endlessbackgroundservice.R;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class UpdateNotification extends Notification
{
    private static final String TAG_CLASS       = "UpdateNotification::";
    public final int NOTIFICATION_ID;
    public final int NOTIFICATION_TYPE;
    private final String CHANNEL_ID = "UPDATEOS:NotificationChannel";

    public static final int ID_DOWNLOAD_NOTIFICATION    = 1;
    public static final int ID_STATE_NOTIFICATION       = 2;

    public static final int TYPE_NO_UPDATE_ON_CLOUD     = 11;
    public static final int TYPE_UPDATE_ON_CLOUD        = 12;
    public static final int TYPE_UPDATE_DOWNLOADED      = 13;
    public static final int TYPE_UPDATE_DOWNLOAD_FAILED = 14;

    private final Context m_Context;
    private final PendingIntent m_PendingIntent;
    private final NotificationManager m_NotificationManager;
    private NotificationChannel m_NotificationChannel;
    private NotificationCompat.Builder  m_NotificationBuilder;

    public UpdateNotification( int notificationID, int notificationType,  Context context )
    {
        this.m_Context = context;
        this.NOTIFICATION_ID = notificationID;
        this.NOTIFICATION_TYPE = notificationType;
        m_NotificationManager = ( NotificationManager ) m_Context.getSystemService( Context.NOTIFICATION_SERVICE );

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O )
        {
            m_NotificationChannel = new NotificationChannel( CHANNEL_ID, "Foreground Service", NotificationManager.IMPORTANCE_DEFAULT );
            m_NotificationManager.createNotificationChannel( m_NotificationChannel );
        }

        Intent notificationIntent = new Intent( context, MainActivity.class );
        TaskStackBuilder stackBuilder = TaskStackBuilder.create( context );
        stackBuilder.addNextIntentWithParentStack( notificationIntent );
        m_PendingIntent =
                stackBuilder.getPendingIntent(0,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE );
    }

    private NotificationCompat.Builder getGeneralNotificationBuilder( String title, String text,
                                                                      int smallIconResId, int largeIconResId,
                                                                      boolean autoCancel, long sendTime )
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder( m_Context );
        BitmapDrawable bitmapDrawable = ( BitmapDrawable ) m_Context.getDrawable( largeIconResId );
        Bitmap largeIconBitmap = bitmapDrawable.getBitmap();
        builder.setLargeIcon( largeIconBitmap );
        builder.setSmallIcon( smallIconResId );
        builder.setContentTitle( title );
        builder.setContentText( text );
        builder.setWhen( sendTime );
        builder.setAutoCancel( autoCancel );
        builder.setNotificationSilent();
        builder.setContentIntent( m_PendingIntent );
        builder.setDefaults( Notification.DEFAULT_ALL );
        builder.setOngoing( true );
        builder.setChannelId( CHANNEL_ID );
        return builder;
    }

    public void initializeUpdateOSNotification(String title, String text,
                                               int smallIconResId, int largeIconResId,
                                               boolean autoCancel )
    {
        long sendTime = System.currentTimeMillis();

        m_NotificationBuilder = getGeneralNotificationBuilder( title, text,
                smallIconResId, largeIconResId, autoCancel, sendTime );
    }

    public void setProgress( int progress )
    {
        m_NotificationBuilder.setProgress( 100, progress, false );
        m_NotificationManager.notify(
                UpdateNotification.ID_DOWNLOAD_NOTIFICATION, getM_NotificationBuilder().build() );
    }

    public NotificationCompat.Builder getM_NotificationBuilder()
    {
        return m_NotificationBuilder;
    }

    public NotificationManager getM_NotificationManager()
    {
        return m_NotificationManager;
    }

    public NotificationChannel getM_NotificationChannel()
    {
        return m_NotificationChannel;
    }

    public void showUpdateStateNotificationNoAvailableUpdate()
    {
        initializeUpdateOSNotification(
                m_Context.getResources().getString( R.string.notification_title ),
                m_Context.getResources().getString( R.string.cloud_progress_bar_no_update ),
                R.drawable.ic_info, R.drawable.ic_launcher, false
        );
    }

    public void showUpdateStateNotificationAvailableUpdate()
    {
        initializeUpdateOSNotification(
                m_Context.getResources().getString( R.string.notification_title ),
                m_Context.getResources().getString( R.string.cloud_progress_bar_new_update ),
                R.drawable.ic_info, R.drawable.ic_launcher, false
        );
    }

    public void showUpdateStateNotificationDownloadFailed()
    {
        initializeUpdateOSNotification(
                m_Context.getResources().getString( R.string.notification_title ),
                m_Context.getResources().getString( R.string.notification_text_failed ),
                R.drawable.ic_info, R.drawable.ic_launcher, false
        );
    }

    public void showUpdateStateNotificationDownloadedUpdate()
    {
        initializeUpdateOSNotification(
                m_Context.getResources().getString( R.string.notification_title ),
                m_Context.getResources().getString( R.string.notification_text_done ),
                R.drawable.ic_info, R.drawable.ic_launcher, false
        );
    }
}