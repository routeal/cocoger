package com.routeal.cocoger.ui.main;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.franmontiel.fullscreendialog.FullScreenDialogFragment;
import com.routeal.cocoger.R;

/**
 * Created by nabe on 7/22/17.
 */

public class FriendListFragment extends PagerFragment {
    private final static String TAG = "FriendListFragment";

    private TextView mEmptyTextView;

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
                        .setContent(UserDialogFragment.class, new Bundle())
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

        recyclerView.setAdapter(mAdapter);

        mEmptyTextView = (TextView) view.findViewById(R.id.empty_view);

        return view;
    }

    @Override
    void empty(boolean v) {
        if (mEmptyTextView == null) return;
        if (v) {
            mEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            mEmptyTextView.setVisibility(View.GONE);
        }
    }

    @Override
    void onViewPageSelected() {
        empty(mAdapter.getItemCount() == 0);
    }
}
