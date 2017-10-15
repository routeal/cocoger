package com.routeal.cocoger.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.provider.DBUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by nabe on 9/5/17.
 */
public class LoadImage {
    private final static String TAG = "LoadImage";

    private ImageView imageView;
    private boolean crop;
    private int borderColor;
    private LoadImageListener listener;

    public LoadImage(ImageView imageView, boolean crop, int borderColor, LoadImageListener listener) {
        this.imageView = imageView;
        this.crop = crop;
        this.borderColor = borderColor;
        this.listener = listener;
    }

    public LoadImage(ImageView imageView) {
        this(imageView, false, 0, null);
    }

    public LoadImage(LoadImageListener listener) {
        this(null, true, 0, listener);
    }

    public LoadImage(LoadImageListener listener, boolean crop) {
        this(null, crop, 0, listener);
    }

    private void onDone(byte bytes[]) {
        if (bytes != null && bytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
            if (imageView != null) {
                if (crop) {
                    Bitmap cropped = Utils.cropCircle(bitmap, borderColor);
                    imageView.setImageBitmap(cropped);
                } else {
                    imageView.setImageBitmap(bitmap);
                }
            }
            if (listener != null) {
                if (crop) {
                    Bitmap cropped = Utils.cropCircle(bitmap, borderColor);
                    listener.onSuccess(cropped);
                } else {
                    listener.onSuccess(bitmap);
                }
            }
        }
    }

    public void loadPlace(String uid, String key) {
        final String dbName = uid + "_" + key + "_" + FB.PLACE_IMAGE;
        Log.d(TAG, "load:" + dbName);
        byte[] bytes = DBUtil.getImage(dbName);
        if (bytes != null) {
            onDone(bytes);
        } else {
            // if not found in the local database, get it from the FB storage
            FB.downloadPlaceImage(uid, key, new FB.DownloadDataListener() {
                @Override
                public void onSuccess(byte[] bytes) {
                    onDone(bytes);
                    DBUtil.saveImage(dbName, bytes);
                }

                @Override
                public void onFail(String err) {
                    onDone(null);
                }
            });
        }
    }

    public void loadProfile(String key) {
        crop = true;
        final String dbName = key + "_" + FB.PROFILE_IMAGE;
        Log.d(TAG, "load:" + dbName);
        byte[] bytes = DBUtil.getImage(dbName);
        if (bytes != null) {
            onDone(bytes);
        } else {
            // if not found in the local database, get it from the FB storage
            FB.downloadProfileImage(key, new FB.DownloadDataListener() {
                @Override
                public void onSuccess(byte[] bytes) {
                    onDone(bytes);
                    DBUtil.saveImage(dbName, bytes);
                }

                @Override
                public void onFail(String err) {
                    onDone(null);
                }
            });
        }
    }

    public void loadUrl(String url) {
        load(url);
    }

    private void load(String url) {
        new DownloadTask().execute(url);
    }

    public interface LoadImageListener {
        void onSuccess(Bitmap bitmap);
    }

    private class DownloadTask extends AsyncTask<String, Void, byte[]> {
        @Override
        protected byte[] doInBackground(String... params) {
            try {
                byte[] bytes = DBUtil.getImage(params[0]);

                if (bytes != null) {
                    return bytes;
                }

                URL url = new URL(params[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                InputStream input = new BufferedInputStream(url.openStream());

                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];

                int len = 0;
                while ((len = input.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }

                input.close();

                bytes = byteBuffer.toByteArray();

                DBUtil.saveImage(params[0], bytes);

                return bytes;
            } catch (Exception e) {
                /* empty */
            }
            return new byte[0];
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            onDone(bytes);
        }
    }
}