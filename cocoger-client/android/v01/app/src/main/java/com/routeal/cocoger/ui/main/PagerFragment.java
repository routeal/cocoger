package com.routeal.cocoger.ui.main;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by nabe on 9/13/17.
 */

abstract public class PagerFragment extends Fragment {
    private static final String TAG = "PagerFragment";
    protected FirebaseRecyclerAdapter mAdapter;
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

    void setRecyclerAdapter(FirebaseRecyclerAdapter adapter) {
        mAdapter = adapter;
    }

    abstract void onViewPageSelected();

    abstract void empty(boolean v);

    public interface ChangeListener {
        void onEmpty(boolean empty);
    }
}
