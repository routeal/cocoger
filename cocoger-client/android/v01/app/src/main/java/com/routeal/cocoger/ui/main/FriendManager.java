package com.routeal.cocoger.ui.main;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Friend;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hwatanabe on 10/15/17.
 */

public class FriendManager {

    private final static String TAG = "FriendManager";

    private static Map<String, Friend> mFriendList = new HashMap<>();

    public static Map<String, Friend> getFriends() {
        return mFriendList;
    }

    public static Friend getFriend(String key) {
        if (key != null && !key.isEmpty()) {
            return mFriendList.get(key);
        }
        return null;
    }

    void add(String key, Friend friend) {
        Log.d(TAG, "add:" + key);

        mFriendList.put(key, friend);

        Intent intent = new Intent(FB.FRIEND_LOCATION_ADD);
        intent.putExtra(FB.KEY, key);
        LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
    }

    void change(String key, Friend newFriend) {
        Log.d(TAG, "change:" + key);

        Friend oldFriend = mFriendList.get(key);

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

        if (newFriend.getLocation() != null && oldFriend.getLocation() != null &&
                !newFriend.getLocation().equals(oldFriend.getLocation())) {
            Intent intent = new Intent(FB.FRIEND_LOCATION_UPDATE);
            intent.putExtra(FB.KEY, key);
                    /* Range movement not implemented
                    intent.putExtra(FB.NEW_LOCATION, newFriend.getLocation());
                    intent.putExtra(FB.OLD_LOCATION, oldFriend.getLocation());
                    */
            LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
        }

        mFriendList.put(key, newFriend);
    }

    void remove(String key) {
        Log.d(TAG, "remove:" + key);

        mFriendList.remove(key);

        Intent intent = new Intent(FB.FRIEND_LOCATION_REMOVE);
        intent.putExtra(FB.KEY, key);
        LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
    }

    public interface FriendListener {
        void onAdded(String key, Friend friend);

        void onChanged(String key, Friend friend);

        void onRemoved(String key);
    }
}
