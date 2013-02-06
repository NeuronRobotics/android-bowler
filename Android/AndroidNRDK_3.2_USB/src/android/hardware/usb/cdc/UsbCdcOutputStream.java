package android.hardware.usb.cdc;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.neuronrobotics.sdk.common.ByteList;

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
		//System.out.println("Starting sending thread:");
		while(cdc.isConnected()){
			try {				
				if( outputData.size()>0){
					//System.out.println("Got data to send");
					byte[] sendData;
					synchronized(outputData){
						if(outputData.size()<=ep.getMaxPacketSize())
							sendData=outputData.popList(outputData.size());
						else
							sendData=outputData.popList(ep.getMaxPacketSize());
					}
					int back=0;
					do{
						back =cdc.getUsbConnection().bulkTransfer(	ep,
																	sendData, 
																	sendData.length, 
																		cdc.getTimeOutTime());
						try {Thread.sleep(cdc.getPollTime());} catch (InterruptedException e) {}
						if(back<0) {
							System.out.println("Transmit failed");
							cdc.reconnect();
							try {Thread.sleep(300);} catch (InterruptedException e) {}
						}
					}while(cdc.isConnected() && back<0 );
						
					//}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {Thread.sleep(cdc.getPollTime());} catch (InterruptedException e) {}
		}
		System.out.println("Output stream clean exit");
	}
	public DataOutputStream getStream(){
		return new DataOutputStream(outs);
	}
}
