package com.routeal.cocoger.ui.main;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by hwatanabe on 10/7/17.
 */

public class MyPoiInfoFragment extends PoiInfoFragment {

    private String mPoiCreator;
    private Bitmap mCopiedBitmap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mActionAddPoiButton.setVisibility(View.GONE);
        mActionEditPoiButton.setVisibility(View.VISIBLE);
        mPoiCreatorTextView.setVisibility(View.VISIBLE);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mPoiCreator != null && !mPoiCreator.isEmpty()) {
            mPoiCreatorTextView.setText(mPoiCreator);
        }
        if (mCopiedBitmap != null) {
            mStreetImageView.setImageBitmap(mCopiedBitmap);
        }
    }

    void setPoiCreator(String creator) {
        mPoiCreator = creator;
    }

    void setStreetViewPicture(Bitmap bitmap) {
        mCopiedBitmap = bitmap;
    }
}


