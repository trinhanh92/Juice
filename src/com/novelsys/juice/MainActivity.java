package com.novelsys.juice;

import com.novelsys.juice.login.LoginActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

public class MainActivity extends Activity {
	private final static int SPLASH_TIME = 4000;
	ImageView mLogoView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mLogoView = (ImageView) findViewById(R.id.imvLoading);
		// splash screen timeout
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// Switches to Instruction Activity
				mLogoView.setImageResource(R.drawable.logo9);
				if (isFirstTimeRun()) {
					startActivity(new Intent(MainActivity.this,
							InstructionActivity.class));
				} else {
					startActivity(new Intent(MainActivity.this,
							LoginActivity.class));
				}
				finish();
			}
		}, SPLASH_TIME);

		// implement frame by frame animation of imageview
		mLogoView.setImageBitmap(null);
		mLogoView.setBackgroundResource(R.drawable.anim_logo);
		final AnimationDrawable frameAnimation = (AnimationDrawable) mLogoView
				.getBackground();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (frameAnimation != null) {
					frameAnimation.start();
				}
			}
		});
	}

	/**
	 * Check app first time run
	 * 
	 * @return
	 */
	private boolean isFirstTimeRun() {
		SharedPreferences pref = getApplicationContext().getSharedPreferences(
				"myPref", 0);
		boolean runBefore = pref.getBoolean("RunBefore", false);
		if (!runBefore) {
			// first time run
			SharedPreferences.Editor editor = pref.edit();
			editor.putBoolean("RunBefore", true);
			editor.commit();
		}
		return !runBefore;

	}

}
