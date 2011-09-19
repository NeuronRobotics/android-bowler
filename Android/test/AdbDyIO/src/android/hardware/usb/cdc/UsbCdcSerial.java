package android.hardware.usb.cdc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import com.neuronrobotics.sdk.common.ByteList;
import com.neuronrobotics.sdk.util.ThreadUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;

public class UsbCdcSerial {
	private static final String TAG = "UsbCdcSerial: ";
    private UsbManager mUsbManager;
    private UsbDevice mDevice;
    private UsbDeviceConnection mConnection;
    private UsbEndpoint mEndpointIntr;
    
    private UsbCdcInputStream in;
    private UsbCdcOutputStream out;
	private boolean connected;
	
	private Activity activity;
	
	
	public UsbCdcSerial(Activity a) {
		activity = a;
		
		mUsbManager = (UsbManager)activity.getSystemService(Context.USB_SERVICE);
	}

	public boolean connect() {
		if(isConnected())
			disconnect();
        Intent intent = activity.getIntent();
        System.out.println(TAG+ "intent: " + intent);
        String action = intent.getAction();

        UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
        	setDevice(device);
        	setConnected(true); 
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
        	setConnected(false); 
        }

		return isConnected();	
	}
	
    private void setDevice(UsbDevice device) {
    	
    	if(device==null) {
    		System.out.println(TAG+ " #@#Bad device");
    		return;
    	}
        System.out.println(TAG+"Number of interfaces="+device.getInterfaceCount());
        UsbInterface intf = device.getInterface(0);
        // device should have one endpoint
        if (intf.getEndpointCount() != 1) {
            System.out.println(TAG+" could not find endpoint");
            return;
        }
        System.out.println(TAG+"Number of endPoints="+intf.getEndpointCount());
        // endpoint should be of type interrupt
        UsbEndpoint ep = intf.getEndpoint(0);
        if (ep.getType() != UsbConstants.USB_ENDPOINT_XFER_INT) {
            System.out.println(TAG+ " endpoint is not interrupt type="+ep.getType()+" should be="+UsbConstants.USB_ENDPOINT_XFER_INT);
            return;
        }
        mDevice = device;
        mEndpointIntr = ep;
        if (device != null) {
            UsbDeviceConnection connection = mUsbManager.openDevice(device);
            if (connection != null && connection.claimInterface(intf, true)) {
                System.out.println(TAG+ "open SUCCESS");
                mConnection = connection;
                in = new UsbCdcInputStream(this,mEndpointIntr);
                in.start();
                out = new UsbCdcOutputStream(this,mEndpointIntr);
                out.start();
            } else {
                System.out.println(TAG+ "open FAIL");
                mConnection = null;
            }
         }
    }
    
	public void disconnect() {
		System.out.println(TAG+"Disconecting");
		mConnection.close();
		setConnected(false); 
	}
	
	public InputStream getInputStream(){
		return in.getStream();
	}
	
	public OutputStream getOutputStream(){
		return out.getStream();
	}
	
	public boolean isConnected(){
		return connected;
	}
	
	
	private void setConnected(boolean connected) {
		if(this.connected == connected)
			return;
		this.connected = connected;
	}

	public UsbDeviceConnection getUsbConnection() {
		return mConnection;
	}
   
}
