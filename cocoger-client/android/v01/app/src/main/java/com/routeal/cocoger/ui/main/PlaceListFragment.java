package com.routeal.cocoger.ui.main;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.routeal.cocoger.R;

/**
 * Created by hwatanabe on 10/8/17.
 */

public class PlaceListFragment extends PagerFragment {

    private RecyclerView mRecyclerView;
    private TextView mEmptyTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_place_list, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        layoutManager.setReverseLayout(false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mEmptyTextView = (TextView) view.findViewById(R.id.empty_view);

        return view;
    }

    @Override
    RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override
    void onEmpty(boolean v) {
        if (mEmptyTextView == null) return;
        if (v) {
            mEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            mEmptyTextView.setVisibility(View.GONE);
        }
    }

    @Override
    void onViewPageSelected() {
        //onEmpty(mAdapter.getItemCount() == 0);
    }

}
