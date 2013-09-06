package com.neuronrobotics.android;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.TextView;

public class WebRoverServer {

	private static final int PORT = 7654;
	private Activity activity;
	private TeaServer webServer;
	private DyIOCommThread commThread;

	public WebRoverServer(DyIOCommThread commThread, Activity activity) throws IOException {
		// TODO Auto-generated constructor stub

		this.commThread=commThread;
		this.activity=activity;

	}
	


}
