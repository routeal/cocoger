package com.routeal.cocoger.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.service.LocationService;
import com.routeal.cocoger.ui.main.SlidingPanelSearchMapsActivity;

/**
 * Created by nabe on 6/11/17.
 */

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ensure that the service is started
        new LocationService().startResident(MainApplication.getContext());

        startMain();
        finish();

        /*

        String token = MainApplication.getString("token", "");

        if (token.isEmpty()) {
            startLogin();
            return;
        }

        Call<User> login = MainApplication.getRestClient().getService().me(token);

        login.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    startMain();
                } else {
                    startLogin();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                startLogin();
            }
        });
*/
    }

    private void startMain() {
        //Intent intent = new Intent(SplashActivity.this, SlidingPanelSearchMapsActivity.class);
        Intent intent = new Intent(SplashActivity.this, FacebookLoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void startLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
