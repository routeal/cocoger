package com.routeal.cocoger.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.service.LocationService;
import com.routeal.cocoger.ui.main.SlidingUpPanelMapActivity;

/**
 * Created by nabe on 6/11/17.
 */

public class SplashActivity extends AppCompatActivity {
    private final static String TAG = "SplashActivity";

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ensure that the service is started
        new LocationService().startResident(MainApplication.getContext());

        // set it in the foreground mode
        LocationService.setForegroundMode();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        Intent intent = null;
        if (mAuth.getCurrentUser() != null) {
            intent = new Intent(getApplicationContext(), SlidingUpPanelMapActivity.class);
        } else {
            if (MainApplication.getLoginEmail() != null) {
                intent = new Intent(getApplicationContext(), LoginActivity.class);
            } else {
                intent = new Intent(getApplicationContext(), SignupActivity.class);
            }
        }

        startActivity(intent);
        finish();
    }
}
