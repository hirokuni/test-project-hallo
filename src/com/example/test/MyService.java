package com.example.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import android.os.Handler;

public class MyService extends Service {
	private static final String TAG = "MyService";
	private static final String SERVICE_NAME = "BTHello";
	private static final String SERIAL_PORT_SERVICE_ID = "00001101-0000-1000-8000-00805F9B34FB";
	private static final UUID SERVICE_ID = UUID
			.fromString(SERIAL_PORT_SERVICE_ID);
	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	AcceptThread thread;
	PowerManager.WakeLock wakelock;
	private boolean DEBUG = true;
	private boolean SEN_DEBUG = false;
	private Handler handler = new Handler();
	private boolean mIsRelease = false;

	private BroadcastReceiver bcr = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null)
				return;

			if (intent.getAction().equals("com.sony.test.haro.led")) {
				String first = intent.getExtras().getString("1st");
				String second = intent.getExtras().getString("2nd");
				int third = intent.getExtras().getInt("3rd");
				int fourth = intent.getExtras().getInt("4th");
				int fifth = intent.getExtras().getInt("5th");

				StringBuffer sb = new StringBuffer(first + ":" + second + ":"
						+ third + ":" + fourth + ":" + fifth + "\n");

				String cmd = sb.toString();
				byte[] cmd_data = cmd.getBytes();

				for (int i = 0; i < cmd_data.length; i++) {
					Log.i(TAG, "" + cmd_data[i]);
				}

				Log.i(TAG, "cmd : " + cmd);
				if (thread != null)
					thread.out(cmd);
			}
		}

	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate()");

		thread = new AcceptThread();
		thread.start();

		// wake lock
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ON_AFTER_RELEASE, "Your App Tag");
		wakelock.acquire();

		IntentFilter ifilter = new IntentFilter();
		ifilter.addAction("com.sony.test.haro.led");
		registerReceiver(bcr, ifilter);
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy()");
		super.onDestroy();

		if (thread != null) {
			thread.release();
			thread.cancel();
		}

		if (wakelock != null)
			wakelock.release();

		wakelock = null;

		mIsRelease = true;

		unregisterReceiver(bcr);
	}

	private class AcceptThread extends Thread {
		SensorDataComposer sdc;
		public final BluetoothServerSocket mmServerSocket;
		public BluetoothSocket socket;
		private droneCtrl mDctrl;
		private boolean isCmdSet = false;
		private String OutCmd = null;

		public AcceptThread() {
			Log.i(TAG, "thread create");
			// Use a temporary object that is later assigned to mmServerSocket,
			// because mmServerSocket is final
			BluetoothServerSocket tmp = null;
			try {
				// MY_UUID is the app's UUID string, also used by the client
				// code
				tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
						"my name", SERVICE_ID);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (mDctrl == null)
				mDctrl = new droneCtrl(MyService.this);

			if (mDctrl == null)
				Log.w(TAG, "Dctrl is null");

			mmServerSocket = tmp;
			sdc = new SensorDataComposer(mDctrl);

		}

		public void out(String cmd) {
			if (cmd == null)
				return;
			isCmdSet = true;
			OutCmd = cmd;
			Log.i(TAG, "out cmd : " + cmd);
		}

		public void release() {
			Log.i(TAG, "mDctrl is released");
			if (mDctrl != null)
				mDctrl.release();
			mDctrl = null;
		}

		private void showToast(final String str) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(MyService.this, str, Toast.LENGTH_LONG)
							.show();
				}
			});
		}

		public void run() {
			socket = null;
			// Keep listening until exception occurs or a socket is returned
			while (true) {
				try {
					Log.d(TAG, "waiting for accept");
					socket = mmServerSocket.accept();
					Log.d(TAG, "accept");
					showToast("BT socket accept");

				} catch (IOException e) {
					Log.w(TAG, e.toString());
					break;
				}
				// If a connection was accepted
				if (socket != null) {
					// Do work to manage the connection (in a separate thread)
					// manageConnectedSocket(socket);
					connect(socket);
					try {
						mmServerSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
			}
			showToast("BT conn is closed");
			sdc.release();

			showToast("service going off");
			MyService.this.stopSelf();

		}

		private void connect(BluetoothSocket socket) {

			// DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

			try {
				InputStream in = socket.getInputStream();
				OutputStream out = socket.getOutputStream();

				Log.d(TAG, "Connection established.");
				showToast("BT conn established");
				// out.write("Hello I'm Bluetooth! Press Q to quit.\r\n".getBytes());
				while (true) {
					byte[] buffer = new byte[1024];
					int bytes = in.read(buffer);

					String cmd_in = new String(buffer, 0, bytes);
					if (SEN_DEBUG)
						Log.d(TAG, "in : " + cmd_in);

					sdc.addData(cmd_in);

					if (isCmdSet) {
						out.write(OutCmd.getBytes());
						isCmdSet = false;
					}
					// out.write((df.format(new Date()) + ": " + new String
					// (buffer, 0, bytes) + "\r\n").getBytes());
					if (mIsRelease == true) {
						// out.write(("Bye!\r\n").getBytes());
						Log.d(TAG, "bye");
						break;
					}
				}
				socket.close();
				socket = null;

			} catch (IOException e) {
				Log.e(TAG, "Something bad happened!", e);
			}
		}

		/** Will cancel the listening socket, and cause the thread to finish */
		public void cancel() {
			Log.i(TAG, "Server Socket is closed by Cancel");
			try {
				mmServerSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
