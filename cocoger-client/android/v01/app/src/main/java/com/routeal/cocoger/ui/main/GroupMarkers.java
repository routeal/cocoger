package com.routeal.cocoger.ui.main;

import android.support.v4.content.ContextCompat;

import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.routeal.cocoger.R;
import com.routeal.cocoger.manager.GroupManager;
import com.routeal.cocoger.model.Group;
import com.routeal.cocoger.model.Member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hwatanabe on 11/5/17.
 */

public class GroupMarkers {

    private final HashMap<String, Integer> mBackgroundColor = new HashMap<String, Integer>() {{
        put("indigo_500", R.color.indigo_500);
        put("red_900", R.color.red_900);
        put("teal_a_700", R.color.teal_a_700);
        put("amber_a_400", R.color.amber_a_400);
        put("pink_a_400", R.color.pink_a_400);
    }};

    private MapActivity mActivity;
    private GoogleMap mMap;
    private InfoWindowManager mInfoWindowManager;
    private UserMarkers mUserMarkers;
    private Map<String, List<ComboMarker>> mGroupMarkers = new HashMap<>();

    GroupMarkers(MapActivity activity, GoogleMap map, InfoWindowManager infoWindowManager) {
        mActivity = activity;
        mMap = map;
        mInfoWindowManager = infoWindowManager;
    }

    void setUserMarkers(UserMarkers userMarkers) {
        mUserMarkers = userMarkers;
    }

    void onPolygonClick(Polygon polygon) {
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
        List<ComboMarker> markers = mGroupMarkers.get(key);
        if (markers == null) {
            markers = new ArrayList<>(); // order???
            mGroupMarkers.put(key, markers);
        }

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

        int bgColorId = mBackgroundColor.get(group.getColor());

        PolygonOptions options = new PolygonOptions();
        options.addAll(points);
        options.strokeColor(ContextCompat.getColor(mActivity, bgColorId));
        options.fillColor(ContextCompat.getColor(mActivity, bgColorId));

        mMap.addPolygon(options);
    }
}
