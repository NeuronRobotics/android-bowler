package android.hardware.usb.cdc;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.neuronrobotics.sdk.common.ByteList;

import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbRequest;

//import com.neuronrobotics.sdk.common.ByteList;

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
			byte[] sendData=new byte[ep.getMaxPacketSize()];
			int back =cdc.getUsbConnection().bulkTransfer(	ep,
															sendData, 
															sendData.length, 
															cdc.getTimeOutTime());
			if(back>0){

				synchronized(inputData){
					for(int i=0;i<back;i++){
						inputData.add(sendData[i]);
					}
				}
			}else if(back<0){
				System.out.println("Receive endpoint failed with code="+back);
				cdc.reconnect();
				try {Thread.sleep(300);} catch (InterruptedException e) {}
			}

			try {Thread.sleep(cdc.getPollTime());} catch (InterruptedException e) {}
		}
		System.out.println("Input stream clean exit");
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
