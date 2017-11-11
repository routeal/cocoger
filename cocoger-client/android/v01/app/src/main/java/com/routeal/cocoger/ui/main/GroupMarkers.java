package com.routeal.cocoger.ui.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.routeal.cocoger.R;
import com.routeal.cocoger.manager.GroupManager;
import com.routeal.cocoger.model.Group;
import com.routeal.cocoger.model.Member;
import com.routeal.cocoger.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by hwatanabe on 11/5/17.
 */

public class GroupMarkers {

    private final HashMap<String, MarkerColor> mBackgroundColor = new HashMap<String, MarkerColor>() {{
        put("indigo_500", new MarkerColor(R.color.indigo_500, R.color.indigo_100));
        put("red_900", new MarkerColor(R.color.red_900, R.color.red_100));
        put("teal_a_700", new MarkerColor(R.color.teal_a_700, R.color.teal_100));
        put("amber_a_400", new MarkerColor(R.color.amber_a_400, R.color.amber_100));
        put("pink_a_400", new MarkerColor(R.color.pink_a_400, R.color.pink_100));
    }};

    private MapActivity mActivity;
    private GoogleMap mMap;
    private InfoWindowManager mInfoWindowManager;
    private UserMarkers mUserMarkers;
    private Map<String, Polygon> mPolygons = new HashMap<>();
    private Marker mMarker;

    GroupMarkers(MapActivity activity, GoogleMap map, InfoWindowManager infoWindowManager) {
        mActivity = activity;
        mMap = map;
        mInfoWindowManager = infoWindowManager;
    }

    private static int getColorWithAlpha(int color, int alpha) {
        int red = Color.red(color);
        int blue = Color.blue(color);
        int green = Color.green(color);
        return Color.argb(alpha, red, green, blue);
    }

    void setUserMarkers(UserMarkers userMarkers) {
        mUserMarkers = userMarkers;
    }

    void onPolygonClick(Polygon polygon) {
        if (mMarker != null) {
            mMarker.remove();
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        List<LatLng> points = polygon.getPoints();
        for (LatLng l : points) {
            builder.include(l);
        }

        LatLngBounds bounds = builder.build();
        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.moveCamera(cu);

        LatLng center = bounds.getCenter();
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);
        BitmapDescriptor transparent = BitmapDescriptorFactory.fromBitmap(bitmap);

        MarkerOptions options = new MarkerOptions()
                .position(center)
                .anchor((float) 0.5, (float) 0.5);
        mMarker = mMap.addMarker(options);
    }

    void notifyChange(ComboMarker marker) {
        for (Map.Entry<String, ComboMarker.MarkerInfo> entry : marker.getInfo().entrySet()) {
            String key = entry.getKey();
            Map<String, Group> groups = GroupManager.getGroups(key);
            for (Map.Entry<String, Group> entry2 : groups.entrySet()) {
                update(entry2.getKey(), entry2.getValue());
            }
        }
    }

    private void update(String key, Group group) {
        Polygon polygon = mPolygons.get(key);
        if (polygon != null) {
            polygon.remove();
        }

        Set<ComboMarker> markers = new HashSet<>();

        for (Map.Entry<String, Member> entry : group.getMembers().entrySet()) {
            String uid = entry.getKey();
            Member member = entry.getValue();
            if (member.getStatus() == Member.CREATED || member.getStatus() == Member.JOINED) {
                ComboMarker marker = mUserMarkers.get(uid);
                if (marker != null) {
                    markers.add(marker);
                }
            }
        }

        if (markers.size() <= 1) {
            return;
        }

        List<LatLng> points = new ArrayList<>();
        for (ComboMarker marker : markers) {
            if (marker.getLocation() != null) {
                points.add(marker.getLocation());
            }
        }

        if (points.size() <= 1) {
            return;
        }

        Collections.sort(points, new SortPoints(points.get(0)));

        MarkerColor markerColor = mBackgroundColor.get(group.getColor());

        PolygonOptions options = new PolygonOptions();
        options.addAll(points);
        options.strokeColor(ContextCompat.getColor(mActivity, markerColor.strokeColor));
        options.fillColor(getColorWithAlpha(ContextCompat.getColor(mActivity, markerColor.fillColor), 100));
        options.clickable(true);

        polygon = mMap.addPolygon(options);
        mPolygons.put(key, polygon);
    }

    void onWindowHidden(InfoWindow infoWindow) {
        Fragment fragment = infoWindow.getWindowFragment();
        if (fragment != null) {
            /*
            if (fragment instanceof PoiInfoFragment) {
                removeInfoWindow();
            }
            */
        }
    }

    private class MarkerColor {
        int strokeColor;
        int fillColor;

        MarkerColor(int strokeColor, int fillColor) {
            this.strokeColor = strokeColor;
            this.fillColor = fillColor;
        }
    }

    private class SortPoints implements Comparator<LatLng> {
        LatLng currentLoc;

        SortPoints(LatLng current) {
            currentLoc = current;
        }

        @Override
        public int compare(final LatLng place1, final LatLng place2) {
            double distanceToPlace1 = Utils.distanceTo(currentLoc, place1);
            double distanceToPlace2 = Utils.distanceTo(currentLoc, place2);
            return (int) (distanceToPlace1 - distanceToPlace2);
        }
    }

}
