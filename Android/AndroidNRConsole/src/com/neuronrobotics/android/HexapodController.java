package com.neuronrobotics.android;

import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.util.ThreadUtil;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

public class HexapodController {
	
	private final Activity activity;
	private DyIO dyio;
	
	private Button backToConnections;
	
	private Button forward;
	private Button backward;
	private Button left;
	private Button right;
	
	private Button reset;
	
	private TextView walkStatus;
	private TextView voltage;
	private WalkingState walkState = WalkingState.STOPPED;
	
	walkingThread myThread;

	public HexapodController(Activity activity) {
		this.activity = activity;
		forward = (Button) getActivity().findViewById(R.id.walk_forward);
		backward= (Button) getActivity().findViewById(R.id.walk_backwards);
		left 	= (Button) getActivity().findViewById(R.id.turn_left);
		right 	= (Button) getActivity().findViewById(R.id.turn_right);
		reset 	= (Button) getActivity().findViewById(R.id.reset_hex);
		
		forward.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		            setWalkState(WalkingState.FORWARD);
		            forward.setSelected(true);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	setWalkState(WalkingState.STOPPED);
		        	 forward.setSelected(false);
		        }
				return true;
			}
		});
		backward.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		            setWalkState(WalkingState.BACKWARD);
		            backward.setSelected(true);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	setWalkState(WalkingState.STOPPED);
		        	backward.setSelected(false);
		        }
				return true;
			}
		});
		left.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		            setWalkState(WalkingState.TURN_LEFT);
		            left.setSelected(true);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	setWalkState(WalkingState.STOPPED);
		        	 left.setSelected(false);
		        }
				return true;
			}
		});
		right.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		            setWalkState(WalkingState.TURN_RIGHT);
		            right.setSelected(true);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	setWalkState(WalkingState.STOPPED);
		        	right.setSelected(false);
		        }
				return true;
			}
		});
		
		walkStatus = (TextView)getActivity().findViewById(R.id.hex_status);
		voltage = (TextView)getActivity().findViewById(R.id.voltage);
		
	}
	
	public void start(DyIO dyio) {
		this.dyio = dyio;
		System.out.println("Hexapod Starting");
		myThread = new walkingThread();
		myThread.start();
	}
	public void stop() {
		myThread.setRunning(false);
		resetWalker();
	}
	
	public void setwalkingText(final String text) {
        new Thread(new Runnable() {
	  		  public void run() {
	  			walkStatus.post(new Runnable() {
	  				  public void run() {
	  					walkStatus.setText(text);
	  				  }
	  			  });
	  		  }
	  	}).start();
	}
	
	private void resetWalker() {
		
	}

	public Activity getActivity() {
		return activity;
	}
	
	public WalkingState getWalkState() {
		return walkState;
	}

	public void setWalkState(WalkingState walkState) {
		this.walkState = walkState;
	}

	private class walkingThread extends Thread{
		private boolean running = true;
		public void run() {
			while(isRunning()) {
				ThreadUtil.wait(30);
				setwalkingText(getWalkState().toString());
			}
		}
		public boolean isRunning() {
			return running;
		}
		public void setRunning(boolean running) {
			this.running = running;
		}
	}

	

}
