package com.routeal.cocoger.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.ContentUris;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by hwatanabe on 4/11/17.
 */

public class DatabaseProvider extends ContentProvider {
    private ProviderHelper mDbHelper;

/*
    private static final int USERS = 1;
    private static final int USERS_ID = 2;
    private static final int FRIENDS = 3;
    private static final int FRIENDS_ID = 4;
*/
    private static final int LOCATIONS = 1;
    private static final int LOCATIONS_ID = 2;

    private static final UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
/*
        mUriMatcher.addURI(DB.AUTHORITY, DB.Users.PATH, USERS);
        mUriMatcher.addURI(DB.AUTHORITY, DB.Users.PATH + "/#", USERS_ID);
        mUriMatcher.addURI(DB.AUTHORITY, DB.Friends.PATH, FRIENDS);
        mUriMatcher.addURI(DB.AUTHORITY, DB.Friends.PATH + "/#", FRIENDS_ID);
*/
        mUriMatcher.addURI(DB.AUTHORITY, DB.Locations.PATH, LOCATIONS);
        mUriMatcher.addURI(DB.AUTHORITY, DB.Locations.PATH + "/#", LOCATIONS_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new ProviderHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        int match = mUriMatcher.match(uri);
        String mime = null;
        switch (match) {
/*
            case USERS:
                mime = DB.Users.CONTENT_TYPE;
                break;
            case USERS_ID:
                mime = DB.Users.CONTENT_ITEM_TYPE;
                break;
            case FRIENDS:
                mime = DB.Friends.CONTENT_TYPE;
                break;
            case FRIENDS_ID:
                mime = DB.Friends.CONTENT_ITEM_TYPE;
                break;
*/
            case LOCATIONS:
                mime = DB.Locations.CONTENT_TYPE;
                break;
            case LOCATIONS_ID:
                mime = DB.Locations.CONTENT_ITEM_TYPE;
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
    public Cursor query(Uri uri, String[] projection,
                        String selection, String[] selectionArgs, String sortOrder) {
        String tableName = null;
        String innerSelection = "1";
        String[] innerSelectionArgs = new String[]{};
        String sortorder = sortOrder;
        List<String> pathSegments = uri.getPathSegments();

        switch (mUriMatcher.match(uri)) {
/*
            case USERS:
                tableName = DB.Users.TABLE;
                break;
            case USERS_ID:
                tableName = DB.Users.TABLE;
                innerSelection = DB.Users._ID + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(1)};
                break;
            case FRIENDS:
                tableName = DB.Friends.TABLE;
                break;
            case FRIENDS_ID:
                tableName = DB.Friends.TABLE;
                innerSelection = DB.Friends._ID + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(1)};
                break;
*/
            case LOCATIONS:
                tableName = DB.Locations.TABLE;
                break;
            case LOCATIONS_ID:
                tableName = DB.Locations.TABLE;
                innerSelection = DB.Locations._ID + " = ? ";
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

        Cursor c = qBuilder.query(mDb, projection, selection, selectionArgs, null, null, sortorder);

        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
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
/*
            case USERS: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                long id = db.insert(DB.Users.TABLE, null, values);
                insertedUri = ContentUris.withAppendedId(uri, id);

                ContentResolver resolver = getContext().getContentResolver();
                resolver.notifyChange(DB.Users.CONTENT_URI, null);
                break;
            }
            case FRIENDS: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                long id = db.insert(DB.Friends.TABLE, null, values);
                insertedUri = ContentUris.withAppendedId(uri, id);

                ContentResolver resolver = getContext().getContentResolver();
                resolver.notifyChange(DB.Friends.CONTENT_URI, null);
                break;
            }
*/
            default:
                break;
        }

        return insertedUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int updates = -1;
/*
        switch (mUriMatcher.match(uri)) {
            case USERS_ID: {
                long id = new Long(uri.getLastPathSegment()).longValue();
                String whereclause = DB.Users._ID + " = " + id;
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                updates = db.update(DB.Users.TABLE, values, whereclause, null);

                ContentResolver resolver = getContext().getContentResolver();
                Uri notifyUri = ContentUris.withAppendedId(DB.Users.CONTENT_URI, id);
                resolver.notifyChange(notifyUri, null);
                resolver.notifyChange(DB.Users.CONTENT_URI, null);
                break;
            }
            case FRIENDS_ID: {
                long id = new Long(uri.getLastPathSegment()).longValue();
                String whereclause = DB.Friends._ID + " = " + id;
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                updates = db.update(DB.Friends.TABLE, values, whereclause, null);

                ContentResolver resolver = getContext().getContentResolver();
                Uri notifyUri = ContentUris.withAppendedId(DB.Friends.CONTENT_URI, id);
                resolver.notifyChange(notifyUri, null);
                resolver.notifyChange(DB.Friends.CONTENT_URI, null);
                break;
            }
            default:
                break;
        }
*/
        return updates;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int affected = 0;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        switch (mUriMatcher.match(uri)) {
/*
            case USERS: {
                affected = db.delete(DB.Users.TABLE, selection, selectionArgs);
                ContentResolver resolver = getContext().getContentResolver();
                resolver.notifyChange(DB.Users.CONTENT_URI, null);
                break;
            }

            case USERS_ID: {
                long id  = new Long(uri.getLastPathSegment()).longValue();
                affected = db.delete(DB.Users.TABLE, DB.Users._ID + "= ?",
                        new String[] { String.valueOf(id) });
                ContentResolver resolver = getContext().getContentResolver();
                Uri notifyUri = ContentUris.withAppendedId(DB.Users.CONTENT_URI, id);
                resolver.notifyChange(notifyUri, null);
                resolver.notifyChange(DB.Users.CONTENT_URI, null);
                break;
            }

            case FRIENDS: {
                affected = db.delete(DB.Friends.TABLE, selection, selectionArgs);
                ContentResolver resolver = getContext().getContentResolver();
                resolver.notifyChange(DB.Friends.CONTENT_URI, null);
                break;
            }

            case FRIENDS_ID: {
                long id  = new Long(uri.getLastPathSegment()).longValue();
                affected = db.delete(DB.Friends.TABLE, DB.Friends._ID + "= ?",
                        new String[] { String.valueOf(id) });
                ContentResolver resolver = getContext().getContentResolver();
                Uri notifyUri = ContentUris.withAppendedId(DB.Friends.CONTENT_URI, id);
                resolver.notifyChange(notifyUri, null);
                resolver.notifyChange(DB.Friends.CONTENT_URI, null);
                break;
            }
*/

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

            default:
                break;
        }

        return affected;
    }
}
