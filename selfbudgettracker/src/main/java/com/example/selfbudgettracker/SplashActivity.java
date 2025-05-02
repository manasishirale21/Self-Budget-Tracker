package com.example.selfbudgettracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Find the root view or a specific view you want to animate
        LinearLayout splashLayout = findViewById(R.id.splash_layout);

        // Load the fade-in animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Start the animation on the root layout
        splashLayout.startAnimation(fadeIn);

        // Delay to show SplashActivity before launching MainActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Open MainActivity after splash screen
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close SplashActivity so the user can't go back to it
            }
        }, 3000); // 3-second delay
    }
}
