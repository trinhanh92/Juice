/**
 * @author ANHPHAM
 *
 */
package com.novelsys.juice;

import com.novelsys.juice.login.LoginActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ViewFlipper;
import android.view.View;
import android.view.View.OnClickListener;


public class InstructionActivity extends Activity {
	private ViewFlipper vfInstruction;
	private Button btnGiveMeCharge;
	private ImageView imvPage;
    private float lastX;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instruction);
		vfInstruction = (ViewFlipper) findViewById(R.id.vfInstruction);
		btnGiveMeCharge = (Button) findViewById(R.id.btnGiveMeCharge);
		btnGiveMeCharge.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onGiveMeCharge();
			}
		});
		imvPage = (ImageView) findViewById(R.id.imvInstructionPage);
	}

    /**
     * Method to handle touch event like left to right swap and right to left swap
     */
	public boolean onTouchEvent(MotionEvent touchevent) {
    	switch (touchevent.getAction()) {
    	// when user first touches the screen to swap
        case MotionEvent.ACTION_DOWN: 
        	lastX = touchevent.getX();
            break;
        case MotionEvent.ACTION_UP: 
            float currentX = touchevent.getX();
            // if left to right swipe on screen
            if (lastX < currentX) {
            	// If no more View/Child to flip
            	int currentView = vfInstruction.getDisplayedChild();
            	if (currentView == 0) break;
            	// set the required Animation type to ViewFlipper
            	// The Next screen will come in form Left and current Screen will go OUT from Right
            	vfInstruction.setOutAnimation(this, R.anim.out_to_right);
            	vfInstruction.setInAnimation(this, R.anim.in_from_left);
                vfInstruction.setDisplayedChild(currentView - 1);
                switch (currentView - 1) {
                case 0:
                	imvPage.setImageResource(R.drawable.ipage0);
                	break;
                case 1:
                	imvPage.setImageResource(R.drawable.ipage1);
                	break;
                case 2:
                	imvPage.setImageResource(R.drawable.ipage2);
                	break;
                }                
            }
                             
            // if right to left swipe on screen
            if (lastX > currentX) {
            	int currentView = vfInstruction.getDisplayedChild();
            	if (currentView == 2) break;
                // set the required Animation type to ViewFlipper
                // The Next screen will come in form Right and current Screen will go OUT from Left
            	vfInstruction.setOutAnimation(this, R.anim.out_to_left);
                vfInstruction.setInAnimation(this, R.anim.in_from_right);
                vfInstruction.setDisplayedChild(currentView + 1);
                switch (currentView + 1) {
                case 0:
                	imvPage.setImageResource(R.drawable.ipage0);
                	break;
                case 1:
                	imvPage.setImageResource(R.drawable.ipage1);
                	break;
                case 2:
                	imvPage.setImageResource(R.drawable.ipage2);
                	break;
                }      
             }
             break;
    	}
        return false;
    }
	
	/**
	 * 
	 */
	private void onGiveMeCharge () {
		// Switch to Login Activity
		startActivity(new Intent(InstructionActivity.this, LoginActivity.class));
		finish();
	}
	
	@Override
	public void onBackPressed () {
		finish();
	}

}
