package com.neuronrobotics.android;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.Tracer;

public class AndroidBluetoothConnection extends BowlerAbstractConnection {
	private Activity activity;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice device;
	private BluetoothSocket mmSocket;
	
	
	private static final int REQUEST_ENABLE_BT = 2;
	public static final int REQUEST_CONNECT_DEVICE = 1;
	
	
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private String TAG="Android Bluetooth Connection ";
	
	public AndroidBluetoothConnection(Activity a) {
		activity = a;
		setSleepTime(2000);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
        	activity.finish();
            return;
        }
        enable();
        
//        Intent discoverableIntent = new
//        Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 30000);
//        activity.startActivity(discoverableIntent);
        
	}
	
	private boolean enabled = false;
	
	private void enable() {
		if(enabled)
			return;
		if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
		enabled=true;
	}
	
	public Set<BluetoothDevice> getPairedDevices(){
		enable();
		return mBluetoothAdapter.getBondedDevices();
	}
	
	public void setDevice(BluetoothDevice d) throws IOException {
		Log.e(TAG, "+++device added: "+d.getName()+" "+d.getAddress());
		System.out.println("setDevice Called from: "+Tracer.calledFrom()); 
		device = mBluetoothAdapter.getRemoteDevice(d.getAddress());
		try {
			mBluetoothAdapter.cancelDiscovery();
		} catch (Exception e) {
			
		}
		try {
			setMmSocket(device.createRfcommSocketToServiceRecord(MY_UUID));
			getMmSocket().connect();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			setMmSocket(null);
			throw e;
		}
		
	}
	
//	private void askUserForDevice() throws IOException {
//		Log.e(TAG, "++Asking user for device ");
//		Intent serverIntent = new Intent(activity, DeviceListActivity.class);
//		activity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
//		Log.e(TAG, "Waiting for connection... ");
//		while(device==null) {
//			try {Thread.sleep(100);} catch (InterruptedException e) {}
//			if(serverIntent.getExtras() != null) {
//				String address = serverIntent.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
//				System.out.println(address);
//				Log.e(TAG, "Got device "+address);
//				BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
//                // Attempt to connect to the device
//                setDevice(device);
//			}
//		}
//	}
	
	@Override
	public boolean connect() {
		System.out.println("connect Called from: "+Tracer.calledFrom()); 
		enable();
        if(device == null) {
//        	try {
//				askUserForDevice();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
        }
        try {
        	Log.e(TAG, "Connecting Socket ");
        	if(getMmSocket() == null) {
        		Log.e(TAG, "--Socket is null!!");
        		setDevice(device);
        	}
			//mmSocket.connect();
			setDataIns(new DataInputStream(getMmSocket().getInputStream()));
			setDataOuts(new DataOutputStream(getMmSocket().getOutputStream()));
			setConnected(true);
			Log.e(TAG, "++++Success!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		return isConnected();
	}

	@Override
	public boolean reconnect() throws IOException {
		System.out.println("reconnect Called from: "+Tracer.calledFrom()); 
		disconnect();
		connect();
		return  isConnected();
	}
	@Override
	public void disconnect() {
		System.out.println("disconnect Called from: "+Tracer.calledFrom()); 
		if(getMmSocket()!= null) {
			try {
				getMmSocket().close();
				setMmSocket(null);
				setConnected(false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
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

}
