package kucko.test.endlessbackgroundservice.asynctask;

// Interface for asynchronous task notification
public interface AsyncTaskNotif
{
    // Asynchronous task
    enum ASYNC_TASK
    {
        eAT_CHECK_FILE,
        eAT_COPY_FILE
    }

    default void onCreateAsyncTaskNotif( ASYNC_TASK eAsyncTask )
    {
    };

    void onFinishAsyncTaskNotif( ASYNC_TASK eAsyncTask, Boolean bResult );
}
