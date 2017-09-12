package com.routeal.cocoger.ui.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.franmontiel.fullscreendialog.FullScreenDialogFragment;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;

/**
 * Created by nabe on 9/9/17.
 */

public class NotifiListFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifi_list, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(layoutManager);

        return view;
    }
}
