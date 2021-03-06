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
    private LoadImageListener listener;

    public LoadImage(ImageView imageView, boolean crop, LoadImageListener listener) {
        this.imageView = imageView;
        this.crop = crop;
        this.listener = listener;
    }

    public LoadImage(ImageView imageView) {
        this(imageView, false, null);
    }

    public LoadImage(LoadImageListener listener) {
        this(null, true, listener);
    }

    public LoadImage(boolean crop, LoadImageListener listener) {
        this(null, crop, listener);
    }

    private void onDone(byte bytes[]) {
        if (bytes != null && bytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
            if (imageView != null) {
                if (crop) {
                    Bitmap cropped = Utils.cropCircle(bitmap);
                    imageView.setImageBitmap(cropped);
                } else {
                    imageView.setImageBitmap(bitmap);
                }
            }
            if (listener != null) {
                if (crop) {
                    Bitmap cropped = Utils.cropCircle(bitmap);
                    listener.onSuccess(cropped);
                } else {
                    listener.onSuccess(bitmap);
                }
            }
        } else {
            if (listener != null) listener.onSuccess(null);
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

    private DownloadTask task;

    private void load(String url) {
        task = new DownloadTask();
        task.execute(url);
    }

    public void cancel() {
        if (task != null) {
            task.cancel(true);
            task = null;
        }
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
        protected void onCancelled() {
            if (listener != null) listener.onSuccess(null);
        }

        @Override
        protected void onCancelled(byte[] bytes) {
            if (listener != null) listener.onSuccess(null);
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            if (!isCancelled()) {
                onDone(bytes);
            }
        }
    }
}