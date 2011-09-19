package android.hardware.usb.cdc;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbRequest;

import com.neuronrobotics.sdk.common.ByteList;

public class UsbCdcInputStream extends Thread {
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
	private UsbCdcSerial cdc;
	UsbEndpoint ep;
	public UsbCdcInputStream(UsbCdcSerial usbCdcSerial,UsbEndpoint mEndpointIntr) {
		cdc=usbCdcSerial;
		ep=mEndpointIntr;
	}
	public void run(){

		while(cdc.isConnected()){
			try {Thread.sleep(50);} catch (InterruptedException e) {}
//			UsbRequest request = new UsbRequest();
//	        request.initialize(cdc.getUsbConnection(),ep);
//	        ByteBuffer buffer = ByteBuffer.allocate(1);
//			
//			request.queue(buffer, 1);
//            if (cdc.getUsbConnection().requestWait() == request) {
//                add(buffer.get(0));
//                try {Thread.sleep(100);} catch (InterruptedException e) {}
//            } else {
//                System.out.println("requestWait failed, exiting");
//                cdc.disconnect();
//            }
//            
			synchronized (cdc.getUsbConnection()) {
				byte[] sendData=new byte[64];
				int back =cdc.getUsbConnection().bulkTransfer(	ep,
																sendData, 
																sendData.length, 
																100);
				if(back>0){
					synchronized(inputData){
						for(int i=0;i<back;i++){
							inputData.add(sendData[i]);
						}
					}
				}
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
