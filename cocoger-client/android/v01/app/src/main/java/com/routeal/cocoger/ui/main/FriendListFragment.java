package com.routeal.cocoger.ui.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.franmontiel.fullscreendialog.FullScreenDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.Friend;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by nabe on 7/22/17.
 */

public class FriendListFragment extends Fragment {
    private final static String TAG = "FriendListFragment";

    private SlidingUpPanelLayout mSlidingUpPanelLayout;

    private FullScreenDialogFragment dialogFragment;

    public FriendListFragment() {
    }

    void setSlidingUpPanelLayout(SlidingUpPanelLayout layout) {
        mSlidingUpPanelLayout = layout;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_friend_list, container, false);

        Button addFriend = (Button) view.findViewById(R.id.add_friend);
        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFragment = new FullScreenDialogFragment.Builder(getActivity())
                        .setTitle("People")
                        .setConfirmButton("Request Friend")
                        .setContent(AddFriendFragment.class, new Bundle())
                        .build();
                dialogFragment.show(getActivity().getSupportFragmentManager(), "user-dialog");
            }
        });

        Button inviteApp = (Button) view.findViewById(R.id.invite_app);
        inviteApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        layoutManager.setReverseLayout(false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(layoutManager);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference friendRef = userRef.child(uid).child("friends");

        FirebaseRecyclerAdapter<Friend, FriendListViewHolder> adapter =
                new FirebaseRecyclerAdapter<Friend, FriendListViewHolder>(
                        Friend.class,
                        R.layout.listview_friend_list,
                        FriendListViewHolder.class,
                        friendRef
                ) {
                    @Override
                    protected void populateViewHolder(FriendListViewHolder viewHolder, Friend model, int position) {
                        viewHolder.bind(model, getRef(position).getKey());
                    }
                };

        recyclerView.setAdapter(adapter);

        return view;
    }
}
