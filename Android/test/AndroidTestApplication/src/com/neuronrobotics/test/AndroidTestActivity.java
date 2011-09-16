package com.neuronrobotics.test;


import gnu.io.RXTXCommDriver;
import android.app.Activity;
import android.os.Bundle;

public class AndroidTestActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        System.out.println("#@#@Starting application!!");
        try {
        	System.out.println("Native version = "+RXTXCommDriver.nativeGetVersionWrapper());
        }catch(Error e) {
        	e.printStackTrace();
        }
    }
}