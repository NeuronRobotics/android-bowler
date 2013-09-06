package com.neuronrobotics.android;

import android.app.Activity;
import android.util.Log;

import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.peripherals.ServoChannel;

public class DyIOCommThread extends Thread {
	private DyIO dyio;
	private Activity activity;
	private int leftSpeed=125;
	public void setLeftSpeed(int leftSpeed) {
		this.leftSpeed = leftSpeed;
	}

	public void setRightSpeed(int rightSpeed) {
		this.rightSpeed = rightSpeed;
	}

	private int rightSpeed=125;
	private ServoChannel left;
	private ServoChannel right;
	
	
	public DyIOCommThread(DyIO dyio2, Activity A) {
		// TODO Auto-generated constructor stub
		this.dyio=dyio2;
		this.activity=A;
		left = new ServoChannel (dyio.getChannel(11));
		right = new ServoChannel (dyio.getChannel(10));
		left.SetPosition(leftSpeed);
		right.SetPosition(rightSpeed);
	}

	public void run(){
		while(true){
		if (dyio.isAvailable()){
			try {
				Thread.sleep(100);
				left.SetPosition(leftSpeed);
				right.SetPosition(rightSpeed);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Log.i("DyIOCommThread","Whoops, time to go..");
				e.printStackTrace();
				return;
			}
		} else {
			Log.e("DyIOCommThread","DyIO Not Available!!");
		}
	}
	}
}
