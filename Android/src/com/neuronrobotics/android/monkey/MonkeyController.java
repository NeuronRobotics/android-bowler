package com.neuronrobotics.android.monkey;

import junit.framework.Test;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.SoundPool;

import com.neuronrobotics.android.R;
import com.neuronrobotics.sdk.common.BowlerMethod;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
import com.neuronrobotics.sdk.dyio.dypid.DyPIDConfiguration;
import com.neuronrobotics.sdk.dyio.peripherals.DigitalInputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.IDigitalInputListener;
import com.neuronrobotics.sdk.dyio.peripherals.ServoChannel;
import com.neuronrobotics.sdk.pid.IPIDEventListener;
import com.neuronrobotics.sdk.pid.PIDChannel;
import com.neuronrobotics.sdk.pid.PIDConfiguration;
import com.neuronrobotics.sdk.pid.PIDEvent;
import com.neuronrobotics.sdk.pid.PIDLimitEvent;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class MonkeyController extends Thread implements IPIDEventListener{
	private PIDChannel headPan;
	private PIDChannel headTilt;
	private PIDChannel armPan;
	private PIDChannel armExtend;
	
	private PIDChannel mouth;
	private ServoChannel tail;
	private PIDChannel hand;
//	private DigitalInputChannel handButton;
//	private DigitalInputChannel mouthButton;
	
	private boolean handOpen = true;
	private boolean mouthOpen = false;
	
	private int handOpenValue = 255;
	private int handClosedValue = 128;
	
	private int mouthOpenValue = 255;
	private int mouthClosedValue = 128;
	private DyIO dyio;
	private SoundPool soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
	private  int screech;
	
	private boolean running = true;
	
	public MonkeyController(DyIO d,Context c) {
		this.dyio = d;
		dyio.stopHeartBeat();
		dyio.setServoPowerSafeMode(false);
		screech = soundPool.load(c, R.raw.monkey_screech_short, 1);
	}

	public void disconect() {
		running=false;
		
	}
	

	public void run(){
		//head
		dyio.ConfigureDynamicPIDChannels(new DyPIDConfiguration(0, 12, DyIOChannelMode.ANALOG_IN,
				 													9, DyIOChannelMode.SERVO_OUT));
		
		dyio.ConfigureDynamicPIDChannels(new DyPIDConfiguration(1, 13, DyIOChannelMode.ANALOG_IN,
																	8, DyIOChannelMode.SERVO_OUT));
		
		dyio.ConfigureDynamicPIDChannels(new DyPIDConfiguration(2, 11, DyIOChannelMode.ANALOG_IN,
																	7, DyIOChannelMode.SERVO_OUT));
		
		//arm
		dyio.ConfigureDynamicPIDChannels(new DyPIDConfiguration(3, 15, DyIOChannelMode.ANALOG_IN,
																	6, DyIOChannelMode.SERVO_OUT));
		
		dyio.ConfigureDynamicPIDChannels(new DyPIDConfiguration(4, 14, DyIOChannelMode.ANALOG_IN,
				 													5, DyIOChannelMode.SERVO_OUT));
		
		dyio.ConfigureDynamicPIDChannels(new DyPIDConfiguration(5, 10, DyIOChannelMode.ANALOG_IN,
																	4, DyIOChannelMode.SERVO_OUT));

		
		
		System.out.println("DyIO configs sent");
		dyio.GetAllPIDPosition();
		System.out.println("All positions retrived");
		for(int i=0;i<6;i++){
			int numIter=0;
			boolean except = false;
			System.out.println("Setting up controller "+i);
			do{
				try{
					ThreadUtil.wait(10);
					dyio.getPIDChannel(i).ConfigurePIDController(new PIDConfiguration(	i,
																	true,
																	false,
																	false,
																	.2,
																	0,
																	0,
																	0,
																	false,
																	false));
					ThreadUtil.wait(10);
					dyio.getPIDChannel(i).SetPIDSetPoint(512, 0);
					
				}catch(Exception ex){
					ex.printStackTrace();
					except = true;
				}
				numIter++;
				
			}while(numIter<5 && except);
		}
		
		headPan = 	dyio.getPIDChannel(0);
		headTilt = 	dyio.getPIDChannel(1);
		mouth = 	dyio.getPIDChannel(2);
		PIDConfiguration configuration = mouth.getPIDConfiguration();
		configuration.setAsync(true);
		mouth.ConfigurePIDController(configuration);
		mouth.addPIDEventListener(this);
		
		armPan = 	dyio.getPIDChannel(3);
		armExtend = dyio.getPIDChannel(4);
		hand = 		dyio.getPIDChannel(5);
		
		tail  = new ServoChannel(dyio.getChannel(3));
		
		mouth.addPIDEventListener(this);
		
		
		soundPool.play(screech , 1.0f, 1.0f, 0, 0, 1.25f);
		ThreadUtil.wait(150);
		System.out.println("Monkey ready");
		while(running){
			ThreadUtil.wait(500);
			tail.SetPosition((int) (Math.random()*200)+30,.5);
		}
	}

	public void onPIDEvent(PIDEvent arg0) {
		System.out.println("PID event Set to "+arg0);
		boolean arg1 = arg0.getValue()<100;
//		if(arg0.getGroup() == hand.getGroup()){
//			if(arg1 != handOpen){
//				System.out.println("Hand Set to "+arg1);
//				handOpen = arg1;
//				if(handOpen){
//
//				}else{
//
//				}
//			}else{
//				
//			}
//		}
		if(arg0.getGroup() == mouth.getGroup()){
			System.out.println("Mouth event");
			if(arg1 != mouthOpen){
				System.out.println("Mouth Set to "+arg1);
				mouthOpen = arg1;
				if(!mouthOpen){
					soundPool.play(screech , 1.0f, 1.0f, 0, 0, 1.25f);
				}else{
					
				}
			}else{
				
			}
		}
	}

	public void onPIDLimitEvent(PIDLimitEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void onPIDReset(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

}
