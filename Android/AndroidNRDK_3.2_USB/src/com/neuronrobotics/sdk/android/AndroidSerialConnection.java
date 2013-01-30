package com.neuronrobotics.sdk.android;



import java.io.DataInputStream;
import java.io.DataOutputStream;

import android.app.Activity;
import android.hardware.usb.cdc.UsbCdcSerial;

import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.common.MissingNativeLibraryException;

public class AndroidSerialConnection extends BowlerAbstractConnection{
	
	private UsbCdcSerial serial;
	private Activity activity;
	
	public AndroidSerialConnection(Activity a) {
		activity = a;
		setSleepTime(3000);
	}
	
	/* (non-Javadoc)
	 * @see com.neuronrobotics.sdk.common.BowlerAbstractConnection#connect()
	 */
	@Override
	public boolean connect() {
		if(isConnected()) {
			System.out.println("Already connected");
			return true;
		}
		
		try 
		{
			serial = new UsbCdcSerial(activity);
			if(serial.connect()){
				setDataIns(new DataInputStream(serial.getInputStream()));
				setDataOuts(new DataOutputStream(serial.getOutputStream()));
				setConnected(true);
			}
		}catch(UnsatisfiedLinkError e){
			throw new MissingNativeLibraryException(e.getMessage());
        }catch (Exception e) {
			e.printStackTrace();
			setConnected(false);
		}
		return isConnected();	
	}

	

	/* (non-Javadoc)
	 * @see com.neuronrobotics.sdk.common.BowlerAbstractConnection#disconnect()
	 */
	@Override
	public void disconnect() {
		if(isConnected())
			Log.info("Disconnecting Serial Connection");
		try{
			super.disconnect();
			try{
				serial.disconnect();
			}catch(Exception e){
				//e.printStackTrace();
				//throw new RuntimeException(e);
			}
			serial = null;
			setConnected(false);
		} catch(UnsatisfiedLinkError e) {
			throw new MissingNativeLibraryException(e.getMessage());
        }
	}

	/* (non-Javadoc)
	 * @see com.neuronrobotics.sdk.common.BowlerAbstractConnection#reconnect()
	 */
	@Override
	public boolean reconnect() {
		if(!isConnected())
			return false;
		else
			return true;
	}

	/* (non-Javadoc)
	 * @see com.neuronrobotics.sdk.common.BowlerAbstractConnection#waitingForConnection()
	 */
	@Override
	public boolean waitingForConnection() {
		// TODO Auto-generated method stub
		return false;
	}
}
