package com.neuronrobotics.android;

import com.neuronrobotics.sdk.dyio.DyIO;

import android.R;
import android.app.Activity;
import android.os.Bundle;

public class AndroidNRConsoleActivity extends Activity {
    /** Called when the activity is first created. */
	AndroidNRConsoleActivity activity;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity=this;
        NRAndroidBluetoothConnection connection = new NRAndroidBluetoothConnection( activity);
        DyIO dyio = new DyIO(connection);
        dyio.connect();
        System.out.println("Ping:"+dyio.ping());
        dyio.disconnect();
        System.out.println("Running");

    }
}