package com.neuronrobotics.sdk.android;

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

import com.neuronrobotics.sdk.common.BowlerAbstractConnection;

public class AndroidBluetoothConnection extends BowlerAbstractConnection {
	private Activity activity;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice device;
	private BluetoothSocket mmSocket;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	
	public AndroidBluetoothConnection(Activity a) {
		activity = a;
		setSleepTime(5000);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
        	activity.finish();
            return;
        }

        
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
	
	public void setDevice(BluetoothDevice d) {
		device = d;
	}
	@Override
	public boolean connect() {
		enable();
        if(device == null)
        	throw new RuntimeException("Device is not set!");
        try {
			mmSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
			mBluetoothAdapter.cancelDiscovery();
			mmSocket.connect();
			setDataIns(new DataInputStream(mmSocket.getInputStream()));
			setDataOuts(new DataOutputStream(mmSocket.getOutputStream()));
			setConnected(true);
		} catch (IOException e) {
		}
		// TODO Auto-generated method stub
		return isConnected();
	}

	@Override
	public boolean reconnect() throws IOException {
		// TODO Auto-generated method stub
		return  isConnected();
	}
	@Override
	public void disconnect() {
		if(mmSocket!= null)
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
	}
	@Override
	public boolean waitingForConnection() {
		// TODO Auto-generated method stub
		return  isConnected();
	}

}
