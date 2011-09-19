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
        System.out.println(TAG+"Number of endPoints="+intf.getEndPointCount());
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
                in = new UsbCdcInputStream();
                in.start();
                out = new UsbCdcOutputStream();
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
	
	private class UsbCdcInputStream extends Thread{
		private ByteList inputData = new ByteList();
		private InputStream ins = new InputStream() {
			public int available(){
				if(inputData.size()>0) {
					return inputData.size();
				}
				return 0;
			}
			public final int read(byte[] b, int off,int len)throws IOException{
				////Log.info("Reading "+len+" bytes from UDP: "+inputData.size());
				int i=0;
				byte[] get;
				synchronized(inputData){
					get = inputData.popList(off,len);
				}
				for(i=0;i<len;i++) {
					if(i==b.length) {
						throw new IOException("Buffer too small to hold data");
					}
					b[i]=get[i];
				}
				////Log.info("Read: "+i+" Bytes, "+inputData.size()+" left");
				return i;
			}
			@Override
			public final int read( byte[] rawBuffer) throws IOException {
				synchronized(inputData){
					return read(rawBuffer,0,inputData.size());
				}
			}

			@Override
			public int read() throws IOException {
				synchronized(inputData){
					if(inputData.size()>0)
						return inputData.pop();
				}
				throw new IOException("Reading from empty buffer!");
			}
		};
		public void run(){
			UsbRequest request = new UsbRequest();
	        request.initialize(mConnection, mEndpointIntr);
	        ByteBuffer buffer = ByteBuffer.allocate(1);
			while(isConnected()){
				try {Thread.sleep(1);} catch (InterruptedException e) {}
				request.queue(buffer, 1);
	            if (mConnection.requestWait() == request) {
	                add(buffer.get(0));
	                try {Thread.sleep(100);} catch (InterruptedException e) {}
	            } else {
	                System.out.println(TAG+ "requestWait failed, exiting");
	                disconnect();
	            }
			}
		}
		private void add(byte b){
			synchronized(inputData){
				System.out.println("Data from USB:"+b);
				inputData.add(b);
			}
		}
		public DataInputStream getStream(){
			return new DataInputStream(ins);
		}
	}
	
	private class UsbCdcOutputStream extends Thread{
		private ByteList outputData = new ByteList();
		private OutputStream outs = new OutputStream() {
			public void write(byte [] raw){
				synchronized(outputData){
					outputData.add(raw);
				}
			}
			public void flush(){
				while( outputData.size()>0);
			}
			@Override
			public void write(int arg) throws IOException {
				synchronized(outputData){
					outputData.add((byte)arg);
				}
			}
		};
		public void run(){
			while(isConnected()){
				ThreadUtil.wait(1);
				try {				
					if( outputData.size()>0){
						byte[] sendData;
						synchronized(outputData){
							sendData=outputData.popList(outputData.size());
						}
						synchronized (this) {
							if(mConnection.controlTransfer(UsbConstants.USB_DIR_OUT, 0x9, 0x200, 0, sendData, sendData.length, 0)<0) {
								System.out.println("#$#$#$Data failed to send:"+sendData);    
							}
						}
					}
				} catch (Exception e) {}
			}
		}
		public DataOutputStream getStream(){
			return new DataOutputStream(outs);
		}
	}
   
}
