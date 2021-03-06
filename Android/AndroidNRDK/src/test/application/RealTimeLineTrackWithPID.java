package test.application;

import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
import com.neuronrobotics.sdk.dyio.dypid.DyPIDConfiguration;
import com.neuronrobotics.sdk.pid.IPIDEventListener;
import com.neuronrobotics.sdk.pid.PIDConfiguration;
import com.neuronrobotics.sdk.pid.PIDEvent;
import com.neuronrobotics.sdk.pid.PIDLimitEvent;
import com.neuronrobotics.sdk.ui.ConnectionDialog;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class RealTimeLineTrackWithPID implements IPIDEventListener {
	
	int lVal=0;
	int rVal=0;
	public RealTimeLineTrackWithPID(DyIO dyio){

		
		
		/**
		 * This configuration uses 2 line sensors and 2 continuous turn servos in the standard "Puck Bot" configuration
		 * 
		 * The purpose of this demonstration is to show how to use the PID controller for more then just motor control.
		 * Since it is a generic controller it can link any input to any output, so in this example i took the input 
		 * from the line sensor and attached it to the output of the drive motor. The PID keeps the robot on the line.
		 * I also added a listener to the system that can be used to detect a "Double Black" condition, which is used for 
		 * stopping. This could be encapsulated as a set of behaviors that can be called up as needed at runtime, and
		 * the closed-loop control stays on the DyIO, while the High level command and decisions take place in Java. 
		 */
		DyPIDConfiguration dypidR = new DyPIDConfiguration(	1,//PID group 1
													
															12,//Input channel number
															DyIOChannelMode.ANALOG_IN,//Input mode
															11,//Output Channel
															DyIOChannelMode.SERVO_OUT);//Output mode
		PIDConfiguration pidR =new PIDConfiguration (		1,//PID group
															true,//enabled
															false,//inverted
															true,//Async
															.15,// Kp
															0,// Ki
															0,//Kd
															//Latch values are only used with the Counter since analog is absolute and can not change its value
															0,//Value to load to the controller if the index pin is used. This value can be anything
															false,//Use the auto-load of a latched in value when using the index pin
															false);//Set the setpoint to the current location when index it reached

		DyPIDConfiguration dypidL = new DyPIDConfiguration(	0,//PID group 0
															13,//Input channel number
															DyIOChannelMode.ANALOG_IN,//Input mode
															10,//Output Channel
															DyIOChannelMode.SERVO_OUT);//Output mode
		PIDConfiguration pidL =new PIDConfiguration (		0,//PID group
															true,//enabled
															true,//inverted
															true,//Async
															.15,// Kp
															0,// Ki
															0,//Kd
															//Latch values are only used with the Counter since analog is absolute and can not change its value
															0,//Value to load to the controller if the index pin is used. This value can be anything
															false,//Use the auto-load of a latched in value when using the index pin
															false);//Set the setpoint to the current location when index it reached
		
		//Setup the controller with the configurations
		dyio.ConfigureDynamicPIDChannels(dypidR);ThreadUtil.wait(10);
		
		dyio.ConfigureDynamicPIDChannels(dypidL);ThreadUtil.wait(10);
		dyio.ConfigurePIDController(pidR);ThreadUtil.wait(10);
		dyio.ConfigurePIDController(pidL);ThreadUtil.wait(10);
		
		//Set a single setpoint to the controler
		dyio.SetPIDSetPoint(	0,//Group 0
								512,//Tell the controller to go to position 500
								0);//Take 0 secoinds to get there
		ThreadUtil.wait(10);
		dyio.SetPIDSetPoint(	1,//Group 1
								512,//Tell the controller to go to position 500
								0);//Take 0 secoinds to get there
		ThreadUtil.wait(10);
		System.out.println("Line Track Started");
		dyio.addPIDEventListener(this);
	}


	@Override
	public void onPIDEvent(PIDEvent e) {
		if(e.getGroup()==0){
			lVal = e.getValue();
		}
		if(e.getGroup()==1){
			rVal = e.getValue();
		}
		if(lVal >500 && rVal>500){
			System.out.println("Stop Condition!");
		}
	}

	@Override
	public void onPIDLimitEvent(PIDLimitEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPIDReset(int group, int currentValue) {
		// TODO Auto-generated method stub
		
	}

}
