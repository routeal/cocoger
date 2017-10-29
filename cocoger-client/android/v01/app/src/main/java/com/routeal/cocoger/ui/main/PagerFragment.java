package com.routeal.cocoger.ui.main;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by nabe on 9/13/17.
 */

public class PagerFragment extends Fragment {
    private static final String TAG = "PagerFragment";
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

    RecyclerView getRecyclerView() {
        return null;
    }

    FirebaseRecyclerAdapter mAdapter;

    void setAdapter(FirebaseRecyclerAdapter adapter) {
        mAdapter = adapter;
    }

    void onSelected() {
        if (getView() != null) {
            Log.d(TAG, "selected");
            onViewPageSelected();
        }
    }

    void onViewPageSelected() {}

    void onEmpty(boolean v) {}

    public interface ChangeListener {
        void onEmpty(boolean empty);
    }
}
