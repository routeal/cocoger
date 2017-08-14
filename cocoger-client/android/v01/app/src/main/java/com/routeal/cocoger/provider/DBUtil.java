package com.routeal.cocoger.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.LocationAddress;
import com.routeal.cocoger.model.User;

import java.util.ArrayList;
import java.util.List;

public class DBUtil {

    private final static String TAG = "DBUtil";

    public static User getUser() {
        User user = new User();
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
            cursor = contentResolver.query(DB.Users.CONTENT_URI, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index;
                index = cursor.getColumnIndex(DB.Users.BIRTHYEAR);
                user.setBirthYear(cursor.getString(index));
                index = cursor.getColumnIndex(DB.Users.EMAIL);
                user.setEmail(cursor.getString(index));
                index = cursor.getColumnIndex(DB.Users.FIRSTNAME);
                user.setFirstName(cursor.getString(index));
                index = cursor.getColumnIndex(DB.Users.LASTNAME);
                user.setLastName(cursor.getString(index));
                index = cursor.getColumnIndex(DB.Users.NAME);
                user.setName(cursor.getString(index));
                index = cursor.getColumnIndex(DB.Users.GENDER);
                user.setGender(cursor.getString(index));
                index = cursor.getColumnIndex(DB.Users.PICTURE);
                user.setPicture(cursor.getString(index));
                index = cursor.getColumnIndex(DB.Users.LOCALE);
                user.setLocale(cursor.getString(index));
                index = cursor.getColumnIndex(DB.Users.TIMEZONE);
                user.setTimezone(cursor.getString(index));
                index = cursor.getColumnIndex(DB.Users.UPDATED);
                user.setUpdated(cursor.getLong(index));
                index = cursor.getColumnIndex(DB.Users.CREATED);
                user.setCreated(cursor.getLong(index));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return user;
    }

    public static void saveUser(User user) {
        ContentValues values = new ContentValues();
        values.put(DB.Users.BIRTHYEAR, user.getBirthYear());
        values.put(DB.Users.EMAIL, user.getEmail());
        values.put(DB.Users.FIRSTNAME, user.getFirstName());
        values.put(DB.Users.LASTNAME, user.getLastName());
        values.put(DB.Users.NAME, user.getName());
        values.put(DB.Users.GENDER, user.getGender());
        values.put(DB.Users.PICTURE, user.getPicture());
        values.put(DB.Users.LOCALE, user.getLocale());
        values.put(DB.Users.TIMEZONE, user.getTimezone());
        values.put(DB.Users.UPDATED, user.getUpdated());
        values.put(DB.Users.CREATED, user.getCreated());

        ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
        contentResolver.insert(DB.Users.CONTENT_URI, values);
    }

    public static void deleteUser() {
        ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
        contentResolver.delete(DB.Users.CONTENT_URI, null, null);
    }

    public static void saveLocation(Location location, Address address) {
        ContentValues values = new ContentValues();
        values.put(DB.Locations.TIME, location.getTime());
        values.put(DB.Locations.LATITUDE, location.getLatitude());
        values.put(DB.Locations.LONGITUDE, location.getLongitude());
        values.put(DB.Locations.ALTITUDE, location.getAltitude());
        values.put(DB.Locations.SPEED, location.getSpeed());
        values.put(DB.Locations.POSTALCODE, address.getPostalCode());
        values.put(DB.Locations.COUNTRYNAME, address.getCountryName());
        values.put(DB.Locations.ADMINAREA, address.getAdminArea());
        values.put(DB.Locations.SUBADMINAREA, address.getSubAdminArea());
        values.put(DB.Locations.LOCALITY, address.getLocality());
        values.put(DB.Locations.SUBLOCALITY, address.getSubLocality());
        values.put(DB.Locations.THOROUGHFARE, address.getThoroughfare());
        values.put(DB.Locations.SUBTHOROUGHFARE, address.getSubThoroughfare());

        ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
        contentResolver.insert(DB.Locations.CONTENT_URI, values);
    }

    public static List<LocationAddress> getLocations(int max) {
        List<LocationAddress> locations = new ArrayList<>();

        Cursor cursor = null;
        try {
            ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
            cursor = contentResolver.query(DB.Locations.CONTENT_URI, null, null, null,
                    DB.Locations.TIME + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                int num = 1;
                int index;
                do {
                    LocationAddress la = new LocationAddress();
                    locations.add(la);
                    index = cursor.getColumnIndex(DB.Locations.TIME);
                    la.setTimestamp(cursor.getLong(index));
                    index = cursor.getColumnIndex(DB.Locations.LATITUDE);
                    la.setLatitude(cursor.getDouble(index));
                    index = cursor.getColumnIndex(DB.Locations.LONGITUDE);
                    la.setLongitude(cursor.getDouble(index));
                    index = cursor.getColumnIndex(DB.Locations.ALTITUDE);
                    la.setLongitude(cursor.getDouble(index));
                    index = cursor.getColumnIndex(DB.Locations.SPEED);
                    la.setLongitude(cursor.getFloat(index));
                    index = cursor.getColumnIndex(DB.Locations.POSTALCODE);
                    la.setPostalCode(cursor.getString(index));
                    index = cursor.getColumnIndex(DB.Locations.COUNTRYNAME);
                    la.setCountryName(cursor.getString(index));
                    index = cursor.getColumnIndex(DB.Locations.ADMINAREA);
                    la.setAdminArea(cursor.getString(index));
                    index = cursor.getColumnIndex(DB.Locations.SUBADMINAREA);
                    la.setSubAdminArea(cursor.getString(index));
                    index = cursor.getColumnIndex(DB.Locations.LOCALITY);
                    la.setlocality(cursor.getString(index));
                    index = cursor.getColumnIndex(DB.Locations.SUBLOCALITY);
                    la.setSubLocality(cursor.getString(index));
                    index = cursor.getColumnIndex(DB.Locations.THOROUGHFARE);
                    la.setThoroughfare(cursor.getString(index));
                    index = cursor.getColumnIndex(DB.Locations.SUBTHOROUGHFARE);
                    la.setSubThoroughfare(cursor.getString(index));
                    index = cursor.getColumnIndex(DB.Locations._ID);
                    la.setId(cursor.getLong(index));
                    index = cursor.getColumnIndex(DB.Locations.UID);
                    la.setUid(cursor.getString(index));
                    if (num++ == max) {
                        break;
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error to retrieve locations", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return locations;
    }

    public static void deleteLocations(List<LocationAddress> locations) {
        ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
        for (LocationAddress la : locations) {
            Uri uri = ContentUris.withAppendedId(DB.Locations.CONTENT_URI, la.getId());
            contentResolver.delete(uri, null, null);
        }
    }

    public static void deleteLocations() {
        ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
        contentResolver.delete(DB.Locations.CONTENT_URI, null, null);
    }

    public static List<Friend> getFriends() {
        return null;
    }

    public static void saveFriend(Friend friend) {
        ContentValues values = new ContentValues();
        values.put(DB.Friends.PROVIDERID, friend.getProviderId());
        values.put(DB.Friends.FIRSTNAME, friend.getFirstName());
        values.put(DB.Friends.LASTNAME, friend.getLastName());
        values.put(DB.Friends.NAME, friend.getName());
        values.put(DB.Friends.GENDER, friend.getGender());
        values.put(DB.Friends.PICTURE, friend.getPicture());
        values.put(DB.Friends.LOCALE, friend.getLocale());
        values.put(DB.Friends.TIMEZONE, friend.getTimezone());
        values.put(DB.Friends.UPDATED, friend.getUpdated());
        values.put(DB.Friends.STATUS, friend.getStatus());
        values.put(DB.Friends.APPROVED, friend.getApproved());

        ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
        contentResolver.insert(DB.Friends.CONTENT_URI, values);
    }

    public static void saveFriends(List<Friend> friends) {
    }

    public static void deleteFriends() {
        ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
        contentResolver.delete(DB.Friends.CONTENT_URI, null, null);
    }
}
