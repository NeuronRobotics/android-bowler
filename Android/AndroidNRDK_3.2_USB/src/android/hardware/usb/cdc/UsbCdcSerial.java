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
    private UsbEndpoint inEp;
    private UsbEndpoint outEp; 
	private boolean connected;
	
	private Activity activity;
	
	protected final  int  pollTime =1;
	
	
	public UsbCdcSerial(Activity a) {
		activity = a;
		
		cdcManager = (UsbManager)activity.getSystemService(Context.USB_SERVICE);
	}

	public boolean connect() {
		if(isConnected())
			disconnect();
		
        UsbDevice device = (UsbDevice)activity.getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
        
        setDevice(device);

		return isConnected();	
	}
	
    private void setDevice(UsbDevice device) {
    	
    	if(device==null) {
    		System.out.println(TAG+ " Device not found");
    		disconnect();
    		return;
    	}
    	if(device.getVendorId()==0x0403){
    		System.out.println("Setting up FTDI device");
    		
    		
    	}else{
    		System.out.println("Setting up generic CDC device");
    		
    	}
        //System.out.println(TAG+"Number of interfaces="+device.getInterfaceCount());
        UsbInterface intf = device.getInterface(0);
        // device should have one endpoint
        if (	(intf.getEndpointCount() != 1)||
        		(intf.getInterfaceClass()!=UsbConstants.USB_CLASS_COMM) ||
        		(intf.getInterfaceSubclass()!=2)||//THis is abstract modem
        		(intf.getInterfaceProtocol()!=1)//AT-commands (v.25ter)
        		) {
            System.out.println(TAG+" Control interface has wrong endpoint, class, subclass or AT set");
            disconnect();
            return;
        }
        
       // System.out.println(TAG+"Number of endPoints="+intf.getEndpointCount());
        // endpoint should be of type interrupt
        cdcControlEndpoint = intf.getEndpoint(0);
        if (cdcControlEndpoint.getType() != UsbConstants.USB_ENDPOINT_XFER_INT) {
            System.out.println(TAG+ " endpoint is not interrupt type="+cdcControlEndpoint.getType()+" should be="+UsbConstants.USB_ENDPOINT_XFER_INT);
            disconnect();
            return;
        }
        //mDevice = device;
        UsbInterface data = device.getInterface(1);
        if (	(data .getEndpointCount() != 2)||
        		(data .getInterfaceClass()!=UsbConstants.USB_CLASS_CDC_DATA) 
        		) {
            System.out.println(TAG+" Data interface has wrong endpoint or class");
            disconnect();
            return;
        }
        
        if (device != null) {
        	cdcDeviceConnection = cdcManager.openDevice(device);
        	UsbRequest request = new UsbRequest();
            request.initialize(cdcDeviceConnection,cdcControlEndpoint);
            
            if (	cdcDeviceConnection != null && 
            		cdcDeviceConnection.claimInterface(intf, true)&& 
            		cdcDeviceConnection.claimInterface(data, true)) {
            	
            	//System.out.println(TAG+"Number of Data end points="+data.getEndpointCount());
            	UsbEndpoint ep1 = data.getEndpoint(1);
            	UsbEndpoint ep0 = data.getEndpoint(0);
            	
            	if(ep0.getDirection()==UsbConstants.USB_DIR_IN&&ep1.getDirection()==UsbConstants.USB_DIR_OUT){
            		inEp=ep0;
            		outEp=ep1;
            	}else if(ep1.getDirection()==UsbConstants.USB_DIR_IN&&ep0.getDirection()==UsbConstants.USB_DIR_OUT){
            		inEp=ep1;
            		outEp=ep0;
            	}else{
            		System.out.println(TAG+" Data directions not correct");
                    disconnect();
                    return;
            	}
            	request = new UsbRequest();
                request.initialize(cdcDeviceConnection,inEp);
                request = new UsbRequest();
                request.initialize(cdcDeviceConnection,outEp);
            	setConnected(true); 
                in = new UsbCdcInputStream(this,inEp);
                in.start();
                out = new UsbCdcOutputStream(this,outEp);
                out.start();
                System.out.println(TAG+ "open SUCCESS"); 
            } else {
                System.out.println(TAG+ "failed to calim interface");
                cdcDeviceConnection = null;
            }
         }
        
    }
    
	public void disconnect() {
		setConnected(false);
		try {
			Thread.sleep(getTimeOutTime()*3);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(TAG+"Disconecting");
		if(cdcDeviceConnection!=null)
			cdcDeviceConnection.close();
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
	private int reconnectCount =0;
	private boolean reconnecting=false;
	public void reconnect() {
		if(!isConnected()){
			System.out.println("Disconnected, not reconnecting");
			return;
		}
		if(reconnecting)
			return;
		reconnecting=true;
		System.out.println("Reconnecting");
		reconnectCount++;
		disconnect();
		if(reconnectCount>10){
			return;
		}
		
		new Thread(){
			@Override
			public void run(){
				try {Thread.sleep(200);} catch (InterruptedException e) {}
				connect();
				reconnecting=false;
			}
		}.start();
	}

	public int getTimeOutTime() {
		return 500;
	}

	public long getPollTime() {
		return pollTime;
	}
   
}
