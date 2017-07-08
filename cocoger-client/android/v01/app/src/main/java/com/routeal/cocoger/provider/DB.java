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
    public static final Uri CONTENT_URI = Uri.parse("content://" + DB.AUTHORITY);

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
        static final String URI_TAG     = "cache";
    }

    public static class ImageColumns extends CacheColumns {
        public static final String NAME	= "name";
        public static final String DATA = "data";
        static final String NAME_TYPE   = "TEXT";
        static final String DATA_TYPE   = "BLOB";
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

    static class GpsColumns extends DBColumns {
        static final String URI_TAG     = "gps";
    }

    public static class LocationsColumns extends GpsColumns {
        public static final String CREATED = "created";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String ZIP = "zip";
        public static final String COUNTRY = "country";
        public static final String STATE = "state";
        public static final String COUNTY = "county";
        public static final String CITY = "city";
        public static final String TOWN = "town";
        public static final String STREET = "street";

        static final String CREATED_TYPE   = "INTEGER";
        static final String LATITUDE_TYPE   = "REAL";
        static final String LONGITUDE_TYPE   = "REAL";
        static final String ZIP_TYPE   = "TEXT";
        static final String COUNTRY_TYPE   = "TEXT";
        static final String STATE_TYPE   = "TEXT";
        static final String COUNTY_TYPE   = "TEXT";
        static final String CITY_TYPE   = "TEXT";
        static final String TOWN_TYPE   = "TEXT";
        static final String STREET_TYPE   = "TEXT";
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
                "," + " " + CREATED + " " + CREATED_TYPE +
                "," + " " + LATITUDE + " " + LATITUDE_TYPE +
                "," + " " + LONGITUDE + " " + LONGITUDE_TYPE +
                ");";
    }

    public static class MailColumns extends DBColumns {
        static final String URI_TAG = "mail";
    }

    public static class AddressColumns extends MailColumns {
        // Owner is Message
        public static final String MESSAGE_OWNER = "message_owner";
        static final String MESSAGE_OWNER_TYPE = "INTEGER";

        public static final String ADDRESS = "address";
        static final String ADDRESS_TYPE = "TEXT";

        public static final String TYPE = "type";
        static final String TYPE_TYPE = "INTEGER";
    }

    public static final class Addresses extends AddressColumns implements BaseColumns {
        public static final String TABLE = "addresses";
        public static final String PATH = URI_TAG + "/" + TABLE;
        public static final String CONTENT_TYPE = MIME_TYPE + TABLE;
        public static final String CONTENT_ITEM_TYPE = MIME_ITEM_TYPE + TABLE;
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + DB.AUTHORITY + "/" + URI_TAG + "/" + TABLE);

        public final static int FROM = (1 << 0);
        public final static int TO = (1 << 1);
        public final static int CC = (1 << 2);
        public final static int BCC = (1 << 3);
        public final static int REPLYTO = (1 << 4);

        static final String CREATE_STATEMENT = "CREATE TABLE " + TABLE +
                "(" + " " + _ID + " " + _ID_TYPE +
                "," + " " + MESSAGE_OWNER + " " + MESSAGE_OWNER_TYPE +
                "," + " " + ADDRESS + " " + ADDRESS_TYPE +
                "," + " " + TYPE + " " + TYPE_TYPE +
                ");";
    }

    public static class MessageColumns extends MailColumns {

        /* Content could be owner */
        public static final String MESSAGE_OWNER = "message_owner";
        static final String MESSAGE_OWNER_TYPE = "INTEGER";

        public static final String CONTENT_OWNER = "content_owner";
        static final String CONTENT_OWNER_TYPE = "INTEGER";

        /* GMAIL MESSAGE */

        public static final String MESSAGE_ID = "message_id";
        static final String MESSAGE_ID_TYPE = "TEXT";

        public static final String THREAD_ID = "thread_id";
        static final String THREAD_ID_TYPE = "TEXT";

        public static final String LABELS = "labels";
        static final String LABELS_TYPE = "INTEGER";

        public static final String HISTORY_ID = "history_id";
        static final String HISTORY_ID_TYPE = "INTEGER";

        public static final String INTERNAL_DATE = "internal_date";
        static final String INTERNAL_DATE_TYPE = "INTEGER";

        public static final String SIZE = "size";
        static final String SIZE_TYPE = "INTEGER";

        /* MIME HEADER */

        public static final String DATE = "date";
        static final String DATE_TYPE = "INTEGER";

        public static final String SUBJECT = "subject";
        static final String SUBJECT_TYPE = "TEXT";

    }

    public static final class Messages extends MessageColumns implements BaseColumns {
        public static final String TABLE = "messages";
        public static final String PATH = URI_TAG + "/" + TABLE;
        public static final String CONTENT_TYPE = MIME_TYPE + TABLE;
        public static final String CONTENT_ITEM_TYPE = MIME_ITEM_TYPE + TABLE;
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + DB.AUTHORITY + "/" + URI_TAG + "/" + TABLE);

        static final String CREATE_STATEMENT = "CREATE TABLE " + TABLE +
                "(" + " " + _ID + " " + _ID_TYPE +
                "," + " " + MESSAGE_OWNER + " " + MESSAGE_OWNER_TYPE +
                "," + " " + CONTENT_OWNER + " " + CONTENT_OWNER_TYPE +
                "," + " " + MESSAGE_ID + " " + MESSAGE_ID_TYPE +
                "," + " " + THREAD_ID + " " + THREAD_ID_TYPE +
                "," + " " + LABELS + " " + LABELS_TYPE +
                "," + " " + HISTORY_ID + " " + HISTORY_ID_TYPE +
                "," + " " + INTERNAL_DATE + " " + INTERNAL_DATE_TYPE +
                "," + " " + SIZE + " " + SIZE_TYPE +
                "," + " " + DATE + " " + DATE_TYPE +
                "," + " " + SUBJECT + " " + SUBJECT_TYPE +
                ");";

        public enum Label {
            INBOX(1, "INBOX"),
            SPAM(2, "SPAM"),
            TRASH(4, "TRASH"),
            UNREAD(8, "UNREAD"),
            STARRED(16, "STARRED"),
            IMPORTANT(32, "IMPORTANT"),
            SENT(64, "SENT"),
            DRAFT(128, "DRAFT");

            public final int value;
            public final String name;

            Label(int value, String name) {
                this.value = value;
                this.name = name;
            }

            static String toString(int value) {
                if (INBOX.value == value) {
                    return INBOX.name;
                } else if (SPAM.value == value) {
                    return SPAM.name;
                } else if (TRASH.value == value) {
                    return TRASH.name;
                } else if (UNREAD.value == value) {
                    return UNREAD.name;
                } else if (STARRED.value == value) {
                    return STARRED.name;
                } else if (IMPORTANT.value == value) {
                    return IMPORTANT.name;
                } else if (SENT.value == value) {
                    return SENT.name;
                } else if (DRAFT.value == value) {
                    return DRAFT.name;
                }
                return null;
            }

            static int toValue(String name) {
                if (INBOX.name.equals(name)) {
                    return INBOX.value;
                } else if (SPAM.name.equals(name)) {
                    return SPAM.value;
                } else if (TRASH.name.equals(name)) {
                    return TRASH.value;
                } else if (UNREAD.name.equals(name)) {
                    return UNREAD.value;
                } else if (STARRED.name.equals(name)) {
                    return STARRED.value;
                } else if (IMPORTANT.name.equals(name)) {
                    return IMPORTANT.value;
                } else if (SENT.name.equals(name)) {
                    return SENT.value;
                } else if (DRAFT.name.equals(name)) {
                    return DRAFT.value;
                }
                return 0;
            }

            public static int toValue(List<String> labels) {
                int v = 0;
                if (labels == null) return v;
                for (String s : labels) {
                    v |= toValue(s);
                }
                return v;
            }

            public static int add(int value, String label) {
                int v = toValue(label);
                return (v | value);
            }

            public static int remove(int value, String label) {
                int v = toValue(label);
                return (v & ~value);
            }
        }
    }

    public static class ContentColumns extends MailColumns {
        public static final String TYPE = "type";
        static final String TYPE_TYPE = "TEXT";

        public static final String MESSAGE_OWNER = "message_owner";
        static final String MESSAGE_OWNER_TYPE = "INTEGER";

        public static final String CONTENT_OWNER = "content_owner";
        static final String CONTENT_OWNER_TYPE = "INTEGER";

        public static final String DATA = "data";
        static final String DATA_TYPE = "BLOB";

        public static final String MESSAGE = "message";
        static final String MESSAGE_TYPE = "INTEGER";
    }

    public static final class Contents extends ContentColumns implements BaseColumns {
        public static final String TABLE = "contents";
        public static final String PATH = URI_TAG + "/" + TABLE;
        public static final String CONTENT_TYPE = MIME_TYPE + TABLE;
        public static final String CONTENT_ITEM_TYPE = MIME_ITEM_TYPE + TABLE;
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + DB.AUTHORITY + "/" + URI_TAG + "/" + TABLE);

        static final String CREATE_STATEMENT = "CREATE TABLE " + TABLE +
                "(" + " " + _ID + " " + _ID_TYPE +
                "," + " " + MESSAGE_OWNER + " " + MESSAGE_OWNER_TYPE +
                "," + " " + CONTENT_OWNER + " " + CONTENT_OWNER_TYPE +
                "," + " " + MESSAGE + " " + MESSAGE_TYPE +
                "," + " " + TYPE + " " + TYPE_TYPE +
                "," + " " + DATA + " " + DATA_TYPE +
                ");";
    }

}
