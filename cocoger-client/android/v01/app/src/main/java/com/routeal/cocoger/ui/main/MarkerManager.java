package com.routeal.cocoger.ui.main;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.util.LoadImage;
import com.routeal.cocoger.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class MarkerManager {

    private final static String TAG = "MarkerManager";

    private List<ComboMarker> mMarkers = new ArrayList<>();

    private InfoWindow.MarkerSpecification mMarkerOffset;

    private InfoWindowManager mInfoWindowManager;

    private GoogleMap mMap;

    private float mMarkerDistance = 10;

    MarkerManager(GoogleMap map, InfoWindowManager infoWindowManager) {
        mMap = map;
        mMap.setOnMarkerClickListener(mMarkerClickListener);
        mMap.setOnCameraMoveListener(mCameraMoveListener);

        mInfoWindowManager = infoWindowManager;

        // init the InfoWindow
        Context context = MainApplication.getContext();
        int offsetX = (int) context.getResources().getDimension(R.dimen.marker_offset_x);
        int offsetY = (int) context.getResources().getDimension(R.dimen.marker_offset_y);
        mMarkerOffset = new InfoWindow.MarkerSpecification(offsetX, offsetY);
    }

    GoogleMap.OnMarkerClickListener mMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            for (ComboMarker entry : mMarkers) {
                if (entry.onMarkerClick(marker)) {
                    return true;
                }
            }
            return false;
        }
    };

    // add a new user or friend
    synchronized void add(String id, String name, String picture, Location location, Address address) {
        for (ComboMarker marker : mMarkers) {
            if (marker.contains(id)) {
                Log.d(TAG, "add: " + id + " update the location and address");
                marker.setPosition(location);
                marker.setAddress(address);
                return;
            }
            if (location.distanceTo(marker.getLocation()) < mMarkerDistance) {
                Log.d(TAG, "add: combined " + id);
                marker.addUser(id, name, picture, location, address);
                return;
            }
        }

        Log.d(TAG, "add: create a new marker " + id);
        ComboMarker m = new ComboMarker(id, name, picture, location, address);
        mMarkers.add(m);
    }

    // reposition the marker owned by the key
    synchronized void reposition(String key, Location location, Address address) {
        Log.d(TAG, "reposition: " + key);

        // remove the marker from the current joined one
        for (Iterator<ComboMarker> ite = mMarkers.iterator(); ite.hasNext(); ) {
            ComboMarker marker = ite.next();
            // found the current marker
            if (marker.contains(key)) {
                // simply change the position when theere is only one in the marker
                if (marker.size() == 1) {
                    marker.setPosition(location);
                    return;
                } else {
                    // too short to reposition, no need to change at all
                    if (location.distanceTo(marker.getLocation()) < mMarkerDistance) {
                        return;
                    }
                    Log.d(TAG, "reposition: remove from the current marker");
                    // remove from the current marker
                    boolean removed = marker.removeUser(key);
                    // remove from the map
                    if (removed) {
                        ite.remove();
                    }
                    break;
                }
            }
        }

        User user = MainApplication.getUser();
        String name = null;
        String picture = null;

        if (key.equals(FB.getUid())) {
            name = user.getDisplayName();
            picture = user.getPicture();
        } else {
            if (user.getFriends() != null) {
                Friend friend = user.getFriends().get(key);
                if (friend != null) {
                    name = friend.getDisplayName();
                    picture = friend.getPicture();
                }
            }
        }

        if (name == null || picture == null) return;

        // find the nearest marker and join
        for (ComboMarker marker : mMarkers) {
            if (location.distanceTo(marker.getLocation()) < mMarkerDistance) {
                Log.d(TAG, "reposition: join to the marker");
                marker.addUser(key, name, picture, location, address);
                return;
            }
        }

        Log.d(TAG, "reposition: add a marker for " + key);
        // add a new marker to map
        mMarkers.add(new ComboMarker(key, name, picture, location, address));
    }

    // apart users from one marker when the distance between them is
    // bigger than the current marker distance
    private synchronized void zoomIn() {
        Log.d(TAG, "zoomIn");
        Map<String, ComboMarker.MarkerInfo> aparted = new HashMap<>();

        ComboMarker[] markers = mMarkers.toArray(new ComboMarker[0]);
        for (int i = 0; i < markers.length; i++) {
            ComboMarker m = markers[i];
            Log.d(TAG, "zoomIn: apart=" + i + " for " + m.mOwner.id + " size=" + m.size());
            m.apart(aparted);
        }

        if (!aparted.isEmpty()) {
            for (Map.Entry<String, ComboMarker.MarkerInfo> entry : aparted.entrySet()) {
                ComboMarker.MarkerInfo info = entry.getValue();
                add(info.id, info.name, info.picture, info.location, info.address);
            }
        }
    }

    // combine the markers when the distance is smaller than the current marker distance
    private synchronized void zoomOut() {
        if (mMarkers.size() <= 1) return;

        ComboMarker[] markers = mMarkers.toArray(new ComboMarker[0]);

        // initial marker
        ComboMarker p = markers[0];

        for (int i = 1; i < markers.length; i++) {
            ComboMarker n = markers[i];
            Location pl = p.getLocation();
            Location nl = n.getLocation();
            if (pl.distanceTo(nl) < mMarkerDistance) {
                Log.d(TAG, "zoom out: removed and added to the other");
                n.copy(p);
                p.remove();
                mMarkers.remove(p);
            }
            p = n;
        }
    }

    private class ComboMarker {

        class MarkerInfo {
            String id;
            String name;
            String picture;
            Location location;
            Address address;
        }

        private List<MarkerInfo> mInfoList = new ArrayList<>();
        private Marker mMarker;
        private Address mAddress;
        private Location mLocation;
        private MarkerInfo mOwner;
        private LoadImage.LoadMarkerImage mImageTask;

        ComboMarker(String id, String name, String picture, Location location, Address address) {
            Log.d(TAG, "ComboMarker: new " + id);

            MarkerInfo markerInfo = new MarkerInfo();
            markerInfo.id = id;
            markerInfo.name = name;
            markerInfo.picture = picture;
            markerInfo.location = location;
            markerInfo.address = address;
            mInfoList.add(markerInfo);

            // initial owner should be the one who constructs the object
            mOwner = markerInfo;
            mLocation = location;
            MarkerOptions options = new MarkerOptions().position(Utils.getLatLng(location));
            mMarker = mMap.addMarker(options);

            getPicture();
        }

        int size() {
            return mInfoList.size();
        }

        synchronized boolean contains(String id) {
            for (MarkerInfo info : mInfoList) {
                if (info.id.equals(id)) {
                    return true;
                }
            }
            return false;
        }

        synchronized boolean removeUser(String id) {
            // if there is only one in the marker, just remove the marker
            if (mInfoList.size() == 1 && mOwner.id.equals(id)) {
                Log.d(TAG, "removeUser: " + id + " removed the marker");
                mMarker.remove();
                return true;
            } else {
                Log.d(TAG, "removeUser: remove=" + id + " size=" + mInfoList.size() +
                        " owner=" + mOwner.id);
                // remove it from the list
                for (int i = 0; i < mInfoList.size(); i++) {
                    MarkerInfo info = mInfoList.get(i);
                    if (info.id.equals(id)) {
                        Log.d(TAG, "removeUser: " + id + " removed");
                        mInfoList.remove(i);
                        break;
                    }
                }

                // replace the owner when the owner is removed
                if (mOwner.id.equals(id)) {
                    Log.d(TAG, "removeUser: " + id + " replace the owner");
                    MarkerInfo newOwner = mInfoList.get(0);
                    mOwner = newOwner;
                    mLocation = newOwner.location;
                    mAddress = newOwner.address;
                    mMarker.setPosition(Utils.getLatLng(mLocation));
                }

                getPicture();
            }
            return false;
        }

        // simply remove this from the map
        synchronized void remove() {
            if (mImageTask != null) {
                mImageTask.cancel(true);
                mImageTask = null;
            }
            Log.d(TAG, "remove: from the map " + mOwner.id);
            mMarker.remove();
        }

        // copy all users in the argument
        synchronized void copy(ComboMarker m) {
            Log.d(TAG, "copy: all children from " + m.mOwner.id);
            for (MarkerInfo info : m.mInfoList) {
                addUser(info.id, info.name, info.picture, info.location, info.address);
            }
        }

        // apart the users in the marker when the distance is longer
        synchronized void apart(Map<String, MarkerInfo> aparted) {
            Log.d(TAG, "apart: " + mOwner.id);
            // no need to apart
            if (mInfoList.size() == 1) {
                Log.d(TAG, "NOP apart: no need to apart - only one");
                return;
            }
            for (Iterator<MarkerInfo> iterator = mInfoList.iterator(); iterator.hasNext(); ) {
                MarkerInfo info = iterator.next();
                // owner should not leave
                if (mOwner == info) continue;
                // remove from this marker and put into the list argument
                if (mOwner.location.distanceTo(info.location) > mMarkerDistance) {
                    Log.d(TAG, "apart: removed and added " + info.id + " size=" + mInfoList.size());
                    iterator.remove();
                    Log.d(TAG, "apart: removed and added after size=" + mInfoList.size());
                    getPicture();
                    aparted.put(info.id, info);
                }
            }
        }

        synchronized void addUser(String id, String name, String picture,
                                  Location location, Address address) {
            boolean hasInfo = contains(id);
            if (hasInfo) return;

            MarkerInfo markerInfo = new MarkerInfo();
            markerInfo.id = id;
            markerInfo.name = name;
            markerInfo.picture = picture;
            markerInfo.location = location;
            markerInfo.address = address;
            mInfoList.add(markerInfo);

            Log.d(TAG, "addUsr: " + id);
            getPicture();
        }

        synchronized void getPicture() {
            if (mMarker == null) return;
            if (mInfoList.isEmpty()) return;
            String[] pictures = new String[mInfoList.size()];
            int i = 0;
            for (MarkerInfo markerInfo : mInfoList) {
                pictures[i++] = markerInfo.picture;
            }
            if (mImageTask != null) {
                mImageTask.cancel(true);
                mImageTask = null;
            }
            Log.d(TAG, "getPicture: owner=" + mOwner.id);
            mImageTask = new LoadImage.LoadMarkerImage(mMarker);
            mImageTask.execute(pictures);
        }

        boolean onMarkerClick(Marker marker) {
            if (marker.getId().compareTo(mMarker.getId()) == 0) {
                if (mInfoList.size() == 1) {
                    MarkerInfo markerInfo = mInfoList.get(0);
                    Bundle args = new Bundle();
                    args.putString("id", markerInfo.id);
                    args.putString("name", markerInfo.name);
                    args.putParcelable("location", mLocation);
                    args.putParcelable("address", mAddress);
                    SingleInfoFragment sif = new SingleInfoFragment();
                    sif.setArguments(args);
                    InfoWindow infoWindow = new InfoWindow(mMarker, mMarkerOffset, sif);
                    mInfoWindowManager.toggle(infoWindow);
                } else if (mInfoList.size() > 1) {
                    Log.d(TAG, "many user's info window");
                }
                return true;
            }
            return false;
        }

        void setPosition(Location location) {
            if (mMarker != null) {
                mMarker.setPosition(Utils.getLatLng(location));
            }
        }

        void setAddress(Address address) {
            mAddress = address;
        }

        Location getLocation() {
            return mLocation;
        }
    }

    private GoogleMap.OnCameraMoveListener mCameraMoveListener =
            new GoogleMap.OnCameraMoveListener() {
                @Override
                public void onCameraMove() {
                    CameraPosition cameraPosition = mMap.getCameraPosition();
                    float oldDistance = mMarkerDistance;
                    //Log.d(TAG, "Zoom: " + cameraPosition.zoom + " old:" + oldDistance);
                    if (cameraPosition.zoom > 20) {
                        mMarkerDistance = 0;
                    } else if (cameraPosition.zoom > 18) {
                        mMarkerDistance = 10;
                    } else if (cameraPosition.zoom > 16) {
                        mMarkerDistance = 100;
                    } else if (cameraPosition.zoom > 14) {
                        mMarkerDistance = 300;
                    } else if (cameraPosition.zoom > 12) {
                        mMarkerDistance = 500;
                    } else if (cameraPosition.zoom > 10) {
                        mMarkerDistance = 1000;
                    } else if (cameraPosition.zoom > 8) {
                        mMarkerDistance = 10000;
                    } else if (cameraPosition.zoom > 6) {
                        mMarkerDistance = 100000;
                    } else if (cameraPosition.zoom > 4) {
                        mMarkerDistance = 1000000;
                    } else if (cameraPosition.zoom > 2) {
                        mMarkerDistance = 10000000;
                    }
                    if (mMarkerDistance == oldDistance) {
                        return;
                    }
                    if (mMarkerDistance < oldDistance) {
                        Log.d(TAG, "zoomIn:" + mMarkerDistance);
                        zoomIn();
                    } else {
                        zoomOut();
                        Log.d(TAG, "zoomOut:" + mMarkerDistance);
                    }
                }
            };

}
