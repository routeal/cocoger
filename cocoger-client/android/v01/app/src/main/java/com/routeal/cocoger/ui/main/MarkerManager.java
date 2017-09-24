package com.routeal.cocoger.ui.main;

import android.location.Address;
import android.location.Location;
import android.util.Log;

import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class MarkerManager {

    private final static String TAG = "MarkerManager";

    private List<ComboMarker> mMarkers = new ArrayList<>();

    private InfoWindowManager mInfoWindowManager;

    private GoogleMap mMap;

    private double mMarkerDistance = 10;

    MarkerManager(GoogleMap map, InfoWindowManager infoWindowManager) {
        mMap = map;
        mMap.setOnMarkerClickListener(mMarkerClickListener);
        mMap.setOnCameraMoveListener(mCameraMoveListener);

        mInfoWindowManager = infoWindowManager;
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
        if (range == 0) return;

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
            if (Utils.distanceTo(rangeLocation, marker.getLocation()) < mMarkerDistance) {
                //Log.d(TAG, "add: combined " + id);
                marker.addUser(id, name, picture, location, address, range);
                return;
            }
        }

        //Log.d(TAG, "add: create a new marker " + id);
        ComboMarker m = new ComboMarker(mMap, mInfoWindowManager,
                id, name, picture, location, address, range);
        mMarkers.add(m);
    }

    // reposition the marker owned by the key
    void reposition(String key, Location location, Address address, int range) {
        Log.d(TAG, "reposition: " + key);

        Location rangeLocation = null;
        if (range > 0) {
            // this is the new position for the key
            rangeLocation = Utils.getRangedLocation(location, address, range);
        }

        // remove the marker from the current joined one
        for (Iterator<ComboMarker> ite = mMarkers.iterator(); ite.hasNext(); ) {
            ComboMarker marker = ite.next();
            // found the current marker
            if (marker.contains(key)) {
                if (range == 0) { // 0 means none
                    boolean removed = marker.removeUser(key);
                    // remove from the map
                    if (removed) {
                        ite.remove();
                    }
                    return;
                }
                // simply change the position when theere is only one in the marker
                else if (marker.size() == 1) {
                    Log.d(TAG, "reposition: reposition");
                    if (Utils.distanceTo(rangeLocation, location) < mMarkerDistance) {
                        Log.d(TAG, "reposition: join to another");
                        marker.removeUser(key);
                        ite.remove();
                        break;
                    } else {
                        Log.d(TAG, "reposition: simply move");
                        marker.setPosition(location, address, range);
                        return;
                    }
                } else {
                    // too short to reposition, no need to change at all
                    if (Utils.distanceTo(rangeLocation, marker.getLocation()) < mMarkerDistance) {
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

        // sometimes, try to reposition with range=0 multiple times
        if (range == 0) return;

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

        // should not happen
        if (name == null || picture == null) return;

        // find the nearest marker and join
        for (ComboMarker marker : mMarkers) {
            if (Utils.distanceTo(rangeLocation, marker.getLocation()) < mMarkerDistance) {
                Log.d(TAG, "reposition: join to the marker");
                marker.addUser(key, name, picture, location, address, range);
                return;
            }
        }

        Log.d(TAG, "reposition: add a marker for " + key);
        // add a new marker to map
/*
        mMarkers.add(new ComboMarker(mMap, mInfoWindowManager,
                key, name, picture, location, address, range));
*/
        add(key, name, picture, location, address, range);
    }

    void reposition(String key, int range) {
        Log.d(TAG, "reposition range: " + key);

        ComboMarker.MarkerInfo info = null;

        // remove the marker from the current joined one
        for (Iterator<ComboMarker> ite = mMarkers.iterator(); ite.hasNext(); ) {
            ComboMarker marker = ite.next();
            // found the current marker
            if (marker.contains(key)) {
                info = marker.getMakerInfo(key);
                info.range = range;
                if (range == 0) {
                    // remove from the current marker
                    boolean removed = marker.removeUser(key);
                    // remove from the map
                    if (removed) {
                        Log.d(TAG, "reposition range: also remove the current marker from the map");
                        ite.remove();
                    }
                    return;
                }
                // simply change the position when there is only one in the marker
                else if (marker.size() == 1) {
                    Log.d(TAG, "reposition range: only one marker - remove and reposition");
                    marker.removeUser(key);
                    ite.remove();
                    break;//return;
                } else {
                    // remove from the current marker
                    boolean removed = marker.removeUser(key);
                    // remove from the map
                    if (removed) {
                        Log.d(TAG, "reposition range: also remove the current marker from the map");
                        ite.remove();
                    } else {
                        Log.d(TAG, "reposition range: remove from the current marker - " + info.name);
                    }
                    break;
                }
            }
        }

        if (info == null) return;

        Location rangeLocation = Utils.getRangedLocation(info.location, info.address, range);

        // find the nearest marker and join
        for (ComboMarker marker : mMarkers) {
            Log.d(TAG, "reposition range: find the nearest to join");
            if (Utils.distanceTo(rangeLocation, marker.getLocation()) < mMarkerDistance) {
                Log.d(TAG, "reposition range: join to the marker");
                marker.addUser(info);
                return;
            }
        }

        Log.d(TAG, "reposition range: add a marker for " + key);
        // add a new marker to map
        mMarkers.add(new ComboMarker(mMap, mInfoWindowManager,
                key, info.name, info.picture, info.location, info.address, range));
    }

    // apart users from one marker when the distance between them is
    // bigger than the current marker distance
    private void zoomIn() {
        Log.d(TAG, "zoomIn");
        Map<String, ComboMarker.MarkerInfo> aparted = new HashMap<>();

        for (Iterator<ComboMarker> ite = mMarkers.iterator(); ite.hasNext(); ) {
            ComboMarker m = ite.next();
            Log.d(TAG, "zoomIn: apart for " + m.getOwner().id + " size=" + m.size());
            m.apart(aparted, mMarkerDistance);
        }

        /*
        ComboMarker[] markers = mMarkers.toArray(new ComboMarker[0]);
        for (int i = 0; i < markers.length; i++) {
            ComboMarker m = markers[i];
            Log.d(TAG, "zoomIn: apart=" + i + " for " + m.getOwner().id + " size=" + m.size());
            m.apart(aparted, mMarkerDistance);
        }
        */

        if (!aparted.isEmpty()) {
            for (Map.Entry<String, ComboMarker.MarkerInfo> entry : aparted.entrySet()) {
                ComboMarker.MarkerInfo info = entry.getValue();
                add(info.id, info.name, info.picture, info.location, info.address, info.range);
            }
        }
    }

    private boolean combineMarkers(ComboMarker n, ComboMarker p) {
        if (n == p) return false;
        Location pl = p.getLocation();
        Location nl = n.getLocation();
        double distance = Utils.distanceTo(pl, nl);
        Log.d(TAG, "zoom out: p=" + Utils.getAddressLine(p.getOwner().address));
        Log.d(TAG, "zoom out: n=" + Utils.getAddressLine(n.getOwner().address));
        Log.d(TAG, "zoom out: distance= " + distance + " max distance=" + mMarkerDistance);
        if (distance < mMarkerDistance) {
            Log.d(TAG, "zoom out: removed and added to the other");
            n.copy(p);
            p.remove();
            return true;
        }
        return false;
    }

    // combineMarkers the markers when the distance is smaller than the current marker distance
    private void zoomOut() {
        if (mMarkers.size() <= 1) return;

        ComboMarker[] markers = mMarkers.toArray(new ComboMarker[0]);

        ComboMarker p = markers[0];

        // compare two markers one by one from 0 to n
        Log.d(TAG, "zoom out: compare from 0 to n");
        for (int i = 1; i < markers.length; i++) {
            ComboMarker n = markers[i];
            if (combineMarkers(n, p)) {
                markers[i - 1] = null;
                mMarkers.remove(p);
            }
            p = n;
        }

        // compare the first and last ones
        if (markers.length > 2) {
            int i = markers.length - 1;
            int j = 0;

            Log.d(TAG, "zoom out: compare first and last");

            // find the first one
            for (; j < markers.length; j++) {
                if (markers[j] != null) break;
            }

            if (i != j && markers[j] != null && markers[i] != null) {
                // 2nd arg will be removed
                if (combineMarkers(markers[j], markers[i])) {
                    mMarkers.remove(markers[i]);
                }
            }
        }
    }

    private GoogleMap.OnCameraMoveListener mCameraMoveListener =
            new GoogleMap.OnCameraMoveListener() {
                @Override
                public void onCameraMove() {
                    CameraPosition cameraPosition = mMap.getCameraPosition();
                    double oldDistance = mMarkerDistance;
                    ////Log.d(TAG, "Zoom: " + cameraPosition.zoom + " old:" + oldDistance);
                    if (cameraPosition.zoom > 20) {
                        mMarkerDistance = 0;
                    } else if (cameraPosition.zoom > 18) {
                        mMarkerDistance = 10;
                    } else if (cameraPosition.zoom > 16) {
                        mMarkerDistance = 50;
                    } else if (cameraPosition.zoom > 15) {
                        mMarkerDistance = 100;
                    } else if (cameraPosition.zoom > 14) {
                        mMarkerDistance = 300;
                    } else if (cameraPosition.zoom > 12) {
                        mMarkerDistance = 1000;
                    } else if (cameraPosition.zoom > 11) {
                        mMarkerDistance = 2000; // 1km
                    } else if (cameraPosition.zoom > 10) {
                        mMarkerDistance = 5000; // 1km
                    } else if (cameraPosition.zoom > 8) {
                        mMarkerDistance = 10000; // 10km
                    } else if (cameraPosition.zoom > 7) {
                        mMarkerDistance = 50000; // 10
                    } else if (cameraPosition.zoom > 6) {
                        mMarkerDistance = 100000;
                    } else if (cameraPosition.zoom > 4) {
                        mMarkerDistance = 200000;
                    } else if (cameraPosition.zoom > 3) {
                        mMarkerDistance = 300000;
                    } else if (cameraPosition.zoom > 2) {
                        mMarkerDistance = 500000;
                    } else if (cameraPosition.zoom > 1) {
                        mMarkerDistance = 1000000;
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
