package com.routeal.cocoger.ui.main;

import android.graphics.drawable.Drawable;
import android.location.Address;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.util.LoadMarkerImage;
import com.routeal.cocoger.util.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class ComboMarker {
    private final static String TAG = "ComboMarker";

    private Map<String, MarkerInfo> mInfoMap = new HashMap<>();
    private Marker mMarker;
    private MarkerInfo mOwner;
    private LoadMarkerImage mImageTask;
    private GoogleMap mMap;
    private InfoWindowManager mInfoWindowManager;
    private InfoWindow mInfoWindow;

    ComboMarker(GoogleMap map, InfoWindowManager infoWindowManager,
                String id, String name, LatLng location, Address address, int range) {
        Log.d(TAG, "ComboMarker: new " + id + " for " + name);
        mMap = map;
        mInfoWindowManager = infoWindowManager;

        MarkerInfo markerInfo = new MarkerInfo();
        markerInfo.id = id;
        markerInfo.name = name;
        markerInfo.location = location;
        markerInfo.address = address;
        markerInfo.range = range;
        markerInfo.rangeLocation = Utils.getRangedLocation(location, address, range);
        mInfoMap.put(id, markerInfo);

        // initial owner should be the one who constructs the object
        mOwner = markerInfo;
        MarkerOptions options = new MarkerOptions().position(markerInfo.rangeLocation);
        options.anchor(0.5f, 0.5f);
        mMarker = mMap.addMarker(options);

        // set the default icon
        Drawable d = Utils.getIconDrawable(MainApplication.getContext(),
                R.drawable.ic_face_black_48dp, R.color.indigo_500);
        BitmapDescriptor icon = Utils.getBitmapDescriptor(d);
        mMarker.setIcon(icon);

        retrieveMarkerImage();
    }

    Map<String, MarkerInfo> getInfo() {
        return mInfoMap;
    }

    MarkerInfo getOwner() {
        return mOwner;
    }

    MarkerInfo getInfo(String key) {
        return mInfoMap.get(key);
    }

    int size() {
        return mInfoMap.size();
    }

    boolean contains(String id) {
        return mInfoMap.containsKey(id);
    }

    boolean removeUser(String id) {
        // if there is only one in the marker, just remove the marker
        if (mInfoMap.size() == 1) {
            Log.d(TAG, "removeUser: " + id + " removed the marker");
            remove();
            return true;
        }

        Log.d(TAG, "removeUser: remove=" + id + " size=" + mInfoMap.size() +
                " owner=" + mOwner.id);

        // remove it from the list
        mInfoMap.remove(id);

        // replace the owner when the owner is removed
        if (mOwner.id.equals(id)) {
            Log.d(TAG, "removeUser: " + id + " replace the owner");
            for (Map.Entry<String, MarkerInfo> entry : mInfoMap.entrySet()) {
                MarkerInfo info = entry.getValue();
                if (!info.id.equals(id)) {
                    mOwner = info;
                    mMarker.setPosition(mOwner.rangeLocation);
                    break;
                }
            }
        }

        retrieveMarkerImage();

        return false;
    }

    // simply remove this from the map
    void remove() {
        if (mImageTask != null) {
            mImageTask.cancel();
            mImageTask = null;
        }
        Log.d(TAG, "remove: from the map " + mOwner.id + " : " + mOwner.name);
        mMarker.remove();
        mMarker = null;
        if (mInfoWindow != null) {
            Fragment fragment = mInfoWindow.getWindowFragment();
            FragmentManager fragmentManager = fragment.getFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction trans = fragmentManager.beginTransaction();
                trans.remove(fragment);
                trans.commit();
            }
            mInfoWindow = null;
        }
        mInfoMap.clear();
    }

    // copy all users in the argument
    void copy(ComboMarker m) {
        Log.d(TAG, "copy: all children from " + m.mOwner.id);
        for (MarkerInfo info : m.mInfoMap.values()) {
            if (!contains(info.id)) {
                mInfoMap.put(info.id, info);
                retrieveMarkerImage();
            }
        }
    }

    // apart the users in the marker when the distance is longer
    void apart(Map<String, MarkerInfo> aparted, double minDistance) {
        //Log.d(TAG, "apart: " + mOwner.key);
        // no need to apart
        if (mInfoMap.size() == 1) {
            Log.d(TAG, "apart: no need to apart - only one");
            return;
        }

        for (Iterator<Map.Entry<String, MarkerInfo>> it = mInfoMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, MarkerInfo> entry = it.next();
            MarkerInfo info = entry.getValue();
            // owner should not leave
            if (mOwner == info) continue;
            // remove from this marker and put into the list argument
            if (Utils.distanceTo(mOwner.rangeLocation, info.rangeLocation) > minDistance) {
                Log.d(TAG, "apart: removed and added " + info.name + " size=" + mInfoMap.size());
                it.remove();
                //Log.d(TAG, "apart: removed and added after size=" + mInfoList.size());
                aparted.put(info.id, info);
            }
        }

        retrieveMarkerImage();
    }

    void addUser(String id, String name, LatLng location, Address address, int range, LatLng rangeLocation) {
        if (contains(id)) return;
        MarkerInfo info = new MarkerInfo();
        info.id = id;
        info.name = name;
        info.location = location;
        info.address = address;
        info.range = range;
        info.rangeLocation = rangeLocation;
        mInfoMap.put(info.id, info);
        Log.d(TAG, "addUsr: " + info.id + " for " + name);
        retrieveMarkerImage();
    }

    private void retrieveMarkerImage() {
        if (mMarker == null) {
            return;
        }
        if (mInfoMap.isEmpty()) {
            return;
        }

        // cancel the previous task if any
        if (mImageTask != null) {
            mImageTask.cancel();
            mImageTask = null;
        }

        Set<String> keys = mInfoMap.keySet();
        String[] ids = keys.toArray(new String[0]);

        mImageTask = new LoadMarkerImage(mMarker);
        mImageTask.load(ids);
    }

    boolean onMarkerClick(Marker marker) {
        if (marker.getId().equals(mMarker.getId())) {
            show();
            return true;
        }
        return false;
    }

    void show() {
        InfoFragment infoFragment = null;
        if (mInfoMap.size() == 1) {
            OneInfoFragment o = new OneInfoFragment();
            o.setMarker(this);
            infoFragment = o;
        } else if (mInfoMap.size() > 1) {
            MultiInfoFragment m = new MultiInfoFragment();
            m.setMarker(this);
            infoFragment = m;
        }
        if (infoFragment != null) {
            InfoWindow.MarkerSpecification markerOffset = new InfoWindow.MarkerSpecification(5, 20);
            mInfoWindow = new InfoWindow(mMarker, markerOffset, infoFragment);
            mInfoWindowManager.setHideOnFling(true);
            mInfoWindowManager.show(mInfoWindow, true);
        }
    }

    void hide() {
        if (mInfoWindow != null) {
            mInfoWindowManager.hide(mInfoWindow, true);
        }
    }

    LatLng getLocation() {
        return mOwner.rangeLocation;
    }

    class MarkerInfo {
        String id;
        String name;
        LatLng location;
        LatLng rangeLocation;
        Address address;
        int range;
    }

}
