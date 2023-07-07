package kucko.test.endlessbackgroundservice;

import static kucko.test.endlessbackgroundservice.MainActivity.LOG_TAG;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.RecoverySystem;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;

import kucko.test.endlessbackgroundservice.asynctask.AsyncTaskNotif;
import kucko.test.endlessbackgroundservice.dialog.CustomAlertDialog;
import kucko.test.endlessbackgroundservice.utils.FileEx;
import kucko.test.endlessbackgroundservice.utils.Utils;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class UpdateOS implements AsyncTaskNotif
{
    private static final String TAG_CLASS                       = "UpdateOS::";
    public static final int BATTERY_MIN_PERCENTAGE_CONST       = 50;

    //constant for the file copy to destination directory
    public static final String DESTINATION_DIR_CONST           = /*"/cache/update";*/"/storage/emulated/legacy/update";

    private final Context                 m_Context;
    private final ProgressDialog          m_ProgressDialog;
    private final CustomAlertDialog       m_CustomAlertDialog;
    private FileEx                        m_SrcFile;

    public UpdateOS(Context context )
    {
        m_Context = context;

        // Create custom alert dialog
        m_CustomAlertDialog = new CustomAlertDialog();

        // Create progress bar dialog
        m_ProgressDialog = new ProgressDialog( m_Context );

        // Create dir for updates
        createUpdateDirInCache();
    }

    private void createUpdateDirInCache()
    {
        File dir = new File( DESTINATION_DIR_CONST );
        if ( !dir.exists() )
        {
            if ( dir.mkdirs() )
            {
                Log.d( LOG_TAG, TAG_CLASS + " directory '" + DESTINATION_DIR_CONST +"' created successfully.");
            } else
            {
                Log.d( LOG_TAG, TAG_CLASS + " failed to create directory '" + DESTINATION_DIR_CONST +"'.");
            }
        }
        else
        {
            Log.d( LOG_TAG, TAG_CLASS + " directory '" + DESTINATION_DIR_CONST +"' already exists.");
        }
    }

    public FileEx getOTAsrcFile()
    {
        return( m_SrcFile );
    }

    public void setOTAsrcFile( String OTAfilePath )
    {
        m_SrcFile = new FileEx( OTAfilePath );
    }

    public void runUpdateOS( FileEx sourceFile )
    {
        do
        {
            if( !checkUpdateOScondition( sourceFile ) )
            {
                break;
            }

            // Create click listener
            DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
                // Run copy OTA file as asynchronous task and receive this result in onFinishCopyFile()
                // if onFinishCheckOTAfile() receive OK then run check OTA file as asynchronous task
                copyOTAfile( sourceFile.getPath(),
                        DESTINATION_DIR_CONST + "/" + m_SrcFile.getName(),
                        m_ProgressDialog );
            };

            m_CustomAlertDialog.showQuestion( m_Context,
                    m_Context.getResources().getString( R.string.updateOsQuestion ),
                    onClickListener,
                    null );
        } while( false );
    }

    @SuppressLint("StringFormatInvalid")
    private Boolean checkUpdateOScondition( FileEx sourceFile )
    {
        boolean bRes = true;

        do
        {
            // Check if file is choose
            if( sourceFile == null )
            {
                m_CustomAlertDialog.showError( m_Context,
                        m_Context.getResources().getString( R.string.noChosenFile ),
                        true,
                        null );
                bRes = false;
                break;
            }


            // Check capacity of battery
            int battery = Utils.getBatteryPercentage( m_Context );
            if( battery < BATTERY_MIN_PERCENTAGE_CONST )
            {
                m_CustomAlertDialog.showError( m_Context,
                        m_Context.getResources().getString( R.string.batteryErrorText, BATTERY_MIN_PERCENTAGE_CONST ),
                        true,
                        null );
                bRes = false;
                break;
            }
        } while( false );

        return( bRes );
    }

    // Run check OTA file as asynchronous task and receive this result in onFinishCheckOTAfile()
    private void checkOTAfile( FileEx sourceFile,
                               ProgressDialog progressBarDlg )
    {
        // Set progres bar dialog
        progressBarDlg.setTitle( m_Context.getResources().getString( R.string.dialogTitle ) );
        progressBarDlg.setIcon( R.drawable.ic_launcher );
        progressBarDlg.setMessage( m_Context.getResources().getText( R.string.checkingFile ) );
        progressBarDlg.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
        progressBarDlg.setMax( 100 );
        progressBarDlg.setCancelable( false );
        progressBarDlg.show();

        // Create progress listener to show progress bar about copy OTA file
        RecoverySystem.ProgressListener progressListener = progressBarDlg::setProgress;

        // Create and run asynchronous task
        Log.d( LOG_TAG, TAG_CLASS + "checkOTAfile() - asynchronous task for Check File - START" );
//        CheckOTAfileAsyncTask checkOTAfileAsyncTask = new CheckOTAfileAsyncTask( this, progressListener );
//        checkOTAfileAsyncTask.execute( sourceFile );
        Log.d( LOG_TAG, TAG_CLASS + "checkOTAfile() - asynchronous task for Check File - END" );
    }

    // Run copy OTA file as asynchronous task and receive this result in onFinishCopyFile()
    private void copyOTAfile( String sourceFilePath,
                              String destinationFilePath,
                              ProgressDialog progressBarDlg )
    {
        do
        {
            // Create destination directory
            if( !FileEx.createDir( DESTINATION_DIR_CONST ) )
            {
                // Show create directory error message
                m_CustomAlertDialog.showError( m_Context,
                        m_Context.getResources().getString( R.string.createDestDirErr ),
                        true,
                        null );
                break;
            }

            // Set progres bar dialog
            progressBarDlg.setTitle( m_Context.getResources().getString( R.string.dialogTitle ) );
            progressBarDlg.setIcon( R.drawable.ic_launcher );
            progressBarDlg.setMessage( m_Context.getResources().getText( R.string.copyingFile ) );
            progressBarDlg.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
            progressBarDlg.setMax( 100 );
            progressBarDlg.setCancelable( false );
            progressBarDlg.show();

            // Create progress listener to show progress bar about copy OTA file
            FileEx.ProgressListener progressListener = progressBarDlg::setProgress;

            deleteOlderDownloadedVersion();

            // Create and run asynchronous task
            Log.d( LOG_TAG, TAG_CLASS + "copyOTAfile() - asynchronous task for Copy File - START" );
//            CopyFileAsyncTask copyFileAsyncTask = new CopyFileAsyncTask( this, progressListener );
//            copyFileAsyncTask.execute( sourceFilePath, destinationFilePath );
            Log.d( LOG_TAG, TAG_CLASS + "copyOTAfile() - asynchronous task for Copy File - END" );
        } while( false );
    }

    //method that starts the system update after checking and copying the .zip file
    public static void installUpdate( Context context, String OTAfilePath )
    {
        Log.d( LOG_TAG, TAG_CLASS + "installUpdate() - Starting install update OS (" + OTAfilePath + ")" );

        File updateFile = new File( OTAfilePath );
        try
        {
            RecoverySystem.installPackage( context, updateFile );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onFinishAsyncTaskNotif( ASYNC_TASK eAsyncTask, Boolean bResult )
    {
        Log.d( LOG_TAG, TAG_CLASS + "onFinishAsyncTaskNotif() - (eAsyncTask = " + eAsyncTask.toString() + ", result = " + bResult.toString() + ")" );
        if( eAsyncTask == ASYNC_TASK.eAT_COPY_FILE )
        {
            onFinishCopyFile( bResult );
        }

        if( eAsyncTask == ASYNC_TASK.eAT_CHECK_FILE )
        {
            onFinishCheckOTAfile( bResult );
        }
    }

    private void onFinishCheckOTAfile( Boolean bResult )
    {
        Log.d( LOG_TAG, TAG_CLASS + "onFinishCheckOTAfile() - result of check OTA file" );

        m_ProgressDialog.hide();

        if( bResult )
        {
            Log.d( LOG_TAG, TAG_CLASS + "onFinishCopyFile() - run install update" );
            installUpdate( m_Context,DESTINATION_DIR_CONST + "/" + m_SrcFile.getName() );
        }
        else
        {
            Log.d( LOG_TAG, TAG_CLASS + "onFinishCheckOTAfile() - clear information about actual file" );

            // Show copy file error message
            m_CustomAlertDialog.showError( m_Context,
                    m_Context.getResources().getString( R.string.incorrectOTAfile ),
                    true,
                    null );

            // Delete file from cache, clear information about actual file and send message to clear preferences to default values
            m_SrcFile = null;
            //m_Context.sendBroadcast( new Intent( UPDATE_OS_PREFER_CLEAR_MSG ) );
        }
    }

    private void onFinishCopyFile( Boolean bResult )
    {
        Log.d( LOG_TAG, TAG_CLASS + "onFinishCopyFile() - result of copy OTA file" );

        //progressDialog.dismiss();
        m_ProgressDialog.hide();

        if( bResult )
        {
            // Run check OTA file as asynchronous task and receive this result in onFinishCheckOTAfile()
            // if onFinishCheckOTAfile() receive OK then run copy OTA file as asynchronous task
            checkOTAfile( m_SrcFile, m_ProgressDialog );
        }
        else
        {
            // Show copy file error message
            m_CustomAlertDialog.showError( m_Context,
                    m_Context.getResources().getString( R.string.prepareUpdateErr ),
                    true,
                    null );
        }
    }

    public static File findFileInCache( String fileName )
    {
        File cacheDir = new File( DESTINATION_DIR_CONST );
        File[] dirInCache = cacheDir.listFiles();

        if( dirInCache != null )
        {
            for ( File file : dirInCache )
            {
                if( file.isFile() && file.getName().equals(fileName) )
                {
                    return file;
                }
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void deleteOlderDownloadedVersion()
    {
        File updateDir = new File( UpdateOS.DESTINATION_DIR_CONST );
        if ( updateDir.exists() )
        {
            File[] files = updateDir.listFiles();
            if( files != null )
            {
                for( File file : files )
                {
                    if( !file.isDirectory() )
                    {
                        if ( file.delete() )
                        {
                            Log.d( LOG_TAG, TAG_CLASS + " file '" + file.getName() + "' deleted successfully." );
                        } else
                        {
                            Log.e( LOG_TAG, TAG_CLASS + " failed to delete file '" + file.getName() + "'." );
                        }
                    }
                }
            }
        }
        else
        {
            Log.d( LOG_TAG, TAG_CLASS + " directory '" + UpdateOS.DESTINATION_DIR_CONST +"' doesn't exist.");
        }
    }

}