package com.routeal.cocoger;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by nabe on 6/11/17.
 */

public class MainApplication extends Application {
    private SharedPreferences preferences;

    private static Context mContext;

    private static MainApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        mContext = getApplicationContext();
        preferences = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
    }

    public static Context getContext() {
        return MainApplication.mContext;
    }

    public static String getApplicationName() {
        return sInstance.getString(R.string.app_name);
    }

    public static void putString(String key, String value) {
        sInstance.preferences.edit().putString(key, value).apply();
    }

    public static String getString(String key) {
        return sInstance.preferences.getString(key, null);
    }

    public static String getString(String key, String value) {
        return sInstance.preferences.getString(key, value);
    }

    public static void putInt(String key, int value) {
        sInstance.preferences.edit().putInt(key, value).apply();
    }

    public static int getInt(String key) {
        return sInstance.preferences.getInt(key, 0);
    }

    public static int getInt(String key, int value) {
        return sInstance.preferences.getInt(key, value);
    }

    public static void putLong(String key, long value) {
        sInstance.preferences.edit().putLong(key, value).apply();
    }

    public static long getLong(String key) {
        return sInstance.preferences.getLong(key, 0);
    }

    public static long getLong(String key, long value) {
        return sInstance.preferences.getLong(key, value);
    }
}
