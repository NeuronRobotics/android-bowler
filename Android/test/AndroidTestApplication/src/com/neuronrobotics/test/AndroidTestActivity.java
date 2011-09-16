package com.neuronrobotics.test;

import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.serial.SerialConnection;

import android.app.Activity;
import android.os.Bundle;

public class AndroidTestActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        System.out.println("#@#@Starting application!!");
        DyIO dyio = new DyIO(new SerialConnection("/dev/ttyACM0"));
        dyio.connect();
        dyio.ping();
    }
}