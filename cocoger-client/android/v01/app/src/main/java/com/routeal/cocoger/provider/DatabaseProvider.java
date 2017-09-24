package com.routeal.cocoger.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by hwatanabe on 4/11/17.
 */

public class DatabaseProvider extends ContentProvider {
    private ProviderHelper mDbHelper;

    private static final int LOCATIONS = 1;
    private static final int LOCATIONS_ID = 2;
    private static final int GEO_LOCATIONS = 3;
    private static final int GEO_LOCATIONS_ID = 4;
    private static final int REVERSE_GEO_LOCATIONS = 5;
    private static final int REVERSE_GEO_LOCATIONS_ID = 6;
    private static final int IMAGES = 7;
    private static final int IMAGES_ID = 8;
    private static final int MESSAGE = 9;
    private static final int MESSAGE_ID = 10;

    private static final UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(DB.AUTHORITY, DB.Locations.PATH, LOCATIONS);
        mUriMatcher.addURI(DB.AUTHORITY, DB.Locations.PATH + "/#", LOCATIONS_ID);
        mUriMatcher.addURI(DB.AUTHORITY, DB.GeoLocations.PATH, GEO_LOCATIONS);
        mUriMatcher.addURI(DB.AUTHORITY, DB.GeoLocations.PATH + "/#", GEO_LOCATIONS_ID);
        mUriMatcher.addURI(DB.AUTHORITY, DB.ReverseGeoLocations.PATH, REVERSE_GEO_LOCATIONS);
        mUriMatcher.addURI(DB.AUTHORITY, DB.ReverseGeoLocations.PATH + "/#", REVERSE_GEO_LOCATIONS_ID);
        mUriMatcher.addURI(DB.AUTHORITY, DB.Images.PATH, IMAGES);
        mUriMatcher.addURI(DB.AUTHORITY, DB.Images.PATH + "/#", IMAGES_ID);
        mUriMatcher.addURI(DB.AUTHORITY, DB.Messages.PATH, MESSAGE);
        mUriMatcher.addURI(DB.AUTHORITY, DB.Messages.PATH + "/#", MESSAGE_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new ProviderHelper(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        int match = mUriMatcher.match(uri);
        String mime = null;
        switch (match) {
            case LOCATIONS:
                mime = DB.Locations.CONTENT_TYPE;
                break;
            case LOCATIONS_ID:
                mime = DB.Locations.CONTENT_ITEM_TYPE;
                break;
            case GEO_LOCATIONS:
                mime = DB.GeoLocations.CONTENT_TYPE;
                break;
            case GEO_LOCATIONS_ID:
                mime = DB.GeoLocations.CONTENT_ITEM_TYPE;
                break;
            case REVERSE_GEO_LOCATIONS:
                mime = DB.ReverseGeoLocations.CONTENT_TYPE;
                break;
            case REVERSE_GEO_LOCATIONS_ID:
                mime = DB.ReverseGeoLocations.CONTENT_ITEM_TYPE;
                break;
            case IMAGES:
                mime = DB.Images.CONTENT_TYPE;
                break;
            case IMAGES_ID:
                mime = DB.Images.CONTENT_ITEM_TYPE;
                break;
            case MESSAGE:
                mime = DB.Messages.CONTENT_TYPE;
                break;
            case MESSAGE_ID:
                mime = DB.Messages.CONTENT_ITEM_TYPE;
                break;
            default:
                break;
        }
        return mime;
    }

    //
    // https://developer.android.com/guide/topics/providers/content-provider-basics.html
    //
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection,
                        String selection, String[] selectionArgs, String sortOrder) {
        String tableName;
        String innerSelection = "1";
        String[] innerSelectionArgs = new String[]{};
        List<String> pathSegments = uri.getPathSegments();

        switch (mUriMatcher.match(uri)) {
            case LOCATIONS:
                tableName = DB.Locations.TABLE;
                break;
            case LOCATIONS_ID:
                tableName = DB.Locations.TABLE;
                innerSelection = DB.Locations._ID + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(1)};
                break;
            case GEO_LOCATIONS:
                tableName = DB.GeoLocations.TABLE;
                break;
            case GEO_LOCATIONS_ID:
                tableName = DB.GeoLocations.TABLE;
                innerSelection = DB.GeoLocations._ID + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(1)};
                break;
            case REVERSE_GEO_LOCATIONS:
                tableName = DB.ReverseGeoLocations.TABLE;
                break;
            case REVERSE_GEO_LOCATIONS_ID:
                tableName = DB.ReverseGeoLocations.TABLE;
                innerSelection = DB.ReverseGeoLocations._ID + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(1)};
                break;
            case IMAGES:
                tableName = DB.Images.TABLE;
                break;
            case IMAGES_ID:
                tableName = DB.Images.TABLE;
                innerSelection = DB.Images._ID + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(1)};
                break;
            case MESSAGE:
                tableName = DB.Messages.TABLE;
                break;
            case MESSAGE_ID:
                tableName = DB.Messages.TABLE;
                innerSelection = DB.Messages._ID + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(1)};
                break;
            default:
                return null;
        }

        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();

        qBuilder.setTables(tableName);

        if (selection == null) {
            selection = innerSelection;
        } else {
            selection = "( " + innerSelection + " ) and " + selection;
        }

        LinkedList<String> allArgs = new LinkedList<String>();

        if (selectionArgs == null) {
            allArgs.addAll(Arrays.asList(innerSelectionArgs));
        } else {
            allArgs.addAll(Arrays.asList(innerSelectionArgs));
            allArgs.addAll(Arrays.asList(selectionArgs));
        }

        selectionArgs = allArgs.toArray(innerSelectionArgs);

        SQLiteDatabase mDb = mDbHelper.getReadableDatabase();

        Cursor c = qBuilder.query(mDb, projection, selection, selectionArgs, null, null, sortOrder);

        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Uri insertedUri = null;

        switch (mUriMatcher.match(uri)) {
            case LOCATIONS: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                long id = db.insert(DB.Locations.TABLE, null, values);
                insertedUri = ContentUris.withAppendedId(uri, id);

                ContentResolver resolver = getContext().getContentResolver();
                resolver.notifyChange(DB.Locations.CONTENT_URI, null);
                break;
            }
            case GEO_LOCATIONS: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                long id = db.insert(DB.GeoLocations.TABLE, null, values);
                insertedUri = ContentUris.withAppendedId(uri, id);

                ContentResolver resolver = getContext().getContentResolver();
                resolver.notifyChange(DB.GeoLocations.CONTENT_URI, null);
                break;
            }
            case REVERSE_GEO_LOCATIONS: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                long id = db.insert(DB.ReverseGeoLocations.TABLE, null, values);
                insertedUri = ContentUris.withAppendedId(uri, id);

                ContentResolver resolver = getContext().getContentResolver();
                resolver.notifyChange(DB.ReverseGeoLocations.CONTENT_URI, null);
                break;
            }
            case IMAGES: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                long id = db.insert(DB.Images.TABLE, null, values);
                insertedUri = ContentUris.withAppendedId(uri, id);

                ContentResolver resolver = getContext().getContentResolver();
                resolver.notifyChange(DB.Images.CONTENT_URI, null);
                break;
            }
            case MESSAGE: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                long id = db.insert(DB.Messages.TABLE, null, values);
                insertedUri = ContentUris.withAppendedId(uri, id);

                ContentResolver resolver = getContext().getContentResolver();
                resolver.notifyChange(DB.Messages.CONTENT_URI, null);
                break;
            }
            default:
                break;
        }

        return insertedUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int updates = -1;

        switch (mUriMatcher.match(uri)) {
            case IMAGES_ID: {
                long id = new Long(uri.getLastPathSegment()).longValue();
                String whereclause = DB.Images._ID + " = " + id;
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                updates = db.update(DB.Images.TABLE, values, whereclause, null);

                ContentResolver resolver = getContext().getContentResolver();
                Uri notifyUri = ContentUris.withAppendedId(DB.Images.CONTENT_URI, id);
                resolver.notifyChange(notifyUri, null);
                break;
            }
            case MESSAGE_ID: {
                long id = new Long(uri.getLastPathSegment()).longValue();
                String whereclause = DB.Messages._ID + " = " + id;
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                updates = db.update(DB.Messages.TABLE, values, whereclause, null);

                ContentResolver resolver = getContext().getContentResolver();
                Uri notifyUri = ContentUris.withAppendedId(DB.Messages.CONTENT_URI, id);
                resolver.notifyChange(notifyUri, null);
                break;
            }
            case GEO_LOCATIONS_ID: {
                long id = new Long(uri.getLastPathSegment()).longValue();
                String whereclause = DB.GeoLocations._ID + " = " + id;
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                updates = db.update(DB.GeoLocations.TABLE, values, whereclause, null);

                ContentResolver resolver = getContext().getContentResolver();
                Uri notifyUri = ContentUris.withAppendedId(DB.GeoLocations.CONTENT_URI, id);
                resolver.notifyChange(notifyUri, null);
                break;
            }
            case REVERSE_GEO_LOCATIONS_ID: {
                long id = new Long(uri.getLastPathSegment()).longValue();
                String whereclause = DB.ReverseGeoLocations._ID + " = " + id;
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                updates = db.update(DB.ReverseGeoLocations.TABLE, values, whereclause, null);

                ContentResolver resolver = getContext().getContentResolver();
                Uri notifyUri = ContentUris.withAppendedId(DB.ReverseGeoLocations.CONTENT_URI, id);
                resolver.notifyChange(notifyUri, null);
                break;
            }
            default:
                break;
        }

        return updates;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int affected = 0;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        switch (mUriMatcher.match(uri)) {
            case LOCATIONS: {
                affected = db.delete(DB.Locations.TABLE, selection, selectionArgs);
                ContentResolver resolver = getContext().getContentResolver();
                resolver.notifyChange(DB.Locations.CONTENT_URI, null);
                break;
            }

            case LOCATIONS_ID: {
                long id = new Long(uri.getLastPathSegment()).longValue();
                affected = db.delete(DB.Locations.TABLE, DB.Locations._ID + "= ?",
                        new String[]{String.valueOf(id)});
                ContentResolver resolver = getContext().getContentResolver();
                Uri notifyUri = ContentUris.withAppendedId(DB.Locations.CONTENT_URI, id);
                resolver.notifyChange(notifyUri, null);
                resolver.notifyChange(DB.Locations.CONTENT_URI, null);
                break;
            }

            case GEO_LOCATIONS: {
                affected = db.delete(DB.GeoLocations.TABLE, selection, selectionArgs);
                ContentResolver resolver = getContext().getContentResolver();
                resolver.notifyChange(DB.GeoLocations.CONTENT_URI, null);
                break;
            }

            case GEO_LOCATIONS_ID: {
                long id = new Long(uri.getLastPathSegment()).longValue();
                affected = db.delete(DB.GeoLocations.TABLE, DB.GeoLocations._ID + "= ?",
                        new String[]{String.valueOf(id)});
                ContentResolver resolver = getContext().getContentResolver();
                Uri notifyUri = ContentUris.withAppendedId(DB.GeoLocations.CONTENT_URI, id);
                resolver.notifyChange(notifyUri, null);
                resolver.notifyChange(DB.GeoLocations.CONTENT_URI, null);
                break;
            }

            case REVERSE_GEO_LOCATIONS: {
                affected = db.delete(DB.ReverseGeoLocations.TABLE, selection, selectionArgs);
                ContentResolver resolver = getContext().getContentResolver();
                resolver.notifyChange(DB.ReverseGeoLocations.CONTENT_URI, null);
                break;
            }

            case REVERSE_GEO_LOCATIONS_ID: {
                long id = new Long(uri.getLastPathSegment()).longValue();
                affected = db.delete(DB.ReverseGeoLocations.TABLE, DB.ReverseGeoLocations._ID + "= ?",
                        new String[]{String.valueOf(id)});
                ContentResolver resolver = getContext().getContentResolver();
                Uri notifyUri = ContentUris.withAppendedId(DB.ReverseGeoLocations.CONTENT_URI, id);
                resolver.notifyChange(notifyUri, null);
                resolver.notifyChange(DB.ReverseGeoLocations.CONTENT_URI, null);
                break;
            }

            case IMAGES: {
                affected = db.delete(DB.Images.TABLE, selection, selectionArgs);
                ContentResolver resolver = getContext().getContentResolver();
                resolver.notifyChange(DB.Images.CONTENT_URI, null);
                break;
            }

            case IMAGES_ID: {
                long id = new Long(uri.getLastPathSegment()).longValue();
                affected = db.delete(DB.Images.TABLE, DB.Images._ID + "= ?",
                        new String[]{String.valueOf(id)});
                ContentResolver resolver = getContext().getContentResolver();
                Uri notifyUri = ContentUris.withAppendedId(DB.Images.CONTENT_URI, id);
                resolver.notifyChange(notifyUri, null);
                resolver.notifyChange(DB.Images.CONTENT_URI, null);
                break;
            }

            case MESSAGE: {
                affected = db.delete(DB.Messages.TABLE, selection, selectionArgs);
                ContentResolver resolver = getContext().getContentResolver();
                resolver.notifyChange(DB.Messages.CONTENT_URI, null);
                break;
            }

            case MESSAGE_ID: {
                long id = new Long(uri.getLastPathSegment()).longValue();
                affected = db.delete(DB.Messages.TABLE, DB.Messages._ID + "= ?",
                        new String[]{String.valueOf(id)});
                ContentResolver resolver = getContext().getContentResolver();
                Uri notifyUri = ContentUris.withAppendedId(DB.Messages.CONTENT_URI, id);
                resolver.notifyChange(notifyUri, null);
                resolver.notifyChange(DB.Messages.CONTENT_URI, null);
                break;
            }

            default:
                break;
        }

        return affected;
    }
}
