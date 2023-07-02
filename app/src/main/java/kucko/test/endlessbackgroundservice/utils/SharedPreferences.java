package kucko.test.endlessbackgroundservice.utils;

import android.content.Context;

import androidx.annotation.NonNull;

public class SharedPreferences
{
    public static final String sharedPreferencesName = "SharedPreferencesAboutDownload";
    public static final int mode = Context.MODE_PRIVATE;

    public enum KEYS
    {
        FILE_NAME(              "FILE_NAME" ),
        FILE_SIZE(              "FILE_SIZE" ),
        SAVED_UPDATE_VERSION(   "SAVED_UPDATE_VERSION" ),
        UPDATE_VERSION(         "UPDATE_VERSION" ),
        UPDATE_URL(             "UPDATE_URL" ),
        IF_NEW_UPDATE(          "IF_NEW_UPDATE" ),
        PR_NFU_HW_VERSION(      "PR_NFU_HW_VERSION"),
        PR_NFU_FW_VERSION(      "PR_NFU_FW_VERSION"),
        PR_ECR_FW_VERSION(      "PR_ECR_FW_VERSION")
        ;

        private final String text;
        KEYS( final String text )
        {
            this.text = text;
        }

        @NonNull
        @Override
        public String toString()
        {
            return text;
        }
    }

    private final android.content.SharedPreferences sharedPreferences;

    public SharedPreferences(Context context )
    {
        sharedPreferences = context.getSharedPreferences( sharedPreferencesName, mode );
    }

    public void saveData( KEYS key, String data )
    {
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString( key.toString(), data );
        editor.apply();
    }

    public String loadData( KEYS key )
    {
        return sharedPreferences.getString( key.toString(), "" );
    }

    public void resetDataInSP()
    {
        saveData( KEYS.FILE_NAME,               "" );
        saveData( KEYS.FILE_SIZE,               "" );
        saveData( KEYS.SAVED_UPDATE_VERSION,    "" );
        saveData( KEYS.UPDATE_VERSION,          "" );
        saveData( KEYS.UPDATE_URL,              "" );
        saveData( KEYS.IF_NEW_UPDATE,           "" );
        saveData( KEYS.PR_NFU_HW_VERSION,       "" );
        saveData( KEYS.PR_NFU_FW_VERSION,       "" );
        saveData( KEYS.PR_ECR_FW_VERSION,       "" );
    }

    public void resetUpdateData()
    {
        saveData( KEYS.FILE_NAME,               "" );
        saveData( KEYS.FILE_SIZE,               "" );
        saveData( KEYS.UPDATE_VERSION,          "" );
        saveData( KEYS.UPDATE_URL,              "" );
    }

}
