package com.routeal.cocoger.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.routeal.cocoger.provider.DBUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nabe on 9/5/17.
 */

// FIXME: SIZE SHOULD BE IN THE ARGUMENT

public class LoadImage extends AsyncTask<String, Void, List<Bitmap>> {
    private final static String TAG = "LoadImage";

    @Override
    protected List<Bitmap> doInBackground(String... params) {
        List<Bitmap> bitmaps = new ArrayList<>();
        for (int i = 0; i < params.length; i++) {
            if (isCancelled()) return null;

            String url = params[i];

            // get from the database
            byte[] data = DBUtil.getImage(url);

            Bitmap bitmap = null;

            if (data == null) {
                bitmap = getBitmapFromURL(url);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                DBUtil.saveImage(url, byteArray);
            } else {
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, null);
            }

            if (bitmap != null) {
                bitmaps.add(bitmap);
            }
/*
            // dose not exist in the database
            if (data == null) {
                URL net;
                try {
                    net = new URL(url);
                } catch (MalformedURLException e) {
                    Log.e(TAG, "Incorrect URL", e);
                    continue;
                }

                InputStream netstream;
                try {
                    netstream = net.openStream();
                } catch (IOException e) {
                    Log.e(TAG, "Can not open stream", e);
                    continue;
                }

                InputStream in = new BufferedInputStream(netstream);
                ByteArrayOutputStream ds = new ByteArrayOutputStream();
                BufferedOutputStream out = new BufferedOutputStream(ds);
                try {
                    iocopy(in, out);
                } catch (IOException e) {
                }
                try {
                    out.flush();
                } catch (IOException e) {
                    continue;
                }

                data = ds.toByteArray();

                if (data == null || data.length == 0) continue;

                DBUtil.saveImage(url, data);
            }

            // FIXME: not working all the time
            // https://developer.android.com/topic/performance/graphics/load-bitmap.html
            BitmapFactory.Options opts = new BitmapFactory.Options();
            //opts.inJustDecodeBounds = true;
            //opts.inPreferredConfig = Bitmap.Config.RGB_565;

            //BitmapFactory.decodeByteArray(data, 0, data.length, opts);
            //opts.inSampleSize = calculateInSampleSize(opts, 128, 128);

            //opts.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);

            bitmaps.add(bitmap);
*/

        }
        return bitmaps;
    }

    static Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return null;
    }

    Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);

        return resizedBitmap;
    }

    private static int iocopy(InputStream input, OutputStream output) throws IOException {
        // FIXME: should grow dynamically
        byte[] buffer = new byte[8192*4];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    protected void onPostExecute(List<Bitmap> bitmaps) {
        super.onPostExecute(bitmaps);
    }

    public static class LoadImageView extends LoadImage {
        ImageView imageView;
        boolean crop;
        int borderColor = -1;

        public LoadImageView(ImageView imageView) {
            this(imageView, true);
        }

        public LoadImageView(ImageView imageView, boolean crop) {
            this.imageView = imageView;
            this.crop = crop;
        }

        public LoadImageView(ImageView imageView, boolean crop, int borderColor) {
            this.imageView = imageView;
            this.crop = crop;
            this.borderColor = borderColor;
        }

        @Override
        protected void onPostExecute(List<Bitmap> bitmaps) {
            super.onPostExecute(bitmaps);
            if (bitmaps.isEmpty()) {
                return;
            }
            if (crop) {
                Bitmap cropped = Utils.cropCircle(bitmaps.get(0), borderColor);
                imageView.setImageBitmap(cropped);
            } else {
                imageView.setImageBitmap(bitmaps.get(0));
            }
        }
    }

    public interface LoadImageListener {
        void onSuccess(Bitmap bitmap);
    }

    public static class LoadImageAsync extends LoadImage {
        boolean crop;
        LoadImageListener listener;
        int borderColor = -1;

        public LoadImageAsync(LoadImageListener listener) {
            this(true, listener);
        }

        public LoadImageAsync(boolean crop, LoadImageListener listener) {
            this.crop = crop;
            this.listener = listener;
        }

        public LoadImageAsync(boolean crop, int borderColor, LoadImageListener listener) {
            this.crop = crop;
            this.listener = listener;
            this.borderColor = borderColor;
        }

        @Override
        protected void onPostExecute(List<Bitmap> bitmaps) {
            super.onPostExecute(bitmaps);
            if (bitmaps.isEmpty()) {
                return;
            }
            if (crop) {
                Bitmap cropped = Utils.cropCircle(bitmaps.get(0), borderColor);
                listener.onSuccess(cropped);
            } else {
                listener.onSuccess(bitmaps.get(0));
            }
        }
    }

    public static class LoadMarkerImage extends LoadImage {
        private final static int MARKER_SZIE = 128;

        Marker marker;
        int borderColor = -1;

        public LoadMarkerImage(Marker marker) {
            this(marker, -1);
        }

        public LoadMarkerImage(Marker marker, int borderColor) {
            this.marker = marker;
            this.borderColor = borderColor;
        }

        @Override
        protected void onPostExecute(List<Bitmap> bitmaps) {
            if (isCancelled()) return;
            super.onPostExecute(bitmaps);
            Bitmap combined = combineBitmaps(bitmaps, MARKER_SZIE);
            Bitmap cropped = Utils.cropCircle(combined, borderColor);
            combined.recycle();
            if (cropped.isRecycled()) return;
            if (marker.isVisible()) {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(cropped));
            }
        }
    }

    static Bitmap combineBitmaps(List<Bitmap> bitmaps, int size) {
        MultipleDrawable drawable = new MultipleDrawable(bitmaps);
        if (drawable == null) return null;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}
