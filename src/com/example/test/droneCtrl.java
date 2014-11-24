package com.example.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class droneCtrl implements SensorEventListener {
	// time3 : 3 seconds
	private String[] sequence03 = {
			// "speeddown","time1",
			// "up", "time2",
			"go", "time8", "left", "time2", "left", "time2", "left", "time20",
	// "go", "time4",
	// "down", "time2",
	// "landing"
	// "go", "time1",
	};

	private String[] sequence02 = {
			// "speeddown","time1",
			// "up", "time2",
			"go", "time2", "up", "time2", "left", "time2", "left", "time2",
			"left", "time2", "down", "time2", "left", "time2", "left", "time2",
			"left", "time2", "left", "time2", "left", "time2", "left", "time2",
			"left", "time2", "left", "time2", "left", "time2", "left", "time2",
			"left", "time2", "left", "time2", "left", "time2", "left", "time2",
			"left", "time2", "left", "time2", "left", "time2", "left", "time2",
			"left", "time2", "left", "time2", "left", "time2", "left", "time2",
			"left", "time2", "left", "time2", "left", "time2", "left", "time2",
			"left", "time2",
	// "go", "time4",
	// "down", "time2",
	// "landing"
	// "go", "time1",
	};

	private String[] sequence01 = {
			// "speeddown","time1",
			// "up", "time2",
			"up","time2",
			"go", "time10", 
			
			"left", "time2", "left", "time2", "left", "time2", "left", "time2","time10"
	// "go", "time4",
	// "down", "time2",
	// "landing"
	// "go", "time1",
	};

	// private String[] sequence01 = {
	// "go", "time1",
	// "speeddown","time1",
	// "left", "time1",
	// "left", "time1",
	// //"go", "time4",
	// "up", "time1",
	// "down","time1",
	// //"go", "time1",
	// };

	// private String[] sequence01 = { "go", "time4", "stop", "left", "time2",
	// "left", "time2", "go", "time4", "stop", "up", "time1", "down",
	// "time1", "go", "time2", "stop", "dst-stop20", "time3", "landing" };

	// private String[] sequence01 = {
	// "go", "time2", "stop",
	// "right", "time2",
	// "right", "time2",
	// "go", "time2",
	// "landing" };

	private boolean DEBUG = true;
	private boolean SEN_DEBUG = false;
	private final static String TAG = "droneCtrl";
	private Context mContext;
	private ctrlHandler mCtrlHandler;
	private HandlerThread handlerThread = new HandlerThread("other");
	private boolean isRelease = false;
	private boolean SequecneMustStop = false;
	private SequenceThread mSequenceThread;
	private Handler handler = new Handler();
	private boolean isStarting = false;

	private boolean AccDown = false;
	private int AccCnt = 0;

	private void mywait(int msec) {

		try {
			Thread.sleep(msec);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private class SequenceThread extends Thread {
		private String[] exe;

		public SequenceThread(String[] sequence) {
			exe = sequence;
		}

		private SequenceThread() {
		}

		@Override
		public void run() {
			init();
			SequecneMustStop = false;
			Log.i(TAG, "Ready....");
			mywait(3000);
			Log.i(TAG, "Drone START");
			isStarting = true;
			for (int i = 0; i < exe.length; i++) {
				if ("go".equals(exe[i])) {
					go();
				} else if ("stop".equals(exe[i])) {
					droneCtrl.this.stop();
				} else if ("left".equals(exe[i])) {
					left();
				} else if ("right".equals(exe[i])) {
					right();
				} else if ("up".equals(exe[i])) {
					up();
				} else if ("down".equals(exe[i])) {
					down();
				} else if ("landing".equals(exe[i])) {
					landing_takeoff();
				} else if ("takeoff".equals(exe[i])) {
					landing_takeoff();
				} else if (("speedup".equals(exe[i]))) {
					speed_up();
				} else if (("speeddown".equals(exe[i]))) {
					speed_down();
				} else if (exe[i].contains("dst-stop")) {
					// ~cm でLandingを始める
					int cm = Integer.valueOf(exe[i].substring(8,
							exe[i].length()));
					// todo implementation
				} else if (exe[i].contains("time")) {
					int time = 0;
					time = Integer
							.valueOf(exe[i].substring(4, exe[i].length())) * 1000;
					Log.i(TAG, "wait time : " + time);
					try {
						Thread.sleep(time);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
				if (isRelease || SequecneMustStop)
					break;
			}
			end();
			mywait(5000);// must be "before isStarting = false;"
			mSequenceThread = null;
			isStarting = false;
			Log.i(TAG, "Drone STOP");
		}
	};

	private Message msg = new Message();

	private void stop() {
		Log.i(TAG, "stop()");
		Message msg = new Message();
		msg.what = ctrlHandler.STOP;
		mCtrlHandler.sendMessage(msg);
	}

	private void go() {
		Log.i(TAG, "go()");
		Message msg = new Message();
		msg.what = ctrlHandler.GO_STRAIGHT;
		mCtrlHandler.sendMessage(msg);
	}

	private void left() {
		Log.i(TAG, "left()");
		Message msg = new Message();
		msg.what = ctrlHandler.TURN_LEFT;
		mCtrlHandler.sendMessage(msg);
	}

	private void right() {
		Log.i(TAG, "right()");
		Message msg = new Message();
		msg.what = ctrlHandler.TURN_RIGHT;
		mCtrlHandler.sendMessage(msg);
	}

	private void up() {
		Log.i(TAG, "up()");
		Message msg = new Message();
		msg.what = ctrlHandler.UP;
		mCtrlHandler.sendMessage(msg);
	}

	private void down() {
		Log.i(TAG, "down()");
		Message msg = new Message();
		msg.what = ctrlHandler.DOWN;
		mCtrlHandler.sendMessage(msg);
	}

	private void init() {
		Log.i(TAG, "init()");
		Message msg = new Message();
		msg.what = ctrlHandler.INIT;
		mCtrlHandler.sendMessage(msg);
	}

	private void end() {
		Log.i(TAG, "end()");
		droneCtrl.this.stop();
		Message msg = new Message();
		msg.what = ctrlHandler.FINALIZE;
		mCtrlHandler.sendMessage(msg);
	}

	private void landing_takeoff() {
		Log.i(TAG, "landing/takeoff()");
		Message msg = new Message();
		msg.what = ctrlHandler.LANDING_TAKEOFF;
		mCtrlHandler.sendMessage(msg);
	}

	private void speed_down() {
		Log.i(TAG, "speed_down()");
		Message msg = new Message();
		msg.what = ctrlHandler.SPEED_DOWN;
		mCtrlHandler.sendMessage(msg);
	}

	private void speed_up() {
		Log.i(TAG, "speed_up()");
		Message msg = new Message();
		msg.what = ctrlHandler.SPEED_UP;
		mCtrlHandler.sendMessage(msg);
	}

	private void startSequence() {
		Log.i(TAG, "startSequence");
		Intent intent = new Intent("com.sony.test.haro.start");
		mContext.sendBroadcast(intent);
	}

	private BroadcastReceiver bcr = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null)
				return;

			if (intent.getAction().equals("com.sony.test.haro.start")) {
				if (mSequenceThread == null) {
					mSequenceThread = new SequenceThread(sequence01);
					mSequenceThread.start();
					Log.i(TAG, "sequence start");
					showToast("Sequence Start !!!!!!!!!!!!!!!!!!!!!");
				} else {
					Log.w(TAG, "on sequencing");
					handler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(mContext,
									"Sequence can't start. Already ongoing",
									Toast.LENGTH_LONG).show();
						}
					});
				}
			} else if (intent.getAction().equals("com.test.k.init")) {
				init();
			} else if (intent.getAction().equals("com.test.k.straight")) {
				go();
			} else if (intent.getAction().equals("com.test.k.right")) {
				right();
			} else if (intent.getAction().equals("com.test.k.left")) {
				left();
			} else if (intent.getAction().equals("com.test.k.landing_takeoff")) {
				landing_takeoff();
			} else if (intent.getAction().equals("com.test.k.stop")) {
				stop();
			} else if (intent.getAction().equals("com.test.k.up")) {
				up();
			} else if (intent.getAction().equals("com.test.k.down")) {
				down();
			} else if (intent.getAction().equals("com.test.k.end")) {
				end();
			}
		}
	};

	public droneCtrl(Context context) {
		IntentFilter ifilter = new IntentFilter();
		ifilter.addAction("com.sony.test.haro.start");
		ifilter.addAction("com.test.k.init");
		ifilter.addAction("com.test.k.straight");
		ifilter.addAction("com.test.k.right");
		ifilter.addAction("com.test.k.left");
		ifilter.addAction("com.test.k.landing_takeoff");
		ifilter.addAction("com.test.k.stop");
		ifilter.addAction("com.test.k.up");
		ifilter.addAction("com.test.k.down");
		ifilter.addAction("com.test.k.end");
		mContext = context;
		mContext.registerReceiver(bcr, ifilter);
		handlerThread.start();
		mCtrlHandler = new ctrlHandler(handlerThread.getLooper());
		acceptDst = true;
	}

	private droneCtrl() {
	}

	private void interruptSequenceThread() {
		if (mSequenceThread != null) {
			if (mSequenceThread.isAlive())
				if (mSequenceThread.isInterrupted())
					mSequenceThread.interrupt();
		}
	}

	public void release() {
		interruptSequenceThread();

		if (bcr != null & mContext != null)
			mContext.unregisterReceiver(bcr);
		bcr = null;
		mContext = null;

		handlerThread.getLooper().quit();

		isRelease = true;

		SequecneMustStop = true;
	}

	@Override
	public void onGroChanged(int x, int y, int z) {
		if (SEN_DEBUG)
			Log.i(TAG, "onGroChanged : x, y, z = " + x + ", " + y + ", " + z);

		// if (y > 800 || z > 800)
		// if(SequecneMustStop)
		// startSequence();
	}

	public void showToast(final String str) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(mContext, str, Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public void onAccChanged(int x, int y, int z) {
		if (SEN_DEBUG)
			Log.i(TAG, "onAccChanged : x, y, z = " + x + ", " + y + ", " + z);

		if (AccDown == true) {
			Log.i(TAG, "AccDown is detected");
			AccCnt++;
		}

		if (AccDown == false)
			if (x < -1000) {
				Log.i(TAG, "acc < -1000");
				AccDown = true;
			}

		if (AccDown == true)
			if (x > -150) {
				if (isStarting == false) {
					startSequence();
					Log.i(TAG, "Detect Acc Start timing!!!");
					AccRelease();
				}
			}

		if (AccCnt > 30) {
			AccRelease();
			Log.i(TAG, "AccRelease");
		}
	}

	private void AccRelease() {
		AccDown = false;
		AccCnt = 0;

	}

	@Override
	public void onMagChanged(int x, int y, int z) {
		if (SEN_DEBUG)
			Log.i(TAG, "onMagChanged : x, y, z = " + x + ", " + y + ", " + z);

	}

	boolean isSpeedDown = false;

	boolean isred = false;

	private void led_green() {
		if (isred == false)
			return;
		Intent intent = new Intent("com.sony.test.haro.led");
		intent.putExtra("1st", "led");
		intent.putExtra("2nd", "m");
		intent.putExtra("3rd", 2);
		intent.putExtra("4th", 1);
		intent.putExtra("5th", 2);
		mContext.sendBroadcast(intent);
		// "led:m:1:2:2" //green
		// "led:m:2:2:1" //red
		isred = false;
	}

	private void led_red() {
		if (isred == true)
			return;

		Intent intent = new Intent("com.sony.test.haro.led");
		intent.putExtra("1st", "led");
		intent.putExtra("2nd", "m");
		intent.putExtra("3rd", 3);
		intent.putExtra("4th", 2);
		intent.putExtra("5th", 2);
		mContext.sendBroadcast(intent);

		Intent intent2 = new Intent("com.sony.test.haro.led");
		intent2.putExtra("1st", "led");
		intent2.putExtra("2nd", "f");
		intent2.putExtra("3rd", 5);
		intent2.putExtra("4th", 5);
		intent2.putExtra("5th", 5);
		mContext.sendBroadcast(intent);
		// "led:m:1:2:2" //green
		// "led:m:2:2:1" //red
		isred = true;
	}

	private boolean acceptDst = true;
	private int dstCnt = 0;

	@Override
	public void onDstChanged(int data) {
		if (SEN_DEBUG)
			Log.i(TAG, "onDstChanged : dst = " + data);

		if (data < 60) {
			// led_red();
			// if (isSpeedDown == false) {
			// speed_down();
			// }
			
		} else {
			
			// if (isSpeedDown == true) {
			// speed_up();
			// }
		}

		if (data < 25) {
			dstCnt++;
			if (dstCnt > 10)
				led_green();
				if (isStarting == true) {
					if (acceptDst == true) {
						Log.i(TAG, "dst < 25");
						Log.i(TAG, "landing!!");
						SequecneMustStop = true;
						interruptSequenceThread();
						landing_takeoff();
						acceptDst = false;
						new Thread(new Runnable() {
							@Override
							public void run() {
								mywait(10000);
								acceptDst = true;
							}
						}).start();
					} else {
						Log.i(TAG, "ignore landing!");
					}
				}
		} else {
			dstCnt = 0;
			led_red();
		}
	}

	private final class ctrlHandler extends Handler {
		static final String OP = "op";
		static final String L_X = "x";
		static final String L_Y = "y";
		static final int L_DEFAULT_X = 300;
		static final int L_DEFAULT_Y = 600;
		static final String R_X = "x2";
		static final String R_Y = "y2";
		static final int R_DEFAULT_X = 1400;
		static final int R_DEFAULT_Y = 600;

		public final static int INIT = 0;
		public final static int GO_STRAIGHT = 1;
		public final static int TURN_RIGHT = 2;
		public final static int TURN_LEFT = 3;
		public final static int LANDING_TAKEOFF = 4;
		public final static int STOP = 5;
		public final static int UP = 6;
		public final static int DOWN = 7;
		public final static int FINALIZE = 8;
		public final static int SPEED_DOWN = 9;
		public final static int SPEED_UP = 10;
		private int currentSpeed = -4;

		Object mLock = new Object();

		public ctrlHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			int request = msg.what;

			switch (request) {
			case INIT:
				Log.i(TAG, "INIT");
				{
					Intent intent = new Intent("test.kawa.motion");
					intent.putExtra(OP, "2_points_test_start");
					intent.putExtra(L_X, L_DEFAULT_X);// 300
					intent.putExtra(L_Y, L_DEFAULT_Y);// 600
					intent.putExtra(R_X, R_DEFAULT_X);// 1400
					intent.putExtra(R_Y, R_DEFAULT_Y);// 600
					mContext.sendBroadcast(intent);

				}
				mywait(100);
				{
					Intent intent = new Intent("test.kawa.motion");
					intent.putExtra(OP, "2_points_test_move_2");
					intent.putExtra(L_X, L_DEFAULT_X);// 300
					intent.putExtra(L_Y, L_DEFAULT_Y);// 600
					intent.putExtra(R_X, R_DEFAULT_X);// 1400
					intent.putExtra(R_Y, R_DEFAULT_Y);// 600
					mContext.sendBroadcast(intent);
				}

				break;
			case SPEED_DOWN:
				Log.i(TAG, "SPEED_DOWN");
				{
					isSpeedDown = true;
					currentSpeed = -2;
					Intent intent = new Intent("com.test.sensor");
					intent.putExtra("x", currentSpeed);
					mContext.sendBroadcast(intent);
				}
				break;
			case SPEED_UP:
				Log.i(TAG, "SPEED_UP");
				{
					isSpeedDown = false;
					currentSpeed = -4;
					Intent intent = new Intent("com.test.sensor");
					intent.putExtra("x", currentSpeed);
					mContext.sendBroadcast(intent);

				}
				break;
			case GO_STRAIGHT:
				Log.i(TAG, "STRAIGHT");
				{
					Intent intent = new Intent("com.test.sensor");
					intent.putExtra("x", currentSpeed);
					mContext.sendBroadcast(intent);
				}
				break;
			case STOP: {
				Log.i(TAG, "STOP");
				Intent intent = new Intent("com.test.sensor");
				intent.putExtra("x", 0);
				mContext.sendBroadcast(intent);

				one_tap(300, 300);
			}
				break;
			case TURN_RIGHT:
				Log.i(TAG, "RIGHT");
				{
					Intent intent = new Intent("test.kawa.motion");
					intent.putExtra(OP, "2_points_test_move_2");
					intent.putExtra(L_X, 500);// 300
					intent.putExtra(L_Y, L_DEFAULT_Y);// 600
					intent.putExtra(R_X, R_DEFAULT_X);// 1400
					intent.putExtra(R_Y, R_DEFAULT_Y);// 600
					mContext.sendBroadcast(intent);
				}
				mywait(1000);
				{
					Intent intent = new Intent("test.kawa.motion");
					intent.putExtra(OP, "2_points_test_move_2");
					intent.putExtra(L_X, L_DEFAULT_X);// 300
					intent.putExtra(L_Y, L_DEFAULT_Y);// 600
					intent.putExtra(R_X, R_DEFAULT_X);// 1400
					intent.putExtra(R_Y, R_DEFAULT_Y);// 600
					mContext.sendBroadcast(intent);
				}
				break;
			case TURN_LEFT:
				Log.i(TAG, "LEFT");
				{
					Intent intent = new Intent("test.kawa.motion");
					intent.putExtra(OP, "2_points_test_move_2");
					intent.putExtra(L_X, 100);// 300
					intent.putExtra(L_Y, L_DEFAULT_Y);// 600
					intent.putExtra(R_X, R_DEFAULT_X);// 1400
					intent.putExtra(R_Y, R_DEFAULT_Y);// 600
					mContext.sendBroadcast(intent);
				}
				mywait(1000);
				{
					Intent intent = new Intent("test.kawa.motion");
					intent.putExtra(OP, "2_points_test_move_2");
					intent.putExtra(L_X, L_DEFAULT_X);// 300
					intent.putExtra(L_Y, L_DEFAULT_Y);// 600
					intent.putExtra(R_X, R_DEFAULT_X);// 1400
					intent.putExtra(R_Y, R_DEFAULT_Y);// 600
					mContext.sendBroadcast(intent);
				}
				break;
			case LANDING_TAKEOFF:
				Log.i(TAG, "LANDING / TAKEOFF");
				{
					Intent intent = new Intent("test.kawa.motion");
					intent.putExtra(OP, "move_start");
					intent.putExtra(L_X, 900);
					intent.putExtra(L_Y, 1050);
					mContext.sendBroadcast(intent);
				}
				{
					Intent intent = new Intent("test.kawa.motion");
					intent.putExtra(OP, "move");
					intent.putExtra(L_X, 900);
					intent.putExtra(L_Y, 1050);
					mContext.sendBroadcast(intent);
				}
				{
					Intent intent = new Intent("test.kawa.motion");
					intent.putExtra(OP, "move_end");
					intent.putExtra(L_X, 900);
					intent.putExtra(L_Y, 1050);
					mContext.sendBroadcast(intent);
				}
				break;
			case UP:
				Log.i(TAG, "UP");
				{
					Intent intent = new Intent("test.kawa.motion");
					intent.putExtra(OP, "2_points_test_move_2");
					intent.putExtra(L_X, L_DEFAULT_X);// 300
					intent.putExtra(L_Y, 400);// 600
					intent.putExtra(R_X, R_DEFAULT_X);// 1400
					intent.putExtra(R_Y, R_DEFAULT_Y);// 600
					mContext.sendBroadcast(intent);
				}
				mywait(1200);
				{
					Intent intent = new Intent("test.kawa.motion");
					intent.putExtra(OP, "2_points_test_move_2");
					intent.putExtra(L_X, L_DEFAULT_X);// 300
					intent.putExtra(L_Y, L_DEFAULT_Y);// 600
					intent.putExtra(R_X, R_DEFAULT_X);// 1400
					intent.putExtra(R_Y, R_DEFAULT_Y);// 600
					mContext.sendBroadcast(intent);
				}
				break;
			case DOWN:
				Log.i(TAG, "DOWN");
				{
					Intent intent = new Intent("test.kawa.motion");
					intent.putExtra(OP, "2_points_test_move_2");
					intent.putExtra(L_X, L_DEFAULT_X);// 300
					intent.putExtra(L_Y, 800);// 600
					intent.putExtra(R_X, R_DEFAULT_X);// 1400
					intent.putExtra(R_Y, R_DEFAULT_Y);// 600
					mContext.sendBroadcast(intent);
				}
				mywait(1000);
				{
					Intent intent = new Intent("test.kawa.motion");
					intent.putExtra(OP, "2_points_test_move_2");
					intent.putExtra(L_X, L_DEFAULT_X);// 300
					intent.putExtra(L_Y, L_DEFAULT_Y);// 600
					intent.putExtra(R_X, R_DEFAULT_X);// 1400
					intent.putExtra(R_Y, R_DEFAULT_Y);// 600
					mContext.sendBroadcast(intent);
				}
				break;
			case FINALIZE: {
				Log.i(TAG, "FINALIZE");
				// Intent intent = new Intent("test.kawa.motion");
				// intent.putExtra(OP, "2_points_test_end");
				// intent.putExtra(L_X, L_DEFAULT_X);// 300
				// intent.putExtra(L_Y, L_DEFAULT_Y);// 600
				// intent.putExtra(R_X, R_DEFAULT_X);// 1400
				// intent.putExtra(R_Y, R_DEFAULT_Y);// 600
				// mContext.sendBroadcast(intent);

				Log.i(TAG, "STOP");
				Intent intent = new Intent("com.test.sensor");
				intent.putExtra("x", 0);
				mContext.sendBroadcast(intent);
			}
				break;
			default:
				Log.i(TAG, "unknown ctrl");
			}
		}

		private void one_tap(int x, int y) {
			{
				Intent intent = new Intent("test.kawa.motion");
				intent.putExtra(OP, "move_start");
				intent.putExtra(L_X, x);
				intent.putExtra(L_Y, y);
				mContext.sendBroadcast(intent);
			}
			{
				Intent intent = new Intent("test.kawa.motion");
				intent.putExtra(OP, "move");
				intent.putExtra(L_X, x);
				intent.putExtra(L_Y, y);
				mContext.sendBroadcast(intent);
			}
			{
				Intent intent = new Intent("test.kawa.motion");
				intent.putExtra(OP, "move_end");
				intent.putExtra(L_X, x);
				intent.putExtra(L_Y, y);
				mContext.sendBroadcast(intent);
			}
		}

		private void mywait(int msec) {

			try {
				Thread.sleep(msec);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}
}
