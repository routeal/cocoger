package com.routeal.cocoger.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.model.Device;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.LocationAddress;
import com.routeal.cocoger.provider.DBUtil;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by nabe on 7/3/17.
 */

public class Utils {
    private final static String TAG = "Utils";

    public static String getShortDateTime(long epoch) {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        return df.format(new Date(epoch));
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

    public static byte[] getBitmapBytes(Context context, Bitmap bitmap) {
        byte[] bytes = null;
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
            bytes = stream.toByteArray();
        } catch (Exception e) {
            Log.d(TAG, "Failed to convert bitmap into byte array");
        }
        return bytes;
    }

    public static byte[] getBitmapBytes(Context context, Uri uri) {
        byte[] bytes = null;
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is == null) {
                return null;
            }

            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }

            bytes = byteBuffer.toByteArray();
        } catch (Throwable e) {
            Log.d(TAG, "Failed to convert bitmap into byte array");
        }
        return bytes;
    }

    public static Bitmap getBitmap(Context context, Uri uri) {
        Bitmap bitmap = null;
        try {
            InputStream in = context.getContentResolver().openInputStream(uri);
            Drawable drawable = Drawable.createFromStream(in, uri.toString());
            if (drawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            }
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Failed to read a local file into bitmap", e);
        }
        return bitmap;
    }

    public static LatLng getLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public static Location getLocation(LatLng ll) {
        Location loc = new Location("");
        loc.setLatitude(ll.latitude);
        loc.setLongitude(ll.longitude);
        return loc;
    }

    public static Location getLocation(double latitude, double longitude) {
        Location loc = new Location("");
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);
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
        return getAddressLine(a, false);
    }

    public static String getAddressLine(Address a, boolean shortFormat) {
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
        if (!shortFormat) {
            if (str.length() > 0 && str.charAt(str.length() - 1) != ' ') str += ", ";
            str += (a.getAdminArea() == null) ? "" : a.getAdminArea();
            if (str.length() > 0 && str.charAt(str.length() - 1) != ' ') str += ", ";
            str += (a.getPostalCode() == null) ? "" : a.getPostalCode();
        }
        return str;
    }

    public static Bitmap createImage(int width, int height, int color, String str) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        // draw circle
        Paint paint2 = new Paint();
        paint2.setColor(color);
        paint2.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(width / 2, height / 2, width / 2, paint2);
        // draw text inisde
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(32);
        paint.setTextScaleX(1);
        Rect result = new Rect();
        paint.getTextBounds(str, 0, str.length(), result);
        float x = (width - result.width()) / 2;
        float y = (height - result.height()) / 2 + result.height();
        canvas.drawText(str, x, y, paint);
        return bitmap;
    }

    public static String getAddressLine(Address address, int range) {
        return getAddressLine(address, range, false);
    }

    public static String getAddressLine(Address address, int range, boolean shortFormat) {
        if (address == null) {
            return "";
        }
        LocationRange value = LocationRange.to(range);
        if (value == LocationRange.NONE) {
            return "";
        }
        if (value == LocationRange.CURRENT) {
            return getAddressLine(address, shortFormat);
        }
        String locationName = "";
        switch (value) {
            case SUBTHOROUGHFARE:
                locationName += (address.getSubThoroughfare() == null) ?
                        "" : (address.getSubThoroughfare() + ", ");
            case THOROUGHFARE:
                locationName += (address.getThoroughfare() == null) ?
                        "" : (address.getThoroughfare() + ", ");
            case SUBLOCALITY:
                locationName += (address.getSubLocality() == null) ?
                        "" : (address.getSubLocality() + ", ");
            case LOCALITY:
                locationName += (address.getLocality() == null) ?
                        "" : (address.getLocality() + ", ");
            case SUBADMINAREA:
                locationName += (address.getSubAdminArea() == null) ?
                        "" : (address.getSubAdminArea() + ", ");
            case ADMINAREA:
                locationName += (address.getAdminArea() == null) ?
                        "" : (address.getAdminArea() + ", ");
            case COUNTRY:
                locationName += (address.getCountryName() == null) ?
                        "" : address.getCountryName();
            default:
                break;
        }
        return locationName;
    }

    private static Location getRangedLocation(Address address, int range) {
        LocationRange value = LocationRange.to(range);
        if (value == LocationRange.NONE) {
            return null;
        }
        if (value == LocationRange.CURRENT) {
            return Utils.getLocation(address);
        }
        Location location = null;
        String locationName = Utils.getAddressLine(address, range);
        if (locationName != null && !locationName.isEmpty()) {
            Log.d(TAG, "getRangedLocation:" + locationName);
            location = getFromAddress(locationName);
        }
        return location;
    }

    public static Location getRangedLocation(Location location, Address address, int range) {
        if (range == LocationRange.CURRENT.range) {
            return location;
        }
        return Utils.getRangedLocation(address, range);
    }

    public static Address getAddress(LatLng location) {
        Address address = null;
        try {
            List<Address> addresses =
                    new Geocoder(MainApplication.getContext(), Locale.getDefault())
                            .getFromLocation(location.latitude, location.longitude, 1);
            address = addresses.get(0);
        } catch (Exception e) {
            Log.d(TAG, "getAddress:", e);
        }
        return address;
    }

    public static Address getAddress(Location location) {
        return getAddress(getLatLng(location));
    }

    public static double distanceTo(Location a, Location b) {
        LatLng point1 = getLatLng(a);
        LatLng point2 = getLatLng(b);
        return SphericalUtil.computeDistanceBetween(point1, point2);
    }

    private static Location getFromAddress(@NonNull String address) {
        Location location = DBUtil.getLocation(address);
        if (location == null) {
            try {
                Log.d(TAG, "getFromAddress: from Geocoder");
                Geocoder geocoder = new Geocoder(MainApplication.getContext(), Locale.getDefault());
                List<Address> newAddresses = geocoder.getFromLocationName(address, 1);
                if (newAddresses != null && !newAddresses.isEmpty()) {
                    Address newAddress = newAddresses.get(0);
                    location = Utils.getLocation(newAddress);
                }
            } catch (Exception e) {
                Log.d(TAG, "getFromAddress: Geocoder failed:" + e.getLocalizedMessage());
            }
            if (location != null) {
                DBUtil.setLocation(address, location);
            }
        }
        return location;
    }

    public static float getSpeed(Location to, Location from) {
        double distance = Utils.distanceTo(to, from);
        double elapsed = Math.abs((float) ((to.getTime() - from.getTime()) / 1000.0));
        if (elapsed > 0) {
            double speed = distance / elapsed; // meter / seconds
            return (float) speed;
        }
        return 0;
    }

    public static Address getFromLocation(Location location) {
        // get the address from the database within 25 meter
        Address address = DBUtil.getAddress(location, 25);
        if (address == null) {
            List<Address> addressList = null;
            try {
                Log.d(TAG, "getFromLocation: from Geocoder");
                Geocoder geocoder = new Geocoder(MainApplication.getContext(), Locale.getDefault());
                addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (Exception e) {
                Log.d(TAG, "getFromLocation: Geocoder failed:" + e.getLocalizedMessage());
            }
            address = (addressList != null && addressList.size() > 0) ? addressList.get(0) : null;
            if (address != null) {
                DBUtil.saveAddress(location, address);
            }
        } else {
            Log.d(TAG, "getFromLocation: from Local database");
            // overwrite the current location
            address.setLatitude(location.getLatitude());
            address.setLongitude(location.getLongitude());
        }
        return address;
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

        BitmapShader shader = new BitmapShader(from, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);

        float radius = width > height ? ((float) height) / 2f : ((float) width) / 2f;

        Canvas canvas = new Canvas(to);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawCircle(width / 2, height / 2, radius, paint);

        if (borderColor > 0) {
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(borderWidth);
            canvas.drawCircle(width / 2, height / 2, radius - borderWidth / 2, paint);
        }

        return to;
    }

    public static int detectRangeMove(Address n, Address o) {
        if (n == null || o == null) {
            return LocationRange.NONE.range;
        }
        if (LocationRange.to(LocationRange.COUNTRY.range) != null &&
                n.getCountryName() != null && o.getCountryName() != null) {
            if (!n.getCountryName().equals(o.getCountryName())) {
                return LocationRange.COUNTRY.range;
            }
        }
        if (LocationRange.to(LocationRange.ADMINAREA.range) != null &&
                n.getAdminArea() != null && o.getAdminArea() != null) {
            if (!n.getAdminArea().equals(o.getAdminArea())) {
                return LocationRange.ADMINAREA.range;
            }
        }
        if (LocationRange.to(LocationRange.SUBADMINAREA.range) != null &&
                n.getSubAdminArea() != null && o.getSubAdminArea() != null) {
            if (!n.getSubAdminArea().equals(o.getSubAdminArea())) {
                return LocationRange.SUBADMINAREA.range;
            }
        }
        if (LocationRange.to(LocationRange.LOCALITY.range) != null &&
                n.getLocality() != null && o.getLocality() != null) {
            if (!n.getLocality().equals(o.getLocality())) {
                return LocationRange.LOCALITY.range;
            }
        }
        if (LocationRange.to(LocationRange.SUBLOCALITY.range) != null &&
                n.getSubLocality() != null && o.getSubLocality() != null) {
            if (!n.getSubLocality().equals(o.getSubLocality())) {
                return LocationRange.SUBLOCALITY.range;
            }
        }
        if (LocationRange.to(LocationRange.THOROUGHFARE.range) != null &&
                n.getThoroughfare() != null && o.getThoroughfare() != null) {
            if (!n.getThoroughfare().equals(o.getThoroughfare())) {
                return LocationRange.THOROUGHFARE.range;
            }
        }
        if (LocationRange.to(LocationRange.SUBTHOROUGHFARE.range) != null &&
                n.getSubThoroughfare() != null && o.getSubThoroughfare() != null) {
            if (!n.getSubThoroughfare().equals(o.getSubThoroughfare())) {
                return LocationRange.SUBTHOROUGHFARE.range;
            }
        }
        return LocationRange.CURRENT.range;
    }

    public static boolean isEqualAddress(Address currentAddress, Address newAddress, int range) {
        if (currentAddress == null || newAddress == null || range == 0) {
            return false;
        }
        if (LocationRange.to(LocationRange.COUNTRY.range) != null) {
            if (range >= LocationRange.COUNTRY.range) {
                if (currentAddress.getCountryName() != null && newAddress.getCountryName() != null) {
                    if (!currentAddress.getCountryName().equals(newAddress.getCountryName())) {
                        return false;
                    }
                }
            }
        }
        if (LocationRange.to(LocationRange.ADMINAREA.range) != null) {
            if (range >= LocationRange.ADMINAREA.range) {
                if (currentAddress.getAdminArea() != null && newAddress.getAdminArea() != null) {
                    if (!currentAddress.getAdminArea().equals(newAddress.getAdminArea())) {
                        return false;
                    }
                }
            }
        }
        if (LocationRange.to(LocationRange.SUBADMINAREA.range) != null) {
            if (range >= LocationRange.SUBADMINAREA.range) {
                if (currentAddress.getSubAdminArea() != null && newAddress.getSubAdminArea() != null) {
                    if (!currentAddress.getSubAdminArea().equals(newAddress.getSubAdminArea())) {
                        return false;
                    }
                }
            }
        }
        if (LocationRange.to(LocationRange.LOCALITY.range) != null) {
            if (range >= LocationRange.LOCALITY.range) {
                if (currentAddress.getLocality() != null && newAddress.getLocality() != null) {
                    if (!currentAddress.getLocality().equals(newAddress.getLocality())) {
                        return false;
                    }
                }
            }
        }
        if (LocationRange.to(LocationRange.SUBLOCALITY.range) != null) {
            if (range >= LocationRange.SUBLOCALITY.range) {
                if (currentAddress.getSubLocality() != null && newAddress.getSubLocality() != null) {
                    if (!currentAddress.getSubLocality().equals(newAddress.getSubLocality())) {
                        return false;
                    }
                }
            }
        }
        if (LocationRange.to(LocationRange.THOROUGHFARE.range) != null) {
            if (range >= LocationRange.THOROUGHFARE.range) {
                if (currentAddress.getThoroughfare() != null && newAddress.getThoroughfare() != null) {
                    if (!currentAddress.getThoroughfare().equals(newAddress.getThoroughfare())) {
                        return false;
                    }
                }
            }
        }
        if (LocationRange.to(LocationRange.SUBTHOROUGHFARE.range) != null) {
            if (range >= LocationRange.SUBTHOROUGHFARE.range) {
                if (currentAddress.getSubThoroughfare() != null && newAddress.getSubThoroughfare() != null) {
                    if (!currentAddress.getSubThoroughfare().equals(newAddress.getSubThoroughfare())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static String getRangeMoveMessage(Friend friend, Address newAddress, Address oldAddress) {
        String name = friend.getDisplayName();
        int range = friend.getRange();
        if (newAddress.getSubThoroughfare() != null && oldAddress.getSubThoroughfare() != null) {
        }
        return "";
    }

    // For now, the device is saved into the memory
    public static Device getDevice() {
        Device mDevice = new Device();
        mDevice.setDeviceId(getDeviceUniqueId());
        mDevice.setBrand(Build.BRAND);
        mDevice.setModel(Build.MODEL);
        mDevice.setPlatformVersion(Build.VERSION.RELEASE);
        mDevice.setSimulator(isEmulator());
        mDevice.setToken(""); // empty for now
        mDevice.setStatus(Device.FOREGROUND);
        mDevice.setAppVersion(MainApplication.getApplicationVersion());
        mDevice.setTimestamp(System.currentTimeMillis());
        mDevice.setCreated(System.currentTimeMillis());
        return mDevice;
    }

    static boolean isEmulator() {
        int rating = 0;

        if ((Build.PRODUCT.equals("sdk")) || (Build.PRODUCT.equals("google_sdk"))
                || (Build.PRODUCT.equals("sdk_x86")) || (Build.PRODUCT.equals("vbox86p"))) {
            rating++;
        }
        if ((Build.MANUFACTURER.equals("unknown")) || (Build.MANUFACTURER.equals("Genymotion"))) {
            rating++;
        }
        if ((Build.BRAND.equals("generic")) || (Build.BRAND.equals("generic_x86"))) {
            rating++;
        }
        if ((Build.DEVICE.equals("generic")) || (Build.DEVICE.equals("generic_x86")) ||
                (Build.DEVICE.equals("vbox86p"))) {
            rating++;
        }
        if ((Build.MODEL.equals("sdk")) || (Build.MODEL.equals("google_sdk"))
                || (Build.MODEL.equals("Android SDK built for x86"))) {
            rating++;
        }
        if ((Build.HARDWARE.equals("goldfish")) || (Build.HARDWARE.equals("vbox86"))) {
            rating++;
        }
        if ((Build.FINGERPRINT.contains("generic/sdk/generic"))
                || (Build.FINGERPRINT.contains("generic_x86/sdk_x86/generic_x86"))
                || (Build.FINGERPRINT.contains("generic/google_sdk/generic"))
                || (Build.FINGERPRINT.contains("generic/vbox86p/vbox86p"))) {
            rating++;
        }

        return rating > 4;
    }

    private static String getDeviceUniqueId() {
        return Settings.Secure.getString(MainApplication.getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static ProgressBarView getProgressBar(Activity activity) {
        return new ProgressBarView(activity);
    }

    @SuppressWarnings("deprecation")
    private static String getCurrentLocale2() {
        return MainApplication.getContext().getResources().getConfiguration().locale.getLanguage();
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Locale getCurrentLocale() {
        return MainApplication.getContext().getResources().getConfiguration().getLocales().get(0);
    }

    public static String getLanguage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return getCurrentLocale().getLanguage();
        }
        return getCurrentLocale2();
    }

    public static class ProgressBarView {
        private ProgressBar mProgressBar;

        ProgressBarView(Activity activity) {
            ViewGroup layout = (ViewGroup) activity.findViewById(android.R.id.content).getRootView();

            mProgressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleLarge);
            mProgressBar.setIndeterminate(true);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

            RelativeLayout rl = new RelativeLayout(activity);
            rl.setGravity(Gravity.CENTER);
            rl.addView(mProgressBar);

            layout.addView(rl, params);
        }

        public void show() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        public void hide() {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}
