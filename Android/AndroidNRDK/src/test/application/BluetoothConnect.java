package test.application;

import java.util.Set;

import com.neuronrobotics.sdk.android.AndroidBluetoothConnection;
import com.neuronrobotics.sdk.android.Tester;
import com.neuronrobotics.sdk.dyio.DyIO;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

public class BluetoothConnect extends Activity {
	private String TAG="NRDK Bluetooth 2.2 ";
    // Layout Views
    private TextView mTitle;
    
    AndroidBluetoothConnection connection;
    DyIO dyio;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "+++ ON CREATE +++");
        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);

       connection=new AndroidBluetoothConnection(this);
       Set<BluetoothDevice> devs = connection.getPairedDevices();
       for(BluetoothDevice d:devs) {
    	   if(d.getName().contains("FireFly")) {
    		   connection.setDevice(d);
    		   return;
    	   }
       }
       Log.e(TAG, "No paired devices found!");
       //finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "++ ON START ++");
        if(dyio == null)
        	dyio = new DyIO(connection);
        else
        	dyio.disconnect();
        try {
        	Tester.runTest(dyio);
        }catch(Exception e) {
        	e.printStackTrace();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        Log.e(TAG, "+ ON RESUME +");

    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        Log.e(TAG, "- ON PAUSE -");
        
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "-- ON STOP --");
        if(dyio != null)
        	dyio.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e(TAG, "--- ON DESTROY ---");
    }
}
