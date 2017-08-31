package com.routeal.cocoger.util;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nabe on 8/31/17.
 */

public enum LocationRange {
    NONE(0),
    COUNTRY(1), // country
    ADMINAREA(2), // state
    SUBADMINAREA(4), // county
    LOCALITY(8), // city
    SUBLOCALITY(16), // town
    THOROUGHFARE(32), // street
    SUBTHOROUGHFARE(64), // street number
    CURRENT(128); // current

    private final int range;

    LocationRange(int range) {
        this.range = range;
    }

    public int toInt() {
        return range;
    }

    static final Map<String, Integer> STR_RANGE = new HashMap<String, Integer>() {{
        put("NONE", 0);
        put("COUNTRY", 1);
        put("ADMINAREA", 2);
        put("SUBADMINAREA", 4);
        put("LOCALITY", 8);
        put("SUBLOCALITY", 16);
        put("THOROUGHFARE", 32);
        put("SUBTHOROUGHFARE", 64);
        put("CURRENT", 128);
    }};

    static Map<Integer, Integer> RANGE_POSITION = new HashMap<>();
    static Map<Integer, Integer> POSITION_RANGE = new HashMap<>();
    static Map<Integer, String> RANGE_LOCALIZED_STR = new HashMap<>();

    static {
        String[] range_map = MainApplication.getContext().getResources().getStringArray(R.array.range_map);
        for (int i = 0; i < range_map.length; i++) {
            String tmp = range_map[i];
            String[] parts = tmp.split("=");
            int range = STR_RANGE.get(parts[1]);
            RANGE_LOCALIZED_STR.put(range, parts[0]);
            RANGE_POSITION.put(range, i);
            POSITION_RANGE.put(i, range);
        }
    }

    public static int toPosition(int range) {
        return RANGE_POSITION.get(range);
    }

    public static int toRange(int position) {
        return POSITION_RANGE.get(position);
    }

    public static String toString(int range) {
        return RANGE_LOCALIZED_STR.get(range);
    }
}