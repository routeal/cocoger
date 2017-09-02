package com.routeal.cocoger.ui.main;

import android.app.NotificationManager;
import android.app.Service;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.service.MainService;
import com.routeal.cocoger.util.LocationRange;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import java.util.ArrayList;
import java.util.List;

public class PanelMapActivity extends SearchMapActivity {
    private final static String TAG = "PanelMapActivity";

    private int[] tabIcons = {
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
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, PanelState previousState, PanelState newState) {
                Log.i(TAG, "onPanelStateChanged " + newState);
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
        adapter.addFragment(friendFragment, null);
        adapter.addFragment(new GroupListFragment(), null);
        viewPager.setAdapter(adapter);

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
        if (action.equals(MainService.ACTION_FRIEND_REQUEST_ACCEPTED)) {
            // delete the invite and invitee from the database
            FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
            final String invitee = fbUser.getUid();
            final String invite = extras.getString("friend_invite");

            final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
            userRef.child(invitee).child("invitees").child(invite).removeValue();
            userRef.child(invite).child("invites").child(invitee).removeValue();

            final long timestamp = System.currentTimeMillis();
            final int defaultLocationChange = LocationRange.SUBADMINAREA.toInt();

            // invitee - me invited by invite, get the information of the invite
            userRef.child(invite).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User inviteUser = dataSnapshot.getValue(User.class);
                            Friend friend = new Friend();
                            friend.setCreated(timestamp);
                            friend.setRange(defaultLocationChange);
                            friend.setDisplayName(inviteUser.getDisplayName());
                            friend.setPicture(inviteUser.getPicture());
                            userRef.child(invitee).child("friends").child(invite).setValue(friend);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

            // invite - me added to invite
            Friend myInfo = new Friend();
            myInfo.setCreated(timestamp);
            myInfo.setRange(defaultLocationChange);
            myInfo.setDisplayName(MainApplication.getUser().getDisplayName());
            myInfo.setPicture(MainApplication.getUser().getPicture());
            userRef.child(invite).child("friends").child(invitee).setValue(myInfo);

            // remove the notification
            int nid = extras.getInt("notification_id");
            if (nid > 0) {
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(nid);
            }

            // set the current page to the friend list fragment
            ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
            viewPager.setCurrentItem(0);

            // show the friend list fragment
            SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
            mLayout.setPanelState(PanelState.ANCHORED);
        }
        else if (action.equals(MainService.ACTION_RANGE_REQUEST_ACCEPTED)) {
            // delete the invite and invitee from the database
            FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
            String responder = fbUser.getUid(); // myself
            String requester = extras.getString("range_requester");
            int range = extras.getInt("range");

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");

            userRef.child(responder).child("friends").child(requester).child("range").setValue(range);
            userRef.child(responder).child("friends").child(requester).child("rangeRequest").removeValue();

            userRef.child(requester).child("friends").child(responder).child("range").setValue(range);

            // remove the notification
            int nid = extras.getInt("notification_id");
            if (nid > 0) {
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(nid);
            }

            // set the current page to the friend list fragment
            ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
            viewPager.setCurrentItem(0);

            // show the friend list fragment
            SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
            mLayout.setPanelState(PanelState.ANCHORED);
        }
    }
}
