package com.routeal.cocoger.ui.main;

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

    private static RecyclerAdapterListener<Place> mRecyclerAdapterListener;

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
        if (mRecyclerAdapterListener != null) {
            mRecyclerAdapterListener.onAdded(key, place);
        }
    }

    public static void change(String key, Place place) {
        mPlaceList.put(key, place);
        if (mRecyclerAdapterListener != null) {
            mRecyclerAdapterListener.onChanged(key, place);
        }
    }

    public static void remove(String key) {
        mPlaceList.remove(key);
        if (mRecyclerAdapterListener != null) {
            mRecyclerAdapterListener.onRemoved(key);
        }
    }

    static void setRecyclerAdapterListener(RecyclerAdapterListener<Place> listener) {
        mRecyclerAdapterListener = listener;
    }

}
