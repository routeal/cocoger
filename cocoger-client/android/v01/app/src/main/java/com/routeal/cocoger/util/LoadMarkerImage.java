package com.routeal.cocoger.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.provider.DBUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hwatanabe on 10/10/17.
 */

public class LoadMarkerImage {
    private final static int MARKER_SZIE = 128;

    private Marker marker;
    private int borderColor;
    private int numLoaded;
    private int totalImages;
    private List<Bitmap> bitmaps;
    private boolean canceled;

    public LoadMarkerImage(Marker marker) {
        this(marker, 0);
    }

    private LoadMarkerImage(Marker marker, int borderColor) {
        this.marker = marker;
        this.borderColor = borderColor;
        this.numLoaded = 0;
        this.canceled = false;
        this.bitmaps = new ArrayList<>();
    }

    private static Bitmap combineBitmaps(List<Bitmap> bitmaps, int size) {
        MultipleDrawable drawable = new MultipleDrawable(bitmaps);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void cancel() {
        canceled = true;
    }

    private void onDone() {
        if (!canceled && numLoaded == totalImages) {
            Bitmap combined = combineBitmaps(bitmaps, MARKER_SZIE);
            if (combined == null) {
                return;
            }

            Bitmap cropped = Utils.cropCircle(combined, borderColor);
            combined.recycle();

            if (marker.isVisible()) {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(cropped));
            }
        }
    }

    public void load(String... params) {
        totalImages = params.length;
        for (String key : params) {
            final String dbname = key + "_" + FB.PROFILE_IMAGE;
            // get from the database
            byte[] bytes = DBUtil.getImage(dbname);
            if (bytes != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                bitmaps.add(bitmap);
                numLoaded++;
                onDone();
            } else {
                // if not found in the local database, get it from the FB storage
                FB.downloadData(FB.PROFILE_IMAGE, key, new FB.DownloadDataListener() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        DBUtil.saveImage(dbname, bytes);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                        bitmaps.add(bitmap);
                        numLoaded++;
                        onDone();
                    }

                    @Override
                    public void onFail(String err) {
                        numLoaded++;
                        onDone();
                    }
                });
            }
        }
    }
}