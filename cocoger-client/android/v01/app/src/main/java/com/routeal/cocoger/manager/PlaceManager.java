package com.routeal.cocoger.manager;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Place;
import com.routeal.cocoger.provider.DBUtil;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by hwatanabe on 10/8/17.
 */

public class PlaceManager {
    private final static String TAG = "PlaceManager";

    // this is maintained by the place database event listener, so even if the ui is closed,
    // the list will be kept
    private static SortedMap<String, Place> mPlaceList = new TreeMap<>();

    private static UpdateListener<Place> mUpdateListener;

    public static Place getPlace(String key) {
        if (key != null && !key.isEmpty()) {
            return mPlaceList.get(key);
        }
        return null;
    }

    public static SortedMap<String, Place> getPlaces() {
        return mPlaceList;
    }

    public static void add(String key, Place place) {
        mPlaceList.put(key, place);
        if (mUpdateListener != null) {
            mUpdateListener.onAdded(key, place);
        }
        Intent intent = new Intent(FB.PLACE_ADD);
        intent.putExtra(FB.KEY, key);
        intent.putExtra(FB.PLACE, place);
        LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
    }

    public static void change(String key, Place place) {
        mPlaceList.put(key, place);
        String name = place.getUid() + "_" + key + "_" + FB.PLACE_IMAGE;
        DBUtil.deleteImage(name);
        if (mUpdateListener != null) {
            mUpdateListener.onChanged(key, place);
        }
        Intent intent = new Intent(FB.PLACE_CHANGE);
        intent.putExtra(FB.KEY, key);
        intent.putExtra(FB.PLACE, place);
        LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
    }

    public static void remove(String key) {
        mPlaceList.remove(key);
        if (mUpdateListener != null) {
            mUpdateListener.onRemoved(key);
        }
        Intent intent = new Intent(FB.PLACE_REMOVE);
        intent.putExtra(FB.KEY, key);
        LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
    }

    public static void setUpdateListener(UpdateListener<Place> listener) {
        mUpdateListener = listener;
    }

}
