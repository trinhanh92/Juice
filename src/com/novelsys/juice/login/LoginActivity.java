package com.novelsys.juice.login;

import java.util.Arrays;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.UserInfoChangedCallback;
import com.novelsys.juice.ChargingActivity;
import com.novelsys.juice.R;
import com.novelsys.juice.maps.MapsActivity;
import com.novelsys.juice.qrcode.QRCodeActivity;

import android.os.Bundle;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LoginActivity extends FragmentActivity {
	;
	private UiLifecycleHelper uiHelper;

	// facebook login button
	private LoginButton btnFaceLogin;
	private Button btnSignIn;
	private Button btnSignUp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(this, statusCallback);
		uiHelper.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		btnFaceLogin = (LoginButton) findViewById(R.id.authButton);
		btnSignIn = (Button) findViewById(R.id.btnSignIn);
		btnSignUp = (Button) findViewById(R.id.btnSignUp);

		// handle button facebook login
		btnFaceLogin.setReadPermissions(Arrays.asList("email"));
		btnFaceLogin.setUserInfoChangedCallback(new UserInfoChangedCallback() {

			@Override
			public void onUserInfoFetched(GraphUser user) {
				if (user != null) {
					Log.i("LoginActivity",
							"you are logged in as " + user.getName());
					Log.i("LoginActivity",
							"you are logged in as ID " + user.getId());
					startActivity(new Intent(LoginActivity.this,
							MapsActivity.class));
					finish();
				} else {
					Log.i("LoginActivity", "you are not logged in");
				}

			}
		});

		// handle button sign in
		btnSignIn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginActivity.this,
						SignInActivity.class));
			}
		});

		// handle button sign up
		btnSignUp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginActivity.this,
						SignUpActivity.class));
			}
		});

	}

	private Session.StatusCallback statusCallback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			if (state.isOpened()) {
				Log.d("LoginActivity", "Facebook session opened.");
				Log.i("LoginActivity",
						" access token key: " + session.getAccessToken());
			} else if (state.isClosed()) {
				Log.d("LoginActivity", "Facebook session closed.");
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onSaveInstanceState(Bundle savedState) {
		super.onSaveInstanceState(savedState);
		uiHelper.onSaveInstanceState(savedState);
	}

}
