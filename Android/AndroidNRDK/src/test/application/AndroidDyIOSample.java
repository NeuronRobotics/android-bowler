/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.application;

import com.android.missilelauncher.R;
import com.neuronrobotics.sdk.android.AndroidSerialConnection;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
import com.neuronrobotics.sdk.dyio.peripherals.DigitalInputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.DigitalOutputChannel;
import com.neuronrobotics.sdk.ui.ConnectionDialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AndroidDyIOSample extends Activity implements View.OnClickListener {

    private Button mFire=null;
    DyIO dyio;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.launcher);
        mFire = (Button)findViewById(R.id.fire);
        mFire.setOnClickListener(this);

        System.out.println("@#@#@#@#@#Calling On Create");
        
        //com.neuronrobotics.sdk.common.Log.enableDebugPrint(true);
    }

    @Override
    public void onPause() {
    	System.out.println("@#@#@#@#@#Calling On Pause");
        onDestroy();
    }

    @Override
    public void onDestroy() {
    	System.out.println("@#@#@#@#@#Calling On Destroy");
    	if(dyio!= null) {
    		dyio.disconnect();
    		dyio = null;
    	}
        super.onDestroy();
    }

    private void runTest( final Activity a){
		new Thread(){
			@Override
			public void run(){
		        System.out.println("Fire a Ping!");
	            try {
	            	if(dyio != null)
	            		dyio.disconnect();
	                dyio = new DyIO(new AndroidSerialConnection(a));
	                dyio.connect(); 
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

    public void onClick(View v) {
        
        System.out.println("DyIO created");
        
        if (v == mFire) {
        	runTest(this);
        }
    }

}


