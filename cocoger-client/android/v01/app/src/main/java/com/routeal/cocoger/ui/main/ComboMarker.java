package com.routeal.cocoger.ui.main;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.util.LoadMarkerImage;
import com.routeal.cocoger.util.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class ComboMarker {
    private final static String TAG = "ComboMarker";

    private static InfoWindow.MarkerSpecification mMarkerOffset;

    static {
        // init the InfoWindow
        Context context = MainApplication.getContext();
        int offsetX = (int) context.getResources().getDimension(R.dimen.marker_offset_x);
        int offsetY = (int) context.getResources().getDimension(R.dimen.marker_offset_y);
        mMarkerOffset = new InfoWindow.MarkerSpecification(offsetX, offsetY);
    }

    private Map<String, MarkerInfo> mInfoMap = new HashMap<>();
    private Marker mMarker;
    private MarkerInfo mOwner;
    private LoadMarkerImage mImageTask;
    private GoogleMap mMap;
    private InfoWindowManager mInfoWindowManager;
    private InfoWindow mInfoWindow;

    ComboMarker(GoogleMap map, InfoWindowManager infoWindowManager,
                String id, String name, Location location, Address address, int range) {
        Log.d(TAG, "ComboMarker: new " + id);
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
        MarkerOptions options = new MarkerOptions().position(Utils.getLatLng(markerInfo.rangeLocation));
        options.anchor(0.5f, 0.5f);
        mMarker = mMap.addMarker(options);

        Drawable d = Utils.getIconDrawable(MainApplication.getContext(),
                R.drawable.ic_face_black_48dp, R.color.steelblue);
        BitmapDescriptor icon = Utils.getBitmapDescriptor(d);

        mMarker.setIcon(icon);

        getPicture();
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
            mMarker.remove();
            return true;
        }

        Log.d(TAG, "removeUser: remove=" + id + " size=" + mInfoMap.size() +
                " owner=" + mOwner.id);

        // remove it from the list
        mInfoMap.remove(id);

        // replace the owner when the owner is removed
        if (mOwner.id.equals(id)) {
            Log.d(TAG, "removeUser: " + id + " replace the owner");
            for (Iterator<Map.Entry<String, MarkerInfo>> it = mInfoMap.entrySet().iterator();
                 it.hasNext(); ) {
                Map.Entry<String, MarkerInfo> entry = it.next();
                MarkerInfo info = entry.getValue();
                if (!info.id.equals(id)) {
                    mOwner = info;
                    mMarker.setPosition(Utils.getLatLng(mOwner.rangeLocation));
                    break;
                }
            }
        }

        getPicture();

        return false;
    }

    // simply remove this from the map
    void remove() {
        if (mImageTask != null) {
            mImageTask.cancel();
            mImageTask = null;
        }
        //Log.d(TAG, "remove: from the map " + mOwner.key);
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
    }

    // copy all users in the argument
    void copy(ComboMarker m) {
        Log.d(TAG, "copy: all children from " + m.mOwner.id);
        for (Object value : m.mInfoMap.values()) {
            MarkerInfo info = (MarkerInfo) value;
            addUser(info);
        }
    }

    // apart the users in the marker when the distance is longer
    void apart(Map<String, MarkerInfo> aparted, double minDistance) {
        //Log.d(TAG, "apart: " + mOwner.key);
        // no need to apart
        if (mInfoMap.size() == 1) {
            //Log.d(TAG, "NOP apart: no need to apart - only one");
            return;
        }
        for (Iterator<Map.Entry<String, MarkerInfo>> it = mInfoMap.entrySet().iterator();
             it.hasNext(); ) {
            Map.Entry<String, MarkerInfo> entry = it.next();
            MarkerInfo info = entry.getValue();
            // owner should not leave
            if (mOwner == info) continue;
            // remove from this marker and put into the list argument
            if (Utils.distanceTo(mOwner.rangeLocation, info.rangeLocation) > minDistance) {
                //Log.d(TAG, "apart: removed and added " + info.key + " size=" + mInfoList.size());
                it.remove();
                //Log.d(TAG, "apart: removed and added after size=" + mInfoList.size());
                getPicture();
                aparted.put(info.id, info);
            }
        }
    }

    void addUser(MarkerInfo info) {
        boolean hasInfo = contains(info.id);
        if (hasInfo) return;
        mInfoMap.put(info.id, info);
        Log.d(TAG, "addUsr: " + info.id);
        getPicture();
    }

    void addUser(String id, String name, Location location, Address address, int range) {
        boolean hasInfo = contains(id);
        if (hasInfo) return;

        MarkerInfo markerInfo = new MarkerInfo();
        markerInfo.id = id;
        markerInfo.name = name;
        markerInfo.location = location;
        markerInfo.address = address;
        markerInfo.range = range;
        markerInfo.rangeLocation = Utils.getRangedLocation(location, address, range);
        mInfoMap.put(id, markerInfo);

        //Log.d(TAG, "addUsr: " + key);
        getPicture();
    }

    void getPicture() {
        if (mMarker == null) {
            return;
        }
        if (mInfoMap.isEmpty()) {
            return;
        }
        String[] uids = new String[mInfoMap.size()];
        int i = 0;
        for (Object value : mInfoMap.values()) {
            MarkerInfo info = (MarkerInfo) value;
            uids[i++] = info.id;
        }
        //Log.d(TAG, "getPicture: owner=" + mOwner.key);
        mImageTask = new LoadMarkerImage(mMarker);
        mImageTask.load(uids);
    }

    boolean onMarkerClick(Marker marker) {
        if (marker.getId().compareTo(mMarker.getId()) == 0) {
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
            mInfoWindow = new InfoWindow(mMarker, mMarkerOffset, infoFragment);
            mInfoWindowManager.setHideOnFling(true);
            mInfoWindowManager.toggle(mInfoWindow, true);
        }
    }

    void hide() {
        if (mInfoWindow != null) {
            mInfoWindowManager.hide(mInfoWindow, true);
        }
    }

    void setPosition(Location location, Address address, int range) {
        if (mMarker != null) {
            mOwner.location = location;
            mOwner.address = address;
            mOwner.range = range;
            mOwner.rangeLocation = Utils.getRangedLocation(location, address, range);
            mMarker.setPosition(Utils.getLatLng(mOwner.rangeLocation));
        }
    }

    Location getLocation() {
        return mOwner.rangeLocation;
    }

    class MarkerInfo {
        String id;
        String name;
        Location location;
        Location rangeLocation;
        Address address;
        int range;
    }

}
