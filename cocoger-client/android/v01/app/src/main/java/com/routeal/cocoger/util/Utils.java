package com.routeal.cocoger.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.LocationAddress;
import com.routeal.cocoger.provider.DBUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by nabe on 7/3/17.
 */

public class Utils {
    private final static String TAG = "Utils";

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

    public static Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return null;
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

    public static Uri saveBitmap(Context context, Bitmap bitmap) {
        File file = null;
        FileOutputStream out = null;
        try {
            file = File.createTempFile("bitmap", null, context.getCacheDir());
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            file = null;
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
            }
        }
        return (file == null) ? null : Uri.fromFile(file);
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

    public static Address getAddress(Location location) {
        Address address = null;
        try {
            List<Address> addresses =
                    new Geocoder(MainApplication.getContext(), Locale.getDefault())
                            .getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            address = addresses.get(0);
        } catch (Exception e) {
            Log.d(TAG, "getAddress:", e);
        }
        return address;
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

        if (borderColor >= 0) {
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(borderWidth);
            canvas.drawCircle(width / 2, height / 2, radius - borderWidth / 2, paint);
        }

        return to;
    }

    public static int detectRangeMove(Address n, Address o, int range) {
        if (n == null || o == null || range == 0) {
            return LocationRange.NONE.range;
        }
        if (range >= LocationRange.COUNTRY.range) {
            if (n.getCountryName() != null && o.getCountryName() != null) {
                if (!n.getCountryName().equals(o.getCountryName())) {
                    return LocationRange.COUNTRY.range;
                }
            }
        }
        if (range >= LocationRange.ADMINAREA.range) {
            if (n.getAdminArea() != null && o.getAdminArea() != null) {
                if (!n.getAdminArea().equals(o.getAdminArea())) {
                    return LocationRange.ADMINAREA.range;
                }
            }
        }
        if (range >= LocationRange.SUBADMINAREA.range) {
            if (n.getSubAdminArea() != null && o.getSubAdminArea() != null) {
                if (!n.getSubAdminArea().equals(o.getSubAdminArea())) {
                    return LocationRange.SUBADMINAREA.range;
                }
            }
        }
        if (range >= LocationRange.LOCALITY.range) {
            if (n.getLocality() != null && o.getLocality() != null) {
                if (!n.getLocality().equals(o.getLocality())) {
                    return LocationRange.LOCALITY.range;
                }
            }
        }
        if (range >= LocationRange.SUBLOCALITY.range) {
            if (n.getSubLocality() != null && o.getSubLocality() != null) {
                if (!n.getSubLocality().equals(o.getSubLocality())) {
                    return LocationRange.SUBLOCALITY.range;
                }
            }
        }
        if (range >= LocationRange.THOROUGHFARE.range) {
            if (n.getThoroughfare() != null && o.getThoroughfare() != null) {
                if (!n.getThoroughfare().equals(o.getThoroughfare())) {
                    return LocationRange.THOROUGHFARE.range;
                }
            }
        }
        if (range >= LocationRange.SUBTHOROUGHFARE.range) {
            if (n.getSubThoroughfare() != null && o.getSubThoroughfare() != null) {
                if (!n.getSubThoroughfare().equals(o.getSubThoroughfare())) {
                    return LocationRange.SUBTHOROUGHFARE.range;
                }
            }
        }
        if (range >= LocationRange.CURRENT.range) {
            return LocationRange.CURRENT.range;
        }
        return LocationRange.NONE.range;
    }

    public static boolean isEqualAddress(Address address1, Address address2, int range) {
        if (address1 == null || address2 == null || range == 0) {
            return false;
        }
        if (range >= LocationRange.COUNTRY.range) {
            if (address1.getCountryName() != null && address2.getCountryName() != null) {
                if (!address1.getCountryName().equals(address2.getCountryName())) {
                    return false;
                }
            }
        }
        if (range >= LocationRange.ADMINAREA.range) {
            if (address1.getAdminArea() != null && address2.getAdminArea() != null) {
                if (!address1.getAdminArea().equals(address2.getAdminArea())) {
                    return false;
                }
            }
        }
        if (range >= LocationRange.SUBADMINAREA.range) {
            if (address1.getSubAdminArea() != null && address2.getSubAdminArea() != null) {
                if (!address1.getSubAdminArea().equals(address2.getSubAdminArea())) {
                    return false;
                }
            }
        }
        if (range >= LocationRange.LOCALITY.range) {
            if (address1.getLocality() != null && address2.getLocality() != null) {
                if (!address1.getLocality().equals(address2.getLocality())) {
                    return false;
                }
            }
        }
        if (range >= LocationRange.SUBLOCALITY.range) {
            if (address1.getSubLocality() != null && address2.getSubLocality() != null) {
                if (!address1.getSubLocality().equals(address2.getSubLocality())) {
                    return false;
                }
            }
        }
        if (range >= LocationRange.THOROUGHFARE.range) {
            if (address1.getThoroughfare() != null && address2.getThoroughfare() != null) {
                if (!address1.getThoroughfare().equals(address2.getThoroughfare())) {
                    return false;
                }
            }
        }
        if (range >= LocationRange.SUBTHOROUGHFARE.range) {
            if (address1.getSubThoroughfare() != null && address2.getSubThoroughfare() != null) {
                if (!address1.getSubThoroughfare().equals(address2.getSubThoroughfare())) {
                    return false;
                }
            }
        }
        if (range >= LocationRange.CURRENT.range) {
            return true;
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
}
