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
import android.util.Log;
import android.widget.Toast;

import com.neuronrobotics.sdk.common.BowlerAbstractConnection;

public class AndroidBluetoothConnection extends BowlerAbstractConnection {
	private Activity activity;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice device;
	private BluetoothSocket mmSocket;
	
	
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int REQUEST_CONNECT_DEVICE = 1;
	
	
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private String TAG="Android Bluetooth Connection ";
	
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
	
	public void setDevice(BluetoothDevice d) {
		Log.e(TAG, "+++device added: "+d.getName()+" "+d.getAddress());
		device = mBluetoothAdapter.getRemoteDevice(d.getAddress());
	}
	
	private void askUserForDevice() {
		Log.d(TAG, "++Asking user for device ");
		Intent serverIntent = new Intent(activity, DeviceListActivity.class);
		activity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	}
	
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                setDevice(device);
            }
            break;
//        case REQUEST_ENABLE_BT:
//            // When the request to enable Bluetooth returns
//            if (resultCode == Activity.RESULT_OK) {
//                // Bluetooth is now enabled, so set up a chat session
//                setupChat();
//            } else {
//                // User did not enable Bluetooth or an error occured
//                Log.d(TAG, "BT not enabled");
//                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
//                finish();
//            }
        }
    }
	
	@Override
	public boolean connect() {
		enable();
        if(device == null) {
        	askUserForDevice();
        }
        try {
			mmSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
			mBluetoothAdapter.cancelDiscovery();
			mmSocket.connect();
			setDataIns(new DataInputStream(mmSocket.getInputStream()));
			setDataOuts(new DataOutputStream(mmSocket.getOutputStream()));
			setConnected(true);
		} catch (IOException e) {
			e.printStackTrace();
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
		if(mmSocket!= null) {
			try {
				mmSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	@Override
	public boolean waitingForConnection() {
		// TODO Auto-generated method stub
		return  isConnected();
	}

}
