package com.example.lankasmartmart;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lankasmartmart.auth.AuthManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AuthManager authManager = AuthManager.getInstance(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (authManager.isUserLoggedIn()) {
                // User is already logged in, go to MainActivity
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // User is not logged in, go to LoginActivity
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 2500); // 2.5 seconds delay to match branding
    }
}
