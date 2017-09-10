package com.routeal.cocoger.ui.main;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.util.LoadImage;
import com.routeal.cocoger.util.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class ComboMarker implements Parcelable {

    private static InfoWindow.MarkerSpecification mMarkerOffset;

    static {
        // init the InfoWindow
        Context context = MainApplication.getContext();
        int offsetX = (int) context.getResources().getDimension(R.dimen.marker_offset_x);
        int offsetY = (int) context.getResources().getDimension(R.dimen.marker_offset_y);
        mMarkerOffset = new InfoWindow.MarkerSpecification(offsetX, offsetY);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

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
        mMarker = map.addMarker(options);

        getPicture();
    }

    Map<String, MarkerInfo> getInfo() {
        return mInfoMap;
    }

    MarkerInfo getOwner() {
        return mOwner;
    }

    int size() {
        return mInfoMap.size();
    }

    boolean contains(String id) {
        return mInfoMap.containsKey(id);
    }

    boolean removeUser(String id) {
        // if there is only one in the marker, just remove the marker
        if (mInfoMap.size() == 1 && mOwner.id.equals(id)) {
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
                MarkerInfo newOwner = mInfoMap.get(0);
                mOwner = newOwner;
                mMarker.setPosition(Utils.getLatLng(newOwner.rangeLocation));
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
        //Log.d(TAG, "copy: all children from " + m.mOwner.id);
        for (Object value : mInfoMap.values()) {
            MarkerInfo info = (MarkerInfo) value;
            addUser(info.id, info.name, info.picture, info.location, info.address, info.range);
        }
    }

    // apart the users in the marker when the distance is longer
    void apart(Map<String, MarkerInfo> aparted, float minDistance) {
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
            if (mOwner.rangeLocation.distanceTo(info.rangeLocation) > minDistance) {
                //Log.d(TAG, "apart: removed and added " + info.id + " size=" + mInfoList.size());
                it.remove();
                //Log.d(TAG, "apart: removed and added after size=" + mInfoList.size());
                getPicture();
                aparted.put(info.id, info);
            }
        }
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
                InfoWindow infoWindow = new InfoWindow(mMarker, mMarkerOffset, infoFragment);
                mInfoWindowManager.toggle(infoWindow);
            }
            return true;
        }
        return false;
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
