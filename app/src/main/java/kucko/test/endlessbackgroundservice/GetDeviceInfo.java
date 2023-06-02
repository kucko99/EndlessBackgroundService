package kucko.test.endlessbackgroundservice;

import static kucko.test.endlessbackgroundservice.MainActivity.LOG_TAG;

import android.util.Log;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

public class GetDeviceInfo
{
    public static final String ERROR_IN_COMMUNICATION   = "Error in communication";
    private static final String TAG_CLASS               = "GetDeviceInfo::";

    private static final Charset defaultCharset = Charset.forName("Windows-1250");
    public static final String elcomPBCPath     = "/dev/elcom_pbc";
    public static final int bufferAllocate      = 2560;

    private final ArrayList<String> deviceInfo;
    private final ArrayList<String> deviceInfoEx;

    private Charset currentCharset;

    public GetDeviceInfo()
    {
        this.currentCharset = defaultCharset;
        deviceInfo = splitInfo( getDevInfo() );
        deviceInfoEx = splitInfo( getDevInfoEx() );

        if( !deviceInfo.isEmpty() )
        {
            String currentEncodingName = getDeviceInfoByIndex(6);
            this.currentCharset = Charset.forName(currentEncodingName);
        }
    }

    private String sendNFUMessage( String message )
    {
        String response = "";

        try
        {
            RandomAccessFile elcom_pbc = new RandomAccessFile( elcomPBCPath, "rw" );
            FileChannel channel = elcom_pbc.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate( bufferAllocate );

            try
            {
                //writing message from buffer into file
                buffer.clear();
                byte[] command = message.getBytes( this.currentCharset );
                buffer.put( command );
                buffer.flip();
                int numOfWrittenBytes = channel.write( buffer );

                //reading response from file channel into buffer
                buffer.clear();
                int numOfReadBytes;
                while( ( numOfReadBytes = channel.read( buffer ) ) == 0 );

                if( numOfReadBytes > 0 )
                {
                    buffer.flip();
                    byte[] rawBytes = new byte[ numOfReadBytes ];
                    buffer.get( rawBytes );
                    response = new String( rawBytes, this.currentCharset );
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

            channel.close();
            elcom_pbc.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return response;
    }

    private ArrayList<String> splitInfo( String info )
    {
        if ( info.isEmpty() )
        {
            return new ArrayList<>();
        }
        else
        {
            ArrayList<String> output = new ArrayList<>( Arrays.asList( info.split( "\t" ) ) );

            //remove Exception info, RSP and Command_ID
            output.remove( 2 );
            output.remove( 1 );
            output.remove( 0 );
            //remove new line in last part
            output.set( output.size()-1,
                    output.get( output.size()-1 ).replace( "\n","" ) );

            return output;
        }
    }

    private boolean checkResponseStatus( String response )
    {
        if( response.isEmpty() )
        {
            return false;
        }
        else
        {
            ArrayList<String> output = new ArrayList<>( Arrays.asList(response.split( "\t" ) ) );
            output.set(output.size() - 1,
                    output.get(output.size() - 1).replace( "\n", "" ) );

            int exception = Integer.parseInt( output.get(2) );
            return exception == 0;
        }
    }

    //5.1. Commands for finding out information on NFU
    private String getDevInfo()
    {
        String message = "GETDEVINFO\tREQ\n";
        String response = sendNFUMessage( message );

        if( checkResponseStatus( response ) )
        {
            return response;
        }
        else
        {
            return "";
        }
    }
    private String getDevInfoEx()
    {
        String message = "GETDEVINFOEX\tREQ\n";
        String response = sendNFUMessage( message );

        if( checkResponseStatus( response ) )
        {
            return response;
        }
        else
        {
            return "";
        }
    }

    /*
        for device info -> index:
                0 - devType         [text; max. 30 chars]
                1 - countryID       [text; max. 2 chars]
                2 - swVersion       [text; max. 6 chars]
                3 - protocolVersion [text; max. 5 chars]
                4 - fiscalType      [<F;N>; F - fiscal, N - non-fiscal]
                5 - serialNum       [text; max. 30 chars]
                6 - codePage        [text; max. 30 chars]
     */
    public String getDeviceInfoByIndex( int index )
    {
        if ( deviceInfo.isEmpty() )
        {
            return ERROR_IN_COMMUNICATION;
        }
        else
        {
            return deviceInfo.get( index );
        }
    }
    public void printDeviceInfoInConsole(){
        String result = " printDeviceInfoInConsole()\n";

        result += "devType: " + getDeviceInfoByIndex(0) + "\n";
        result += "countryID: " + getDeviceInfoByIndex(1) + "\n";
        result += "swVersion: " + getDeviceInfoByIndex(2) + "\n";
        result += "protocolVersion: " + getDeviceInfoByIndex(3) + "\n";
        result += "fiscalType: " + getDeviceInfoByIndex(4) + "\n";
        result += "serialNum: " + getDeviceInfoByIndex(5) + "\n";
        result += "codePage: " + getDeviceInfoByIndex(6);

        Log.d( LOG_TAG, TAG_CLASS + result );
    }

    /*
        for device info (extended) -> index:
                0 - deviceName      [text; max. 30 chars]
                1 - manufacturer    [text; max. 30 chars]
                2 - nfuFwVer        [text; max. 30 chars]
                3 - hwVer           [text; max. 30 chars]
                4 - drvVer          [text; max. 30 chars]
                5 - llpVer          [text; max. 5 chars]
                6 - alpVer          [text; max. 5 chars]
                7 - serialNum       [text; max. 30 chars]
                8 - codePage        [text; max. 30 chars]
     */
    public String getDeviceInfoExByIndex( int index )
    {
        if ( deviceInfoEx.isEmpty() )
        {
            return ERROR_IN_COMMUNICATION;
        }
        else
        {
            return deviceInfoEx.get( index );
        }
    }
    public void printDeviceInfoExInConsole(){
        String result = " printDeviceInfoExInConsole()\n";

        result += "deviceName: " + getDeviceInfoExByIndex(0) + "\n";
        result += "manufacturer: " + getDeviceInfoExByIndex(1) + "\n";
        result += "nfuFwVer: " + getDeviceInfoExByIndex(2) + "\n";
        result += "hwVer: " + getDeviceInfoExByIndex(3) + "\n";
        result += "drvVer: " + getDeviceInfoExByIndex(4) + "\n";
        result += "llpVer: " + getDeviceInfoExByIndex(5) + "\n";
        result += "alpVer: " + getDeviceInfoExByIndex(6) + "\n";
        result += "serialNum: " + getDeviceInfoExByIndex(7) + "\n";
        result += "codePage: " + getDeviceInfoExByIndex(8);

        Log.d( LOG_TAG, TAG_CLASS + result );
    }

}
