package com.routeal.cocoger.util;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

/**
 * Created by nabe on 7/22/17.
 */

public class CircleTransform implements Transformation {
    int size = 128;
    boolean hasBorder = false;

    public CircleTransform() {}

    public CircleTransform(boolean hasBorder) {
        this.hasBorder = hasBorder;
    }

    public CircleTransform(int size) {
        this.size = size;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        Bitmap squaredBitmap = Bitmap.createScaledBitmap(source, size, size, false);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        Paint paintBorder = null;
        if (hasBorder) {
            paintBorder = new Paint();
            paintBorder.setAntiAlias(true);
            paintBorder.setShadowLayer(4.0f, 0.0f, 2.0f, Color.BLACK);
        }

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap,
                BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);
        paint.setDither(true);

        float r = size / 2f;
        if (hasBorder && paintBorder != null) {
            canvas.drawCircle(r, r, r, paintBorder);
            canvas.drawCircle(r, r, r - 4.0f, paint);
        } else {
            canvas.drawCircle(r, r, r, paint);
        }

        squaredBitmap.recycle();

        return bitmap;
    }

    @Override
    public String key() {
        return "circle";
    }
}