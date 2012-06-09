package com.neuronrobotics.android;

import java.io.IOException;
import java.util.Set;

import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIOChannel;
import com.neuronrobotics.sdk.dyio.DyIOChannelEvent;
import com.neuronrobotics.sdk.dyio.IChannelEventListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.ViewFlipper;

public class AndroidNRConsoleActivity extends Activity implements IChannelEventListener {
    /** Called when the activity is first created. */
	AndroidNRConsoleActivity activity;
	DyIO dyio;
	NRAndroidBluetoothConnection connection;
	private String content="";
	Button connect;
	Button test;
	Button start;
	Button backToConnections;
	Button disconnect;
	EditText mTitle;
	ScrollView displayScroller;
	ViewFlipper switcher;
	Activity myActivity;
	ProgressDialog dialog;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myActivity=this;
        setContentView(R.layout.main);
        content="";
        
        connect = (Button) findViewById(R.id.connect);
        disconnect = (Button) findViewById(R.id.disconnect);
        test = (Button) findViewById(R.id.test);
        start= (Button) findViewById(R.id.start);
        
        mTitle = (EditText) findViewById(R.id.display);
        mTitle.setKeyListener(null);
        displayScroller = (ScrollView)findViewById(R.id.displayScroller);
        switcher = (ViewFlipper) findViewById(R.id.startSwitch);
        
        test.setEnabled(false);
        disconnect.setEnabled(false);
        start.setEnabled(false);
        addToDisplay("NR-Console");
        connection = new NRAndroidBluetoothConnection( activity);
        connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	connect();
            }
        });
        disconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	addToDisplay("Disconnecting...");
            	onDestroy();

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
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	System.out.println("NR-Console Start");
            	switcher.showNext();
            	backToConnections = (Button) findViewById(R.id.backToConnections);
            	backToConnections.setOnClickListener(new View.OnClickListener() {
	                 public void onClick(View v) {
	              	    System.out.println("NR-Console");
	              	    switcher.showPrevious();
	                 }
            	});
            }
        });
        
        
    }
    private void connect(){
    	if(dialog !=null){
    		dialog.dismiss();
    	}
    	dialog = ProgressDialog.show(myActivity, "", 
                "Connecting. Please wait...", true);
    	addToDisplay("Connecting...");
    	new Thread(){
    		public void run(){
                //Log.enableDebugPrint(true);
                
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
                dialog.dismiss();
    		}
    	}.start();
    }
    void setMainVIew(int view){
    	setContentView(R.layout.main);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
        activity=this;
		
    }
    
    private void setRunningButtons(final boolean b){
    	new Thread(new Runnable() {
	  		  public void run() {
	  			test.post(new Runnable() {
	  				  public void run() {
	  					test.setEnabled(b);
	  				  }
	  			  });
	  		  }
	  	}).start();

        new Thread(new Runnable() {
	  		  public void run() {
	  			start.post(new Runnable() {
	  				  public void run() {
	  					start.setEnabled(b);
	  				  }
	  			  });
	  		  }
	  	}).start();
        new Thread(new Runnable() {
	  		  public void run() {
	  			connect.post(new Runnable() {
	  				  public void run() {
	  					connect.setEnabled(!b);
	  				  }
	  			  });
	  		  }
	  	}).start();
        new Thread(new Runnable() {
	  		  public void run() {
	  			disconnect.post(new Runnable() {
	  				  public void run() {
	  					disconnect.setEnabled(b);
	  				  }
	  			  });
	  		  }
	  	}).start();
    }
    
    private void setupDyIO(NRAndroidBluetoothConnection connection){
    	try{
	        dyio = new DyIO(connection);
	        dyio.connect();
	        addToDisplay("Running");
	        for(DyIOChannel c:dyio.getChannels()){
	        	c.addChannelEventListener(this);
	        }
	        setRunningButtons(true);
    	}catch(Exception ex){
        	if(dialog !=null){
        		dialog.dismiss();
        	}
    		setRunningButtons(false);	
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setMessage("Error connecting")
    		       .setCancelable(false)
    		       .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		                myActivity.finish();
    		           }
    		       })
    		       .setNegativeButton("Retry", new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface d, int id) {
    		                d.cancel();
    		                connect();
    		           }
    		       });
    		AlertDialog alert = builder.create();
    		alert.show();
    	}
    }
    private void addToDisplay(String s){
    	//System.out.println(s);
    	content+=s+"\n";
    	//This is some hacky shit to get around the single threading issue
    	new Thread(new Runnable() {
    		  public void run() {
    			  mTitle.post(new Runnable() {
    				  public void run() {
    					  mTitle.setText(content);
    					  mTitle.setSelection(mTitle.getText().length()-1);
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
    	setRunningButtons(false);  
    	dyio.disconnect();
    	content="";
    	addToDisplay("NR-Console");
    }
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    	System.out.println("Calling on stop");
    	//onDestroy();
    }
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	System.out.println("Calling on pause");
    }


	@Override
	public void onChannelEvent(DyIOChannelEvent p) {
		addToDisplay("Ch="+p.getChannel().getChannelNumber()+" "+p.getChannel().getMode()+" value="+p.getValue());
	}

}