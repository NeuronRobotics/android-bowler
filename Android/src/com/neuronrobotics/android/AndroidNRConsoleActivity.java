package com.neuronrobotics.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import com.neuronrobotics.android.monkey.MonkeyController;
import com.neuronrobotics.sdk.common.IConnectionEventListener;
import com.neuronrobotics.sdk.common.Tracer;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIOChannel;
import com.neuronrobotics.sdk.dyio.DyIOChannelEvent;
import com.neuronrobotics.sdk.dyio.DyIORegestry;
import com.neuronrobotics.sdk.dyio.IChannelEventListener;
import com.neuronrobotics.sdk.dyio.IDyIOEvent;
import com.neuronrobotics.sdk.dyio.IDyIOEventListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
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

public class AndroidNRConsoleActivity extends Activity implements IConnectionEventListener {
    /** Called when the activity is first created. */
	private NRAndroidBluetoothConnection connection;
	private String content="";
	private Button connect;
	//private Button test;
	private Button nrconsoleMainStart;
	//private Button hexapodStart;
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
	
	//private HexapodController hexapodMain;
	
	private MonkeyController monkey;
	
	
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	System.out.println("On Destroy");
    }
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    	System.out.println("Calling on stop");

    }
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	System.out.println("Calling on pause");

    }
	
    @Override
    protected void onResume() {
    	super.onResume();
		System.out.println("On Resume");

    }
    @Override
    protected void  onStart(){
    	super.onStart();
    	initializeGUI();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      System.out.println("onConfigurationChanged");
      initializeGUI();
    }
    
    private void initializeGUI() {
    	//This is the starting point for the application
    	if(DyIORegestry.get() == null) {
    		//THe DyIO is not connected
			System.out.println("DyIO is null");
			//Set buttons to starting state and ensure all variables are set to disconnected state
			disconnect();
			return;
		}
		if(DyIORegestry.get().isAvailable() == false) {
			System.out.println("Connection is not connected");
			//Set buttons to starting state and ensure all variables are set to disconnected state
	    	disconnect();
		}else {
			System.out.println("Setting connected view!");
			/**
			 * DyIO is already connected, make sure the GUI is in the connected state
			 */
			setRunningButtons(true); 
		}
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        content="";
        /**
         * set up all GUI variables. 
         */
        connect = (Button) findViewById(R.id.connect);
        disconnect = (Button) findViewById(R.id.disconnect);
        nrconsoleMainStart= (Button) findViewById(R.id.start);
        //hexapodStart= (Button) findViewById(R.id.hexapodStart);
        mTitle = (EditText) findViewById(R.id.display);
        mTitle.setKeyListener(null);
        switcher = (ViewFlipper) findViewById(R.id.startSwitch);
        //hexapodMain = new HexapodController(getActivity());
        
        /**
         * set default states for buttons
         */
        disconnect.setEnabled(false);
        nrconsoleMainStart.setEnabled(false);
        //hexapodStart.setEnabled(false);
        
        /**
         * Set a string to the display with the application name
         */
        addToDisplay("NR-Console");
        
        /**
         * Set up the bluetooth connection object
         */
        connection = new NRAndroidBluetoothConnection( getActivity());
        
        /**
         * Set up the event listener for the connection
         */
        connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	connect();
            }
        });
        /**
         * Set up the event listener for the disconnect
         */
        disconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	addToDisplay("Disconnecting...");
            	disconnect();
            }
        });
        
        /**
         * The tells the view flipper to switch to the nrconsole view
         */
        nrconsoleMainStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	monkeyStart();
            }
        });
        /**
         * The tells the view flipper to switch to the hexapod view
         */
//        hexapodStart.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//            	//THis comes from where the view is defined in the XML
//            	switcher.showNext();
//            	switcher.showNext();
//            	/**
//            	 * Start the hexapod code
//            	 */
//            	hexapodMain.start(getDyio());
//            	/**
//            	 * THis sets up the listener for the back button. it is the same back button for both flipper layouts
//            	 */
//            	backToConnections = (Button) getActivity().findViewById(R.id.backToConnectionsHex);
//            	backToConnections.setOnClickListener(new View.OnClickListener() {
//                     public void onClick(View v) {
//                    	//THis comes from where the view is defined in the XML
//                  	    switcher.showPrevious();
//                  	    switcher.showPrevious();
//                  	    
//                  	    hexapodMain.stop();
//                     }
//            	});
//            }
//        });
        
        
        
    }
    
    private void monkeyStart(){
    	System.out.println("NR-Console Start");
    	if(monkey != null)
    		monkey.disconect();
    	monkey = new MonkeyController(getDyio(),getApplicationContext());
    	monkey.start();
    	new Thread(new Runnable() {
  		  public void run() {
  			switcher.post(new Runnable() {
  				  public void run() {
  					  	switcher.showNext();//THis comes from where the view is defined in the XML
	  				  }
	  			  });
	  		  }
	  	}).start();
    	
    	/**
    	 * THis sets up the listener for the back button. it is the same back button for both flipper layouts
    	 */
    	backToConnections = (Button) findViewById(R.id.backToConnectionsNR);
    	backToConnections.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
          	    System.out.println("NR-Console");
          	    switcher.showPrevious();//THis comes from where the view is defined in the XML
              	if(monkey != null)
              		monkey.disconect();
             }
    	});
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
                disconnect();
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
    

    
    private void setRunningButtons(final boolean b){
    	System.out.println("setRunningButtons Called from: "+Tracer.calledFrom()); 
        new Thread(new Runnable() {
	  		  public void run() {
	  			connect.post(new Runnable() {
	  				  public void run() {
	  					connect.setEnabled(!b);
	  					nrconsoleMainStart.setEnabled(b);
	  					disconnect.setEnabled(b);
	  					//test.setEnabled(b);
	  					//hexapodStart.setEnabled(b);
	  				  }
	  			  });
	  		  }
	  	}).start();

    }
    
    private void setupDyIO(NRAndroidBluetoothConnection connection){
    	try{
    		DyIO.disableFWCheck();
	        DyIORegestry.setConnection(connection);
	        //getDyio().connect();
	        if(!getDyio().isAvailable()){
	        	setRunningButtons(false); 
	        	addToDisplay("Connection failed, device unavailible");
	        	disconnect();
	        	return;
	        }
	        addToDisplay("Running");
	        for(DyIOChannel c:getDyio().getChannels()){
	        	//c.addChannelEventListener(this);
	        }
	        setRunningButtons(true);
	        //getDyio().addDyIOEventListener(this);
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
    	monkeyStart();
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
    	System.out.println("Disconnection NRConsole");
    	if(getDyio() != null){
	        for(DyIOChannel c:getDyio().getChannels()){
	        	//c.removeChannelEventListener(this);
	        }
    		getDyio().disconnect();
    	}
    	addToDisplay("NR-Console Log");
    	setRunningButtons(false); 
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
		return DyIORegestry.get();
	}

}