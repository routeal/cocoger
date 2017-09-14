package com.routeal.cocoger.ui.main;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by nabe on 9/13/17.
 */

public class PagerFragment extends Fragment {
    private static final String TAG = "PagerFragment";

    private SlidingUpPanelLayout mSlidingUpPanelLayout;

    private ViewPager viewPager;

    void setSlidingUpPanelLayout(SlidingUpPanelLayout layout) {
        mSlidingUpPanelLayout = layout;
    }

    SlidingUpPanelLayout getSlidingUpPanelLayout() { return mSlidingUpPanelLayout; }

    void setViewPager(ViewPager viewPager) { this.viewPager = viewPager; }

    ViewPager getViewPager() { return viewPager; }

    void onSelected() {
        Log.d(TAG, "selected");
    }
}
