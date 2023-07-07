package kucko.test.endlessbackgroundservice.utils;

import static kucko.test.endlessbackgroundservice.MainActivity.LOG_TAG;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class FileEx extends File
{
    private static final String TAG_CLASS = "FileEx::";

    public FileEx(String pathname )
    {
        super( pathname );
    }

    public FileEx(String parent, String child )
    {
        super( parent, child );
    }

    public FileEx(File parent, String child )
    {
        super( parent, child );
    }

    public FileEx(URI uri )
    {
        super( uri );
    }

    /**
     * Interface definition for a callback to be invoked regularly as
     * verification proceeds.
     */
    public interface ProgressListener
    {
        /**
         * Called periodically as the verification progresses.
         *
         * @param progress  the approximate percentage of the
         *        verification that has been completed, ranging from 0
         *        to 100 (inclusive).
         */
        public void onProgress( int progress );
    }

    public static String getFilePath( final Context context, final Uri uri )
    {
        String retVal = null;

        retVal = getPath( context, uri );
        Log.e( LOG_TAG, TAG_CLASS + "getFilePath() - Path decode 1. iteration (retVal = " + retVal + ")" );

        if( retVal != null )
        {
            if( retVal.contains( "/legacy" ) )
            {
                retVal = getUsbPath( retVal );
                Log.e( LOG_TAG, TAG_CLASS + "getFilePath() - Path decode 2. iteration (retVal = " + retVal + ")" );
            }
        }

        return( retVal );
    }

    // Get humen readable size of file (in B, KB, MB, ...)
    public String getHumenReadableFileSize()
    {
        String retVal = "";

        Log.e( LOG_TAG, TAG_CLASS + "getHumenReadableFileSize() - File length " + String.valueOf( length() ) );

        do
        {
            long fileSize = length();

            //Bytes
            if( fileSize < 1024 )
            {
                retVal = String.valueOf( fileSize ) + " B";
                break;
            }

            //KiloBytes
            if( fileSize < Math.pow( 1024, 2 ) )
            {
                retVal = String.valueOf( fileSize / 1024 ) + " KB";
                break;
            }

            //MegaBytes
            if( fileSize < Math.pow( 1024, 3 ) )
            {
                retVal = String.valueOf( fileSize / (long)Math.pow( 1024, 2 ) ) + " MB";
                break;
            }

            //GigaBytes
            if( fileSize < Math.pow( 1024, 4 ) )
            {
                retVal = String.valueOf( fileSize / (long)Math.pow( 1024, 3 ) ) + " GB";
                break;
            }
        } while( false );


        return( retVal );
    }

    // Create directory if not exists
    public static Boolean createDir( String pathToDir )
    {
        File dirTmp = new File( pathToDir );
        Boolean bRetVal = true;

        Log.e( LOG_TAG, TAG_CLASS + "createDir() '" + pathToDir + "'" );

        try
        {
            if( !dirTmp.exists() )
            {
                if( !dirTmp.mkdir() )
                {
                    Log.e( LOG_TAG, TAG_CLASS + "createDir() - failed" );
                    bRetVal = false;
                }
            }
        }
        catch( SecurityException e )
        {
            Log.e( LOG_TAG, TAG_CLASS + "createDir() - Exception = " + e.getMessage() );
            bRetVal = false;
        }

        return( bRetVal );
    }

    // Copy of file can take significant time, so this
    // function should not be called from a UI thread.
    public static Boolean copyFile( String sourceFilePath,
                                    String destinationFilePath,
                                    ProgressListener progressListener )
    {
        Boolean bRes    = false;
        int lastPercent = 0;

        do
        {
            Log.d( LOG_TAG, TAG_CLASS + "copyFile() - START" );

            if( progressListener != null )
            {
                progressListener.onProgress( lastPercent );
            }

            try
            {
                File srcFileTmp = new File( sourceFilePath );
                InputStream in = new FileInputStream( srcFileTmp );

                File destFileTmp = new File( destinationFilePath );
                OutputStream out = new FileOutputStream( destFileTmp );

                long lenghtOfFile = srcFileTmp.length();
                byte[] buf = new byte[ 4096 ];
                int bytesRead;
                long totalCopied = 0;

                // Copy file with progress notification
                while( (bytesRead = in.read( buf )) >= 0 )
                {
                    out.write( buf, 0, bytesRead );
                    totalCopied += bytesRead;

                    if( progressListener != null )
                    {
                        int actualPercent = (int)( (totalCopied * 100) / lenghtOfFile );
                        if( lastPercent != actualPercent )
                        {
                            lastPercent = actualPercent;
                            progressListener.onProgress( lastPercent );
                        }
                    }
                }

                if( progressListener != null )
                {
                    progressListener.onProgress( 100 );
                }
                Log.d( LOG_TAG, TAG_CLASS + "copyFile() - END" );

                in.close();
                out.close();

                bRes = true;
            }
            catch( IOException e )
            {
                e.printStackTrace();
                Log.d( LOG_TAG, TAG_CLASS + "copyFile() - Exception = " + e.getMessage() );
            }
        } while( false );

        return( bRes );
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br>
     * <br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     */
    @SuppressLint("NewApi")
    private static String getPath( final Context context, final Uri uri )
    {
        Uri contentUri = null;

        // check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        String selection = null;
        String[] selectionArgs = null;

        // DocumentProvider
        if( isKitKat && DocumentsContract.isDocumentUri( context, uri ) )
        {
            if( isExternalStorageDocument( uri ) )              // ExternalStorageProvider
            {
                final String docId = DocumentsContract.getDocumentId( uri );
                final String[] split = docId.split( ":" );
                final String type = split[ 0 ];

                String fullPath = getPathFromExtSD( split );
                if( fullPath != "" )
                {
                    return fullPath;
                }
                else
                {
                    return null;
                }
            }
            else if( isDownloadsDocument(uri) )                 // DownloadsProvider
            {
                if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
                {
                    final String id;
                    Cursor cursor = null;
                    try
                    {
                        cursor = context.getContentResolver().query( uri, new String[]{ MediaStore.MediaColumns.DISPLAY_NAME }, null, null, null );
                        if( cursor != null && cursor.moveToFirst() )
                        {
                            String fileName = cursor.getString( 0 );
                            String path = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
                            if( !TextUtils.isEmpty(path) )
                            {
                                return path;
                            }
                        }
                    }
                    finally
                    {
                        if( cursor != null )
                        {
                            cursor.close();
                        }
                    }

                    id = DocumentsContract.getDocumentId( uri );
                    if( !TextUtils.isEmpty( id ) )
                    {
                        if( id.startsWith( "raw:" ) )
                        {
                            return id.replaceFirst( "raw:", "" );
                        }
                        String[] contentUriPrefixesToTry = new String[]
                        {
                            "content://downloads/public_downloads",
                            "content://downloads/my_downloads"
                        };
                        for( String contentUriPrefix : contentUriPrefixesToTry )
                        {
                            try
                            {
                                contentUri = ContentUris.withAppendedId( Uri.parse( contentUriPrefix ), Long.valueOf( id ) );

                                /*   final Uri contentUri = ContentUris.withAppendedId( Uri.parse( "content://downloads/public_downloads" ), Long.valueOf( id ) );*/

                                return getDataColumn( context, contentUri, null, null );
                            }
                            catch( NumberFormatException e )
                            {
                                //In Android 8 and Android P the id is not a number
                                return uri.getPath().replaceFirst( "^/document/raw:", "" ).replaceFirst( "^raw:", "" );
                            }
                        }
                    }
                }
                else
                {
                    final String id = DocumentsContract.getDocumentId( uri );
                    final boolean isOreo = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
                    if( id.startsWith( "raw:" ) )
                    {
                        return id.replaceFirst( "raw:", "" );
                    }
                    try
                    {
                        contentUri = ContentUris.withAppendedId( Uri.parse( "content://downloads/public_downloads" ), Long.valueOf( id ) );

                    }
                    catch( NumberFormatException e )
                    {
                        e.printStackTrace();
                    }
                    if( contentUri != null )
                    {
                        return getDataColumn( context, contentUri, null, null );
                    }
                }
            }
            else if( isMediaDocument( uri ) )                   // MediaProvider
            {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split( ":" );
                final String type = split[ 0 ];

                if( "image".equals(type) )
                {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }
                else if( "video".equals( type ) )
                {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }
                else if( "audio".equals( type ) )
                {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{ split[ 1 ] };


                return getDataColumn( context, contentUri, selection, selectionArgs );
            }
            else if( isGoogleDriveUri( uri ) )
            {
                return getDriveFilePath( uri, context );
            }
        }
        else if( "content".equalsIgnoreCase(uri.getScheme() ) ) // MediaStore (and general)
        {
            if( isGooglePhotosUri( uri ) )
            {
                return uri.getLastPathSegment();
            }

            if( isGoogleDriveUri( uri ) )
            {
                return getDriveFilePath( uri, context );
            }
            if( Build.VERSION.SDK_INT == Build.VERSION_CODES.N )
            {
                // return getFilePathFromURI(context,uri);
                return getMediaFilePathForN( uri, context );
                // return getRealPathFromURI(context,uri);
            }
            else
            {
                return getDataColumn( context, uri, null, null );
            }
        }
        else if( "file".equalsIgnoreCase( uri.getScheme() ) )   // File
        {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Check if a file exists on device
     *
     * @param filePath The absolute file path
     */
    private static boolean fileExists( String filePath )
    {
        File file = new File( filePath );

        return file.exists();
    }


    /**
     * Get full file path from external storage
     *
     * @param pathData The storage type and the relative path
     */
    private static String getPathFromExtSD( String[] pathData )
    {
        final String type = pathData[ 0 ];
        final String relativePath = "/" + pathData[ 1 ];
        String fullPath = "";

        // on my Sony devices (4.4.4 & 5.1.1), `type` is a dynamic string
        // something like "71F8-2C0A", some kind of unique id per storage
        // don't know any API that can get the root path of that storage based on its id.
        //
        // so no "primary" type, but let the check here for other devices
        if ("primary".equalsIgnoreCase( type ))
        {
            fullPath = Environment.getExternalStorageDirectory() + relativePath;
            if( fileExists( fullPath ) )
            {
                return fullPath;
            }
        }

        // Environment.isExternalStorageRemovable() is `true` for external and internal storage
        // so we cannot relay on it.
        //
        // instead, for each possible path, check if file exists
        // we'll start with secondary storage as this could be our (physically) removable sd card
        fullPath = System.getenv( "SECONDARY_STORAGE" ) + relativePath;
        if( fileExists(fullPath ) )
        {
            return fullPath;
        }

        fullPath = System.getenv( "EXTERNAL_STORAGE" ) + relativePath;
        if( fileExists( fullPath ) )
        {
            return fullPath;
        }

        return fullPath;
    }

    private static String getDriveFilePath( Uri uri, Context context )
    {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query( returnUri, null, null, null, null );

        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex( OpenableColumns.DISPLAY_NAME );
        int sizeIndex = returnCursor.getColumnIndex( OpenableColumns.SIZE );
        returnCursor.moveToFirst();
        String name = returnCursor.getString( nameIndex );
        String size = Long.toString( returnCursor.getLong( sizeIndex ) );
        File file = new File( context.getCacheDir(), name );
        try
        {
            InputStream inputStream = context.getContentResolver().openInputStream( uri );
            FileOutputStream outputStream = new FileOutputStream( file );
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();
            int bufferSize = Math.min( bytesAvailable, maxBufferSize );
            final byte[] buffers = new byte[ bufferSize ];

            while( (read = inputStream.read( buffers ) ) != -1 )
            {
                outputStream.write( buffers, 0, read );
            }

            inputStream.close();
            outputStream.close();
            Log.e( LOG_TAG, TAG_CLASS + "getDriveFilePath() - File path " + file.getPath() );
            Log.e( LOG_TAG, TAG_CLASS + "getDriveFilePath() - File size " + file.length() );
        }
        catch( Exception e )
        {
            Log.e( LOG_TAG, TAG_CLASS + "getDriveFilePath() - Exception = " + e.getMessage() );
        }

        return file.getPath();
    }

    private static String getMediaFilePathForN( Uri uri, Context context )
    {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query( returnUri, null, null, null, null );

        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex( OpenableColumns.DISPLAY_NAME );
        int sizeIndex = returnCursor.getColumnIndex( OpenableColumns.SIZE );
        returnCursor.moveToFirst();
        String name = returnCursor.getString( nameIndex );
        String size = Long.toString( returnCursor.getLong( sizeIndex ) );
        File file = new File( context.getFilesDir(), name );
        try
        {
            InputStream inputStream = context.getContentResolver().openInputStream( uri );
            FileOutputStream outputStream = new FileOutputStream( file );
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();
            int bufferSize = Math.min( bytesAvailable, maxBufferSize );
            final byte[] buffers = new byte[ bufferSize ];

            while( (read = inputStream.read( buffers ) ) != -1 )
            {
                outputStream.write( buffers, 0, read );
            }

            inputStream.close();
            outputStream.close();
            Log.e( LOG_TAG, TAG_CLASS + "getMediaFilePathForN() - File path " + file.getPath() );
            Log.e( LOG_TAG, TAG_CLASS + "getMediaFilePathForN() - File size " + file.length() );
        }
        catch( Exception e )
        {
            Log.e( LOG_TAG, TAG_CLASS + "getMediaFilePathForN() - Exception = " + e.getMessage() );
        }

        return file.getPath();
    }

    private static String getDataColumn( Context context, Uri uri,
                                         String selection, String[] selectionArgs )
    {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try
        {
            cursor = context.getContentResolver().query( uri, projection,
                                                         selection, selectionArgs, null );

            if( cursor != null && cursor.moveToFirst() )
            {
                final int index = cursor.getColumnIndexOrThrow( column );
                return cursor.getString( index );
            }
        }
        finally
        {
            if( cursor != null )
            {
                cursor.close();
            }
        }

        return null;
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument( Uri uri )
    {
        return "com.android.externalstorage.documents".equals( uri.getAuthority() );
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument( Uri uri )
    {
        return "com.android.providers.downloads.documents".equals( uri.getAuthority() );
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument( Uri uri )
    {
        return "com.android.providers.media.documents".equals( uri.getAuthority() );
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri( Uri uri )
    {
        return "com.google.android.apps.photos.content".equals( uri.getAuthority() );
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Drive.
     */
    private static boolean isGoogleDriveUri( Uri uri )
    {
        return "com.google.android.apps.docs.storage".equals( uri.getAuthority() ) || "com.google.android.apps.docs.storage.legacy".equals( uri.getAuthority() );
    }

    private static String getUsbPath( String filePath )
    {
        String result;

        File storageDirectory = new File( "/storage" );
        if( !storageDirectory.exists() )
        {
            Log.e( LOG_TAG, TAG_CLASS + "getUsbPath() - '/storage' does not exist on this device" );

            return "";
        }

        File[] files = storageDirectory.listFiles();
        if( files == null )
        {
            Log.e( LOG_TAG, TAG_CLASS + "getUsbPath() - Null when requesting directories inside '/storage'" );

            return "";
        }

        List<String> possibleUSBStorageMounts = new ArrayList<>();
        for( File file : files )
        {
            String path = file.getPath();
            if( path.contains( "emulated" ) ||
                path.contains( "sdcard" ) ||
                path.contains( "self" ) )
            {
                Log.e( LOG_TAG, TAG_CLASS + "getUsbPath() - Found '" + path + "' - not USB" );
            }
            else
            {
                possibleUSBStorageMounts.add( path );
            }
        }

        if( possibleUSBStorageMounts.size() == 0 )
        {
            Log.e( LOG_TAG, TAG_CLASS + "getUsbPath() - Did not find any possible USB mounts" );

            return "";
        }

        if( possibleUSBStorageMounts.size() > 1 )
        {
            Log.e( LOG_TAG, TAG_CLASS + "getUsbPath() - Found multiple possible USB mount points, choosing the first one" );
        }

        filePath = filePath.replace( "/storage/emulated/legacy", possibleUSBStorageMounts.get( 0 ) );
        result = filePath;
        Log.e( LOG_TAG, TAG_CLASS + "getUsbPath() - Path result: " + result );

        return result;
    }
}
