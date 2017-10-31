package com.routeal.cocoger.manager;

import com.routeal.cocoger.model.Place;

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
    }

    public static void change(String key, Place place) {
        mPlaceList.put(key, place);
        if (mUpdateListener != null) {
            mUpdateListener.onChanged(key, place);
        }
    }

    public static void remove(String key) {
        mPlaceList.remove(key);
        if (mUpdateListener != null) {
            mUpdateListener.onRemoved(key);
        }
    }

    public static void setUpdateListener(UpdateListener<Place> listener) {
        mUpdateListener = listener;
    }

}
