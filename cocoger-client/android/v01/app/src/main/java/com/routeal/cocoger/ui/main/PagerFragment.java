package com.routeal.cocoger.ui.main;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by nabe on 9/13/17.
 */

public class PagerFragment extends Fragment {
    private static final String TAG = "PagerFragment";
    protected PlaceMarkers mPlaceMarkers;
    protected GoogleMap mMap;
    protected PanelMapActivity mActivity;
    private SlidingUpPanelLayout mSlidingUpPanelLayout;
    private ViewPager mViewPager;

    SlidingUpPanelLayout getSlidingUpPanelLayout() {
        return mSlidingUpPanelLayout;
    }

    void setSlidingUpPanelLayout(SlidingUpPanelLayout layout) {
        mSlidingUpPanelLayout = layout;
    }

    ViewPager getViewPager() {
        return mViewPager;
    }

    void setViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
    }

    void onSelected() {
        if (getView() != null) {
            Log.d(TAG, "selected");
            onViewPageSelected();
        }
    }

    void onViewPageSelected() {
    }

    void setPlaceMarkers(PlaceMarkers placeMarkers) {
        mPlaceMarkers = placeMarkers;
    }

    void setGoogleMap(GoogleMap map) {
        mMap = map;
    }

    void setActivity(PanelMapActivity activity) {
        mActivity = activity;
    }
}
