package com.routeal.cocoger.util;

import android.app.Activity;
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
import android.view.View;
import android.view.inputmethod.InputMethodManager;

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

    public static Drawable getIconDrawable(Context context, int resourceId, int backgroundColor) {
        Drawable drawable = ContextCompat.getDrawable(context, resourceId);
        drawable.mutate();
        DrawableCompat.setTint(drawable, ContextCompat.getColor(context, backgroundColor));
        return drawable;
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
