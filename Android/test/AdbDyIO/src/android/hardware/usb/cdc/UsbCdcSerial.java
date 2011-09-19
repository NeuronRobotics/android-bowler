package android.hardware.usb.cdc;

import java.io.InputStream;
import java.io.OutputStream;
import android.app.Activity;
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
    private UsbManager cdcManager;
    private UsbDeviceConnection cdcDeviceConnection;
    private UsbEndpoint cdcControlEndpoint;
    
    private UsbCdcInputStream in;
    private UsbCdcOutputStream out;
	private boolean connected;
	
	private Activity activity;
	
	
	public UsbCdcSerial(Activity a) {
		activity = a;
		
		cdcManager = (UsbManager)activity.getSystemService(Context.USB_SERVICE);
	}

	public boolean connect() {
		if(isConnected())
			disconnect();
        Intent intent = activity.getIntent();
//        System.out.println(TAG+ "intent: " + intent);
//        String action = intent.getAction();

        //UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        setDevice((UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE));
//        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
//        	//setDevice(device);
//        	setConnected(true); 
//        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
//        	setConnected(false); 
//        }

		return isConnected();	
	}
	
    private void setDevice(UsbDevice device) {
    	
    	if(device==null) {
    		System.out.println(TAG+ " #@#Bad device");
    		disconnect();
    		return;
    	}
        System.out.println(TAG+"Number of interfaces="+device.getInterfaceCount());
        UsbInterface intf = device.getInterface(0);
        // device should have one endpoint
        if (intf.getEndpointCount() != 1) {
            System.out.println(TAG+" could not find endpoint");
            disconnect();
            return;
        }
        System.out.println(TAG+"Number of endPoints="+intf.getEndpointCount());
        // endpoint should be of type interrupt
        cdcControlEndpoint = intf.getEndpoint(0);
        if (cdcControlEndpoint.getType() != UsbConstants.USB_ENDPOINT_XFER_INT) {
            System.out.println(TAG+ " endpoint is not interrupt type="+cdcControlEndpoint.getType()+" should be="+UsbConstants.USB_ENDPOINT_XFER_INT);
            disconnect();
            return;
        }
        //mDevice = device;
        
        if (device != null) {
        	cdcDeviceConnection = cdcManager.openDevice(device);
        	UsbRequest request = new UsbRequest();
            request.initialize(cdcDeviceConnection,cdcControlEndpoint);
            
            if (cdcDeviceConnection != null && cdcDeviceConnection.claimInterface(intf, true)) {
            	UsbInterface data = device.getInterface(1);
            	System.out.println(TAG+"Number of Data end points="+data.getEndpointCount());
            	UsbEndpoint inEp = data.getEndpoint(1);
            	UsbEndpoint outEp = data.getEndpoint(0);
            	
                in = new UsbCdcInputStream(this,inEp);
                in.start();
                out = new UsbCdcOutputStream(this,outEp);
                out.start();
                System.out.println(TAG+ "open SUCCESS");
            } else {
                System.out.println(TAG+ "open FAIL");
                cdcDeviceConnection = null;
            }
         }
        setConnected(true); 
    }
    
	public void disconnect() {
		System.out.println(TAG+"Disconecting");
		cdcDeviceConnection.close();
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
		return cdcDeviceConnection;
	}
   
}
