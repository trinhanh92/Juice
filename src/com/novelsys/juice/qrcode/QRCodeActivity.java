package com.novelsys.juice.qrcode;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.novelsys.juice.R;

import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.res.Resources.NotFoundException;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

public class QRCodeActivity extends Activity {
	protected static final String TAG = "CamTest";
	private Camera mCamera;
	private CameraPreview mCamPrev;
	private Handler autoFocusHandler;

	private boolean previewing = true;
	
	private MediaPlayer mPlayer;
	private View redLine;
	private Animation mFadeAnim;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qrcode_scan);

		// hide action bar
		ActionBar mActionBar = getActionBar();
		mActionBar.hide();

		redLine = (View) findViewById(R.id.redLine);
		mPlayer = MediaPlayer.create(this, R.raw.beep);
		mFadeAnim = AnimationUtils.loadAnimation(QRCodeActivity.this,
				R.anim.tween);
		autoFocusHandler = new Handler();
		// create an instance of camera
		mCamera = getCameraInstance();

		// create preview view and set it as the content of our activity;
		if (mCamera != null) {
			mCamPrev = new CameraPreview(this, mCamera, autoFocusCB, mPreviewCB);
			FrameLayout mPrevLayout = (FrameLayout) findViewById(R.id.camera_preview);
			mPrevLayout.addView(mCamPrev);
			redLine.startAnimation(mFadeAnim);
		}
	}

	@Override
	protected void onPause() {
		if (mCamera != null) {
			previewing = false;
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
		super.onPause();
	}

	/** A safe way to get an instance of camera object. */
	public static Camera getCameraInstance() {
		Camera cam = null;
		try {
			cam = Camera.open(); // attempt to get a camera instance
		} catch (Exception e) {
			// Camera not available ( in use or not exist)
		}
		return cam;

	}

	/** auto focus call back */
	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);

		}
	};

	/** handle preview frame changed callback */
	PreviewCallback mPreviewCB = new PreviewCallback() {

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			Log.d(TAG, "preview frame");
			Camera.Parameters parameters = camera.getParameters();
			Size size = parameters.getPreviewSize();
			int iWidth = size.width;
			int iHeight = size.height;
			decodeQRcodeHandler(data, iWidth, iHeight);
		}
	};

	/** task in focus callback */
	private Runnable doAutoFocus = new Runnable() {

		@Override
		public void run() {
			if (previewing) {
				mCamera.autoFocus(autoFocusCB);
			}

		}
	};

	/**
	 * Detect and decode QRCode from preview frame using zxing-lib
	 * 
	 * @param data
	 * @param iWidth
	 * @param iHeight
	 */
	private void decodeQRcodeHandler(byte[] data, int iWidth, int iHeight) {
		LuminanceSource source = new PlanarYUVLuminanceSource(data, iWidth,
				iHeight, 0, 0, iWidth, iHeight, false);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		MultiFormatReader mMultiFormatReader = new MultiFormatReader();
		Result result;

		try {
			try {
				result = mMultiFormatReader.decode(bitmap, null);
				if (result != null) {
					previewing = false;
					mCamera.setPreviewCallback(null);
					mCamera.stopPreview();
					Log.d(TAG, "barcode result: " + result.getText());
					redLine.setAnimation(null);
					redLine.setVisibility(View.INVISIBLE);
					showDialog(result.getText());
				}
			} catch (com.google.zxing.NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/** show alert dialog */
	private void showDialog(String msg) {
		AlertDialog.Builder mBuilder = new Builder(QRCodeActivity.this);
		mBuilder.setTitle("result");
		mBuilder.setMessage(msg);
		mBuilder.setCancelable(false);
		mBuilder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						previewing = true;
						mCamera.setPreviewCallback(mPreviewCB);
						mCamera.startPreview();
						mCamera.autoFocus(autoFocusCB);
						dialog.dismiss();
						redLine.setVisibility(View.VISIBLE);
						redLine.startAnimation(mFadeAnim);
					}
				});
		mPlayer.start();
		mBuilder.create();
		mBuilder.show();
	}

}
