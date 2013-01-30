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

import com.neuronrobotics.sdk.android.AndroidSerialConnection;
import com.neuronrobotics.sdk.dyio.DyIO;
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

  

    public void onClick(View v) {
        
        System.out.println("DyIO created");
        
        if (v == mFire) {
        	DyIO.disableFWCheck();
        	if(dyio== null){
        		dyio = new DyIO(new AndroidSerialConnection(this));
        	}else{
        		dyio.disconnect();
        	}
        	if(!dyio.isAvailable())
        		dyio.connect();
        	if(dyio.isAvailable())
        		Tester.runTest(dyio, null);
        }
    }

}


