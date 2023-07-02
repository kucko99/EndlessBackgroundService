package kucko.test.endlessbackgroundservice.services;

import android.content.Context;
import android.content.SharedPreferences;

public class UpdateOSServiceState
{
    public enum ServiceState
    {
        STARTED,
        STOPPED
    }

    private static final String NAME = "SPYSERVICE_KEY";
    private static final String KEY = "SPYSERVICE_STATE";

    public static void setServiceState( Context context, ServiceState state )
    {
        SharedPreferences sharedPrefs = getPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString( KEY, state.name() );
        editor.apply();
    }

    public static ServiceState getServiceState( Context context )
    {
        SharedPreferences sharedPrefs = getPreferences( context );
        String value = sharedPrefs.getString( KEY, ServiceState.STOPPED.name() );
        return ServiceState.valueOf( value );
    }

    private static SharedPreferences getPreferences( Context context )
    {
        return context.getSharedPreferences( NAME, 0 );
    }
}
