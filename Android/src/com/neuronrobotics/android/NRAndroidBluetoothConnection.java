package com.neuronrobotics.android;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.Tracer;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class NRAndroidBluetoothConnection extends BowlerAbstractConnection {
	private Activity activity;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice device;
	private BluetoothSocket mmSocket;
	private ArrayList<BluetoothDevice> unpaired= new ArrayList<BluetoothDevice> ();
	
	private static final int REQUEST_ENABLE_BT = 2;
	public static final int REQUEST_CONNECT_DEVICE = 1;
	
	
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private String TAG="Android Bluetooth Connection ";
	
	public NRAndroidBluetoothConnection(Activity a) {
		activity = a;
		setSleepTime(2000);
        // Get local Bluetooth adapter
        setBluetoothAdapter(BluetoothAdapter.getDefaultAdapter());

        // If the adapter is null, then Bluetooth is not supported
        if (getBluetoothAdapter() == null) {
        	System.out.println("No bluetooth hardware availible");
        	activity.finish();
            return;
        }
        enable();
        
	}
	
	@Override 
	public boolean isConnected() {
		if( super.isConnected() == false) {
			System.out.println("Connection is already disconnected");
		}else {
			try {
				if(!mBluetoothAdapter.isEnabled()) {
					System.err.println("Adapter disconected");
					setConnected(false);
				}
				if(device.getBondState()!= BluetoothDevice.BOND_BONDED) {
					System.err.println("Device not bonded");
					setConnected(false);
				}
				if(mmSocket.getRemoteDevice() != device) {
					System.err.println("Socket not connected to device");
					setConnected(false);
				}
			}catch (Exception ex) {
				System.err.println("Connection exception");
				ex.printStackTrace();
				setConnected(false);
			}
		}
		
		
		return super.isConnected();
	}
	
	private boolean enabled = false;
	
	private void enable() {
		if(enabled)
			return;
		if (!getBluetoothAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
		enabled=true;
	}
	
	public Set<BluetoothDevice> getPairedDevices(){
		enable();
		return getBluetoothAdapter().getBondedDevices();
	}
	public ArrayList<BluetoothDevice> getVisibleDevices(){
		final Set<BluetoothDevice> paired = getPairedDevices();
		final BroadcastReceiver mReceiver = new BroadcastReceiver() 
        { 
			@Override
            public void onReceive(Context context, Intent intent) 
            {
                String action = intent.getAction(); 
                // When discovery finds a device 
                if (BluetoothDevice.ACTION_FOUND.equals(action)) 
                {
	                // Get the BluetoothDevice object from the Intent 
	                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	                System.err.println("BlueTooth Testing "+device.getName() + "\n"+ device.getAddress()); 
	                boolean isPaired = false;
	                for(BluetoothDevice d:paired){
	                	if(d.getAddress() == device.getAddress()){
	                		isPaired =true;
	                	}
	                }
	                if(!isPaired){
	                	if(!unpaired.contains(device)){
	                		unpaired.add(device);
	                		System.out.println("Adding "+device.getName());
	                	}
	                }
                }
            } 
        };

        //String aDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
        //activity.startActivityForResult(new Intent(aDiscoverable),DISCOVERY_REQUEST);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND); 
        activity.registerReceiver(mReceiver, filter); 
        getBluetoothAdapter().startDiscovery();
        long start = System.currentTimeMillis();
        boolean waiting = true;
        while(waiting){
        	ThreadUtil.wait(100);
        	if((System.currentTimeMillis()-start)>5000){
        		waiting = false;
        	}
        	for(int i=0;i<unpaired.size();i++){
        		BluetoothDevice d = unpaired.get(i);
        		if(d!= null){
	        		if(d.getName().contains("NR") || d.getName().contains("FireFly") ||d.getName().contains("linvor")||d.getName().contains("DyIO")   ){
	        			waiting=false;
	        		}
        		}
        	}
        }
		
		return unpaired;
	}
	
	public void setDevice(BluetoothDevice d) throws IOException {
		if(d == device)
			return;
		Log.e(TAG, "+++device added: "+d.getName()+" "+d.getAddress());
		System.out.println("setDevice Called from: "+Tracer.calledFrom()); 
		device = getBluetoothAdapter().getRemoteDevice(d.getAddress());
		try {
			getBluetoothAdapter().cancelDiscovery();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			setMmSocket(device.createRfcommSocketToServiceRecord(MY_UUID));
			System.out.println("Set up device");
			return;
		} catch (IOException e) {
			e.printStackTrace();
			setMmSocket(null);
			throw e;
		}
		
	}

	@Override
	
	public void setConnected(boolean b) {
		System.out.println("setConnected Called from: "+Tracer.calledFrom()); 
		System.out.println("setConnected set to: "+b); 
		super.setConnected(b);
	}
	
	@Override
	public boolean connect() {
		System.out.println("connect Called from: "+Tracer.calledFrom()); 
		enable();
        if(device == null) {
        	throw new RuntimeException("Device not selected and set before connecting, bailing out");
        }
        try {
        	Log.e(TAG, "Connecting Socket ");
        	if(getMmSocket() == null) {
        		Log.e(TAG, "--Socket is null!!");
        		setDevice(device);
        	}
        	getBluetoothAdapter().cancelDiscovery();
        	getMmSocket().connect();
			setDataIns(new DataInputStream(getMmSocket().getInputStream()));
			setDataOuts(new DataOutputStream(getMmSocket().getOutputStream()));
			setConnected(true);
			Log.e(TAG, "++++Success!");
		} catch (IOException e) {
			e.printStackTrace();
			disconnect();
		}
		// TODO Auto-generated method stub
		return isConnected();
	}

	@Override
	public boolean reconnect() throws IOException {
		System.out.println("reconnect Called from: "+Tracer.calledFrom()); 
		disconnect();
		BluetoothDevice d = device;
		device=null;
		setDevice(d);
		connect();
		return  isConnected();
	}
	@Override
	public void disconnect() {
		System.out.println("disconnect Called from: "+Tracer.calledFrom()); 
		if(getMmSocket()!= null) {
			try {
				getBluetoothAdapter().cancelDiscovery();
				if(getDataIns() != null) {
					getDataIns().close();
				}
				if(getDataOuts() != null) {
					getDataOuts().close();
				}
				setDataIns(null);
				setDataOuts(null);
				
				getMmSocket().close();
				setMmSocket(null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		setConnected(false);
	}
	@Override
	public boolean waitingForConnection() {
		// TODO Auto-generated method stub
		return  false;
	}

	public void setMmSocket(BluetoothSocket mmSocket) {
		
		System.out.println("setMmSocket Called from: "+Tracer.calledFrom()); 
		System.out.println("setMmSocket is now = "+mmSocket ); 
		this.mmSocket = mmSocket;
	}

	public BluetoothSocket getMmSocket() {
		return mmSocket;
	}

	public BluetoothAdapter getBluetoothAdapter() {
		return mBluetoothAdapter;
	}

	public void setBluetoothAdapter(BluetoothAdapter mBluetoothAdapter) {
		this.mBluetoothAdapter = mBluetoothAdapter;
	}

}
