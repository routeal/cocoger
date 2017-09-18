package com.routeal.cocoger.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.service.MainService;
import com.routeal.cocoger.ui.main.AccountActivity;
import com.routeal.cocoger.ui.main.PanelMapActivity;
import com.routeal.cocoger.ui.main.TimelineActivity;

/**
 * Created by nabe on 6/11/17.
 */

public class SplashActivity extends AppCompatActivity {
    private final static String TAG = "SplashActivity";

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent;

        // ensure that the service is started
        MainService.start(getApplicationContext());

        if (FB.isAuthenticated()) {
             intent = new Intent(getApplicationContext(), PanelMapActivity.class);
        } else {
            intent = new Intent(getApplicationContext(), LoginActivity.class);
        }

        startActivity(intent);

        finish();
    }
}
