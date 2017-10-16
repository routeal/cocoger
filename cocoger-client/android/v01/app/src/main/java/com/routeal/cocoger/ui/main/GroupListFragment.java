package com.routeal.cocoger.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.routeal.cocoger.R;

/**
 * Created by nabe on 7/22/17.
 */

public class GroupListFragment extends PagerFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_list, container, false);
    }

    @Override
    void onViewPageSelected() {
        // empty
    }
}
