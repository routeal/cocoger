package com.routeal.cocoger.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nabe on 9/4/17.
 */

public class MultipleDrawable extends Drawable {

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    List<PhotoItem> items = new ArrayList<>();
    List<Bitmap> bitmaps;
    Rect bounds;

    public MultipleDrawable(List<Bitmap> bitmaps) {
        this.bitmaps = bitmaps;
    }

    private Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        return ThumbnailUtils.extractThumbnail(source, newWidth, newHeight);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        for (int i = 0; i < items.size(); i++) {
            PhotoItem item = items.get(i);
            canvas.drawBitmap(item.bitmap, null, item.rect, paint);
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        init(bounds);
    }

    void init(Rect bounds) {
        this.bounds = bounds;
        items.clear();
        if (bitmaps.size() == 1) {
            Bitmap bitmap = scaleCenterCrop(bitmaps.get(0), bounds.width(), bounds.height());
            items.add(new PhotoItem(bitmap, new Rect(0, 0, bounds.width(), bounds.height())));
        } else if (bitmaps.size() == 2) {
            Bitmap bitmap1 = scaleCenterCrop(bitmaps.get(0), bounds.width(), bounds.height() / 2);
            Bitmap bitmap2 = scaleCenterCrop(bitmaps.get(1), bounds.width(), bounds.height() / 2);
            items.add(new PhotoItem(bitmap1, new Rect(0, 0, bounds.width() / 2, bounds.height())));
            items.add(new PhotoItem(bitmap2, new Rect(bounds.width() / 2, 0, bounds.width(), bounds.height())));
        } else if (bitmaps.size() == 3) {
            Bitmap bitmap1 = scaleCenterCrop(bitmaps.get(0), bounds.width(), bounds.height() / 2);
            Bitmap bitmap2 = scaleCenterCrop(bitmaps.get(1), bounds.width() / 2, bounds.height() / 2);
            Bitmap bitmap3 = scaleCenterCrop(bitmaps.get(2), bounds.width() / 2, bounds.height() / 2);
            items.add(new PhotoItem(bitmap1, new Rect(0, 0, bounds.width() / 2, bounds.height())));
            items.add(new PhotoItem(bitmap2, new Rect(bounds.width() / 2, 0, bounds.width(), bounds.height() / 2)));
            items.add(new PhotoItem(bitmap3, new Rect(bounds.width() / 2, bounds.height() / 2, bounds.width(), bounds.height())));
        } else if (bitmaps.size() == 4) {
            Bitmap bitmap1 = scaleCenterCrop(bitmaps.get(0), bounds.width() / 2, bounds.height() / 2);
            Bitmap bitmap2 = scaleCenterCrop(bitmaps.get(1), bounds.width() / 2, bounds.height() / 2);
            Bitmap bitmap3 = scaleCenterCrop(bitmaps.get(2), bounds.width() / 2, bounds.height() / 2);
            Bitmap bitmap4 = scaleCenterCrop(bitmaps.get(3), bounds.width() / 2, bounds.height() / 2);
            items.add(new PhotoItem(bitmap1, new Rect(0, 0, bounds.width() / 2, bounds.height() / 2)));
            items.add(new PhotoItem(bitmap2, new Rect(0, bounds.height() / 2, bounds.width() / 2, bounds.height())));
            items.add(new PhotoItem(bitmap3, new Rect(bounds.width() / 2, 0, bounds.width(), bounds.height() / 2)));
            items.add(new PhotoItem(bitmap4, new Rect(bounds.width() / 2, bounds.height() / 2, bounds.width(), bounds.height())));
        }
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    class PhotoItem {
        Bitmap bitmap;
        Rect rect;

        PhotoItem(Bitmap bitmap, Rect rect) {
            this.bitmap = bitmap;
            this.rect = rect;
        }
    }
}
