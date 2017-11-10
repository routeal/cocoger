package com.routeal.cocoger.ui.main;

import android.location.Address;
import android.location.Location;
import android.util.Log;

import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.manager.FriendManager;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.service.LocationUpdate;
import com.routeal.cocoger.util.LocationRange;
import com.routeal.cocoger.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class UserMarkers {

    private final static String TAG = "UserMarkers";

    private List<ComboMarker> mMarkers = new ArrayList<>();
    private GoogleMap mMap;
    private InfoWindowManager mInfoWindowManager;
    private GroupMarkers mGroupMarkers;
    private double mMarkerDistance = 10;

    UserMarkers(GoogleMap map, InfoWindowManager infoWindowManager) {
        mMap = map;
        mInfoWindowManager = infoWindowManager;
    }

    void setGroupMarkers(GroupMarkers groupMarkers) {
        mGroupMarkers = groupMarkers;
    }

    void onCameraMove() {
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

    boolean onMarkerClick(Marker marker) {
        for (ComboMarker entry : mMarkers) {
            if (entry.onMarkerClick(marker)) {
                return true;
            }
        }
        return false;
    }

    // remove the user (friend)
    void remove(String uid) {
        move(uid, null, null, null, 0);
    }

    void move(String uid, String name, LatLng location, Address address, int range) {
        Log.d(TAG, "move:" + Arrays.toString(Thread.currentThread().getStackTrace()));
        LatLng rangeLocation = null;

        // remove the marker from the current joined one
        for (Iterator<ComboMarker> ite = mMarkers.iterator(); ite.hasNext(); ) {
            ComboMarker marker = ite.next();
            // found the current marker
            if (marker.contains(uid)) {
                if (range == 0) { // 0 means none
                    boolean removed = marker.removeUser(uid);
                    // remove from the map
                    if (removed) {
                        ite.remove();
                    }
                    mGroupMarkers.notifyChange(marker);
                    return;
                } else {
                    rangeLocation = Utils.getRangedLocation(location, address, range);

                    // too short to move, no need to change at all
                    if (rangeLocation != null &&
                            Utils.distanceTo(rangeLocation, marker.getLocation()) < mMarkerDistance) {
                        Log.d(TAG, "move: no need to apart from the current marker");
                        return;
                    }

                    Log.d(TAG, "move: remove from the current marker");
                    // remove from the current marker
                    boolean removed = marker.removeUser(uid);
                    // remove from the map
                    if (removed) {
                        ite.remove();
                    }
                    break;
                }
            }
        }

        if (range == 0) return;

        if (rangeLocation == null) {
            rangeLocation = Utils.getRangedLocation(location, address, range);
        }

        for (ComboMarker marker : mMarkers) {
            if (Utils.distanceTo(rangeLocation, marker.getLocation()) < mMarkerDistance) {
                Log.d(TAG, "add: combined " + uid);
                marker.addUser(uid, name, location, address, range, rangeLocation);
                mGroupMarkers.notifyChange(marker);
                return;
            }
        }

        ComboMarker marker = new ComboMarker(mMap, mInfoWindowManager, uid, name, location, address, range);
        mMarkers.add(marker);
        mGroupMarkers.notifyChange(marker);
    }

    // apart users from one marker when the distance between them is
    // bigger than the current marker distance
    private void zoomIn() {
        Log.d(TAG, "zoomIn");
        Map<String, ComboMarker.MarkerInfo> aparted = new HashMap<>();

        for (ComboMarker marker : mMarkers) {
            Log.d(TAG, "zoomIn: apart for " + marker.getOwner().id + " size=" + marker.size());
            marker.apart(aparted, mMarkerDistance);
        }

        if (!aparted.isEmpty()) {
            for (Map.Entry<String, ComboMarker.MarkerInfo> entry : aparted.entrySet()) {
                ComboMarker.MarkerInfo info = entry.getValue();
                move(info.id, info.name, info.location, info.address, info.range);
            }
        }
    }

    private boolean combineMarkers(ComboMarker n, ComboMarker p) {
        if (n == p) return false;
        LatLng pl = p.getLocation();
        LatLng nl = n.getLocation();
        double distance = Utils.distanceTo(pl, nl);
        Log.d(TAG, "combineMarkers: p=" + Utils.getAddressLine(p.getOwner().address));
        Log.d(TAG, "combineMarkers: n=" + Utils.getAddressLine(n.getOwner().address));
        Log.d(TAG, "combineMarkers: distance= " + distance + " max distance=" + mMarkerDistance);
        if (distance < mMarkerDistance) {
            Log.d(TAG, "combineMarkers: removed and added to the other");
            n.copy(p);
            p.remove();
            return true;
        } else {
            Log.d(TAG, "combineMarkers: two are too far to combine");
        }
        return false;
    }

    // combineMarkers the markers when the distance is smaller than the current marker distance
    private void zoomOut() {
        if (mMarkers.size() <= 1) return;

        Log.d(TAG, "zoomOut: compare from 0 to n");

        // convert to array to avoid concurrent modification
        ComboMarker[] markers = mMarkers.toArray(new ComboMarker[0]);

        // compare one to next each other
        ComboMarker p = markers[0];
        for (int i = 1; i < markers.length; i++) {
            ComboMarker n = markers[i];
            if (combineMarkers(n, p)) {
                mMarkers.remove(p);
            }
            p = n;
        }

        markers = mMarkers.toArray(new ComboMarker[0]);

        // compare first and last ones
        if (markers.length > 1) {
            ComboMarker f = markers[0];
            ComboMarker l = markers[markers.length - 1];
            if (combineMarkers(f, l)) {
                mMarkers.remove(l);
            }
        }
    }

    // zoom to the user (friend)
    void zoom(String uid) {
        for (ComboMarker marker : mMarkers) {
            if (marker.contains(uid)) {
                ComboMarker.MarkerInfo markerInfo = marker.getOwner();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        markerInfo.rangeLocation, mMap.getCameraPosition().zoom));
                return;
            }
        }
    }

    // update the user (friend)
    void update(String uid) {
        String name = null;
        LatLng location = null;
        Address address = null;
        int range = 0;

        // remove first
        for (Iterator<ComboMarker> ite = mMarkers.iterator(); ite.hasNext(); ) {
            ComboMarker marker = ite.next();
            if (marker.contains(uid)) {
                ComboMarker.MarkerInfo info = marker.getInfo(uid);
                location = info.location;
                address = info.address;
                range = info.range;
                boolean removed = marker.removeUser(uid);
                if (removed) {
                    ite.remove();
                }
                break;
            }
        }

        if (location == null) return; // error, not found

        // name can be also updated
        if (uid.equals(FB.getUid())) {
            name = FB.getUser().getDisplayName();
        } else {
            name = FriendManager.getFriend(uid).getDisplayName();
        }

        // add again with the new info
        move(uid, name, location, address, range);
    }

    ComboMarker get(String key) {
        for (ComboMarker marker : mMarkers) {
            if (marker.contains(key)) {
                return marker;
            }
        }
        return null;
    }

    void setup(LatLng location, Address address) {
        // run only once
        if (location == null) return;

        User user = FB.getUser();
        if (user == null) {
            Log.d(TAG, "setup: user not available");
            return;
        }

        Log.d(TAG, "setup: start processing");

        // user marker is already added
        for (ComboMarker marker : mMarkers) {
            if (marker.contains(FB.getUid())) {
                return;
            }
        }

        move(FB.getUid(), user.getDisplayName(), location, address, LocationRange.CURRENT.range);

        Map<String, Friend> friends = FriendManager.getFriends();
        if (friends.isEmpty()) {
            Log.d(TAG, "setup: empty friend");
            return;
        }

        for (Map.Entry<String, Friend> entry : FriendManager.getFriends().entrySet()) {
            final String key = entry.getKey();
            final Friend friend = entry.getValue();
            if (friend.getLocation() != null) {
                FB.getLocation(friend.getLocation(), new FB.LocationListener() {
                    @Override
                    public void onSuccess(Location location, Address address) {
                        LatLng latLng = Utils.getLatLng(location);
                        move(key, friend.getDisplayName(), latLng, address, friend.getRange());
                    }

                    @Override
                    public void onFail(String err) {
                        Log.d(TAG, "Failed to get a friend's location: " + err);
                    }
                });
            }
        }
    }
}
