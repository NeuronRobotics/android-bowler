package com.neuronrobotics.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import com.neuronrobotics.sdk.common.IConnectionEventListener;
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

public class AndroidNRConsoleActivity extends Activity implements IChannelEventListener, IDyIOEventListener, IConnectionEventListener {
    /** Called when the activity is first created. */
	private DyIO dyio;
	private NRAndroidBluetoothConnection connection;
	private String content="";
	private Button connect;
	//private Button test;
	private Button nrconsoleMainStart;
	private Button hexapodStart;
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
	
	private HexapodController hexapodMain;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        content="";
        
        connect = (Button) findViewById(R.id.connect);
        disconnect = (Button) findViewById(R.id.disconnect);
        //test = (Button) findViewById(R.id.test);
        nrconsoleMainStart= (Button) findViewById(R.id.start);
        hexapodStart= (Button) findViewById(R.id.hexapodStart);
        
        mTitle = (EditText) findViewById(R.id.display);
        mTitle.setKeyListener(null);
        switcher = (ViewFlipper) findViewById(R.id.startSwitch);
        
        //test.setEnabled(false);
        disconnect.setEnabled(false);
        nrconsoleMainStart.setEnabled(false);
        hexapodStart.setEnabled(false);
        
        
        addToDisplay("NR-Console");
        connection = new NRAndroidBluetoothConnection( getActivity());
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
        
//        test.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//         	    System.out.println("Testing");
//	       		double avg=0;
//	       		int i;
//	       		avg=0;
//	       		long start = System.currentTimeMillis();
//	       		for(i=0;i<10;i++) {
//	       			getDyio().ping();
//	       			double ms=System.currentTimeMillis()-start;
//	       			avg +=ms;
//	       			start = System.currentTimeMillis();
//	       			//System.out.println("Average cycle time: "+(int)(avg/i)+"ms\t\t\t this loop was: "+ms);
//	       		}
//	       		addToDisplay("Average cycle time for ping: "+(avg/i)+" ms");
//            }
//        });
        nrconsoleMainStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	System.out.println("NR-Console Start");
            	switcher.showNext();
            	backToConnections = (Button) findViewById(R.id.backToConnectionsNR);
            	backToConnections.setOnClickListener(new View.OnClickListener() {
	                 public void onClick(View v) {
	              	    System.out.println("NR-Console");
	              	    switcher.showPrevious();
	                 }
            	});
            }
        });
        
        hexapodStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	switcher.showNext();
            	switcher.showNext();
            	hexapodMain.start(getDyio());
            	backToConnections = (Button) getActivity().findViewById(R.id.backToConnectionsHex);
            	backToConnections.setOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
                  	    switcher.showPrevious();
                  	    switcher.showPrevious();
                  	    hexapodMain.stop();
                     }
            	});
            }
        });
        
        hexapodMain = new HexapodController(getActivity());
        
    }
    private void connect(){
    	if(dialog !=null){
    		dialog.dismiss();
    	}
    	dialog = ProgressDialog.show(getActivity(), "", 
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
                	
                	builder = new AlertDialog.Builder(getActivity());
                	builder.setTitle("Select Device to Pair");
                	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 	    		           public void onClick(DialogInterface d, int id) {
 	    		        	   	pairDialog.dismiss();
	  	    		           }
	  	    		});
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
			      	          	  	        getActivity().startActivity(intent);
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
		
    }
    
    private void setRunningButtons(final boolean b){
        new Thread(new Runnable() {
	  		  public void run() {
	  			connect.post(new Runnable() {
	  				  public void run() {
	  					connect.setEnabled(!b);
	  					nrconsoleMainStart.setEnabled(b);
	  					disconnect.setEnabled(b);
	  					//test.setEnabled(b);
	  					hexapodStart.setEnabled(b);
	  				  }
	  			  });
	  		  }
	  	}).start();

    }
    
    private void setupDyIO(NRAndroidBluetoothConnection connection){
    	try{
	        setDyio(new DyIO(connection));
	        getDyio().connect();
	        if(!getDyio().isAvailable()){
	        	setRunningButtons(false); 
	        	disconnect();
	        	return;
	        }
	        addToDisplay("Running");
	        for(DyIOChannel c:getDyio().getChannels()){
	        	c.addChannelEventListener(this);
	        }
	        setRunningButtons(true);
	        getDyio().addDyIOEventListener(this);
	        getDyio().addConnectionEventListener(this);
	        if(dialog !=null){
        		dialog.dismiss();
	        }
    	}catch(Exception ex){
    		ex.printStackTrace();
        	if(dialog !=null){
        		dialog.dismiss();
        	}
        	disconnect();
    		setRunningButtons(false);	
    		new Thread(new Runnable() {
	  	  		  public void run() {
	  	  			connect.post(new Runnable() {
	  	  				  public void run() {
	  	  		    		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		  	  	    		builder.setMessage("Error connecting")
		  	  	    		       .setCancelable(false)
		  	  	    		       .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
		  	  	    		           public void onClick(DialogInterface dialog, int id) {
		  	  	    		                getActivity().finish();
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
	  	  			  });
	  	  		  }
	  	  	}).start();
    		
    	}
    }
    private void addToDisplay(String s){
    	//System.out.println(s);
    	content+=s+"\n";
    	//This is some hacky shit to get around the single threading issue
    	final String str = new String(content.getBytes());
    	new Thread(new Runnable() {
    		  public void run() {
    			  mTitle.post(new Runnable() {
    				  public void run() {
    					  mTitle.setText(str);
    					  mTitle.setSelection(mTitle.getText().length()-1);
    				  }
    			  });
    		  }
    	}).start();
    	
    }
    
    private void disconnect(){
    	if(getDyio() != null){
	        for(DyIOChannel c:getDyio().getChannels()){
	        	c.removeChannelEventListener(this);
	        }
    		getDyio().disconnect();
    	}
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	System.err.println("Closing Bluetooth");
    	setRunningButtons(false); 
    	disconnect();
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


	public void onChannelEvent(final DyIOChannelEvent p) {
		new Thread(){
			public void run(){
				addToDisplay("Ch="+p.getChannel().getChannelNumber()+" "+p.getChannel().getMode()+" value="+p.getValue());
			}
		}.start();
		
	}
	public void onDyIOEvent(final IDyIOEvent arg0) {
		new Thread(){
			public void run(){
				addToDisplay("Ev="+arg0);
			}
		}.start();
		
	}
	public AndroidNRConsoleActivity getActivity() {
		return this;
	}
	public void onConnect() {
		// TODO Auto-generated method stub
		
	}
	public void onDisconnect() {
		System.err.println("On disconnect called");
    	setRunningButtons(false); 
    	disconnect();
	}
	public DyIO getDyio() {
		return dyio;
	}
	public void setDyio(DyIO dyio) {
		this.dyio = dyio;
	}

}