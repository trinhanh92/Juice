/**
 * @author ANHPHAM
 *
 */
 
package com.novelsys.juice;

import com.novelsys.juice.custom_view.CircularProgressBar;

import android.os.BatteryManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class ChargingActivity extends Activity {
	private TextView txtCountDownTimer, txtCharingInfo;
	private CircularProgressBar battState;
	private ImageView imvUsbCable;
	private static final long TIME_OUT_SET = 1000 * 10; // 15 mins
	private CountDownTimer mCountDownTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_charging);
		
		txtCountDownTimer = (TextView) findViewById(R.id.txtCntDowmTimer);
		txtCharingInfo = (TextView) findViewById(R.id.txtChargingInfo);		
		battState = (CircularProgressBar) findViewById(R.id.battLevel);
		imvUsbCable = (ImageView) findViewById(R.id.imvUsbCable);

		mCountDownTimer = new CountDownTimer(10000, 1000) {
			@Override
			public void onTick(long timeup) {
				//long s = (timeup/1000) % 60;
				//long m = ((timeup/1000) / 60) % 60;
				//long h = ((timeup/1000)  / (60 * 60)) % 24;
				//String timeString = String.format("%02dh %02dm %02ds", h, m, s);
				//String timeString = String.format("%d", timeup/1000);
				txtCountDownTimer.setText(String.valueOf(timeup));
			}

			@Override
			public void onFinish() {
				txtCountDownTimer.setText("Time Out");
				txtCharingInfo.setVisibility(View.GONE);
				mCountDownTimer.cancel();
			}
		};

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(mPowerStatReceiver, iFilter);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mPowerStatReceiver);
		super.onDestroy();
	}
	
	// receive the change of charging state and battery level
	private BroadcastReceiver mPowerStatReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// are we charging/charged ?
			int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
			//boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING)
			//		|| (status == BatteryManager.BATTERY_STATUS_FULL);

			// how are we charging? AC or USB?
			int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			final boolean usbCharge = (chargePlug == BatteryManager.BATTERY_PLUGGED_USB);
			final boolean acCharge = (chargePlug == BatteryManager.BATTERY_PLUGGED_AC);

			// get battery level
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

			float battPct = level / (float) scale;
			final int battLevel = (int) (battPct * 100);

			// display
			battState.setProgress(battLevel);
			if (status  == BatteryManager.BATTERY_STATUS_FULL)
				battState.setTitle("Full");
			else 
				battState.setTitle(Integer.toString(battLevel) + "%");

			if (acCharge || usbCharge) {
				mCountDownTimer.start();
				txtCharingInfo.setVisibility(View.VISIBLE);
				imvUsbCable.setImageResource(R.drawable.charging);
			} else {
				mCountDownTimer.cancel();
				txtCountDownTimer.setText("Connect to your charger!");
				txtCharingInfo.setVisibility(View.GONE);
				imvUsbCable.setImageResource(R.drawable.usb_cable);
			}

		}
	};



}
