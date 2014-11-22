package com.example.test;


import java.util.List;

import com.example.test.MyService;

import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	private TextView textSetting;
	private Context mContext;
	private boolean isStop = false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); 
		
        mContext = this;
        
        textSetting = (TextView) findViewById(R.id.textView1);
        
        new Thread (new Runnable(){
			@Override
			public void run() {		
				while(true){
					textSetting.post(new Runnable(){
						@Override
						public void run() {
							if (textSetting == null)
								Log.e(TAG, "textSetting is null");
							else {
								if (checkServiceRunning())
									textSetting.setText("Running");
								else
									textSetting.setText("Not running");
							}
						}
					});
					
					if (isStop == true)
						break;
					
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
        }).start();

		Button start_button = (Button) findViewById(R.id.button1);
		Button stop_button = (Button) findViewById(R.id.button2);

		if (start_button == null) {
			Log.i("TEST", "null");
		} else {
			Log.i("TEST", "got");
			start_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// ボタンがクリックされた時に呼び出されます
					Button button = (Button) v;
					
					Intent intent = new Intent();
					intent.setComponent(new ComponentName(MyService.class.getPackage().getName(), MyService.class.getName()));
					mContext.startService(intent);
				}
			});
		}
		
		if (stop_button == null) {
			Log.i("TEST", "null");
		} else {
			Log.i("TEST", "got");
			stop_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// ボタンがクリックされた時に呼び出されます
					Button button = (Button) v;
					
					Intent intent = new Intent();
					intent.setComponent(new ComponentName(MyService.class.getPackage().getName(), MyService.class.getName()));
					mContext.stopService(intent);
					
				
				}
			});
		}

			
    }

    private boolean checkServiceRunning() {
		boolean isRunning = false;
		String mServiceName = MyService.class.getSimpleName();

		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> services = activityManager
				.getRunningServices(Integer.MAX_VALUE);
		if (services != null) {
			for (RunningServiceInfo info : services) {
				if (info.service.getClassName().endsWith(mServiceName)) {
					//Log.i(TAG, mServiceName + " Running!!");
					isRunning = true;
					break;
				}
			}
		}
		return isRunning;
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
