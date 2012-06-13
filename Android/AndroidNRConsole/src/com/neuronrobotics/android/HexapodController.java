package com.neuronrobotics.android;

import com.neuronrobotics.sdk.addons.walker.BasicWalker;
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
	
	private walkingThread myThread;
	private BasicWalker walker;

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
		setDyio(dyio);
		System.out.println("Hexapod Starting");
		myThread = new walkingThread();
		myThread.start();
	}
	public void stop() {
		myThread.setRunning(false);
		resetWalker();
		getDyio().setCachedMode(false);
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

	public DyIO getDyio() {
		return dyio;
	}

	public void setDyio(DyIO dyio) {
		this.dyio = dyio;
	}

	public BasicWalker getWalker() {
		return walker;
	}

	public void setWalker(BasicWalker walker) {
		this.walker = walker;
	}

	private class walkingThread extends Thread{
		private boolean running = true;
		private long loopTime = 100;
		public void run() {
			setWalker(new BasicWalker(getDyio()));
			getWalker().initialize();
			while(isRunning() && getDyio().isAvailable()) {
				if(getWalkState() != WalkingState.STOPPED)
					setwalkingText(getWalkState().toString());
				else
					setwalkingText("Ready...");
				long start = System.currentTimeMillis();
				switch(getWalkState()) {
				case FORWARD:
					getWalker().incrementAllY(-1*.2,getLoopTime());
					break;
				case BACKWARD:
					getWalker().incrementAllY(getYinc(), getLoopTime());
					break;
				case TURN_LEFT:
					getWalker().turnBody(getTurnDeg(), getLoopTime());
					break;
				case TURN_RIGHT:
					getWalker().turnBody(-1*getTurnDeg(), getLoopTime());
					break;
				case STRAIF_LEFT:
					getWalker().incrementAllX(-1*getXinc(), getLoopTime());
					break;
				case STRAIF_RIGHT:
					getWalker().incrementAllX(getXinc(), getLoopTime());
					break;
				default:
					break;
				}
				//Next loop should be as long as last one took
				//setLoopTime(System.currentTimeMillis()-start);
				try {
					Thread.sleep(getLoopTime());
				} catch (InterruptedException e) {
				}
			}
		}
		private double getXinc() {
			// TODO Auto-generated method stub
			return .2;
		}
		private double getTurnDeg() {
			return 5;
		}
		private double getYinc() {
			return .2;
		}
		public boolean isRunning() {
			return running;
		}
		public void setRunning(boolean running) {
			this.running = running;
		}
		public long getLoopTime() {
			return loopTime;
		}
		public void setLoopTime(long loopTime) {
//			if(loopTime<50)
//				loopTime=50;
			this.loopTime = loopTime;
		}
	}

	

}
