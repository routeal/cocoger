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

    void remove(String id) {
        for (ComboMarker marker : mMarkers) {
            if (marker.contains(id)) {
                boolean removed = marker.removeUser(id);
                if (removed) {
                    mMarkers.remove(marker);
                }
            }
        }
    }

    // add a new user or friend
    void add(String id, String name, String picture, Location location, Address address, int range) {
        if (address != null) {
            Log.d(TAG, "add: address = " + Utils.getAddressLine(address));
        } else {
            Log.d(TAG, "add: address empty");
        }

        for (ComboMarker marker : mMarkers) {
            if (marker.contains(id)) {
                //Log.d(TAG, "add: " + id + " update the location and address");
                marker.setPosition(location, address, range);
                return;
            }
            Location rangeLocation = Utils.getRangedLocation(location, address, range);
            if (rangeLocation.distanceTo(marker.getLocation()) < mMarkerDistance) {
                //Log.d(TAG, "add: combined " + id);
                marker.addUser(id, name, picture, location, address, range);
                return;
            }
        }

        //Log.d(TAG, "add: create a new marker " + id);
        ComboMarker m = new ComboMarker(id, name, picture, location, address, range);
        mMarkers.add(m);
    }

    // reposition the marker owned by the key
    void reposition(String key, Location location, Address address, int range) {
        //Log.d(TAG, "reposition: " + key);

        Location rangeLocation = null;

        // remove the marker from the current joined one
        for (Iterator<ComboMarker> ite = mMarkers.iterator(); ite.hasNext(); ) {
            ComboMarker marker = ite.next();
            // found the current marker
            if (marker.contains(key)) {
                // simply change the position when theere is only one in the marker
                if (marker.size() == 1) {
                    marker.setPosition(location, address, range);
                    return;
                } else {
                    // too short to reposition, no need to change at all
                    if (rangeLocation == null) {
                        rangeLocation = Utils.getRangedLocation(location, address, range);
                    }
                    if (rangeLocation.distanceTo(marker.getLocation()) < mMarkerDistance) {
                        return;
                    }
                    //Log.d(TAG, "reposition: remove from the current marker");
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

        if (rangeLocation == null) {
            rangeLocation = Utils.getRangedLocation(location, address, range);
        }

        // find the nearest marker and join
        for (ComboMarker marker : mMarkers) {
            if (rangeLocation.distanceTo(marker.getLocation()) < mMarkerDistance) {
                //Log.d(TAG, "reposition: join to the marker");
                marker.addUser(key, name, picture, location, address, range);
                return;
            }
        }

        //Log.d(TAG, "reposition: add a marker for " + key);
        // add a new marker to map
        mMarkers.add(new ComboMarker(key, name, picture, location, address, range));
    }

    // apart users from one marker when the distance between them is
    // bigger than the current marker distance
    private void zoomIn() {
        //Log.d(TAG, "zoomIn");
        Map<String, ComboMarker.MarkerInfo> aparted = new HashMap<>();

        ComboMarker[] markers = mMarkers.toArray(new ComboMarker[0]);
        for (int i = 0; i < markers.length; i++) {
            ComboMarker m = markers[i];
            //Log.d(TAG, "zoomIn: apart=" + i + " for " + m.mOwner.id + " size=" + m.size());
            m.apart(aparted);
        }

        if (!aparted.isEmpty()) {
            for (Map.Entry<String, ComboMarker.MarkerInfo> entry : aparted.entrySet()) {
                ComboMarker.MarkerInfo info = entry.getValue();
                add(info.id, info.name, info.picture, info.location, info.address, info.range);
            }
        }
    }

    // combine the markers when the distance is smaller than the current marker distance
    private void zoomOut() {
        if (mMarkers.size() <= 1) return;

        ComboMarker[] markers = mMarkers.toArray(new ComboMarker[0]);

        // initial marker
        ComboMarker p = markers[0];

        for (int i = 1; i < markers.length; i++) {
            ComboMarker n = markers[i];
            Location pl = p.getLocation();
            Location nl = n.getLocation();
            if (pl.distanceTo(nl) < mMarkerDistance) {
                //Log.d(TAG, "zoom out: removed and added to the other");
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
            Location rangeLocation;
            Address address;
            int range;
        }

        private Map<String, MarkerInfo> mInfoMap = new HashMap<>();
        private Marker mMarker;
        private MarkerInfo mOwner;
        private LoadImage.LoadMarkerImage mImageTask;

        ComboMarker(String id, String name, String picture, Location location, Address address, int range) {
            //Log.d(TAG, "ComboMarker: new " + id);

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
            mMarker = mMap.addMarker(options);

            getPicture();
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
        void apart(Map<String, MarkerInfo> aparted) {
            //Log.d(TAG, "apart: " + mOwner.id);
            // no need to apart
            if (mInfoMap.size() == 1) {
                //Log.d(TAG, "NOP apart: no need to apart - only one");
                return;
            }
            for(Iterator<Map.Entry<String, MarkerInfo>> it = mInfoMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, MarkerInfo> entry = it.next();
                MarkerInfo info = entry.getValue();
                // owner should not leave
                if (mOwner == info) continue;
                // remove from this marker and put into the list argument
                if (mOwner.rangeLocation.distanceTo(info.rangeLocation) > mMarkerDistance) {
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
                if (mInfoMap.size() == 1) {
                    Object v[] = mInfoMap.values().toArray();
                    MarkerInfo markerInfo = (MarkerInfo) v[0];
                    Bundle args = new Bundle();
                    args.putString("uid", markerInfo.id);
                    args.putString("name", markerInfo.name);
                    args.putParcelable("location", markerInfo.location);
                    args.putParcelable("address", markerInfo.address);
                    args.putParcelable("rangeLocation", markerInfo.rangeLocation);
                    args.putInt("range", markerInfo.range);
                    OneInfoFragment oneInfo = new OneInfoFragment();
                    oneInfo.setArguments(args);
                    InfoWindow infoWindow = new InfoWindow(mMarker, mMarkerOffset, oneInfo);
                    mInfoWindowManager.toggle(infoWindow);
                } else if (mInfoMap.size() > 1) {
                    //Log.d(TAG, "many user's info window");
                    Bundle args = new Bundle();
                    args.putParcelable("location", mOwner.location);
                    args.putParcelable("address", mOwner.address);
                    args.putParcelable("rangeLocation", mOwner.rangeLocation);
                    args.putInt("range", mOwner.range);
                    MultiInfoFragment multiInfo = new MultiInfoFragment();
                    multiInfo.setArguments(args);
                    InfoWindow infoWindow = new InfoWindow(mMarker, mMarkerOffset, multiInfo);
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

    private GoogleMap.OnCameraMoveListener mCameraMoveListener =
            new GoogleMap.OnCameraMoveListener() {
                @Override
                public void onCameraMove() {
                    CameraPosition cameraPosition = mMap.getCameraPosition();
                    float oldDistance = mMarkerDistance;
                    ////Log.d(TAG, "Zoom: " + cameraPosition.zoom + " old:" + oldDistance);
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
                    } else if (cameraPosition.zoom > 7) {
                        mMarkerDistance = 1000000;
                    } else if (cameraPosition.zoom > 6) {
                        mMarkerDistance = 100000000;
                    } else if (cameraPosition.zoom > 4) {
                        mMarkerDistance = 2100000000;
                    } else if (cameraPosition.zoom > 2) {
                        mMarkerDistance = 2100000000;
                    }
                    if (mMarkerDistance == oldDistance) {
                        return;
                    }
                    if (mMarkerDistance < oldDistance) {
                        Log.d(TAG, "zoomIn:" + cameraPosition.zoom + " distance=" + mMarkerDistance);
                        zoomIn();
                    } else {
                        zoomOut();
                        Log.d(TAG, "zoomOut:" + cameraPosition.zoom + " distance=" + mMarkerDistance);
                    }
                }
            };

}
