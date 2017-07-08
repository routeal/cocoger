package com.routeal.cocoger.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.widget.ImageView;

import com.routeal.cocoger.R;
import com.routeal.cocoger.provider.DB;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.io.ByteArrayOutputStream;

public class ImageUtil {

    class CircleTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }

    /*
    void load(Context context, String url, ImageView target) {
        load(context, url, target, 0, false);
    }

    void load(Context context, String url, ImageView target, int placeholder, boolean cacheInMemory) {
        if (!cacheInMemory) {
            ;
        }
        int res = (placeholder == 0) ? 0 : placeholder;
        Picasso.with(context).load(url).placeholder(res).into(target);
    }
    */

    void loadProfilePicture(Context context, String url, ImageView target) {
        loadRound(context, url, target, R.drawable.ic_face_black_24dp, false);
    }

    void loadRound(final Context context, final String url, final ImageView target, int placeholder, final boolean cacheInMemory) {
        if (!cacheInMemory) {
            Cursor cursor = null;
            try {
                Uri uri = DB.Images.CONTENT_URI;
                String selection = DB.Images.NAME + "=?";
                String[] selectionArgs = new String[]{url};
                ContentResolver contentResolver = context.getContentResolver();
                cursor = contentResolver.query(uri, null, selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(DB.Images.DATA);
                    byte blob[] = cursor.getBlob(index);
                    if (blob != null && blob.length > 0) {
                        Bitmap bm = BitmapFactory.decodeByteArray(blob, 0, blob.length);
                        target.setImageBitmap(bm);
                        return;
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        int res = (placeholder == 0) ? 0 : placeholder;

        Callback mCallback = new Callback() {
            @Override
            public void onSuccess() {
                // caching is handled by Picasso
                if (cacheInMemory) return;

                // get a bitmap from the imageview
                Bitmap bitmap = ((BitmapDrawable) target.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                // get a byte array from the bitmap
                byte[] imageInByte = baos.toByteArray();
                if (imageInByte == null || imageInByte.length == 0) {
                    return; // empty image
                }

                // save the byte array into db
                ContentValues values = new ContentValues();
                values.put(DB.Images.NAME, url);
                values.put(DB.Images.DATA, imageInByte);
                ContentResolver contentResolver = context.getContentResolver();
                contentResolver.insert(DB.Images.CONTENT_URI, values);
            }

            @Override
            public void onError() {
                // do nothing
            }
        };

        RequestCreator creator = Picasso.with(context).load(url).placeholder(res).transform(new CircleTransform());
        creator.into(target, mCallback);
    }

}