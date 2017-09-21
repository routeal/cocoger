package com.routeal.cocoger;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.JsonReader;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.routeal.cocoger.model.Device;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.net.RestClient;
import com.routeal.cocoger.util.Utils;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

/**
 * Created by nabe on 6/11/17.
 */

public class MainApplication extends Application {

    private final static String LOGIN_EMAIL = "login_email";

    private final static String JSON_FILENAME = "cocoger.json";

    private final static String LOCATION_PERMISSION = "locationPermission";

    private SharedPreferences mPreferences;

    private static Context mContext;

    private static MainApplication mInstance;

    private static RestClient mRestClient;

    private static String mAppVersion = "0.01";

    private static User mUser;

    @Override
    public void onCreate() {
        super.onCreate();

        // DEBUG
        Stetho.initializeWithDefaults(this);

        new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();

        mInstance = this;

        mContext = getApplicationContext();

        mPreferences = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);

        String server_url = "";
        boolean enable_server_debug = false;

        try {
            JsonReader reader =
                    new JsonReader(new InputStreamReader(getAssets().open(JSON_FILENAME)));
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("server_url")) {
                    server_url = reader.nextString();
                } if (name.equals("enable_server_debug")) {
                    enable_server_debug = reader.nextBoolean();
                } if (name.equals("app_version")) {
                    mAppVersion = reader.nextString();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        } catch (Exception e) {
        }

        if (server_url.isEmpty()) {
            Toast.makeText(mContext, "Empty Server URL", Toast.LENGTH_SHORT).show();
        }

        mRestClient = new RestClient(server_url, true /*enable_server_debug*/);
    }

    public static Context getContext() {
        return MainApplication.mContext;
    }

    public static Application getInstance() { return mInstance; }

    public static String getApplicationName() {
        return mInstance.getString(R.string.app_name);
    }

    public static String getApplicationVersion() {
        return mAppVersion;
    }

    public static RestClient getRestClient() { return mRestClient; }

    /*
    public static boolean isLocationPermitted() {
        return mInstance.mPreferences.getBoolean(LOCATION_PERMISSION, false);
    }

    public static void permitLocation(boolean permit) {
        mInstance.mPreferences.edit().putBoolean(LOCATION_PERMISSION, permit).apply();
    }
    */

    /*
    public static void setLoginEmail(String value) {
        mInstance.mPreferences.edit().putString(LOGIN_EMAIL, value).apply();
    }

    public static String getLoginEmail() {
        return mInstance.mPreferences.getString(LOGIN_EMAIL, null);
    }
*/

    public static User getUser() {
        return mUser;
    }

    public static void setUser(User user) {
        mUser = user;
    }

    public static void putString(String key, String value) {
        mInstance.mPreferences.edit().putString(key, value).apply();
    }

    public static String getString(String key) {
        return mInstance.mPreferences.getString(key, null);
    }

    /*
    public static String getString(String key, String value) {
        return mInstance.mPreferences.getString(key, value);
    }

    public static void putInt(String key, int value) {
        mInstance.mPreferences.edit().putInt(key, value).apply();
    }

    public static int getInt(String key) {
        return mInstance.mPreferences.getInt(key, 0);
    }

    public static int getInt(String key, int value) {
        return mInstance.mPreferences.getInt(key, value);
    }

    public static void putLong(String key, long value) {
        mInstance.mPreferences.edit().putLong(key, value).apply();
    }

    public static long getLong(String key) {
        return mInstance.mPreferences.getLong(key, 0);
    }

    public static long getLong(String key, long value) {
        return mInstance.mPreferences.getLong(key, value);
    }
    */

    public static void putBool(String key, boolean value) {
        mInstance.mPreferences.edit().putBoolean(key, value).apply();
    }

    public static boolean getBool(String key) {
        return mInstance.mPreferences.getBoolean(key, false);
    }

    public static boolean getBool(String key, boolean value) {
        return mInstance.mPreferences.getBoolean(key, value);
    }
}
