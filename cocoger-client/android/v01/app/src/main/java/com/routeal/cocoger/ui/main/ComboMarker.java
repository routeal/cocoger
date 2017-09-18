package com.routeal.cocoger.ui.main;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.util.LoadImage;
import com.routeal.cocoger.util.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

class ComboMarker implements Parcelable {
    private final static String TAG = "ComboMarker";

    private static InfoWindow.MarkerSpecification mMarkerOffset;

    static {
        // init the InfoWindow
        Context context = MainApplication.getContext();
        int offsetX = (int) context.getResources().getDimension(R.dimen.marker_offset_x);
        int offsetY = (int) context.getResources().getDimension(R.dimen.marker_offset_y);
        mMarkerOffset = new InfoWindow.MarkerSpecification(offsetX, offsetY);
    }

    class MarkerInfo {
        String id;
        String name;
        String picture;
        Location location;
        Location rangeLocation;
        Address address;
        int range;
    }

    private Map<String, MarkerInfo> mInfoMap = new HashMap<>();
    private Marker mMarker;
    private MarkerInfo mOwner;
    private LoadImage.LoadMarkerImage mImageTask;
    private InfoWindowManager mInfoWindowManager;

    private int[] colors = {
            R.color.teal300,
            R.color.red,
            R.color.steelblue
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    ComboMarker(GoogleMap map, InfoWindowManager infoWindowManager, String id, String name, String picture, Location location, Address address, int range) {
        //Log.d(TAG, "ComboMarker: new " + id);

        mInfoWindowManager = infoWindowManager;

        MarkerInfo markerInfo = new MarkerInfo();
        markerInfo.id = id;
        markerInfo.name = name;
        markerInfo.picture = picture;
        markerInfo.location = location;
        markerInfo.address = address;
        markerInfo.range = range;
        markerInfo.rangeLocation = Utils.getRangedLocation(location, address, range);
        mInfoMap.put(id, markerInfo);

        // initial owner should be the one who constructs the object
        mOwner = markerInfo;
        MarkerOptions options = new MarkerOptions().position(Utils.getLatLng(markerInfo.rangeLocation));
        options.anchor(0.5f, 0.5f);
        mMarker = map.addMarker(options);

        int color = Utils.randInt(0, colors.length-1);
        Drawable d = Utils.getIconDrawable(MainApplication.getContext(), R.drawable.ic_face_black_48dp, colors[color]);
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

    MarkerInfo getMakerInfo(String key) {
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
            //Log.d(TAG, "removeUser: " + id + " removed the marker");
            mMarker.remove();
            return true;
        } else {
            //Log.d(TAG, "removeUser: remove=" + id + " size=" + mInfoList.size() +
            //        " owner=" + mOwner.id);
            // remove it from the list
            mInfoMap.remove(id);

            // replace the owner when the owner is removed
            if (mOwner.id.equals(id)) {
                //Log.d(TAG, "removeUser: " + id + " replace the owner");
                Iterator<Map.Entry<String, MarkerInfo>> it = mInfoMap.entrySet().iterator();
                Map.Entry<String, MarkerInfo> entry = it.next();
                mOwner = entry.getValue();
                mMarker.setPosition(Utils.getLatLng(mOwner.rangeLocation));
            }

            getPicture();
        }
        return false;
    }

    // simply remove this from the map
    void remove() {
        if (mImageTask != null) {
            mImageTask.cancel(true);
            mImageTask = null;
        }
        //Log.d(TAG, "remove: from the map " + mOwner.id);
        mMarker.remove();
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
        //Log.d(TAG, "apart: " + mOwner.id);
        // no need to apart
        if (mInfoMap.size() == 1) {
            //Log.d(TAG, "NOP apart: no need to apart - only one");
            return;
        }
        for (Iterator<Map.Entry<String, MarkerInfo>> it = mInfoMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, MarkerInfo> entry = it.next();
            MarkerInfo info = entry.getValue();
            // owner should not leave
            if (mOwner == info) continue;
            // remove from this marker and put into the list argument
            if (Utils.distanceTo(mOwner.rangeLocation, info.rangeLocation) > minDistance) {
                //Log.d(TAG, "apart: removed and added " + info.id + " size=" + mInfoList.size());
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

    void addUser(String id, String name, String picture,
                 Location location, Address address, int range) {
        boolean hasInfo = contains(id);
        if (hasInfo) return;

        MarkerInfo markerInfo = new MarkerInfo();
        markerInfo.id = id;
        markerInfo.name = name;
        markerInfo.picture = picture;
        markerInfo.location = location;
        markerInfo.address = address;
        markerInfo.range = range;
        markerInfo.rangeLocation = Utils.getRangedLocation(location, address, range);
        mInfoMap.put(id, markerInfo);

        //Log.d(TAG, "addUsr: " + id);
        getPicture();
    }

    void getPicture() {
        if (mMarker == null) return;
        if (mInfoMap.isEmpty()) return;
        String[] pictures = new String[mInfoMap.size()];
        int i = 0;
        for (Object value : mInfoMap.values()) {
            MarkerInfo info = (MarkerInfo) value;
            pictures[i++] = info.picture;
        }
        if (mImageTask != null) {
            mImageTask.cancel(true);
            mImageTask = null;
        }
        //Log.d(TAG, "getPicture: owner=" + mOwner.id);
        mImageTask = new LoadImage.LoadMarkerImage(mMarker);
        mImageTask.execute(pictures);
    }

    private InfoWindow mInfoWindow;

    boolean onMarkerClick(Marker marker) {
        if (marker.getId().compareTo(mMarker.getId()) == 0) {
            Fragment infoFragment = null;
            if (mInfoMap.size() == 1) {
                infoFragment = new OneInfoFragment();
            } else if (mInfoMap.size() > 1) {
                infoFragment = new MultiInfoFragment();
            }
            if (infoFragment != null) {
                Bundle args = new Bundle();
                args.putParcelable("marker", this);
                infoFragment.setArguments(args);
                mInfoWindow = new InfoWindow(mMarker, mMarkerOffset, infoFragment);
                mInfoWindowManager.show(mInfoWindow, true);
            }
            return true;
        }
        return false;
    }

    void hide() {
        if (mInfoWindow != null) {
            mInfoWindowManager.hide(mInfoWindow, true);
            mInfoWindow = null;
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

}
