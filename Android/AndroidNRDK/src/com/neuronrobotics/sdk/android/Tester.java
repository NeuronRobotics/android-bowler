package com.neuronrobotics.sdk.android;

import android.app.Activity;

import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
import com.neuronrobotics.sdk.dyio.peripherals.DigitalInputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.DigitalOutputChannel;

public class Tester {
	  public static void runTest( final DyIO dyio){
		  if(dyio == null)
			  throw new NullPointerException("DyIO must be instantiated");
		  if(!dyio.connect())
			  throw new NullPointerException("DyIO must have connection availible");
		  new Thread(){
				@Override
				public void run(){
			        System.out.println("Fire a Ping!");
		            try {
		        		for (int i=0;i<24;i++){
		        			dyio.setMode(i, DyIOChannelMode.DIGITAL_IN,false);
		        		}
		        		DigitalInputChannel dip = new DigitalInputChannel(dyio.getChannel(0));
		        		DigitalOutputChannel dop = new DigitalOutputChannel(dyio.getChannel(1));
		        		
		        		double avg=0;
		        		
		        		int i;
		        		boolean high = false;
		        		//dyio.setCachedMode(true);
		        		long start = System.currentTimeMillis();
		        		for(i=0;i<100;i++) {
		        			//dyio.flushCache(0);
		        			high = !high;
		        			high = dip.getValue()==1;
		        			dop.setHigh(high);
		        			double ms=System.currentTimeMillis()-start;
		        			avg +=ms;
		        			start = System.currentTimeMillis();
		        			//System.out.println("Average cycle time: "+(int)(avg/i)+"ms\t\t\t this loop was: "+ms);
		        		}
		        		System.out.println("Average cycle time for IO get/set: "+(avg/i)+" ms");
		        		
		        		avg=0;
		        		dyio.setCachedMode(true);
		        		start = System.currentTimeMillis();
		        		for(i=0;i<100;i++) {
		        			dyio.flushCache(0);
		        			double ms=System.currentTimeMillis()-start;
		        			avg +=ms;
		        			start = System.currentTimeMillis();
		        			//System.out.println("Average cycle time: "+(int)(avg/i)+"ms\t\t\t this loop was: "+ms);
		        		}
		        		System.out.println("Average cycle time for cache flush: "+(avg/i)+" ms");
		        		
		        		avg=0;
		        		start = System.currentTimeMillis();
		        		for(i=0;i<100;i++) {
		        			dyio.ping();
		        			double ms=System.currentTimeMillis()-start;
		        			avg +=ms;
		        			start = System.currentTimeMillis();
		        			//System.out.println("Average cycle time: "+(int)(avg/i)+"ms\t\t\t this loop was: "+ms);
		        		}
		        		System.out.println("Average cycle time for ping: "+(avg/i)+" ms");
		        		dyio.disconnect(); 
		            }catch(Exception ex) {
		            	ex.printStackTrace();
		            }	
				}
			}.start();
	    }
}
