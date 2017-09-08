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
import java.util.Iterator;
import java.util.List;

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

    // add a new user or friend
    void add(String id, String name, String picture, Location location, Address address) {
        for (ComboMarker marker : mMarkers) {
            if (marker.contains(id)) {
                Log.d(TAG, "add(): update the location and address");
                marker.setPosition(location);
                marker.setAddress(address);
                return;
            }
            if (location.distanceTo(marker.getLocation()) < mMarkerDistance) {
                Log.d(TAG, "add(): combine");
                marker.addUser(id, name, picture, location, address);
                return;
            }
        }

        Log.d(TAG, "add(): create a new marker");
        ComboMarker m = new ComboMarker(id, name, picture, location, address);
        mMarkers.add(m);
    }

    // reposition the marker owned by the key
    void reposition(String key, Location location, Address address) {
        Log.d(TAG, "reposition");

        // remove the marker from the current joined one
        for (ComboMarker marker : mMarkers) {
            // found the current marker
            if (marker.contains(key)) {
                // too short to reposition, no need to change at all
                if (location.distanceTo(marker.getLocation()) < mMarkerDistance) {
                    return;
                }
                // remove from the current marker
                boolean removed = marker.removeUser(key);
                // remove from the map
                if (removed) {
                    mMarkers.remove(key);
                }
                break;
            }
        }

        User user = MainApplication.getUser();
        String name;
        String picture;

        if (key.equals(FB.getUid())) {
            name = user.getDisplayName();
            picture = user.getPicture();
        } else {
            Friend friend = user.getFriends().get(key);
            name = friend.getDisplayName();
            picture = friend.getPicture();
        }

        // find the nearest marker and join
        for (ComboMarker marker : mMarkers) {
            if (location.distanceTo(marker.getLocation()) < mMarkerDistance) {
                Log.d(TAG, "pic added");
                marker.addUser(key, name, picture, location, address);
                return;
            }
        }

        // add a new marker to map
        mMarkers.add(new ComboMarker(key, name, picture, location, address));
    }

    // apart users from one marker when the distance between them is
    // bigger than the current marker distance
    private void zoomIn() {
        ComboMarker [] markers = mMarkers.toArray(new ComboMarker[0]);

        for (int i = 0; i < markers.length; i++) {
            ComboMarker m = markers[i];
            if (m.size() == 1) continue;
            MarkerInfo [] infos = m.mInfoList.toArray(new MarkerInfo[0]);
            // find the owner
            int ownerId = 0;
            for (int j = 0; j < infos.length; j++) {
                if (infos[j].id.equals(m.owner())) {
                    ownerId = j;
                    break;
                }
            }
            // the owner will not be removed from the marker
            MarkerInfo p = infos[ownerId];
            for (int j = 0; j < infos.length; j++) {
                if (j == ownerId) continue;
                MarkerInfo n = infos[j];
                if (p.location.distanceTo(n.location) > mMarkerDistance) {
                    Log.d(TAG, "zoom In: " + n.id + " removed and added a new marker");
                    m.removeUser(n.id);
                    mMarkers.add(new ComboMarker(n.id, n.name, n.picture, n.location, n.address));
                }
            }
        }
    }

    // combine the markers when the distance is smaller than the current marker distance
    private void zoomOut() {
        if (mMarkers.size() <= 1) return;

        ComboMarker [] markers = mMarkers.toArray(new ComboMarker[0]);

        // initial marker
        ComboMarker p = markers[0];

        for (int i = 1; i < markers.length; i++) {
            ComboMarker n = markers[i];
            Location pl = p.getLocation();
            Location nl = n.getLocation();
            if (pl.distanceTo(nl) < mMarkerDistance) {
                for (int j =0; j < p.mInfoList.size(); j++) {
                    MarkerInfo mi = p.mInfoList.get(j);
                    n.addUser(mi.id, mi.name, mi.picture, mi.location, mi.address);
                }
                p.remove();
            }
            p = n;
        }
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

    class MarkerInfo {
        String id;
        String name;
        String picture;
        Location location;
        Address address;
    }

    private class ComboMarker {

        private List<MarkerInfo> mInfoList = new ArrayList<>();
        private Marker mMarker;
        private LoadImage.LoadMarkerImage mImageTask;
        private Address mAddress;
        private Location mLocation;
        private String mOwner;

        ComboMarker(String id, String name, String picture, Location location, Address address) {
            MarkerInfo markerInfo = new MarkerInfo();
            markerInfo.id = id;
            markerInfo.name = name;
            markerInfo.picture = picture;
            markerInfo.location = location;
            markerInfo.address = address;
            mInfoList.add(markerInfo);

            mOwner = id;
            mLocation = location;
            MarkerOptions options = new MarkerOptions().position(Utils.getLatLng(location));
            mMarker = mMap.addMarker(options);

            getPicture();
        }

        String owner() {
            return mOwner;
        }

        int size() {
            return mInfoList.size();
        }

        boolean contains(String id) {
            for (Iterator<MarkerInfo> it = mInfoList.iterator(); it.hasNext(); ) {
                MarkerInfo info = it.next();
                if (info.id.equals(id)) {
                    return true;
                }
            }
            return false;
        }

        boolean removeUser(String id) {
            for (Iterator<MarkerInfo> it = mInfoList.iterator(); it.hasNext(); ) {
                MarkerInfo info = it.next();
                if (info.id.equals(id)) {
                    it.remove();
                    break;
                }
            }
            if (mInfoList.size() > 0) {
                if (mOwner.equals(id)) {
                    MarkerInfo newOwer = mInfoList.get(0);
                    mOwner = newOwer.id;
                    mLocation = newOwer.location;
                    mMarker.setPosition(Utils.getLatLng(mLocation));
                }
                getPicture();
            } else {
                mMarker.remove();
                return true;
            }
            return false;
        }

        void remove() {
            if (mImageTask != null) {
                mImageTask.cancel(true);
                mImageTask = null;
            }
            mMarker.remove();
        }

        void addUser(String id, String name, String picture, Location location, Address address) {
            boolean hasInfo = contains(id);
            if (hasInfo) return;

            MarkerInfo markerInfo = new MarkerInfo();
            markerInfo.id = id;
            markerInfo.name = name;
            markerInfo.picture = picture;
            markerInfo.location = location;
            markerInfo.address = address;
            mInfoList.add(markerInfo);

            Log.d(TAG, "pic should be redraw");
            getPicture();
        }

        void getPicture() {
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
            Log.d(TAG, "Running LoadMarkerImage");
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
            /*
            if (mMarker == null) {
                return null;
            }
            return Utils.getLocation(mMarker.getPosition());
            */
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
