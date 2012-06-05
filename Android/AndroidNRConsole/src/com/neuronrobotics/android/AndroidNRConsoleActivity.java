package com.neuronrobotics.android;

import com.neuronrobotics.sdk.dyio.DyIO;

import android.app.Activity;
import android.os.Bundle;

public class AndroidNRConsoleActivity extends Activity {
    /** Called when the activity is first created. */
	AndroidNRConsoleActivity activity;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        activity=this;
        new Thread(){
        	public void run(){
		        NRAndroidBluetoothConnection connection = new NRAndroidBluetoothConnection( activity);
		        DyIO.disableFWCheck();
		        DyIO dyio = new DyIO(connection);
		        dyio.connect();
		        System.out.println("Ping:"+dyio.ping());
		        dyio.disconnect();
        	}
        }.start();
    }
}