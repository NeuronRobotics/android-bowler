package com.neuronrobotics.android;

import com.neuronrobotics.sdk.addons.walker.BasicWalker;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIOPowerEvent;
import com.neuronrobotics.sdk.dyio.IDyIOEvent;
import com.neuronrobotics.sdk.dyio.IDyIOEventListener;
import com.neuronrobotics.sdk.util.ThreadUtil;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

public class HexapodController implements IDyIOEventListener {
	
	private final Activity activity;
	private DyIO dyio;
	
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
		
		forward.setEnabled(false);
		backward.setEnabled(false);
		left.setEnabled(false);
		right.setEnabled(false);
		reset.setEnabled(false);
		
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
		
		reset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	new Thread() {
            		public void run() {
            			resetWalker();
            		}
            	}.start();
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
		setButtonsEnabled(false);
		setwalkingText("Waiting...");
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
	public void setButtonsEnabled(final boolean b) {
        new Thread(new Runnable() {
	  		  public void run() {
	  			forward.post(new Runnable() {
	  				  public void run() {
	  					forward.setEnabled(b);
	  					backward.setEnabled(b);
	  					left.setEnabled(b);
	  					right.setEnabled(b);
	  					reset.setEnabled(b);
	  				  }
	  			  });
	  		  }
	  	}).start();
	}
	public void setVoltage(final double volts) {
		new Thread(new Runnable() {
	  		  public void run() {
	  			voltage.post(new Runnable() {
	  				  public void run() {
	  					voltage.setText(volts+" volts");
	  				  }
	  			  });
	  		  }
	  	}).start();
	}
	
	private void resetWalker() {
		getWalker().initialize();
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
		dyio.addDyIOEventListener(this);
		setVoltage(dyio.getBatteryVoltage(true));
	}

	public BasicWalker getWalker() {
		return walker;
	}

	public void setWalker(BasicWalker walker) {
		this.walker = walker;
	}

	private class walkingThread extends Thread{
		private boolean running = true;
		private long loopTime = 200;
		public void run() {
			setWalker(new BasicWalker(getDyio()));
			
			resetWalker();
			setButtonsEnabled(true);
			int loop = 0;
			while(isRunning() && getDyio().isAvailable()) {
				if(getWalkState() != WalkingState.STOPPED)
					setwalkingText(getWalkState().toString());
				else
					setwalkingText("Ready...");
				long start = System.currentTimeMillis();
				switch(getWalkState()) {
				case FORWARD:
					getWalker().incrementAllY(-1*getYinc(),getLoopTime()/1000);
					break;
				case BACKWARD:
					getWalker().incrementAllY(getYinc(), getLoopTime()/1000);
					break;
				case TURN_LEFT:
					getWalker().turnBody(-1*getTurnDeg(), getLoopTime()/1000);
					break;
				case TURN_RIGHT:
					getWalker().turnBody(getTurnDeg(), getLoopTime()/1000);
					break;
				case STRAIF_LEFT:
					getWalker().incrementAllX(-1*getXinc(), getLoopTime()/1000);
					break;
				case STRAIF_RIGHT:
					getWalker().incrementAllX(getXinc(), getLoopTime()/1000);
					break;
				default:
					break;
				}
				//Next loop should be as long as last one took
				//setLoopTime(System.currentTimeMillis()-start);
				try {
					long time = (long) getLoopTime()-(System.currentTimeMillis()-start);
					if(time>0)
						Thread.sleep(time);
				} catch (InterruptedException e) {
				}
				loop++;
				if(loop>10) {
					loop=0;
					setVoltage(dyio.getBatteryVoltage(true));
				}
			}
		}
		private double getXinc() {
			return getYinc();
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
		public double getLoopTime() {
			return loopTime;
		}
		public void setLoopTime(long loopTime) {
//			if(loopTime<50)
//				loopTime=50;
			this.loopTime = loopTime;
		}
	}

	public void onDyIOEvent(IDyIOEvent arg0) {
		if(arg0.getClass()== DyIOPowerEvent.class) {
			final DyIOPowerEvent ev = (DyIOPowerEvent)arg0;
			setVoltage(ev.getVoltage());
		}
	}

	

}
