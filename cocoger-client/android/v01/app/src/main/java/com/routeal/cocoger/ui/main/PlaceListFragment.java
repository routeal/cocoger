package com.routeal.cocoger.ui.main;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;

/**
 * Created by hwatanabe on 10/8/17.
 */

public class PlaceListFragment extends PagerFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_place_list, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        layoutManager.setReverseLayout(false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(layoutManager);

        FirebaseRecyclerAdapter adapter = FB.getPlaceRecyclerAdapter();
        adapter.startListening();
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    void onViewPageSelected() {
        // empty
    }

}
