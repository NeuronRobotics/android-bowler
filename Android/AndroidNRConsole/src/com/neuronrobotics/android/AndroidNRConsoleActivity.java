package com.neuronrobotics.android;

import java.io.IOException;
import java.util.Set;

import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIOChannel;
import com.neuronrobotics.sdk.dyio.DyIOChannelEvent;
import com.neuronrobotics.sdk.dyio.IChannelEventListener;
import com.neuronrobotics.sdk.dyio.IDyIOEvent;
import com.neuronrobotics.sdk.dyio.IDyIOEventListener;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AndroidNRConsoleActivity extends Activity implements IChannelEventListener {
    /** Called when the activity is first created. */
	AndroidNRConsoleActivity activity;
	DyIO dyio;
	private String content="";
	Button connect;
	Button test;
	Button start;
	EditText mTitle;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        content="";
        addToDisplay("NR-Console");
        connect = (Button) findViewById(R.id.connect);
        test = (Button) findViewById(R.id.test);
        start= (Button) findViewById(R.id.start);
        mTitle = (EditText) findViewById(R.id.display);
        mTitle.setKeyListener(null);
        
        test.setEnabled(false);
        start.setEnabled(false);
        
        connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	addToDisplay("Connecting...");
                //Log.enableDebugPrint(true);
                NRAndroidBluetoothConnection connection = new NRAndroidBluetoothConnection( activity);
                Set<BluetoothDevice>  devices = connection.getPairedDevices();
                BluetoothDevice myDev = null;
                for(BluetoothDevice d : devices){
                	addToDisplay("\tPaired Device found: "+d.getName());
                	if(d.getName().contains("DyIO")){
                		myDev = d;
                		addToDisplay("Using device "+d.getName());
                		break;
                	}
                }
                if(myDev==null){
                	addToDisplay("No device found, return");
                	return;
                }
                try {
        			connection.setDevice(myDev);
        		} catch (IOException e) {
        			e.printStackTrace();
        			return;
        		}
                setupDyIO( connection);

            }
        });
        
        test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
         	    System.out.println("Testing");
	       		double avg=0;
	       		int i;
	       		avg=0;
	       		long start = System.currentTimeMillis();
	       		for(i=0;i<10;i++) {
	       			dyio.ping();
	       			double ms=System.currentTimeMillis()-start;
	       			avg +=ms;
	       			start = System.currentTimeMillis();
	       			//System.out.println("Average cycle time: "+(int)(avg/i)+"ms\t\t\t this loop was: "+ms);
	       		}
	       		addToDisplay("Average cycle time for ping: "+(avg/i)+" ms");
            }
        });
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
        activity=this;
		
    }
    private void setupDyIO(NRAndroidBluetoothConnection connection){
        dyio = new DyIO(connection);
        dyio.connect();
        addToDisplay("Running");
        for(DyIOChannel c:dyio.getChannels()){
        	c.addChannelEventListener(this);
        }
        test.setEnabled(true);
        start.setEnabled(true);
        connect.setEnabled(false);
    }
    private void addToDisplay(String s){
    	System.out.println(s);
    	content=s+"\n"+content;
    	//This is some hacky shit to get around the single threading issue
    	new Thread(new Runnable() {
    		  public void run() {
    			  mTitle.post(new Runnable() {
    				  public void run() {
    					  mTitle.setText(content);
    				  }
    			  });
    		  }
    	}).start();
    	
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	System.err.println("Closing Bluetooth");
    	content="";
    	addToDisplay("NR-Console");

        connect.setEnabled(true);
        test.setEnabled(false);
        start.setEnabled(false);
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


	@Override
	public void onChannelEvent(DyIOChannelEvent p) {
		addToDisplay("Async: ch="+p.getChannel().getChannelNumber()+" mode="+p.getChannel().getMode()+" value="+p.getValue());
	}
}