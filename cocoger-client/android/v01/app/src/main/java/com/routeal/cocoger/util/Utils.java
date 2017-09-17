package com.routeal.cocoger.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.LocationAddress;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Created by nabe on 7/3/17.
 */

public class Utils {
    private final static String TAG = "Utils";

    private static Random mRand = new Random();

    public static int randInt(int min, int max) {
        return mRand.nextInt((max - min) + 1) + min;
    }

    public static <K, V> Map<K, V> diffMaps(Map<? extends K, ? extends V> left, Map<? extends K, ? extends V> right) {
        Map<K, V> difference = new HashMap<>();
        difference.putAll(left);
        difference.putAll(right);
        difference.entrySet().removeAll(left.size() <= right.size() ? left.entrySet() : right.entrySet());
        return difference;
    }

    public static void showSoftKeyboard(Activity activity, View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static void hideSoftKeyboard(Activity activity, View view) {
        InputMethodManager imm = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static ProgressDialog getBusySpinner(Context context) {
        ProgressDialog dialog = ProgressDialog.show(context, null, null, false, true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.progressbar_spinner);
        return dialog;
    }

    public static BitmapDescriptor getBitmapDescriptor(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static Drawable getIconDrawable(Context context, int resourceId, int backgroundColor) {
        Drawable drawable = ContextCompat.getDrawable(context, resourceId);
        drawable.mutate();
        if (backgroundColor >= 0) {
            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, backgroundColor));
        }
        return drawable;
    }

    public static Drawable getIconDrawable(Context context, int resourceId) {
        return getIconDrawable(context, resourceId, -1);
    }

    public static LatLng getLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public static Location getLocation(LatLng ll) {
        Location loc = new Location("");
        loc.setLongitude(ll.longitude);
        loc.setLatitude(ll.latitude);
        return loc;
    }

    public static Location getLocation(LocationAddress la) {
        Location l = new Location("");
        l.setAltitude(la.getAltitude());
        l.setLatitude(la.getLatitude());
        l.setLongitude(la.getLongitude());
        l.setSpeed(la.getSpeed());
        l.setTime(la.getTimestamp());
        return l;
    }

    public static Location getLocation(Address a) {
        Location l = new Location("");
        l.setLatitude(a.getLatitude());
        l.setLongitude(a.getLongitude());
        return l;
    }

    public static Address getAddress(LocationAddress la) {
        Address a = new Address(Locale.getDefault());
        a.setAdminArea(la.getAdminArea());
        a.setCountryName(la.getCountryName());
        a.setLatitude(la.getLatitude());
        a.setLocality(la.getLocality());
        a.setLongitude(la.getLongitude());
        a.setPostalCode(la.getPostalCode());
        a.setSubAdminArea(la.getSubAdminArea());
        a.setSubLocality(la.getSubLocality());
        a.setSubThoroughfare(la.getSubThoroughfare());
        a.setThoroughfare(la.getThoroughfare());
        return a;
    }

    public static String getAddressLine(Address a) {
        String str = a.getAddressLine(0);
        if (str != null && !str.isEmpty()) {
            return str;
        }
        str = "";
        str += (a.getSubThoroughfare() == null) ? "" : a.getSubThoroughfare();
        if (str.length() > 0 && str.charAt(str.length() - 1) != ' ') str += ", ";
        str += (a.getThoroughfare() == null) ? "" : a.getThoroughfare();
        if (str.length() > 0 && str.charAt(str.length() - 1) != ' ') str += ", ";
        str += (a.getSubLocality() == null) ? "" : a.getSubLocality();
        if (str.length() > 0 && str.charAt(str.length() - 1) != ' ') str += ", ";
        str += (a.getLocality() == null) ? "" : a.getLocality();
        if (str.length() > 0 && str.charAt(str.length() - 1) != ' ') str += ", ";
        str += (a.getAdminArea() == null) ? "" : a.getAdminArea();
        if (str.length() > 0 && str.charAt(str.length() - 1) != ' ') str += ", ";
        str += (a.getPostalCode() == null) ? "" : a.getPostalCode();
        return str;
    }

    public static String getAddressLine(Address address, int range) {
        String locationName = "";

        LocationRange value = LocationRange.to(range);

        switch (value) {
            case NONE:
                break;
            case CURRENT:
                locationName = getAddressLine(address);
                break;
            case SUBTHOROUGHFARE:
                locationName += address.getSubThoroughfare() + ", ";
            case THOROUGHFARE:
                locationName += address.getThoroughfare() + ", ";
            case SUBLOCALITY:
                locationName += address.getSubLocality() + ", ";
            case LOCALITY:
                locationName += address.getLocality() + ", ";
            case SUBADMINAREA:
                locationName += address.getSubAdminArea() + ", ";
            case ADMINAREA:
                locationName += address.getAdminArea() + ", ";
            case COUNTRY:
                locationName += address.getCountryName();
            default:
                break;
        }

        return locationName;
    }

    public static Location getRangedLocation(Address address, int range) {
        Location location = null;

        LocationRange value = LocationRange.to(range);

        if (value == LocationRange.NONE) return null;

        if (value == LocationRange.CURRENT) return Utils.getLocation(address);

        String locationName = Utils.getAddressLine(address, range);

        if (!locationName.isEmpty()) {
            Log.d(TAG, "getRangedLocation:" + locationName);

            try {
                List<Address> newAddresses =
                        new Geocoder(MainApplication.getContext(), Locale.getDefault())
                                .getFromLocationName(locationName, 1);
                Address newAddress = newAddresses.get(0);
                if (newAddress != null) {
                    location = Utils.getLocation(newAddress);
                }
            } catch (Exception e) {
            }
        }

        return location;
    }

    public static Location getRangedLocation(Location location, Address address, int range) {
        if (range == LocationRange.CURRENT.range) {
            return location;
        }
        return Utils.getRangedLocation(address, range);
    }

    public static Address getAddress(Location location) {
        Address address = null;
        try {
            List<Address> addresses =
                    new Geocoder(MainApplication.getContext(), Locale.getDefault())
                            .getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            address = addresses.get(0);
        } catch (Exception e) {
        }
        return address;
    }

    public static double distanceTo(Location a, Location b) {
        return distanceImpl(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude());
    }

    private static double distanceImpl(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist * 1.609344);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public static Bitmap cropCircle(Bitmap from) {
        return cropCircle(from, -1);
    }

    public static Bitmap cropCircle(Bitmap from, int borderColor) {
        if (from == null || from.isRecycled()) {
            return null;
        }

        int borderWidth = 4;
        int width = from.getWidth() + borderWidth;
        int height = from.getHeight() + borderWidth;

        Bitmap to = Bitmap.createBitmap(width, height, from.getConfig());

        BitmapShader shader =
                new BitmapShader(from, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);

        float radius = width > height ? ((float) height) / 2f : ((float) width) / 2f;

        Canvas canvas = new Canvas(to);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawCircle(width / 2, height / 2, radius, paint);

        if (borderColor >= 0) {
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(borderWidth);
            canvas.drawCircle(width / 2, height / 2, radius - borderWidth / 2, paint);
        }

        return to;

/*

        // create a copy of the bitmap
        Bitmap to = Bitmap.createBitmap(from.getWidth(), from.getHeight(), from.getConfig());

        BitmapShader shader =
                new BitmapShader(from, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);

        Paint paint = new Paint();
        paint.setShader(shader);
        paint.setAntiAlias(true);
        paint.setDither(true);

        Paint paintBorder = null;
        if (hasBorder) {
            paintBorder = new Paint();
            paintBorder.setAntiAlias(true);
            paintBorder.setShadowLayer(4.0f, 0.0f, 2.0f, Color.BLACK);
            paintBorder.setColor(Color.WHITE);
        }

        float r = (from.getWidth() + from.getHeight()) / 4f;
        Canvas canvas = new Canvas(to);
        if (paintBorder != null) {
            canvas.drawCircle(r, r, r, paintBorder);
            canvas.drawCircle(r, r, r - 4.0f, paint);
        } else {
            canvas.drawCircle(r, r, r, paint);
        }

        return to;
*/
    }
}
