package com.routeal.cocoger.ui.main;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.provider.DB;
import com.routeal.cocoger.util.CircleTransform;
import com.routeal.cocoger.util.SnappingSeekBar;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

/**
 * Created by nabe on 7/22/17.
 */

public class FriendListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final static String TAG = "FriendListFragment";

    private SimpleCursorAdapter mAdapter;

    private SlidingUpPanelLayout mSlidingUpPanelLayout;

    private static final String[] FROM_COLUMNS = new String[]{
            DB.Friends.NAME,
            DB.Friends.PICTURE,
            DB.Friends.RANGE
    };

    private static final int[] TO_FIELDS = new int[]{
            R.id.name,
            R.id.picture,
            R.id.seekbar
    };

    public FriendListFragment() {
    }

    void setSlidingUpPanelLayout(SlidingUpPanelLayout layout) {
        mSlidingUpPanelLayout = layout;
    }

    private AppInviteDialog appInviteDialog;

    private CallbackManager callbackManager;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View headerView = getActivity().getLayoutInflater().inflate(R.layout.listview_header_friend, null);
        getListView().addHeaderView(headerView);

        FacebookCallback<AppInviteDialog.Result> appInviteCallback =
                new FacebookCallback<AppInviteDialog.Result>() {
                    @Override
                    public void onSuccess(AppInviteDialog.Result result) {
                        Log.d(TAG, "Success!");
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "Canceled");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG, String.format("Error: %s", error.toString()));
                    }
                };

        callbackManager = CallbackManager.Factory.create();

        appInviteDialog = new AppInviteDialog(this);
        appInviteDialog.registerCallback(callbackManager, appInviteCallback);

        AppCompatButton button = (AppCompatButton) headerView.findViewById(R.id.invite);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppInviteContent content = new AppInviteContent.Builder()
                        .setApplinkUrl("https://d3uu10x6fsg06w.cloudfront.net/hosting-rps/applink.html")
                        .setPreviewImageUrl("https://d3uu10x6fsg06w.cloudfront.net/hosting-rps/rps-preview-image.jpg")
                        .build();
                if (AppInviteDialog.canShow()) {
                    appInviteDialog.show(getActivity(), content);
                } else {
                    Toast.makeText(getContext(), getResources().getString(R.string.appinvite_error),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mSlidingUpPanelLayout != null) {
            mSlidingUpPanelLayout.setScrollableView(getListView());
        }

        setEmptyText(getResources().getString(R.string.no_friend));

        setListShown(false);

        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.listview_friend,
                null,
                FROM_COLUMNS,
                TO_FIELDS,
                0);

        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == cursor.getColumnIndex(DB.Friends.NAME)) {
                    if (view instanceof TextView) {
                        ((TextView) view).setText(cursor.getString(columnIndex));
                    }
                    return true;
                } else if (columnIndex == cursor.getColumnIndex(DB.Friends.PICTURE)) {
                    if (view instanceof ImageView) {
                        ImageView image = (ImageView) view;
                        String picture = cursor.getString(columnIndex);
                        Picasso.with(getContext())
                                .load(picture)
                                .transform(new CircleTransform())
                                .into(image);
                    }
                    return true;
                } else if (columnIndex == cursor.getColumnIndex(DB.Friends.RANGE)) {
                    if (view instanceof SnappingSeekBar) {
                        SnappingSeekBar seekbar  = (SnappingSeekBar) view;
                        int range = cursor.getInt(columnIndex);
                        seekbar.setProgressToIndex(range);
                    }
                    return true;
                }
                return false;
            }
        });

        setListAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), DB.Friends.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent intent = new Intent(getActivity(), FriendConfigActivity.class);
        //intent.putExtra("id", id);
        startActivityForResult(intent, hashCode() % 1000);
    }
}
