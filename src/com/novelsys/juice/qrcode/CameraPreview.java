package com.novelsys.juice.qrcode;

import java.io.IOException;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {
	private static final String TAG = "CameraPreview";
	private SurfaceHolder mSurfHolder;
	private Camera mCamera;
	private AutoFocusCallback autoFocusCB;
	private PreviewCallback prevCB;
	public CameraPreview(Context context, Camera camera,
			AutoFocusCallback autoFocusCB, PreviewCallback prevCB) {
		super(context);
		mCamera = camera;
		this.autoFocusCB = autoFocusCB;
		this.prevCB = prevCB;
		// install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mSurfHolder = getHolder();
		mSurfHolder.addCallback(this);

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// the surface has been created, now tell the camera where to draw the
		// preview

		Log.d(TAG, "surfaceCreated");
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
		} catch (IOException e) {
			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(TAG, "surfaceChanged");
		// if your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.
		if (mSurfHolder.getSurface() == null) {
			// preview surface not exist
			return;
		}

		// stop the preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		// set preview size and make any size, rotate or reformatting changes
		// here.
		Camera.Parameters params = mCamera.getParameters();
		params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//		CameraConfiguration.setFocusArea(params);
		mCamera.setParameters(params);
		// start the preview with new settings.
		try {
			mCamera.setPreviewDisplay(mSurfHolder);
			mCamera.setPreviewCallback(prevCB);
			mCamera.startPreview();
			mCamera.autoFocus(autoFocusCB);
		} catch (Exception e) {
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// empty. take care of releasing the camera preview in activity
		Log.d(TAG, "surfaceDestroyed");
	}

}
