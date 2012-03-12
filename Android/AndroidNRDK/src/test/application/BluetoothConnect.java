package test.application;

import java.io.IOException;
import java.util.Set;

import com.neuronrobotics.sdk.android.AndroidBluetoothConnection;
import com.neuronrobotics.sdk.android.DeviceListActivity;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.util.ThreadUtil;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class BluetoothConnect extends Activity implements Runnable {
	private String TAG="NRDK Bluetooth 2.2 ";
    // Layout Views
    private TextView mTitle;
    
    AndroidBluetoothConnection connection;
    DyIO dyio;
    Button start;
    Button stop;

	boolean ready = false;
    
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
       new Thread(this).start();
       
	   
       start = (Button) findViewById(R.id.button_start);
       start.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
        	   System.out.println("Start");
        	   setRunning(true);
           }
       });
       stop = (Button) findViewById(R.id.button_stop);
       stop.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
               System.out.println("Stop");
               setRunning(false);
           }
       });
       start.setEnabled(false);
       stop.setEnabled(false);
    }
	
	public void setRunning(boolean r) {
		start.setEnabled(!r);
		stop.setEnabled(r);
		if(r) {
	        new Thread() {
	        	public void run() {

			        Log.e(TAG, "Starting code");
					try{
				        DyIO.disableFWCheck();
				        dyio = new DyIO(connection);
				        dyio.connect();
				        System.out.println("Ping:"+dyio.ping());
			
						System.out.println("Starting tracker");
						new RealTimeLineTrackWithPID(dyio);
						System.out.println("Tracker started");
					}catch (Exception ex){
						ex.printStackTrace();
					}
	        	}
	        }.start();
		}else {
			try{
		        if(dyio != null) {
		        	dyio.killAllPidGroups();
		        	dyio.disconnect();
		        }
			}catch (Exception ex){
				ex.printStackTrace();
			}
		}
	}

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "++ ON START ++");
        setRunning(false);
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        Log.e(TAG, "+ ON RESUME +");
        setRunning(false);
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        Log.e(TAG, "- ON PAUSE -");
        onStop();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "-- ON STOP --");
        setRunning(false);
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e(TAG, "--- ON DESTROY ---");
        onStop();
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case AndroidBluetoothConnection.REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
            	Log.e(TAG, "Got device ");
                // Get the device MAC address
                String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
                // Attempt to connect to the device
                try {
					connection.setDevice(device);
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
            break;
        }
    }

	@Override
	public void run() {
	       Log.e(TAG, "searching for paired devices");
	       Set<BluetoothDevice> devs = connection.getPairedDevices();
	       for(BluetoothDevice d:devs) {
	    	   if(d.getName().toLowerCase().contains("firefly") || (d.getName().toLowerCase().contains("neuron") && d.getName().toLowerCase().contains("robotics"))) {
	    		   Log.e(TAG,"Testing device: "+d.getName()+" "+d.getAddress());
	    		   try {
		    		   connection.setDevice(d);
		    		   ready=true;
		    		   Log.e(TAG,"Using Device: "+d.getName()+" "+d.getAddress());
		    		   
		    		   return;
	    		   }catch(Exception ex){
	    			   ex.printStackTrace();
	    		   }	    		   
	    	   }
	       }
	       Log.e(TAG, "No paired devices found!");
	}
}
