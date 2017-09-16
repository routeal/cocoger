package com.routeal.cocoger.util;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.routeal.cocoger.MainApplication;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nabe on 9/5/17.
 */

public class LoadImage extends AsyncTask<String, Void, List<Bitmap>> {
    @Override
    protected List<Bitmap> doInBackground(String... params) {
        List<Bitmap> bitmaps = new ArrayList<>();
        for (int i = 0; i < params.length; i++) {
            String url = params[i];
            try {
                if (isCancelled()) return null;
                Bitmap bitmap = Picasso.with(MainApplication.getContext()).load(url).get();
                bitmaps.add(bitmap);
            } catch (IOException e) {
            }
        }
        return bitmaps;
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

    public
    interface LoadImageListener {
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
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(cropped));
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
