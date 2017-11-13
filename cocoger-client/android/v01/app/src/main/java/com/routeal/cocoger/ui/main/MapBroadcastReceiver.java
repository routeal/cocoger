package com.routeal.cocoger.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.manager.FriendManager;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.Group;
import com.routeal.cocoger.model.Place;
import com.routeal.cocoger.util.LocationRange;
import com.routeal.cocoger.util.Utils;

/**
 * Created by hwatanabe on 10/15/17.
 */

public class MapBroadcastReceiver extends BroadcastReceiver {
    private final static String TAG = "MapBroadcastReceiver";

    private GoogleMap mMap;
    private InfoWindowManager mInfoWindowManager;
    private GeoDataClient mGeoDataClient;
    private MapActivity mActivity;
    private MapDirection mDirection;
    private UserMarkers mUserMarkers;
    private PlaceMarkers mPlaceMarkers;
    private GroupMarkers mGroupMarkers;

    private LatLng mLocation;
    private Address mAddress;

    MapBroadcastReceiver(MapActivity activity) {
        mActivity = activity;
    }

    void setup(GoogleMap map, InfoWindowManager infoWindowManager,
               UserMarkers userMarkers, PlaceMarkers placeMarkers, GroupMarkers groupMarkers,
               MapDirection mapDirection) {
        mInfoWindowManager = infoWindowManager;
        mMap = map;
        mUserMarkers = userMarkers;
        mPlaceMarkers = placeMarkers;
        mGroupMarkers = groupMarkers;
        mDirection = mapDirection;
    }

    void register() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(FB.USER_AVAILABLE);
        mFilter.addAction(FB.USER_UPDATE);
        mFilter.addAction(FB.USER_CHANGE);
        mFilter.addAction(FB.USER_SHOW);
        mFilter.addAction(FB.USER_LOCATION);
        mFilter.addAction(FB.FRIEND_ADD);
        mFilter.addAction(FB.FRIEND_LOCATION);
        mFilter.addAction(FB.FRIEND_REMOVE);
        mFilter.addAction(FB.FRIEND_RANGE);
        mFilter.addAction(FB.FRIEND_SHOW);
        mFilter.addAction(FB.DIRECTION_ADD);
        mFilter.addAction(FB.DIRECTION_REMOVE);
        mFilter.addAction(FB.PLACE_SAVE);
        mFilter.addAction(FB.PLACE_UPDATE);
        mFilter.addAction(FB.PLACE_DELETE);
        mFilter.addAction(FB.PLACE_SHOW);
        mFilter.addAction(FB.PLACE_ADD);
        mFilter.addAction(FB.PLACE_CHANGE);
        mFilter.addAction(FB.PLACE_REMOVE);
        mFilter.addAction(FB.GROUP_ADD);
        mFilter.addAction(FB.GROUP_INVITE);
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(this, mFilter);
    }

    void unregister() {
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Action=" + intent.getAction());
        if (intent.getAction().equals(FB.USER_LOCATION)) {
            Address address = intent.getParcelableExtra(FB.ADDRESS);
            LatLng location = intent.getParcelableExtra(FB.LOCATION);
            if (mLocation == null) {
                Log.d(TAG, "Receive Last_location_update: setup");
                // try to setup the markers and save the initial location, but this may fail due to
                // the unavailability of the user.
                mActivity.saveInitialLocation();
                if (mUserMarkers != null) mUserMarkers.setup(location, address);
            } else {
                if (mUserMarkers != null) {
                    mUserMarkers.move(FB.getUid(), FB.getUser().getDisplayName(),
                            location, address, LocationRange.CURRENT.range);
                }
            }
            mLocation = location;
            mAddress = address;
        } else if (intent.getAction().equals(FB.USER_AVAILABLE)) {
            if (mUserMarkers == null) return;
            Log.d(TAG, "Receive User_available: setup");
            // in order to set up the markers and initial location upload, there must be the user
            // available.  So, if the initial location is set, this has to work.
            if (mLocation != null) {
                if (mAddress == null) {
                    mAddress = Utils.getAddress(mLocation);
                }
                mActivity.saveInitialLocation();
                mUserMarkers.setup(mLocation, mAddress);
            }
        } else if (intent.getAction().equals(FB.USER_UPDATE)) {
            if (mUserMarkers == null) return;
            mUserMarkers.update(FB.getUid());
        } else if (intent.getAction().equals(FB.USER_CHANGE)) {
            mActivity.updateMessage();
        } else if (intent.getAction().equals(FB.USER_SHOW)) {
            if (mMap == null) return;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLocation, MapActivity.DEFAULT_ZOOM));
        } else if (intent.getAction().equals(FB.FRIEND_ADD)) {
            final String fid = intent.getStringExtra(FB.KEY);
            final Friend friend = FriendManager.getFriend(fid);
            if (friend == null || friend.getLocation() == null) return;
            FB.getLocation(friend.getLocation(), new FB.LocationListener() {
                @Override
                public void onFail(String err) {
                    Log.d(TAG, "Failed to get location for added Friend: " + err);
                }

                @Override
                public void onSuccess(Location location, Address address) {
                    if (location == null || address == null) return;
                    LatLng latLng = Utils.getLatLng(location);
                    mUserMarkers.move(fid, friend.getDisplayName(), latLng, address, friend.getRange());
                }
            });
        } else if (intent.getAction().equals(FB.FRIEND_LOCATION)) {
            final String fid = intent.getStringExtra(FB.KEY);
            final String oldLocationKey = intent.getStringExtra(FB.LOCATION);
            final Friend friend = FriendManager.getFriend(fid);
            if (friend == null) return; // shouldn't happen
            FB.getLocation(friend.getLocation(), new FB.LocationListener() {
                @Override
                public void onFail(String err) {
                    Log.d(TAG, "Friend new location: " + err);
                }

                @Override
                public void onSuccess(final Location newLocation, final Address newAddress) {
                    FriendManager.setLocation(fid, newLocation, newAddress);

                    final int range = friend.getRange();
                    // for testing
                    LatLng latLng = Utils.getLatLng(newLocation);
                    mUserMarkers.move(fid, friend.getDisplayName(), latLng, newAddress, range);

                    // FIXME:  the old location should be retrieved from ComboMarker
/*
                    if (oldLocationKey == null) {
                        // move the cursor
                        mMm.move(fid, newLocation, newAddress, range);
                    } else {
                        // compare the new location with the old location to issue the range movement
                        FB.getLocation(oldLocationKey, new FB.LocationListener() {
                                @Override
                                public void onFail(String err) {
                                    Log.d(TAG, "Friend old location: " + err);
                                }

                                @Override
                                public void onSuccess(Location oldLocation, Address oldAddress) {
                                    // detect move in the range or above
                                    int moved = Utils.detectRangeMove(newAddress, oldAddress);
                                    if (moved == LocationRange.NONE.range) {
                                        return;
                                    }

                                    if (moved == LocationRange.CURRENT.range) {
                                        // move
                                        mMm.move(fid, newLocation, newAddress, range);
                                        return;
                                    }

                                    // if the address is the same as the user, send a notification
                                    if (Utils.isEqualAddress(mAddress, newAddress, range)) {
                                        // send notification
                                        int nid = Math.abs((int) friend.getCreated());
                                        String message = Utils.getRangeMoveMessage(friend, newAddress, oldAddress);
                                        Notifi.send(nid, fid, friend.getDisplayName(), message);
                                    }
                                }
                        });
                    }
  */
                }
            });
        } else if (intent.getAction().equals(FB.FRIEND_REMOVE)) {
            String fid = intent.getStringExtra(FB.KEY);
            mUserMarkers.remove(fid);
        } else if (intent.getAction().equals(FB.FRIEND_SHOW)) {
            String fid = intent.getStringExtra(FB.KEY);
            Log.d(TAG, "FRIEND_SHOW:" + fid);
            mUserMarkers.zoom(fid);
            mActivity.closeSlidePanel();
        } else if (intent.getAction().equals(FB.FRIEND_RANGE)) {
            String fid = intent.getStringExtra(FB.KEY);
            Friend friend = FriendManager.getFriend(fid);
            int range = friend.getRange();
            Log.d(TAG, "FRIEND_RANGE:" + fid);
            ComboMarker marker = mUserMarkers.get(fid);
            if (marker != null) {
                ComboMarker.MarkerInfo info = marker.getInfo(fid);
                if (info != null) {
                    info.range = range;
                    mUserMarkers.move(fid, info.name, info.location, info.address, range);
                }
            }
        } else if (intent.getAction().equals(FB.DIRECTION_ADD)) {
            LatLng location = intent.getParcelableExtra(FB.LOCATION);
            mDirection.addDirection(mActivity, location, mLocation);
            mActivity.closeSlidePanel();
        } else if (intent.getAction().equals(FB.DIRECTION_REMOVE)) {
            mDirection.removeDirection();
        } else if (intent.getAction().equals(FB.PLACE_SAVE)) {
            LatLng location = intent.getParcelableExtra(FB.LOCATION);
            String address = intent.getStringExtra(FB.ADDRESS);
            String title = intent.getStringExtra(FB.TITLE);
            Bitmap bitmap = intent.getParcelableExtra(FB.IMAGE);
            mPlaceMarkers.addPlace(title, location, address, bitmap);
        } else if (intent.getAction().equals(FB.PLACE_ADD)) {
            String key = intent.getStringExtra(FB.KEY);
            Place place = (Place) intent.getSerializableExtra(FB.PLACE);
            mPlaceMarkers.add(key, place);
        } else if (intent.getAction().equals(FB.PLACE_CHANGE)) {
            String key = intent.getStringExtra(FB.KEY);
            Place place = (Place) intent.getSerializableExtra(FB.PLACE);
            mPlaceMarkers.change(key, place);
        } else if (intent.getAction().equals(FB.PLACE_REMOVE)) {
            String key = intent.getStringExtra(FB.KEY);
            mPlaceMarkers.remove(key);
        } else if (intent.getAction().equals(FB.GROUP_ADD)) {
            String key = intent.getStringExtra(FB.KEY);
            Group group = (Group) intent.getSerializableExtra(FB.GROUP);
//            mGroupMarkers.add(key, group);
        } else if (intent.getAction().equals(FB.GROUP_INVITE)) {
            String key = intent.getStringExtra(FB.KEY);
            Group group = (Group) intent.getSerializableExtra(FB.GROUP);
        }
    }
}
