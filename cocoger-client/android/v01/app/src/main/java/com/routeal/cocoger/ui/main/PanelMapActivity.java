package com.routeal.cocoger.ui.main;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.util.Notifi;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import java.util.ArrayList;
import java.util.List;

public class PanelMapActivity extends SearchMapActivity {
    private final static String TAG = "PanelMapActivity";

    private int[] tabIcons = {
            R.drawable.ic_notifications_black_24dp,
            R.drawable.ic_contacts_black_24dp,
            R.drawable.ic_group_black_24dp
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.addPanelSlideListener(new PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                //Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, PanelState previousState, PanelState newState) {
                //Log.i(TAG, "onPanelStateChanged " + newState);
            }
        });

        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "setFadeOnClickListener ");
                mLayout.setPanelState(PanelState.COLLAPSED);
            }
        });

        FriendListFragment friendFragment = new FriendListFragment();
        friendFragment.setSlidingUpPanelLayout(mLayout);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new NotifiListFragment(), null);
        adapter.addFragment(friendFragment, null);
        adapter.addFragment(new GroupListFragment(), null);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        for (int i = 0; i < tabIcons.length; i++) {
            tabLayout.getTabAt(i).setIcon(tabIcons[i]);
        }

        handleIntent();
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    // most of the notification intent come here
    private void handleIntent() {
        String action = getIntent().getAction();
        Bundle extras = getIntent().getExtras();
        if (action == null || extras == null) return;
        if (action.equals(FB.ACTION_FRIEND_REQUEST_ACCEPTED)) {
            try {
                String invite = extras.getString(FB.NOTIFI_FRIEND_INVITE);
                FB.acceptFriendRequest(invite);
            } catch (Exception e) {
            }

            // remove the notification
            int nid = extras.getInt(Notifi.ID);
            Notifi.remove(nid);

            // set the current page to the friend list fragment
            ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
            viewPager.setCurrentItem(1);

            // show the friend list fragment
            SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
            mLayout.setPanelState(PanelState.ANCHORED);
        } else if (action.equals(FB.ACTION_RANGE_REQUEST_ACCEPTED)) {
            try {
                String requester = extras.getString(FB.NOTIFI_RANGE_REQUETER);
                int range = extras.getInt(FB.NOTIFI_RANGE);
                FB.acceptRangeRequest(requester, range);
            } catch (Exception e) {
            }

            // remove the notification
            int nid = extras.getInt(Notifi.ID);
            Notifi.remove(nid);

            // set the current page to the friend list fragment
            ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
            viewPager.setCurrentItem(1);

            // show the friend list fragment
            SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
            mLayout.setPanelState(PanelState.ANCHORED);
        }
    }
}
