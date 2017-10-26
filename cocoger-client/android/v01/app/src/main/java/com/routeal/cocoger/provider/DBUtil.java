package com.routeal.cocoger.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.PointF;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.LocationAddress;
import com.routeal.cocoger.model.NoticeMessage;
import com.routeal.cocoger.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DBUtil {

    private final static String TAG = "DBUtil";

    private final static int MAX_IMAGES = 200;
    private final static int MAX_GEO_LOCATIONS = 1000;
    private final static int MAX_REVERSE_GEO_LOCATIONS = 1000;

    public static void saveSentLocation(Location location, Address address, String key) {
        saveLocation(location, address, key);
    }

    public static void saveUnsentLocation(Location location, Address address) {
        saveLocation(location, address, null);
    }

    private static void saveLocation(Location location, Address address, String key) {
        ContentValues values = new ContentValues();
        values.put(DB.Locations.TIMESTAMP, location.getTime());
        values.put(DB.Locations.LATITUDE, location.getLatitude());
        values.put(DB.Locations.LONGITUDE, location.getLongitude());
        values.put(DB.Locations.ALTITUDE, location.getAltitude());
        values.put(DB.Locations.SPEED, location.getSpeed());
        values.put(DB.Locations.DESCRIPTION, address.describeContents());
        values.put(DB.Locations.POSTALCODE, address.getPostalCode());
        values.put(DB.Locations.COUNTRYNAME, address.getCountryName());
        values.put(DB.Locations.ADMINAREA, address.getAdminArea());
        values.put(DB.Locations.SUBADMINAREA, address.getSubAdminArea());
        values.put(DB.Locations.LOCALITY, address.getLocality());
        values.put(DB.Locations.SUBLOCALITY, address.getSubLocality());
        values.put(DB.Locations.THOROUGHFARE, address.getThoroughfare());
        values.put(DB.Locations.SUBTHOROUGHFARE, address.getSubThoroughfare());
        values.put(DB.Locations.KEY, key);

        ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
        contentResolver.insert(DB.Locations.CONTENT_URI, values);
    }

    private static LocationAddress getLocationAddress(Cursor cursor) {
        int index;
        LocationAddress la = new LocationAddress();
        la.setUid(FB.getUid());
        index = cursor.getColumnIndex(DB.Locations._ID);
        la.setId(cursor.getLong(index));
        index = cursor.getColumnIndex(DB.Locations.TIMESTAMP);
        la.setTimestamp(cursor.getLong(index));
        index = cursor.getColumnIndex(DB.Locations.LATITUDE);
        la.setLatitude(cursor.getDouble(index));
        index = cursor.getColumnIndex(DB.Locations.LONGITUDE);
        la.setLongitude(cursor.getDouble(index));
        index = cursor.getColumnIndex(DB.Locations.ALTITUDE);
        la.setAltitude(cursor.getDouble(index));
        index = cursor.getColumnIndex(DB.Locations.SPEED);
        la.setSpeed(cursor.getFloat(index));
        index = cursor.getColumnIndex(DB.Locations.DESCRIPTION);
        la.setDescription(cursor.getString(index));
        index = cursor.getColumnIndex(DB.Locations.POSTALCODE);
        la.setPostalCode(cursor.getString(index));
        index = cursor.getColumnIndex(DB.Locations.COUNTRYNAME);
        la.setCountryName(cursor.getString(index));
        index = cursor.getColumnIndex(DB.Locations.ADMINAREA);
        la.setAdminArea(cursor.getString(index));
        index = cursor.getColumnIndex(DB.Locations.SUBADMINAREA);
        la.setSubAdminArea(cursor.getString(index));
        index = cursor.getColumnIndex(DB.Locations.LOCALITY);
        la.setLocality(cursor.getString(index));
        index = cursor.getColumnIndex(DB.Locations.SUBLOCALITY);
        la.setSubLocality(cursor.getString(index));
        index = cursor.getColumnIndex(DB.Locations.THOROUGHFARE);
        la.setThoroughfare(cursor.getString(index));
        index = cursor.getColumnIndex(DB.Locations.SUBTHOROUGHFARE);
        la.setSubThoroughfare(cursor.getString(index));
        index = cursor.getColumnIndex(DB.Locations.PLACEID);
        la.setPlaceId(cursor.getString(index));
        return la;
    }

    public static List<LocationAddress> getUnsentLocations() {
        List<LocationAddress> locations = new ArrayList<>();

        Cursor cursor = null;
        try {
            ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
            String selectionClause = DB.Locations.KEY + " is null";
            cursor = contentResolver.query(DB.Locations.CONTENT_URI, null,
                    selectionClause, null,
                    DB.Locations.TIMESTAMP + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    LocationAddress locationAddress = getLocationAddress(cursor);
                    locations.add(locationAddress);
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

    public static List<LocationAddress> getSentLocations(long start, long end) {
        List<LocationAddress> locations = new ArrayList<>();

        Cursor cursor = null;
        try {
            ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
            String selectionClause = "( " +
                    DB.Locations.KEY + " is not null AND " +
                    DB.Locations.KEY + " != '' AND " +
                    DB.Locations.TIMESTAMP + " >= ? AND " +
                    DB.Locations.TIMESTAMP + " <= ? " +
                    ")";
            String[] selectionArgs = new String[]{
                    String.valueOf(start),
                    String.valueOf(end)
            };
            cursor = contentResolver.query(DB.Locations.CONTENT_URI, null,
                    selectionClause, selectionArgs,
                    DB.Locations.TIMESTAMP + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    LocationAddress locationAddress = getLocationAddress(cursor);
                    locations.add(locationAddress);
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

    public static void deleteLocation(long id) {
        ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
        Uri uri = ContentUris.withAppendedId(DB.Locations.CONTENT_URI, id);
        contentResolver.delete(uri, null, null);
    }

    public static void deleteLocations() {
        ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
        contentResolver.delete(DB.Locations.CONTENT_URI, null, null);
    }

    public static void saveAddress(Location location, Address address) {
        ContentValues values = new ContentValues();
        values.put(DB.GeoLocations.TIMESTAMP, location.getTime());
        values.put(DB.GeoLocations.LATITUDE, location.getLatitude());
        values.put(DB.GeoLocations.LONGITUDE, location.getLongitude());
        values.put(DB.GeoLocations.ALTITUDE, location.getAltitude());
        values.put(DB.GeoLocations.SPEED, location.getSpeed());
        values.put(DB.GeoLocations.DESCRIPTION, address.describeContents());
        values.put(DB.GeoLocations.POSTALCODE, address.getPostalCode());
        values.put(DB.GeoLocations.COUNTRYNAME, address.getCountryName());
        values.put(DB.GeoLocations.ADMINAREA, address.getAdminArea());
        values.put(DB.GeoLocations.SUBADMINAREA, address.getSubAdminArea());
        values.put(DB.GeoLocations.LOCALITY, address.getLocality());
        values.put(DB.GeoLocations.SUBLOCALITY, address.getSubLocality());
        values.put(DB.GeoLocations.THOROUGHFARE, address.getThoroughfare());
        values.put(DB.GeoLocations.SUBTHOROUGHFARE, address.getSubThoroughfare());
        values.put(DB.GeoLocations.REFCOUNT, 0);

        ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
        contentResolver.insert(DB.GeoLocations.CONTENT_URI, values);
    }

    public static void saveImage(String url, byte[] bytes) {
        ContentValues values = new ContentValues();
        values.put(DB.Images.NAME, url);
        values.put(DB.Images.DATA, bytes);
        values.put(DB.Images.REFCOUNT, 0);
        ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
        contentResolver.insert(DB.Images.CONTENT_URI, values);
    }

    public static byte[] getImage(String url) {
        ContentResolver contentResolver = MainApplication.getContext().getContentResolver();

        String selectionClause = DB.Images.NAME + " = ?";
        String[] selectionArgs = new String[]{url};
        Cursor cursor = contentResolver.query(DB.Images.CONTENT_URI, null, selectionClause, selectionArgs, null);

        long id = 0;
        byte[] data = null;
        int refcount = 0;

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getLong(0);
                data = cursor.getBlob(cursor.getColumnIndex(DB.Images.DATA));
                refcount = cursor.getInt(cursor.getColumnIndex(DB.Images.REFCOUNT));
            }
            cursor.close();
        }

        if (data != null && refcount < 1000) {
            ContentValues values = new ContentValues();
            values.put(DB.Images.REFCOUNT, refcount + 1);
            Uri uri = ContentUris.withAppendedId(DB.Images.CONTENT_URI, id);
            contentResolver.update(uri, values, null, null);
        }

        return data;
    }

    public static void deleteImage(String url) {
        ContentResolver contentResolver = MainApplication.getContext().getContentResolver();

        long id = 0;
        String selectionClause = DB.Images.NAME + " = ?";
        String[] selectionArgs = new String[]{url};
        Cursor cursor = contentResolver.query(DB.Images.CONTENT_URI, null, selectionClause, selectionArgs, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getLong(0);
            }
            cursor.close();
        }

        if (id == 0) return; // not found

        Uri uri = ContentUris.withAppendedId(DB.Images.CONTENT_URI, id);
        contentResolver.delete(uri, null, null);
    }

    /**
     * Calculates the end-point from a given source at a given range (meters)
     * and bearing (degrees). This methods uses simple geometry equations to
     * calculate the end-point.
     *
     * @param point   Point of origin
     * @param range   Range in meters
     * @param bearing Bearing in degrees
     * @return End-point from the source given the desired range and bearing.
     */
    static PointF calculateDerivedPosition(PointF point, double range, double bearing) {
        double EarthRadius = 6371000; // m

        double latA = Math.toRadians(point.x);
        double lonA = Math.toRadians(point.y);
        double angularDistance = range / EarthRadius;
        double trueCourse = Math.toRadians(bearing);

        double lat = Math.asin(
                Math.sin(latA) * Math.cos(angularDistance) +
                        Math.cos(latA) * Math.sin(angularDistance)
                                * Math.cos(trueCourse));

        double dlon = Math.atan2(
                Math.sin(trueCourse) * Math.sin(angularDistance)
                        * Math.cos(latA),
                Math.cos(angularDistance) - Math.sin(latA) * Math.sin(lat));

        double lon = ((lonA + dlon + Math.PI) % (Math.PI * 2)) - Math.PI;

        lat = Math.toDegrees(lat);
        lon = Math.toDegrees(lon);

        PointF newPoint = new PointF((float) lat, (float) lon);

        return newPoint;
    }

    // radius should be meter
    public static Address getAddress(Location location, double radius) {
        // https://stackoverflow.com/questions/3695224/sqlite-getting-nearest-locations-with-latitude-and-longitude
        float latitude = (float) location.getLatitude();
        float longitude = (float) location.getLongitude();
        PointF center = new PointF(latitude, longitude);
        final double mult = 1.1;

        PointF p1 = calculateDerivedPosition(center, mult * radius, 0);
        PointF p2 = calculateDerivedPosition(center, mult * radius, 90);
        PointF p3 = calculateDerivedPosition(center, mult * radius, 180);
        PointF p4 = calculateDerivedPosition(center, mult * radius, 270);

        //strWhere =  " WHERE "
        //        + COL_X + " > " + String.valueOf(p3.x) + " AND "
        //        + COL_X + " < " + String.valueOf(p1.x) + " AND "
        //        + COL_Y + " < " + String.valueOf(p2.y) + " AND "
        //        + COL_Y + " > " + String.valueOf(p4.y)

        String whereClause = "( " +
                DB.GeoLocations.LATITUDE + " > ? AND " +
                DB.GeoLocations.LATITUDE + " < ? AND " +
                DB.GeoLocations.LONGITUDE + " < ? AND " +
                DB.GeoLocations.LONGITUDE + " > ? )";

        String[] whereArgs = new String[]{
                String.valueOf(p3.x),
                String.valueOf(p1.x),
                String.valueOf(p2.y),
                String.valueOf(p4.y)
        };

        Map<Long, Address> addressList = new HashMap<>();
        Map<Long, Integer> refcountList = new HashMap<>();
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
            cursor = contentResolver.query(DB.GeoLocations.CONTENT_URI, null, whereClause, whereArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index;
                do {
                    Address address = new Address(Locale.getDefault());
                    addressList.put(cursor.getLong(0), address);
                    index = cursor.getColumnIndex(DB.GeoLocations.LATITUDE);
                    address.setLatitude(cursor.getDouble(index));
                    index = cursor.getColumnIndex(DB.GeoLocations.LONGITUDE);
                    address.setLongitude(cursor.getDouble(index));
                    index = cursor.getColumnIndex(DB.GeoLocations.POSTALCODE);
                    address.setPostalCode(cursor.getString(index));
                    index = cursor.getColumnIndex(DB.GeoLocations.COUNTRYNAME);
                    address.setCountryName(cursor.getString(index));
                    index = cursor.getColumnIndex(DB.GeoLocations.ADMINAREA);
                    address.setAdminArea(cursor.getString(index));
                    index = cursor.getColumnIndex(DB.GeoLocations.SUBADMINAREA);
                    address.setSubAdminArea(cursor.getString(index));
                    index = cursor.getColumnIndex(DB.GeoLocations.LOCALITY);
                    address.setLocality(cursor.getString(index));
                    index = cursor.getColumnIndex(DB.GeoLocations.SUBLOCALITY);
                    address.setSubLocality(cursor.getString(index));
                    index = cursor.getColumnIndex(DB.GeoLocations.THOROUGHFARE);
                    address.setThoroughfare(cursor.getString(index));
                    index = cursor.getColumnIndex(DB.GeoLocations.SUBTHOROUGHFARE);
                    address.setSubThoroughfare(cursor.getString(index));
                    address.setAddressLine(0, Utils.getAddressLine(address));
                    index = cursor.getColumnIndex(DB.GeoLocations.REFCOUNT);
                    refcountList.put(cursor.getLong(0), cursor.getInt(index));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error to retrieve locations", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (addressList.isEmpty()) return null;

        // minimum distance address
        Address minAddress = null;

        float min = Float.MAX_VALUE;
        long updateId = 0;
        for (Map.Entry<Long, Address> entry : addressList.entrySet()) {
            Address address = entry.getValue();
            Location loc = Utils.getLocation(address);
            float v = loc.distanceTo(location);
            if (v < min) {
                min = v;
                minAddress = address;
                updateId = entry.getKey();
            }
        }

        if (min > radius) return null;

        if (minAddress != null) {
            int refcount = refcountList.get(updateId);
            if (refcount < 1000) {
                ContentValues values = new ContentValues();
                values.put(DB.GeoLocations.REFCOUNT, refcount + 1);
                Uri uri = ContentUris.withAppendedId(DB.GeoLocations.CONTENT_URI, updateId);
                ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
                contentResolver.update(uri, values, null, null);
            }
        }

        return minAddress;
    }

    public static Location getLocation(@NonNull String address) {
        ContentResolver contentResolver = MainApplication.getContext().getContentResolver();

        String selectionClause = DB.ReverseGeoLocations.ADDRESSLINE + " = ?";
        String[] selectionArgs = new String[]{address};
        Cursor cursor = contentResolver.query(DB.ReverseGeoLocations.CONTENT_URI, null, selectionClause, selectionArgs, null);

        long id = 0;
        int refcount = 0;
        Location location = null;

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getLong(0);
                refcount = cursor.getInt(cursor.getColumnIndex(DB.ReverseGeoLocations.REFCOUNT));
                double latitude = cursor.getDouble(cursor.getColumnIndex(DB.ReverseGeoLocations.LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndex(DB.ReverseGeoLocations.LONGITUDE));
                location = Utils.getLocation(latitude, longitude);
            }
            cursor.close();
        }

        if (location != null && refcount < 1000) {
            ContentValues values = new ContentValues();
            values.put(DB.ReverseGeoLocations.REFCOUNT, refcount + 1);
            Uri uri = ContentUris.withAppendedId(DB.ReverseGeoLocations.CONTENT_URI, id);
            contentResolver.update(uri, values, null, null);
        }

        return location;
    }

    public static void setLocation(@NonNull String address, @NonNull Location location) {
        ContentValues values = new ContentValues();
        values.put(DB.ReverseGeoLocations.ADDRESSLINE, address);
        values.put(DB.ReverseGeoLocations.LATITUDE, location.getLatitude());
        values.put(DB.ReverseGeoLocations.LONGITUDE, location.getLongitude());
        values.put(DB.ReverseGeoLocations.REFCOUNT, 0);
        ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
        contentResolver.insert(DB.ReverseGeoLocations.CONTENT_URI, values);
    }

    public static void purgeImages() {
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
            String selectionClause = DB.Images.REFCOUNT + " = 0";
            cursor = contentResolver.query(DB.Images.CONTENT_URI, null, selectionClause, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                if (cursor.getCount() < MAX_IMAGES) {
                    return;
                }
                do {
                    Uri uri = ContentUris.withAppendedId(DB.Images.CONTENT_URI, cursor.getLong(0));
                    contentResolver.delete(uri, null, null);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error to purge images", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void purgeGeoLocations() {
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
            String selectionClause = DB.GeoLocations.REFCOUNT + " = 0";
            cursor = contentResolver.query(DB.GeoLocations.CONTENT_URI, null, selectionClause, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                if (cursor.getCount() < MAX_GEO_LOCATIONS) {
                    return;
                }
                do {
                    Uri uri = ContentUris.withAppendedId(DB.GeoLocations.CONTENT_URI, cursor.getLong(0));
                    contentResolver.delete(uri, null, null);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error to purge locations", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void purgeReverseGeoLocations() {
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
            String selectionClause = DB.ReverseGeoLocations.REFCOUNT + " = 0";
            cursor = contentResolver.query(DB.ReverseGeoLocations.CONTENT_URI, null, selectionClause, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                if (cursor.getCount() < MAX_REVERSE_GEO_LOCATIONS) {
                    return;
                }
                do {
                    Uri uri = ContentUris.withAppendedId(DB.ReverseGeoLocations.CONTENT_URI, cursor.getLong(0));
                    contentResolver.delete(uri, null, null);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error to purge locations", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void saveMessage(String key, String title, String message, int resourceId, long created) {
        saveMessage(key, title, message, null, resourceId, created);
    }

    public static void saveMessage(String key, String title, String message, String picture, long created) {
        saveMessage(key, title, message, picture, 0, created);
    }

    private static void saveMessage(String key, String title, String message, String picture, int resourceId, long created) {
        ContentValues values = new ContentValues();
        values.put(DB.Messages.TITLE, title);
        values.put(DB.Messages.MESSAGE, message);
        values.put(DB.Messages.PICTURE, picture);
        values.put(DB.Messages.RESOURCEID, resourceId);
        values.put(DB.Messages.CREATED, created);
        values.put(DB.Messages.KEY, key);
        ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
        contentResolver.insert(DB.Messages.CONTENT_URI, values);
    }

    public static void deleteMessage(long id) {
        ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
        Uri uri = ContentUris.withAppendedId(DB.Messages.CONTENT_URI, id);
        contentResolver.delete(uri, null, null);
    }

    public static List<NoticeMessage> getMessages() {
        List<NoticeMessage> messages = new ArrayList<>();

        Cursor cursor = null;
        try {
            ContentResolver contentResolver = MainApplication.getContext().getContentResolver();
            cursor = contentResolver.query(DB.Messages.CONTENT_URI, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index;
                do {
                    NoticeMessage message = new NoticeMessage();
                    messages.add(message);
                    message.setId(cursor.getLong(0));
                    index = cursor.getColumnIndex(DB.Messages.TITLE);
                    message.setTitle(cursor.getString(index));
                    index = cursor.getColumnIndex(DB.Messages.MESSAGE);
                    message.setMessage(cursor.getString(index));
                    index = cursor.getColumnIndex(DB.Messages.PICTURE);
                    message.setPicture(cursor.getString(index));
                    index = cursor.getColumnIndex(DB.Messages.RESOURCEID);
                    message.setResourceId(cursor.getInt(index));
                    index = cursor.getColumnIndex(DB.Messages.KEY);
                    message.setKey(cursor.getString(index));
                    index = cursor.getColumnIndex(DB.Messages.CREATED);
                    message.setCreated(cursor.getLong(index));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error to retrieve locations", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return messages;
    }

}
