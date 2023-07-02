package kucko.test.endlessbackgroundservice.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import kucko.test.endlessbackgroundservice.R;

public class CustomAlertDialog
{
    private static final String TAG_CLASS = "CustomAlertDialog::";

    private AlertDialog.Builder     m_AlertDialog;

    public void showError( Context context,
                           String messageDlg,
                           boolean cancelable,
                           final DialogInterface.OnClickListener listener )
    {
        m_AlertDialog = new AlertDialog.Builder( context );
        m_AlertDialog.setIcon( R.drawable.ic_error );
        m_AlertDialog.setTitle( R.string.dialogTitle );
        m_AlertDialog.setMessage( messageDlg );
        m_AlertDialog.setPositiveButton( R.string.button_ok, listener );
        m_AlertDialog.setCancelable( cancelable );
        m_AlertDialog.show();
    }

    public void showWarning( Context context,
                             String messageDlg,
                             boolean cancelable,
                             final DialogInterface.OnClickListener listener )
    {
        m_AlertDialog = new AlertDialog.Builder( context );
        m_AlertDialog.setIcon( R.drawable.ic_warning );
        m_AlertDialog.setTitle( R.string.dialogTitle );
        m_AlertDialog.setMessage( messageDlg );
        m_AlertDialog.setPositiveButton( R.string.button_ok, listener );
        m_AlertDialog.setCancelable( cancelable );
        m_AlertDialog.show();
    }

    public void showInfo( Context context,
                          String messageDlg,
                          final DialogInterface.OnClickListener listener )
    {
        m_AlertDialog = new AlertDialog.Builder( context );
        m_AlertDialog.setIcon( R.drawable.ic_info );
        m_AlertDialog.setTitle( R.string.dialogTitle );
        m_AlertDialog.setMessage( messageDlg );
        m_AlertDialog.setPositiveButton( R.string.button_ok, listener );
        m_AlertDialog.show();
    }

    public void showQuestion( Context context,
                              String messageDlg,
                              final DialogInterface.OnClickListener listenerPositive,
                              final DialogInterface.OnClickListener listenerNegative )
    {
        m_AlertDialog = new AlertDialog.Builder( context );
        m_AlertDialog.setIcon( R.drawable.ic_question );
        m_AlertDialog.setTitle( R.string.dialogTitle );
        m_AlertDialog.setMessage( messageDlg );
        m_AlertDialog.setPositiveButton( R.string.button_yes, listenerPositive );
        m_AlertDialog.setNegativeButton( R.string.button_no, listenerNegative );
        m_AlertDialog.show();
    }
}
