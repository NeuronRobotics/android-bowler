package android.hardware.usb.cdc;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.hardware.usb.UsbEndpoint;
//import com.neuronrobotics.sdk.common.ByteList;
//import com.neuronrobotics.sdk.util.ThreadUtil;

public class UsbCdcOutputStream extends Thread {
	private ByteList outputData = new ByteList();
	private OutputStream outs = new OutputStream() {
		public void write(byte [] raw){
			synchronized(outputData){
				//System.out.println("Got data array to send");
				outputData.add(raw);
			}
		}
		public void flush(){
			while( outputData.size()>0);
		}
		@Override
		public void write(int arg) throws IOException {
			synchronized(outputData){
				//System.out.println("Got data byte to send");
				outputData.add((byte)arg);
			}
		}
	};
	private UsbCdcSerial cdc;
	UsbEndpoint ep;
	public UsbCdcOutputStream(UsbCdcSerial usbCdcSerial,UsbEndpoint mEndpointIntr) {
		cdc=usbCdcSerial;
		ep=mEndpointIntr;
	}
	public void run(){
		System.out.println("Starting sending thread:");
		while(cdc.isConnected()){
			try {Thread.sleep(1);} catch (InterruptedException e) {}
			try {				
				if( outputData.size()>0){
					//System.out.println("Got data to send");
					byte[] sendData;
					synchronized(outputData){
						sendData=outputData.popList(outputData.size());
					}
					//synchronized (cdc.getUsbConnection()) {
						//System.out.println("Attempting to send: "+sendData.length+" bytes");
						int back =cdc.getUsbConnection().bulkTransfer(	ep,
																		sendData, 
																		sendData.length, 
																		10);
						if(back<0) {
							System.out.println("#$#$#$Data failed to send:"+sendData);    
						}
					//}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public DataOutputStream getStream(){
		return new DataOutputStream(outs);
	}
}
