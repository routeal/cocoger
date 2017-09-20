package com.routeal.cocoger.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by hwatanabe on 4/11/17.
 */

public class ProviderHelper extends SQLiteOpenHelper {

    public ProviderHelper(Context context) {
        super(context, DB.DATABASE_NAME, null, DB.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL(DB.Locations.CREATE_STATEMENT);
            db.execSQL(DB.GeoLocations.CREATE_STATEMENT);
            db.execSQL(DB.ReverseGeoLocations.CREATE_STATEMENT);
            db.execSQL(DB.Images.CREATE_STATEMENT);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int current, int targetVersion) {
    }
}
