package android.hardware.usb.cdc;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbEndpoint;

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
	public UsbCdcOutputStream(UsbCdcSerial usbCdcSerial,UsbEndpoint mEndpointIntr) {
		// TODO Auto-generated constructor stub
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
					synchronized (this) {
						if(cdc.getUsbConnection().controlTransfer(UsbConstants.USB_DIR_OUT, 0x9, 0x200, 0, sendData, sendData.length, 0)<0) {
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
