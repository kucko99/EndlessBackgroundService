package kucko.test.endlessbackgroundservice.notification;

import android.app.Notification;
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

public class UpdateNotification extends Notification
{
    private static final String TAG_CLASS       = "UpdateNotification::";
    private final int NOTIFICATION_ID;

    public static final int DOWNLOAD_NOTIFICATION   = 1;
    public static final int STATE_NOTIFICATION      = 2;

    private final Context m_Context;
    private final PendingIntent m_PendingIntent;
    private final NotificationManager m_NotificationManager;
    private NotificationCompat.Builder  m_NotificationBuilder;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public UpdateNotification( int typeOfNotification, Context context )
    {
        this.m_Context = context;
        this.NOTIFICATION_ID = typeOfNotification;
        m_NotificationManager = ( NotificationManager ) m_Context.getSystemService( Context.NOTIFICATION_SERVICE );

        Intent notificationIntent = new Intent( context, MainActivity.class );
        TaskStackBuilder stackBuilder = TaskStackBuilder.create( context );
        stackBuilder.addNextIntentWithParentStack( notificationIntent );
        m_PendingIntent =
                stackBuilder.getPendingIntent(0,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public NotificationCompat.Builder getGeneralNotificationBuilder( String title, String text,
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
        return builder;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void initializeUpdateOSNotification(String title, String text,
                                               int smallIconResId, int largeIconResId,
                                               boolean autoCancel )
    {
        long sendTime = System.currentTimeMillis();

        m_NotificationBuilder = getGeneralNotificationBuilder( title, text,
                smallIconResId, largeIconResId, autoCancel, sendTime );
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setProgress( int progress )
    {
        m_NotificationBuilder.setProgress( 100, progress, false );
        m_NotificationManager.notify(
                UpdateNotification.DOWNLOAD_NOTIFICATION, getM_NotificationBuilder().build() );
    }

    public NotificationCompat.Builder getM_NotificationBuilder()
    {
        return m_NotificationBuilder;
    }

    public NotificationManager getM_NotificationManager()
    {
        return m_NotificationManager;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void showUpdateStateNotificationNoAvailableUpdate()
    {
        initializeUpdateOSNotification( m_Context.getResources().getString( R.string.notification_title ),
                m_Context.getResources().getString( R.string.cloud_progress_bar_no_update ),
                R.drawable.ic_info, R.drawable.icon, false );
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void showUpdateStateNotificationAvailableUpdate()
    {
        initializeUpdateOSNotification( m_Context.getResources().getString( R.string.notification_title ),
                m_Context.getResources().getString( R.string.cloud_progress_bar_new_update ),
                R.drawable.ic_info, R.drawable.icon, false );
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void showUpdateStateNotificationDownloadFailed()
    {
        initializeUpdateOSNotification( m_Context.getResources().getString( R.string.notification_title ),
                m_Context.getResources().getString( R.string.notification_text_failed ),
                R.drawable.ic_info, R.drawable.icon, false );
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void showUpdateStateNotificationDownloadedUpdate()
    {
        initializeUpdateOSNotification( m_Context.getResources().getString( R.string.notification_title ),
                m_Context.getResources().getString( R.string.notification_text_done ),
                R.drawable.ic_info, R.drawable.icon, false );
    }
}
