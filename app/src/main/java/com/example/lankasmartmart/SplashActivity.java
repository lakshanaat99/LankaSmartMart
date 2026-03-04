package com.example.lankasmartmart;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import com.example.lankasmartmart.auth.AuthManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This is the new Android 12+ way to show a splash screen while the app loads
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Get our AuthManager to check if we know this user
        AuthManager authManager = AuthManager.getInstance(this);

        Intent intent;
        // If they are logged in, take them straight to the Main app screen
        if (authManager.isUserLoggedIn()) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            // Otherwise, they need to log in first!
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        startActivity(intent);

        // Close the splash screen so they can't go back to it
        finish();
    }
}
