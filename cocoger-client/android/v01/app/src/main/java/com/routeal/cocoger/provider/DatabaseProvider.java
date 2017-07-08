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

    private static final int IMAGES = 1;
    private static final int IMAGES_ID = 2;
    private static final int ADDRESSES = 3;
    private static final int ADDRESSES_ID = 4;
    private static final int MESSAGES = 5;
    private static final int MESSAGE_ID = 6;
    private static final int CONTENTS = 7;
    private static final int CONTENTS_ID = 8;

    private static final UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(DB.AUTHORITY, DB.Images.PATH, IMAGES);
        mUriMatcher.addURI(DB.AUTHORITY, DB.Images.PATH + "/#", IMAGES_ID);
        mUriMatcher.addURI(DB.AUTHORITY, DB.Addresses.PATH, ADDRESSES);
        mUriMatcher.addURI(DB.AUTHORITY, DB.Addresses.PATH + "/#", ADDRESSES_ID);
        mUriMatcher.addURI(DB.AUTHORITY, DB.Messages.PATH, MESSAGES);
        mUriMatcher.addURI(DB.AUTHORITY, DB.Messages.PATH + "/#", MESSAGE_ID);
        mUriMatcher.addURI(DB.AUTHORITY, DB.Contents.PATH, CONTENTS);
        mUriMatcher.addURI(DB.AUTHORITY, DB.Contents.PATH + "/#", CONTENTS_ID);
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
            case IMAGES:
                mime = DB.Images.CONTENT_TYPE;
                break;
            case IMAGES_ID:
                mime = DB.Images.CONTENT_ITEM_TYPE;
                break;
            case ADDRESSES:
                mime = DB.Addresses.CONTENT_TYPE;
                break;
            case ADDRESSES_ID:
                mime = DB.Addresses.CONTENT_ITEM_TYPE;
                break;
            case MESSAGES:
                mime = DB.Messages.CONTENT_TYPE;
                break;
            case MESSAGE_ID:
                mime = DB.Messages.CONTENT_ITEM_TYPE;
                break;
            case CONTENTS:
                mime = DB.Contents.CONTENT_TYPE;
                break;
            case CONTENTS_ID:
                mime = DB.Contents.CONTENT_ITEM_TYPE;
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
            case IMAGES:
                tableName = DB.Images.TABLE;
                break;
            case IMAGES_ID:
                tableName = DB.Images.TABLE;
                innerSelection = DB.Images._ID + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(2)};
                break;
            case ADDRESSES:
                tableName = DB.Addresses.TABLE;
                break;
            case ADDRESSES_ID:
                tableName = DB.Addresses.TABLE;
                innerSelection = DB.Addresses.MESSAGE_OWNER + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(2)};
                break;
            case MESSAGES:
                tableName = DB.Messages.TABLE;
                break;
            case MESSAGE_ID:
                tableName = DB.Messages.TABLE;
                innerSelection = DB.Messages._ID + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(2)};
                break;
            case CONTENTS:
                tableName = DB.Contents.TABLE;
                break;
            case CONTENTS_ID:
                tableName = DB.Contents.TABLE;
                innerSelection = DB.Contents.MESSAGE_OWNER + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(2)};
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
            case IMAGES: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                long id = db.insert(DB.Images.TABLE, null, values);
                insertedUri = ContentUris.withAppendedId(uri, id);

                ContentResolver resolver = getContext().getContentResolver();
                resolver.notifyChange(DB.Images.CONTENT_URI, null);
                break;
            }
            case ADDRESSES: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                long id = db.insert(DB.Addresses.TABLE, null, values);
                insertedUri = ContentUris.withAppendedId(uri, id);

                ContentResolver resolver = getContext().getContentResolver();
                resolver.notifyChange(DB.Addresses.CONTENT_URI, null);
                break;
            }
            case MESSAGES: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                long id = db.insert(DB.Messages.TABLE, null, values);
                insertedUri = ContentUris.withAppendedId(uri, id);

                ContentResolver resolver = getContext().getContentResolver();
                resolver.notifyChange(DB.Messages.CONTENT_URI, null);
                break;
            }
            case CONTENTS: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                long id = db.insert(DB.Contents.TABLE, null, values);
                insertedUri = ContentUris.withAppendedId(uri, id);

                ContentResolver resolver = getContext().getContentResolver();
                resolver.notifyChange(DB.Contents.CONTENT_URI, null);
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
            case CONTENTS_ID: {
                long id = new Long(uri.getLastPathSegment()).longValue();
                String whereclause = DB.Contents._ID + " = " + id;
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                updates = db.update(DB.Contents.TABLE, values, whereclause, null);

                ContentResolver resolver = getContext().getContentResolver();
                Uri notifyUri = ContentUris.withAppendedId(DB.Contents.CONTENT_URI, id);
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
            default:
                break;
        }
        return updates;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int affected = 0;
        long id = new Long(uri.getLastPathSegment()).longValue();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        switch (mUriMatcher.match(uri)) {
            case IMAGES_ID: {
                affected = db.delete(DB.Images.TABLE, DB.Images._ID + "= ?",
                        new String[]{String.valueOf(id)});
                break;
            }

            case MESSAGE_ID: {
                ContentResolver resolver = getContext().getContentResolver();

                // delete the addresses owned by the message id
                uri = ContentUris.withAppendedId(DB.Addresses.CONTENT_URI, id);
                resolver.delete(uri, null, null);

                // delete the contents owned by the message id
                uri = ContentUris.withAppendedId(DB.Contents.CONTENT_URI, id);
                resolver.delete(uri, null, null);

                // delete the specified message row
                try {
                    db.beginTransaction();
                    affected += db.delete(DB.Messages.TABLE, DB.Messages._ID + "= ?",
                            new String[]{String.valueOf(id)});
                    db.setTransactionSuccessful();
                } finally {
                    if (db.inTransaction()) {
                        db.endTransaction();
                    }
                }

                resolver.notifyChange(DB.Messages.CONTENT_URI, null);
                resolver.notifyChange(ContentUris.withAppendedId(DB.Messages.CONTENT_URI, id), null);
                break;
            }
            case ADDRESSES_ID: {
                affected = db.delete(DB.Addresses.TABLE, DB.Addresses.MESSAGE_OWNER + "= ?",
                        new String[]{String.valueOf(id)});
                break;
            }
            case CONTENTS_ID: {
                // find the messages where the content owns as a rfc822 message
                Cursor cursor = null;
                try {
                    ContentResolver resolver = getContext().getContentResolver();

                    db.beginTransaction();

                    selection = DB.Messages.MESSAGE_OWNER + " = ?";
                    selectionArgs = new String[]{String.valueOf(id)};

                    cursor = resolver.query(DB.Contents.CONTENT_URI, null,
                            selection, selectionArgs, null);

                    if (cursor.moveToFirst()) {
                        do {
                            int index = cursor.getColumnIndex(DB.Messages.MESSAGE_OWNER);
                            long messageId = cursor.getLong(index);
                            uri = ContentUris.withAppendedId(DB.Messages.CONTENT_URI, messageId);
                            resolver.delete(uri, null, null);
                        } while (cursor.moveToNext());
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (db.inTransaction()) {
                        db.endTransaction();
                    }
                }

                affected = db.delete(DB.Contents.TABLE, DB.Contents.MESSAGE_OWNER + "= ?",
                        new String[]{String.valueOf(id)});
                break;
            }
            default:
                break;
        }

        return affected;
    }
}
