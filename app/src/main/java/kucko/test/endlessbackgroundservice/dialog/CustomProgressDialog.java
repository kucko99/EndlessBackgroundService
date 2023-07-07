package kucko.test.endlessbackgroundservice.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import kucko.test.endlessbackgroundservice.R;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CustomProgressDialog
{

    private final Context context;
    public final Dialog dialog;

    private Button cancelButton;

    private ProgressBar progressBar;

    private TextView progressPercentage1;
    private TextView progressPercentage2;

    public CustomProgressDialog(Context context, int currentlyProgress )
    {
        this.context = context;
        this.dialog = new Dialog( context );

        createProgressDialogView();
        setProgress( currentlyProgress );
    }

    private void createProgressDialogView()
    {
        dialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
        dialog.setContentView( R.layout.custom_progress_dialog );
        dialog.setCancelable( false );

        progressBar = dialog.findViewById( R.id.dialogProgressBar );

        TextView progressDialogTitle = dialog.findViewById( R.id.dialogTitle );
        progressDialogTitle.setText( context.getResources().getString( R.string.notification_title ) );
        TextView progressDialogMessage = dialog.findViewById( R.id.dialogMessage );
        progressDialogMessage.setText( context.getResources().getString( R.string.notification_text_downloading ) );

        progressPercentage1 = dialog.findViewById( R.id.dialogProgress1 );
        progressPercentage2 = dialog.findViewById( R.id.dialogProgress2 );

        cancelButton = dialog.findViewById( R.id.cancelButton );
        cancelButton.setText( context.getResources().getString( R.string.button_cancel ) );

        dialog.create();
    }

    public void setProgress( int progress )
    {
        progressBar.setProgress( progress );
        progressPercentage1.setText( String.valueOf( progress ) );
        progressPercentage2.setText( String.valueOf( progress ) );
    }

    public void setButton( View.OnClickListener onClickListener )
    {
        cancelButton.setOnClickListener( onClickListener );
    }

}

