package com.routeal.cocoger.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import com.google.android.gms.maps.model.LatLng;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.Device;
import com.routeal.cocoger.model.LocationAddress;

import java.util.Locale;

/**
 * Created by nabe on 7/3/17.
 */

public class Utils {

    public static ProgressDialog getBusySpinner(Context context) {
        ProgressDialog dialog = ProgressDialog.show(context, null, null, false, true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.progressbar_spinner);
        return dialog;
    }

    public static Drawable getIconDrawable(Context context, int resourceId, int backgroundColor) {
        Drawable drawable = ContextCompat.getDrawable(context, resourceId);
        drawable.mutate();
        DrawableCompat.setTint(drawable, ContextCompat.getColor(context, backgroundColor));
        return drawable;
    }

    public static String getDeviceUniqueID() {
        return Settings.Secure.getString(MainApplication.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static final boolean isEmulator() {
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
        if ((Build.DEVICE.equals("generic")) || (Build.DEVICE.equals("generic_x86")) || (Build.DEVICE.equals("vbox86p"))) {
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

    public static int getColorWithAlpha(int color, float ratio) {
        int newColor = 0;
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        newColor = Color.argb(alpha, r, g, b);
        return newColor;
    }

    // For now, the device is saved into the memory
    public static Device getDevice() {
        Device mDevice = new Device();
        mDevice.setDeviceId(Utils.getDeviceUniqueID());
        mDevice.setBrand(Build.BRAND);
        mDevice.setModel(Build.MODEL);
        mDevice.setPlatformVersion(Build.VERSION.RELEASE);
        mDevice.setSimulator(Utils.isEmulator());
        mDevice.setToken(""); // empty for now
        mDevice.setStatus(Device.FOREGROUND);
        mDevice.setAppVersion(MainApplication.getApplicationVersion());
        mDevice.setTimestamp(System.currentTimeMillis());
        return mDevice;
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

    public static Address getAddress(LocationAddress la) {
        Address a = new Address(Locale.getDefault());
        a.setAdminArea(la.getAdminArea());
        a.setCountryName(la.getCountryName());
        a.setLatitude(la.getLatitude());
        a.setLocality(la.getlocality());
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
        if (str.charAt(str.length()-1) != ' ') str += ", ";
        str += (a.getThoroughfare() == null) ? "" : a.getThoroughfare();
        if (str.charAt(str.length()-1) != ' ') str += ", ";
        str += (a.getSubLocality() == null) ? "" : a.getSubLocality();
        if (str.charAt(str.length()-1) != ' ') str += ", ";
        str += (a.getLocality() == null) ? "" : a.getLocality();
        if (str.charAt(str.length()-1) != ' ') str += ", ";
        str += (a.getAdminArea() == null) ? "" : a.getAdminArea();
        if (str.charAt(str.length()-1) != ' ') str += ", ";
        str += (a.getPostalCode() == null) ? "" : a.getPostalCode();
        return str;
    }

}
