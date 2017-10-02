package com.routeal.cocoger;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.routeal.cocoger.model.User;

import okhttp3.OkHttpClient;

/**
 * Created by nabe on 6/11/17.
 */

public class MainApplication extends Application {

    private SharedPreferences mPreferences;

    private static Context mContext;

    private static MainApplication mInstance;

    private static String mAppVersion = "0.01";

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
    }

    public static Context getContext() {
        return MainApplication.mContext;
    }

    public static Application getInstance() {
        return mInstance;
    }

    public static String getApplicationName() {
        return mInstance.getString(R.string.app_name);
    }

    public static String getApplicationVersion() {
        return mAppVersion;
    }

    public static void putString(String key, String value) {
        mInstance.mPreferences.edit().putString(key, value).apply();
    }

    public static String getString(String key) {
        return mInstance.mPreferences.getString(key, null);
    }

    public static void putInt(String key, int value) {
        mInstance.mPreferences.edit().putInt(key, value).apply();
    }

    public static int getInt(String key, int value) {
        return mInstance.mPreferences.getInt(key, value);
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
