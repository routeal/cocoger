package com.routeal.cocoger.ui.main;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.franmontiel.fullscreendialog.FullScreenDialogFragment;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;

/**
 * Created by nabe on 7/22/17.
 */

public class FriendListFragment extends PagerFragment {
    private final static String TAG = "FriendListFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_list, container, false);

        Button addFriend = (Button) view.findViewById(R.id.add_friend);
        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullScreenDialogFragment dialogFragment = new FullScreenDialogFragment.Builder(getActivity())
                        .setTitle(R.string.search_users_title)
                        .setConfirmButton(R.string.request_friend)
                        .setContent(UserListFragment.class, new Bundle())
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

        FirebaseRecyclerAdapter adapter = FB.getFriendRecyclerAdapter();
        adapter.startListening();

        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    void onViewPageSelected() {
        // empty
    }
}
