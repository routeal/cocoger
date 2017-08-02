package com.routeal.cocoger.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.net.RestClient;
import com.routeal.cocoger.provider.DB;
import com.routeal.cocoger.provider.DBUtil;
import com.routeal.cocoger.service.LocationService;
import com.routeal.cocoger.ui.main.SlidingUpPanelMapActivity;
import com.routeal.cocoger.util.AppVisibilityDetector;
import com.routeal.cocoger.util.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by nabe on 6/11/17.
 */

public class SplashActivity extends AppCompatActivity {
    private final static String TAG = "SplashActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ensure that the service is started
        new LocationService().startResident(MainApplication.getContext());

        // set it in the foreground mode
        LocationService.setForegroundMode();

        // set two different modes to the service either when the device is foreground or background
        AppVisibilityDetector.init(MainApplication.getInstance(), new AppVisibilityDetector.AppVisibilityCallback() {
            @Override
            public void onAppGotoForeground() {
                //app is from background to foreground
                LocationService.setForegroundMode();
            }

            @Override
            public void onAppGotoBackground() {
                //app is from foreground to background
                LocationService.setBackgroundMode();
            }
        });

        // currently not being logged in when the toke is empty, so starts the login screen
        if (RestClient.token() == null) {
            startLogin();
        } else {
            User user = DBUtil.getUser();
            user.setDevice(Utils.getDevice());

            startMain();
            return;
/*

            // login to the server in the background and starts the main(map) screen
            Call<User> login = RestClient.service().login(RestClient.token(), user);

            login.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    Log.d(TAG, "Response: " + response.body().toString());
                    User user = response.body();
                    DBUtil.deleteUser();
                    DBUtil.saveUser(user);
                    Log.d(TAG, "User: " + user.toString());
                    startMain();
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    startMain();
                }
            });
  */
        }
    }

    private void startMain() {
        Intent intent = new Intent(getApplicationContext(), SlidingUpPanelMapActivity.class);
        startActivity(intent);
        finish();
    }

    private void startLogin() {
        Intent intent = new Intent(getApplicationContext(), FacebookLoginActivity.class);
        startActivity(intent);
        finish();
    }

}
