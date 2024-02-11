package com.example.irisbeats;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.content.Intent;

import androidx.core.content.ContextCompat;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.splash_background));
        }
        // Find the view that will show the circle
//        View circleView = findViewById(R.id.circle_view);

        // Load the animation from the anim resource
//        Animation anim = AnimationUtils.loadAnimation(this, R.anim.circle_expand);
//        circleView.startAnimation(anim);

        // New Handler to start the next activity (e.g., MainActivity)
        // and close this SplashActivity after some seconds.
        // Duration of the splash screen display in milliseconds
        int SPLASH_DISPLAY_LENGTH = 3000;
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                // Create an intent that will start the main activity.
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish(); // Destroy the current activity
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
