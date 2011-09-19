package android.hardware.usb.cdc;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbRequest;

import com.neuronrobotics.sdk.common.ByteList;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class UsbCdcOutputStream extends Thread {
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
	private UsbCdcSerial cdc;
	UsbEndpoint ep;
	public UsbCdcOutputStream(UsbCdcSerial usbCdcSerial,UsbEndpoint mEndpointIntr) {
		cdc=usbCdcSerial;
		ep=mEndpointIntr;
	}
	public void run(){
		while(cdc.isConnected()){
			ThreadUtil.wait(1);
			try {				
				if( outputData.size()>0){
					byte[] sendData;
					synchronized(outputData){
						sendData=outputData.popList(outputData.size());
					}
					synchronized (cdc.getUsbConnection()) {
						System.out.println("Attempting to send: "+sendData.length+" bytes");
						int back =cdc.getUsbConnection().bulkTransfer(	ep,
																		sendData, 
																		sendData.length, 
																		10);
						if(back<0) {
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
