package com.routeal.cocoger.manager;

import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Friend;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by hwatanabe on 10/15/17.
 */

/**
 * Note that FriendManger will be accessed from the background process without the UI.
 */
public class FriendManager {

    private final static String TAG = "FriendManager";

    private static SortedMap<String, Friend> mFriendList = new TreeMap<>();
    private static Map<String, LocationAddress> mLocationList = new HashMap<>();
    private static UpdateListener<Friend> mUpdateListener;

    public static SortedMap<String, Friend> getFriends() {
        return mFriendList;
    }

    public static Friend getFriend(String key) {
        if (key != null && !key.isEmpty()) {
            return mFriendList.get(key);
        }
        return null;
    }

    public static boolean isEmpty() {
        return mFriendList.isEmpty();
    }

    public static void setLocation(String key, Location location, Address address) {
        LocationAddress lc = new LocationAddress();
        lc.location = location;
        lc.address = address;
        mLocationList.put(key, lc);
    }

    public static Location getLocation(String key) {
        LocationAddress lc = mLocationList.get(key);
        if (lc != null) {
            return lc.location;
        }
        return null;
    }

    public static Address getAddress(String key) {
        LocationAddress lc = mLocationList.get(key);
        if (lc != null) {
            return lc.address;
        }
        return null;
    }

    public static void add(String key, Friend friend) {
        Log.d(TAG, "add:" + key);

        if (mUpdateListener != null) {
            mUpdateListener.onAdded(key, friend);
        }

        mFriendList.put(key, friend);

        Intent intent = new Intent(FB.FRIEND_LOCATION_ADD);
        intent.putExtra(FB.KEY, key);
        LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
    }

    public static void change(String key, Friend newFriend) {
        Log.d(TAG, "change:" + key);

        if (mUpdateListener != null) {
            mUpdateListener.onChanged(key, newFriend);
        }

        Friend oldFriend = mFriendList.get(key);

        /* TODO: don't know what to do by knowing the current status
        if (oldFriend.getStatus() != newFriend.getStatus()) {
            if (newFriend.getStatus() == Friend.ONLINE) {
            } else if (newFriend.getStatus() == Friend.OFFLINE) {
            }
        }
        */

        if (newFriend.getRangeRequest() != null) {
            int requestRange = newFriend.getRangeRequest().getRange();
            int currentRange = newFriend.getRange();

            // new range request found
            if (oldFriend.getRangeRequest() == null) {
                FB.sendRangeNotification(key, newFriend, requestRange, currentRange);
            }
        }

        // the range has been update, notify the map acitivity
        // to change the marker location
        if (newFriend.getRange() != oldFriend.getRange()) {
            Intent intent = new Intent(FB.FRIEND_RANGE_UPDATE);
            intent.putExtra(FB.KEY, key);
            LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
        }

        if (oldFriend.getLocation() == null ||
                (newFriend.getLocation() != null && oldFriend.getLocation() != null &&
                        !newFriend.getLocation().equals(oldFriend.getLocation()))) {
            Intent intent = new Intent(FB.FRIEND_LOCATION_UPDATE);
            intent.putExtra(FB.KEY, key);
            intent.putExtra(FB.LOCATION, oldFriend.getLocation());
            LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
        }

        mFriendList.put(key, newFriend);
    }

    public static void remove(String key) {
        Log.d(TAG, "remove:" + key);

        mFriendList.remove(key);
        mLocationList.remove(key);

        if (mUpdateListener != null) {
            mUpdateListener.onRemoved(key);
        }

        Intent intent = new Intent(FB.FRIEND_LOCATION_REMOVE);
        intent.putExtra(FB.KEY, key);
        LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
    }

    public static void setUpdateListener(UpdateListener<Friend> listener) {
        mUpdateListener = listener;
    }

    private static class LocationAddress {
        Location location;
        Address address;
    }

}
