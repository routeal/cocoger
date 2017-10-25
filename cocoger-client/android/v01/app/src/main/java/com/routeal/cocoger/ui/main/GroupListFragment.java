package com.routeal.cocoger.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;

/**
 * Created by nabe on 7/22/17.
 */

public class GroupListFragment extends PagerFragment {

    private Button mCreateGroup;
    private TextView mEmptyTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group_list, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        layoutManager.setReverseLayout(false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);

        mEmptyTextView = (TextView) view.findViewById(R.id.empty_view);

        mCreateGroup = (Button) view.findViewById(R.id.create_group);
        mCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FB.GROUP_CREATE);
                LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
            }
        });

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
        if (FriendManager.isEmpty()) {
            mCreateGroup.setEnabled(false);
        } else {
            mCreateGroup.setEnabled(true);
        }
        empty(mAdapter.getItemCount() == 0);
    }
}
