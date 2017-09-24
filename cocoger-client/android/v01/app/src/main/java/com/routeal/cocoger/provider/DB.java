package com.routeal.cocoger.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by hwatanabe on 4/11/17.
 */

public final class DB {
    static final String VENDER_NAME = "routeal.cocoger";
    static final String ORG_NAME = "com";

    // unique name to avoid conflicts with other providers
    public static final String AUTHORITY = ORG_NAME + "." + VENDER_NAME + "." + "provider";

    static final String DATABASE_NAME = VENDER_NAME + ".db";
    static final int DATABASE_VERSION = 1;

    static final String MIME_TYPE = "vnd.android.cursor.dir/vnd." + VENDER_NAME + ".";
    static final String MIME_ITEM_TYPE = "vnd.android.cursor.item/vnd." + VENDER_NAME + ".";

    static class DBColumns {
        static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
    }

    /**
     *
     * https://stackoverflow.com/questions/10600670/sqlitedatabase-query-method
     */

    /**
     * SQLITE DATA TYPES
     *
     * Each value stored in an SQLite database (or manipulated by the
     * database engine) has one of the following storage classes:
     *
     * - NULL. The value is a NULL value.
     *
     * - INTEGER. The value is a signed integer, stored in 1, 2, 3, 4,
     *   6, or 8 bytes depending on the magnitude of the value.
     *
     * - REAL. The value is a floating point value, stored as an
     *   8-byte IEEE floating point number.
     *
     * - TEXT. The value is a text string, stored using the database
     *   encoding (UTF-8, UTF-16BE or UTF-16LE).
     *
     * - BLOB. The value is a blob of data, stored exactly as it was input.
     *
     */

    /**
     * Boolean Datatype
     *
     * SQLite does not have a separate Boolean storage class. Instead,
     * Boolean values are stored as integers 0 (false) and 1 (true).
     *
     */

    /**
     * Date and Time Datatype
     * <p>
     * - TEXT as ISO8601 strings ("YYYY-MM-DD HH:MM:SS.SSS").
     * <p>
     * - REAL as Julian day numbers, the number of days since noon in
     * Greenwich on November 24, 4714 B.C. according to the
     * proleptic Gregorian calendar.
     * <p>
     * - INTEGER as Unix Time, the number of seconds since 1970-01-01
     * 00:00:00 UTC.
     * <p>
     * Applications can chose to store dates and times in any of these
     * formats and freely convert between formats using the built-in
     * date and time functions.
     */


    public static class CacheColumns extends DBColumns {
        static final String URI_TAG = "cache";
    }

    public static class ImagesColumns extends CacheColumns {
        public static final String NAME = "name";
        public static final String DATA = "data";
        public static final String REFCOUNT = "refcount";

        static final String NAME_TYPE = "TEXT";
        static final String DATA_TYPE = "BLOB";
        static final String REFCOUNT_TYPE = "INTEGER";
    }

    public static final class Images extends ImagesColumns implements BaseColumns {
        public static final String TABLE = "images";
        public static final String PATH = URI_TAG + "/" + TABLE;
        public static final String CONTENT_TYPE = MIME_TYPE + TABLE;
        public static final String CONTENT_ITEM_TYPE = MIME_ITEM_TYPE + TABLE;
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + DB.AUTHORITY + "/" + URI_TAG + "/" + TABLE);

        static final String CREATE_STATEMENT = "CREATE TABLE " + TABLE +
                "(" + " " + _ID + " " + _ID_TYPE +
                "," + " " + NAME + " " + NAME_TYPE +
                "," + " " + DATA + " " + DATA_TYPE +
                "," + " " + REFCOUNT + " " + REFCOUNT_TYPE +
                ");";
    }

    public static class MessagesColumns extends CacheColumns {
        public static final String TITLE = "title";
        public static final String MESSAGE = "message";
        public static final String PICTURE = "picture";
        public static final String RESOURCEID = "resourceid";
        public static final String DATE = "date";
        public static final String KEY = "key";

        static final String TITLE_TYPE = "TEXT";
        static final String MESSAGE_TYPE = "TEXT";
        static final String PICTURE_TYPE = "TEXT";
        static final String RESOURCEID_TYPE = "INTEGER";
        static final String DATE_TYPE = "INTEGER";
        static final String KEY_TYPE = "TEXT";
    }

    public static final class Messages extends MessagesColumns implements BaseColumns {
        public static final String TABLE = "messages";
        public static final String PATH = URI_TAG + "/" + TABLE;
        public static final String CONTENT_TYPE = MIME_TYPE + TABLE;
        public static final String CONTENT_ITEM_TYPE = MIME_ITEM_TYPE + TABLE;
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + DB.AUTHORITY + "/" + URI_TAG + "/" + TABLE);

        static final String CREATE_STATEMENT = "CREATE TABLE " + TABLE +
                "(" + " " + _ID + " " + _ID_TYPE +
                "," + " " + TITLE + " " + TITLE_TYPE +
                "," + " " + MESSAGE + " " + MESSAGE_TYPE +
                "," + " " + PICTURE + " " + PICTURE_TYPE +
                "," + " " + RESOURCEID + " " + RESOURCEID_TYPE +
                "," + " " + DATE + " " + DATE_TYPE +
                "," + " " + KEY + " " + KEY_TYPE +
                ");";
    }

    public static class LocationsColumns extends CacheColumns {
        public static final String TIMESTAMP = "timestamp";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String ALTITUDE = "altitude";
        public static final String SPEED = "speed";
        public static final String DESCRIPTION = "description";
        public static final String POSTALCODE = "postalCode";
        public static final String COUNTRYNAME = "countryName";
        public static final String ADMINAREA = "adminArea";
        public static final String SUBADMINAREA = "subAdminArea";
        public static final String LOCALITY = "locality";
        public static final String SUBLOCALITY = "subLocality";
        public static final String THOROUGHFARE = "thoroughfare";
        public static final String SUBTHOROUGHFARE = "subThoroughfare";
        public static final String PLACEID = "placeId";
        public static final String SENT = "sent";

        static final String TIMESTAMP_TYPE = "INTEGER";
        static final String LATITUDE_TYPE = "REAL";
        static final String LONGITUDE_TYPE = "REAL";
        static final String ALTITUDE_TYPE = "REAL";
        static final String SPEED_TYPE = "REAL";
        static final String DESCRIPTION_TYPE = "TEXT";
        static final String POSTALCODE_TYPE = "TEXT";
        static final String COUNTRYNAME_TYPE = "TEXT";
        static final String ADMINAREA_TYPE = "TEXT";
        static final String SUBADMINAREA_TYPE = "TEXT";
        static final String LOCALITY_TYPE = "TEXT";
        static final String SUBLOCALITY_TYPE = "TEXT";
        static final String THOROUGHFARE_TYPE = "TEXT";
        static final String SUBTHOROUGHFARE_TYPE = "TEXT";
        static final String PLACEID_TYPE = "TEXT";
        static final String SENT_TYPE = "INTEGER";
    }

    public static final class Locations extends LocationsColumns implements BaseColumns {
        public static final String TABLE = "locations";
        public static final String PATH = URI_TAG + "/" + TABLE;
        public static final String CONTENT_TYPE = MIME_TYPE + TABLE;
        public static final String CONTENT_ITEM_TYPE = MIME_ITEM_TYPE + TABLE;
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + DB.AUTHORITY + "/" + URI_TAG + "/" + TABLE);

        static final String CREATE_STATEMENT = "CREATE TABLE " + TABLE +
                "(" + " " + _ID + " " + _ID_TYPE +
                "," + " " + TIMESTAMP + " " + TIMESTAMP_TYPE +
                "," + " " + LATITUDE + " " + LATITUDE_TYPE +
                "," + " " + LONGITUDE + " " + LONGITUDE_TYPE +
                "," + " " + ALTITUDE + " " + ALTITUDE_TYPE +
                "," + " " + SPEED + " " + SPEED_TYPE +
                "," + " " + DESCRIPTION + " " + DESCRIPTION_TYPE +
                "," + " " + POSTALCODE + " " + POSTALCODE_TYPE +
                "," + " " + COUNTRYNAME + " " + COUNTRYNAME_TYPE +
                "," + " " + ADMINAREA + " " + ADMINAREA_TYPE +
                "," + " " + SUBADMINAREA + " " + SUBADMINAREA_TYPE +
                "," + " " + LOCALITY + " " + LOCALITY_TYPE +
                "," + " " + SUBLOCALITY + " " + SUBLOCALITY_TYPE +
                "," + " " + THOROUGHFARE + " " + THOROUGHFARE_TYPE +
                "," + " " + SUBTHOROUGHFARE + " " + SUBTHOROUGHFARE_TYPE +
                "," + " " + PLACEID + " " + PLACEID_TYPE +
                "," + " " + SENT + " " + SENT_TYPE +
                ");";
    }

    public static class GeoLocationsColumns extends LocationsColumns {
        public static final String REFCOUNT = "refcount";
        static final String REFCOUNT_TYPE = "INTEGER";
    }

    public static final class GeoLocations extends GeoLocationsColumns implements BaseColumns {
        public static final String TABLE = "geo_locations";
        public static final String PATH = URI_TAG + "/" + TABLE;
        public static final String CONTENT_TYPE = MIME_TYPE + TABLE;
        public static final String CONTENT_ITEM_TYPE = MIME_ITEM_TYPE + TABLE;
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + DB.AUTHORITY + "/" + URI_TAG + "/" + TABLE);

        static final String CREATE_STATEMENT = "CREATE TABLE " + TABLE +
                "(" + " " + _ID + " " + _ID_TYPE +
                "," + " " + TIMESTAMP + " " + TIMESTAMP_TYPE +
                "," + " " + LATITUDE + " " + LATITUDE_TYPE +
                "," + " " + LONGITUDE + " " + LONGITUDE_TYPE +
                "," + " " + ALTITUDE + " " + ALTITUDE_TYPE +
                "," + " " + SPEED + " " + SPEED_TYPE +
                "," + " " + DESCRIPTION + " " + DESCRIPTION_TYPE +
                "," + " " + POSTALCODE + " " + POSTALCODE_TYPE +
                "," + " " + COUNTRYNAME + " " + COUNTRYNAME_TYPE +
                "," + " " + ADMINAREA + " " + ADMINAREA_TYPE +
                "," + " " + SUBADMINAREA + " " + SUBADMINAREA_TYPE +
                "," + " " + LOCALITY + " " + LOCALITY_TYPE +
                "," + " " + SUBLOCALITY + " " + SUBLOCALITY_TYPE +
                "," + " " + THOROUGHFARE + " " + THOROUGHFARE_TYPE +
                "," + " " + SUBTHOROUGHFARE + " " + SUBTHOROUGHFARE_TYPE +
                "," + " " + REFCOUNT + " " + REFCOUNT_TYPE +
                ");";
    }

    public static class ReverseGeoLocationsColumns extends CacheColumns {
        public static final String REFCOUNT = "refcount";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String ADDRESSLINE = "addressline";
        static final String LATITUDE_TYPE = "REAL";
        static final String LONGITUDE_TYPE = "REAL";
        static final String REFCOUNT_TYPE = "INTEGER";
        static final String ADDRESSLINE_TYPE = "TEXT";
    }

    public static final class ReverseGeoLocations extends ReverseGeoLocationsColumns implements BaseColumns {
        public static final String TABLE = "reverse_geo_locations";
        public static final String PATH = URI_TAG + "/" + TABLE;
        public static final String CONTENT_TYPE = MIME_TYPE + TABLE;
        public static final String CONTENT_ITEM_TYPE = MIME_ITEM_TYPE + TABLE;
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + DB.AUTHORITY + "/" + URI_TAG + "/" + TABLE);

        static final String CREATE_STATEMENT = "CREATE TABLE " + TABLE +
                "(" + " " + _ID + " " + _ID_TYPE +
                "," + " " + LATITUDE + " " + LATITUDE_TYPE +
                "," + " " + LONGITUDE + " " + LONGITUDE_TYPE +
                "," + " " + ADDRESSLINE + " " + ADDRESSLINE_TYPE +
                "," + " " + REFCOUNT + " " + REFCOUNT_TYPE +
                ");";
    }

}
