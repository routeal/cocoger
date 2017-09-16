package com.routeal.cocoger.provider;

import android.net.Uri;
import android.provider.BaseColumns;

import java.util.List;

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

/*
    public static class ImageColumns extends CacheColumns {
        public static final String NAME = "name";
        public static final String DATA = "data";
        static final String NAME_TYPE = "TEXT";
        static final String DATA_TYPE = "BLOB";
    }

    public static final class Images extends ImageColumns implements BaseColumns {
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
                ");";
    }

    public static class UsersColumns extends CacheColumns {
        public static final String BIRTHYEAR = "birthYear";
        public static final String EMAIL = "email";
        public static final String FIRSTNAME = "firstName";
        public static final String LASTNAME = "lastName";
        public static final String NAME = "name";
        public static final String GENDER = "gender";
        public static final String PICTURE = "picture";
        public static final String LOCALE = "locale";
        public static final String TIMEZONE = "timezone";
        public static final String UPDATED = "updated";
        public static final String CREATED = "created";
        static final String BIRTHYEAR_TYPE = "TEXT";
        static final String EMAIL_TYPE = "TEXT";
        static final String FIRSTNAME_TYPE = "TEXT";
        static final String LASTNAME_TYPE = "TEXT";
        static final String NAME_TYPE = "TEXT";
        static final String GENDER_TYPE = "TEXT";
        static final String PICTURE_TYPE = "TEXT";
        static final String LOCALE_TYPE = "TEXT";
        static final String TIMEZONE_TYPE = "TEXT";
        static final String UPDATED_TYPE = "INTEGER";
        static final String CREATED_TYPE = "INTEGER";
    }

    public static final class Users extends UsersColumns implements BaseColumns {
        public static final String TABLE = "users";
        public static final String PATH = URI_TAG + "/" + TABLE;
        public static final String CONTENT_TYPE = MIME_TYPE + TABLE;
        public static final String CONTENT_ITEM_TYPE = MIME_ITEM_TYPE + TABLE;
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + DB.AUTHORITY + "/" + URI_TAG + "/" + TABLE);

        static final String CREATE_STATEMENT = "CREATE TABLE " + TABLE +
                "(" + " " + _ID + " " + _ID_TYPE +
                "," + " " + BIRTHYEAR + " " + BIRTHYEAR_TYPE +
                "," + " " + EMAIL + " " + EMAIL_TYPE +
                "," + " " + FIRSTNAME + " " + FIRSTNAME_TYPE +
                "," + " " + LASTNAME + " " + LASTNAME_TYPE +
                "," + " " + NAME + " " + NAME_TYPE +
                "," + " " + GENDER + " " + GENDER_TYPE +
                "," + " " + PICTURE + " " + PICTURE_TYPE +
                "," + " " + LOCALE + " " + LOCALE_TYPE +
                "," + " " + TIMEZONE + " " + TIMEZONE_TYPE +
                "," + " " + UPDATED + " " + UPDATED_TYPE +
                "," + " " + CREATED + " " + CREATED_TYPE +
                ");";
    }

    public static class FriendsColumns extends CacheColumns {
        public static final String PROVIDERID = "providerId";
        public static final String FIRSTNAME = "firstName";
        public static final String LASTNAME = "lastName";
        public static final String NAME = "name";
        public static final String GENDER = "gender";
        public static final String PICTURE = "picture";
        public static final String LOCALE = "locale";
        public static final String TIMEZONE = "timezone";
        public static final String UPDATED = "updated";
        public static final String STATUS = "status";
        public static final String RANGE = "range";
        public static final String APPROVED = "approved";

        static final String PROVIDERID_TYPE = "TEXT";
        static final String FIRSTNAME_TYPE = "TEXT";
        static final String LASTNAME_TYPE = "TEXT";
        static final String NAME_TYPE = "TEXT";
        static final String GENDER_TYPE = "TEXT";
        static final String PICTURE_TYPE = "TEXT";
        static final String LOCALE_TYPE = "TEXT";
        static final String TIMEZONE_TYPE = "TEXT";
        static final String UPDATED_TYPE = "TEXT";
        static final String STATUS_TYPE = "INTEGER";
        static final String RANGE_TYPE = "INTEGER";
        static final String APPROVED_TYPE = "TEXT";
    }

    public static final class Friends extends FriendsColumns implements BaseColumns {
        public static final String TABLE = "friends";
        public static final String PATH = URI_TAG + "/" + TABLE;
        public static final String CONTENT_TYPE = MIME_TYPE + TABLE;
        public static final String CONTENT_ITEM_TYPE = MIME_ITEM_TYPE + TABLE;
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + DB.AUTHORITY + "/" + URI_TAG + "/" + TABLE);

        static final String CREATE_STATEMENT = "CREATE TABLE " + TABLE +
                "(" + " " + _ID + " " + _ID_TYPE +
                "," + " " + PROVIDERID + " " + PROVIDERID_TYPE +
                "," + " " + FIRSTNAME + " " + FIRSTNAME_TYPE +
                "," + " " + LASTNAME + " " + LASTNAME_TYPE +
                "," + " " + NAME + " " + NAME_TYPE +
                "," + " " + GENDER + " " + GENDER_TYPE +
                "," + " " + PICTURE + " " + PICTURE_TYPE +
                "," + " " + LOCALE + " " + LOCALE_TYPE +
                "," + " " + TIMEZONE + " " + TIMEZONE_TYPE +
                "," + " " + UPDATED + " " + UPDATED_TYPE +
                "," + " " + STATUS + " " + STATUS_TYPE +
                "," + " " + RANGE + " " + RANGE_TYPE +
                "," + " " + APPROVED + " " + APPROVED_TYPE +
                ");";
    }
*/

    public static class LocationsColumns extends CacheColumns {
        public static final String UID = "uid";
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

        static final String UID_TYPE = "TEXT";
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
                "," + " " + UID + " " + UID_TYPE +
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
                ");";
    }

}
