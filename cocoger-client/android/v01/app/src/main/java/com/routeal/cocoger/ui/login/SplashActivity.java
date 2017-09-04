package com.routeal.cocoger.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.service.MainService;
import com.routeal.cocoger.ui.main.PanelMapActivity;

/**
 * Created by nabe on 6/11/17.
 */

public class SplashActivity extends AppCompatActivity {
    private final static String TAG = "SplashActivity";

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

/*
        // ensure that the service is started
        if (!MainService.instantiated) {
            new MainService().startResident(getApplicationContext());
        }
*/

        MainService.start(getApplicationContext());

        // set it in the foreground mode
        MainService.setForegroundMode();

        Intent intent;

        if (FB.isAuthenticated()) {
            intent = new Intent(getApplicationContext(), PanelMapActivity.class);
        } else {
            intent = new Intent(getApplicationContext(), LoginActivity.class);
        }

        startActivity(intent);

        finish();
    }
}
