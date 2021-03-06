package test.application;

import android.widget.TextView;

import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.peripherals.DigitalInputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.DigitalOutputChannel;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class Tester {
	static String log="";
	private static TextView mConversationView;
	public static void runTest(final DyIO dyio, final TextView mConversationView){
		Tester.mConversationView = mConversationView;
		if(mConversationView!=null){
	        new Thread(){
	        	
	        	public void run(){
	        		System.err.println("Starting Log Display");
	        		while(dyio.isAvailable()){
	                	ThreadUtil.wait(100);
	                	try{
	                		mConversationView.setText(Tester.getLog());
	                	}catch (Exception ex){
	                		ex.printStackTrace();
	                	}
	                	//System.err.println(Tester.getLog());
	                }
	        	}
	        }.start();
		}
		new Thread(){
			@Override
			public void run(){
				log="";
				if(dyio == null)
					  throw new NullPointerException("DyIO must be instantiated");
				if(dyio.getConnection()==null)
					  throw new NullPointerException("DyIO must have connection availible");
				if(!dyio.getConnection().isConnected())
					  throw new NullPointerException("DyIO must be connected");
				System.out.println("Fire a Ping!");
				try {
					DigitalInputChannel dip = new DigitalInputChannel(dyio.getChannel(0));
					DigitalOutputChannel dop = new DigitalOutputChannel(dyio.getChannel(1));
					
					double avg=0;
					
					int i;
					boolean high = false;
					//dyio.setCachedMode(true);
					long start = System.currentTimeMillis();
					int reps =10;
					for(i=0;i<reps;i++) {
						//dyio.flushCache(0);
						high = !high;
						high = dip.getValue()==1;
						dop.setHigh(high);
						double ms=System.currentTimeMillis()-start;
						avg +=ms;
						start = System.currentTimeMillis();
						
					}
					
					log+=("Average cycle time for IO get/set: "+(avg/i)+" ms\r\n");
					System.out.println(log);
					avg=0;
					dyio.setCachedMode(true);
					start = System.currentTimeMillis();
					for(i=0;i<reps;i++) {
						dyio.flushCache(0);
						double ms=System.currentTimeMillis()-start;
						avg +=ms;
						start = System.currentTimeMillis();
						
					}
					log+=("Average cycle time for cache flush: "+(avg/i)+" ms\r\n");
					System.out.println(log);
					avg=0;
					start = System.currentTimeMillis();
					for(i=0;i<reps;i++) {
						dyio.ping();
						double ms=System.currentTimeMillis()-start;
						avg +=ms;
						start = System.currentTimeMillis();
					}
					log+=("Average cycle time for ping: "+(avg/i)+" ms\r\n");
					System.out.println(log);
					dyio.disconnect(); 
				}catch(Exception ex) {
		            	ex.printStackTrace();
				}	
			}
		}.start();
	}

	public static String getLog() {
		// TODO Auto-generated method stub
		return log;
	}
}
