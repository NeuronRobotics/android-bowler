package com.neuronrobotics.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIOChannel;
import com.neuronrobotics.sdk.dyio.DyIOChannelEvent;
import com.neuronrobotics.sdk.dyio.IChannelEventListener;
import com.neuronrobotics.sdk.dyio.IDyIOEvent;
import com.neuronrobotics.sdk.dyio.IDyIOEventListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class AndroidNRConsoleActivity extends Activity implements IChannelEventListener, IDyIOEventListener {
    /** Called when the activity is first created. */
	private AndroidNRConsoleActivity activity;
	private DyIO dyio;
	private NRAndroidBluetoothConnection connection;
	private String content="";
	private Button connect;
	private Button test;
	private Button start;
	private Button backToConnections;
	private Button disconnect;
	private EditText mTitle;
	private ViewFlipper switcher;
	private ProgressDialog dialog;
	private ArrayList<BluetoothDevice> unpaired;
	private String [] unpairedStrings;
	private BluetoothDevice myDev;
	private AlertDialog pairDialog;
	private AlertDialog.Builder builder ;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity=this;
        setContentView(R.layout.main);
        content="";
        
        connect = (Button) findViewById(R.id.connect);
        disconnect = (Button) findViewById(R.id.disconnect);
        test = (Button) findViewById(R.id.test);
        start= (Button) findViewById(R.id.start);
        
        mTitle = (EditText) findViewById(R.id.display);
        mTitle.setKeyListener(null);
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
    	dialog = ProgressDialog.show(activity, "", 
                "Connecting. Please wait...", true);
    	addToDisplay("Connecting...");
    	new Thread(){
    		public void run(){
                //Log.enableDebugPrint(true);
                
                Set<BluetoothDevice>  devices = connection.getPairedDevices();
                myDev = null;
                for(BluetoothDevice d : devices){
                	addToDisplay("\tPaired Device found: "+d.getName());
                	if(d.getName().contains("DyIO")){
                		myDev = d;
                		addToDisplay("Using device "+d.getName());
                		break;
                	}
                }
                if(myDev==null){
                	addToDisplay("No paired device found, checking for visible devices...");
                	unpaired = connection.getVisibleDevices();
                	addToDisplay("Devices Visible: ");
                	for(int i=0;i<unpaired.size();i++){
                		addToDisplay("\t"+unpaired.get(i).getName());
                	}
                	dialog.dismiss();
                	
                	builder = new AlertDialog.Builder(activity);
                	builder.setTitle("Select Device to Pair");
                	unpairedStrings = new String[unpaired.size()];
                	for(int i=0;i<unpaired.size();i++){
                		unpairedStrings[i]=unpaired.get(i).getName();
                	}
                	builder.setItems(unpairedStrings, new DialogInterface.OnClickListener() {
                	    public void onClick(DialogInterface dialog, int item) {
                	        Toast.makeText(getApplicationContext(), unpairedStrings[item], Toast.LENGTH_SHORT).show();
                	        myDev = unpaired.get(item);
                	        
                	        pairDialog.dismiss();
                	        new Thread(new Runnable() {
	      	          	  		  public void run() {
	      	          	  			connect.post(new Runnable() {
	      	          	  				  public void run() {
	      	          	  					String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
			      	          	  	        Intent intent = new Intent(ACTION_PAIRING_REQUEST);
			      	          	  	        String EXTRA_DEVICE = "android.bluetooth.device.extra.DEVICE";
			      	          	  	        intent.putExtra(EXTRA_DEVICE, myDev);
			      	          	  	        String EXTRA_PAIRING_VARIANT = "android.bluetooth.device.extra.PAIRING_VARIANT";
			      	          	  	        int PAIRING_VARIANT_PIN = 0;
			      	          	  	        intent.putExtra(EXTRA_PAIRING_VARIANT, PAIRING_VARIANT_PIN);
			      	          	  	        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			      	          	  	        activity.startActivity(intent);
	      	          	  					try {
	      	          	  						  connection.setDevice(myDev);
	      	          	  					} catch (IOException e) {
	      	          	  						  e.printStackTrace();
	      	          	  						  pairDialog.dismiss();
				      	                  		  return;
	      	          	  					}
	      	          	  					  	  setupDyIO( connection);
				      	                          pairDialog.dismiss();
	      	          	  				  	}
	      	          	  			  });
	      	          	  		  }
	      	          	  	}).start();
                	    }
                	});
                	new Thread(new Runnable() {
	          	  		  public void run() {
	          	  			connect.post(new Runnable() {
	          	  				  public void run() {
	          	  					pairDialog = builder.create();
	          	                	pairDialog.show();
	          	  				  }
	          	  			  });
	          	  		  }
	          	  	}).start();
                	
                	return;
                }
                try {
        			connection.setDevice(myDev);
        		} catch (IOException e) {
        			e.printStackTrace();
        			dialog.dismiss();
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
	        dyio.addDyIOEventListener(this);
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
    		                activity.finish();
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
    	addToDisplay("NR-Console Log");
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
		addToDisplay("Ch="+p.getChannel().getChannelNumber()+" "+p.getChannel().getMode()+" value="+p.getValue());
	}
	@Override
	public void onDyIOEvent(IDyIOEvent arg0) {
		// TODO Auto-generated method stub
		addToDisplay("Ev="+arg0);
	}

}