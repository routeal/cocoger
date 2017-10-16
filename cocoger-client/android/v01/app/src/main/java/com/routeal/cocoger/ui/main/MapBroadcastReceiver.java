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

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.Place;
import com.routeal.cocoger.util.LocationRange;
import com.routeal.cocoger.util.Utils;

/**
 * Created by hwatanabe on 10/15/17.
 */

public class MapBroadcastReceiver extends BroadcastReceiver {
    private final static String TAG = "MapBroadcastReceiver";

    private GoogleMap mMap;
    private GeoDataClient mGeoDataClient;
    private MapActivity mActivity;
    private MarkerManager mMm;
    private MapDirection mDirection;
    private PlaceManager mPlace;

    private Location mLocation;
    private Address mAddress;

    MapBroadcastReceiver(MapActivity activity, GoogleMap map, MarkerManager markerManager, MapDirection mapDirection, PlaceManager placeManager) {
        mActivity = activity;
        mMap = map;
        mMm = markerManager;
        mDirection = mapDirection;
        mPlace = placeManager;
        IntentFilter filter = new IntentFilter();
        filter.addAction(FB.USER_AVAILABLE);
        filter.addAction(FB.USER_LOCATION_UPDATE);
        filter.addAction(FB.FRIEND_LOCATION_ADD);
        filter.addAction(FB.FRIEND_LOCATION_UPDATE);
        filter.addAction(FB.FRIEND_LOCATION_REMOVE);
        filter.addAction(FB.FRIEND_RANGE_UPDATE);
        filter.addAction(FB.FRIEND_MARKER_SHOW);
        filter.addAction(FB.DIRECTION_ROUTE_ADD);
        filter.addAction(FB.DIRECTION_ROUTE_REMOVE);
        filter.addAction(FB.PLACE_SAVE);
        filter.addAction(FB.PLACE_EDIT);
        filter.addAction(FB.PLACE_REMOVE);
        filter.addAction(FB.PLACE_SHOW);
        LocalBroadcastManager.getInstance(activity).registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Action=" + intent.getAction());
        if (intent.getAction().equals(FB.USER_LOCATION_UPDATE)) {
            Address address = intent.getParcelableExtra(FB.ADDRESS);
            Location location = intent.getParcelableExtra(FB.LOCATION);
            if (location == null || address == null) {
                return;
            }
            if (mMap != null) {
                // first time only
                if (mLocation == null) {
                    Log.d(TAG, "Receive Last_location_update: setupMarkers");
                    if (FB.getUser() != null) {
                        mMm.setupMarkers(location, address);
                    }
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            Utils.getLatLng(location), MapActivity.DEFAULT_ZOOM));
                } else {
                    if (FB.getUser() != null) {
                        mMm.reposition(FB.getUid(), location, address, LocationRange.CURRENT.range);
                    }
                }
            }
            mLocation = location;
            mAddress = address;
        } else if (intent.getAction().equals(FB.USER_AVAILABLE)) {
            Log.d(TAG, "Receive User_available: setupMarkers");
            if (mMm != null) {
                if (mAddress == null) {
                    mAddress = Utils.getAddress(mLocation);
                }
                mMm.setupMarkers(mLocation, mAddress);
            }
            if (mPlace != null) {
                mPlace.init();
            }
        } else if (intent.getAction().equals(FB.FRIEND_LOCATION_ADD)) {
            final String fid = intent.getStringExtra(FB.KEY);
            final Friend friend = FB.getFriend(fid);
            if (friend == null) return; // shouldn't happen
            FB.getLocation(friend.getLocation(), new FB.LocationListener() {
                @Override
                public void onFail(String err) {
                    Log.d(TAG, "Failed to get location for added Friend: " + err);
                }

                @Override
                public void onSuccess(Location location, final Address address) {
                    mMm.reposition(fid, location, address, friend.getRange());
                }
            });
        } else if (intent.getAction().equals(FB.FRIEND_LOCATION_UPDATE)) {
            final String fid = intent.getStringExtra(FB.KEY);
            //final String newLocationKey = intent.getStringExtra(FB.NEW_LOCATION);
            //final String oldLocationKey = intent.getStringExtra(FB.OLD_LOCATION);
            final Friend friend = FB.getFriend(fid);
            if (friend == null) return; // shouldn't happen
            final int range = friend.getRange();
            FB.getLocation(friend.getLocation(), new FB.LocationListener() {
                @Override
                public void onFail(String err) {
                    Log.d(TAG, "Friend new location: " + err);
                }

                @Override
                public void onSuccess(Location newLocation, final Address newAddress) {
                    // move the cursor
                    mMm.reposition(fid, newLocation, newAddress, range);

                        /*
                        // compare the new location with the old location to issue the range movement
                        FB.getLocation(oldLocationKey, new FB.LocationListener() {
                            @Override
                            public void onFail(String err) {
                                Log.d(TAG, "Friend old location: " + err);
                            }

                            @Override
                            public void onSuccess(Location oldLocation, Address oldAddress) {
                                // detect move in the range or above
                                // TODO
                                int movedRange = Utils.detectRangeMove(newAddress, oldAddress, range);
                                if (movedRange > 0) {
                                    // if the address is the same as the user, send a notification
                                    if (Utils.isEqualAddress(newAddress, getAddress(), range)) {
                                        // send notification
                                        int nid = Math.abs((int) friend.getCreated());
                                        String message = Utils.getRangeMoveMessage(friend, newAddress, oldAddress);
                                        Notifi.send(nid, friend.getDisplayName(), message, friend.getPicture());
                                    }
                                }
                            }
                        });
                        */
                }
            });
        } else if (intent.getAction().equals(FB.FRIEND_LOCATION_REMOVE)) {
            String fid = intent.getStringExtra(FB.KEY);
            if (fid == null) {
                return;
            }
            mMm.remove(fid);
        } else if (intent.getAction().equals(FB.FRIEND_RANGE_UPDATE)) {
            String fid = intent.getStringExtra(FB.KEY);
            if (fid == null) {
                return;
            }
            Friend friend = FB.getFriend(fid);
            if (friend == null || friend.getLocation() == null) {
                return;
            }
            int range = friend.getRange();
            Log.d(TAG, "FRIEND_RANGE_UPDATE:" + fid);
            mMm.reposition(fid, range);
        } else if (intent.getAction().equals(FB.FRIEND_MARKER_SHOW)) {
            String fid = intent.getStringExtra(FB.KEY);
            if (fid == null) {
                return;
            }
            Log.d(TAG, "FRIEND_MARKER_SHOW:" + fid);
            mMm.show(fid);
            mActivity.closeSlidePanel();
        } else if (intent.getAction().equals(FB.DIRECTION_ROUTE_ADD)) {
            Location location = intent.getParcelableExtra(FB.LOCATION);
            mDirection.addDirection(mActivity, location, mLocation);
            mActivity.closeSlidePanel();
        } else if (intent.getAction().equals(FB.DIRECTION_ROUTE_REMOVE)) {
            mDirection.removeDirection();
        } else if (intent.getAction().equals(FB.PLACE_SAVE)) {
            Location location = intent.getParcelableExtra(FB.LOCATION);
            String address = intent.getStringExtra(FB.ADDRESS);
            String title = intent.getStringExtra(FB.TITLE);
            Bitmap bitmap = intent.getParcelableExtra(FB.IMAGE);
            mPlace.addPlace(mActivity, title, location, address, bitmap);
        } else if (intent.getAction().equals(FB.PLACE_EDIT)) {
            String key = intent.getStringExtra(FB.KEY);
            Place place = (Place) intent.getSerializableExtra(FB.PLACE);
            mPlace.editPlace(mActivity, place, key);
        } else if (intent.getAction().equals(FB.PLACE_REMOVE)) {
            String key = intent.getStringExtra(FB.KEY);
            Place place = (Place) intent.getSerializableExtra(FB.PLACE);
            mPlace.removePlace(place, key);
        } else if (intent.getAction().equals(FB.PLACE_SHOW)) {
            String key = intent.getStringExtra(FB.KEY);
            Place place = (Place) intent.getSerializableExtra(FB.PLACE);
            mPlace.showPlace(key);
            mActivity.closeSlidePanel();
        }
    }

    Location getLocation() {
        return mLocation;
    }

    void setLocation(Location location) {
        mLocation = location;
    }
}
