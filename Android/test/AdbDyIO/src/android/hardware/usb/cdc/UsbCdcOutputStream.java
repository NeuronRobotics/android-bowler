package android.hardware.usb.cdc;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

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
		UsbRequest request = new UsbRequest();
        request.initialize(cdc.getUsbConnection(), ep);
		while(cdc.isConnected()){
			ThreadUtil.wait(1);
			try {				
				if( outputData.size()>0){
					int size;
					ByteBuffer buffer;
					synchronized(outputData){
						size = outputData.size();
						buffer = ByteBuffer.allocate(outputData.size());
						buffer.put(outputData.popList(outputData.size()));
						//sendData=new ByteBuffer(outputData.popList(outputData.size()));
					}
					synchronized (this) {
//						int back =cdc.getUsbConnection().controlTransfer(	UsbConstants.USB_DIR_OUT, 
//																						 0x9, 
//																						 0x200, 
//																						 0, 
//																						 sendData, 
//																						 sendData.length, 
//																						 0);
						boolean back=request.queue(buffer, size);
						if(back) {
							System.out.println("#$#$#$Data failed to send:"+buffer);    
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
