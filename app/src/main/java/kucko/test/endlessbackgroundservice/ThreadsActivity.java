package kucko.test.endlessbackgroundservice;

import static java.lang.Thread.sleep;

import static kucko.test.endlessbackgroundservice.MainActivity.LOG_TAG;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ThreadsActivity extends AppCompatActivity
{
    private static final String TAG_CLASS = "MainActivity::";

    private TextView resultText;
    private static int count;
    private static String outputLog;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.threads_activity );

        resultText = findViewById( R.id.result_text );
        Button startWithoutMutexButton = findViewById( R.id.start_without_mutex_button );
        Button startWithMutexButton = findViewById( R.id.start_with_mutex_button );
        Button clearButton = findViewById( R.id.clear_button );

        startWithoutMutexButton.setOnClickListener( v -> runTest( false ) );

        startWithMutexButton.setOnClickListener( v -> runTest( true ) );

        clearButton.setOnClickListener( v -> resultText.setText( "" ) );
    }

    private void runTest( final boolean withMutex )
    {
        outputLog =  "Test spustený " + ( withMutex ? "s mutexom." : "bez mutexu." ) + "\n" ;
        Log.d(LOG_TAG, TAG_CLASS + "Test spustený " + ( withMutex ? "s mutexom." : "bez mutexu." ) );

            Thread[] threads = new Thread[10];

        for ( int i=0; i < threads.length; i++ )
        {
            threads[i] = new MutexThreads( i, withMutex );
        }

        for ( Thread thread : threads )
        {
            thread.start();
        }

        new Handler().postDelayed( () -> resultText.setText( outputLog ), 1000 );
    }

    public static synchronized void incrementWithMutex( int threadId )
    {
        for (int i = 0; i < 10; i++)
        {
            count++;
            outputLog += "Vlákno " + threadId + ": count = " + count + "\n" ;
            Log.d(LOG_TAG, TAG_CLASS + " incrementWithMutex(): Vlákno " + threadId + ": count = " + count );
        }
    }

    public static void incrementWithoutMutex( int threadId )
    {
        for (int i = 0; i < 10; i++)
        {
            count++;
            outputLog += "Vlákno " + threadId + ": count = " + count + "\n" ;
            Log.d(LOG_TAG, TAG_CLASS + " incrementWithoutMutex(): Vlákno " + threadId + ": count = " + count );
        }
    }
}

class MutexThreads extends Thread
{
    private final int threadID;
    private final boolean mutex;

    public MutexThreads( int threadID, boolean mutex )
    {
        this.threadID = threadID;
        this.mutex = mutex;
    }

    @Override
    public void run()
    {
        super.run();

        if( mutex )
        {
            ThreadsActivity.incrementWithMutex( threadID );
        }
        else
        {
            ThreadsActivity.incrementWithoutMutex( threadID );
        }
    }
}
