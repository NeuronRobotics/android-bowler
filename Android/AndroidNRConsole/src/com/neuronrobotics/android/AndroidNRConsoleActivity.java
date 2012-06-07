package com.neuronrobotics.android;

import java.io.IOException;
import java.util.Set;

import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;

import android.R;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

public class AndroidNRConsoleActivity extends Activity {
    /** Called when the activity is first created. */
	AndroidNRConsoleActivity activity;
	DyIO dyio;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    
    @Override
    protected void onResume() {
    	super.onResume();
        activity=this;
        Log.enableDebugPrint(true);
        NRAndroidBluetoothConnection connection = new NRAndroidBluetoothConnection( activity);
        Set<BluetoothDevice>  devices = connection.getPairedDevices();
        BluetoothDevice myDev = null;
        for(BluetoothDevice d : devices){
        	System.out.println("Device found: "+d.getName());
        	if(d.getName().contains("DyIO"))
        		myDev = d;
        }
        if(myDev==null){
        	System.out.println("No device found, return");
        	return;
        }
        try {
			connection.setDevice(myDev);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
        
        dyio = new DyIO(connection);
        dyio.connect();
        System.out.println("Running");
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	System.err.println("Closing Bluetooth");
    	dyio.disconnect();
    }
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    	System.out.println("Calling on stop");
    	onDestroy();
    }
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	System.out.println("Calling on pause");
    	onDestroy();
    }
}