package test.application;

import java.util.Set;

import com.neuronrobotics.sdk.android.AndroidBluetoothConnection;
import com.neuronrobotics.sdk.android.DeviceListActivity;
import com.neuronrobotics.sdk.android.Tester;
import com.neuronrobotics.sdk.dyio.DyIO;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
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
       Log.e(TAG, "searching for paired devices");
       Set<BluetoothDevice> devs = connection.getPairedDevices();
       for(BluetoothDevice d:devs) {
    	   if(d.getName().contains("FireFly")) {
//    		   connection.setDevice(d);
//    		   return;
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

    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        Log.e(TAG, "+ ON RESUME +");
        try {
        	Tester.runTest(dyio);
        }catch(Exception e) {
        	e.printStackTrace();
        }
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
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case AndroidBluetoothConnection.REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
                // Attempt to connect to the device
                connection.setDevice(device);
            }
            break;
//        case REQUEST_ENABLE_BT:
//            // When the request to enable Bluetooth returns
//            if (resultCode == Activity.RESULT_OK) {
//                // Bluetooth is now enabled, so set up a chat session
//                setupChat();
//            } else {
//                // User did not enable Bluetooth or an error occured
//                Log.d(TAG, "BT not enabled");
//                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
//                finish();
//            }
        }
    }
}
