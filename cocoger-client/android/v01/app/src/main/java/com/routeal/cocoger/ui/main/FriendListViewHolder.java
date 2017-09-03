package com.routeal.cocoger.ui.main;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.util.CircleTransform;
import com.routeal.cocoger.util.LocationRange;
import com.routeal.cocoger.util.SnappingSeekBar;
import com.squareup.picasso.Picasso;

public class FriendListViewHolder extends RecyclerView.ViewHolder {
    private final ImageView mPicture;
    private final TextView mName;
    private final SnappingSeekBar mSeekBar;
    private final View mView;
    private int mCurrentRange;
    private String mFriendId;

    public FriendListViewHolder(View itemView) {
        super(itemView);

        mView = itemView;
        mPicture = (ImageView) itemView.findViewById(R.id.picture);
        mName = (TextView) itemView.findViewById(R.id.name);
        mSeekBar = (SnappingSeekBar) itemView.findViewById(R.id.seekbar);

        mSeekBar.setOnItemSelectionListener(new SnappingSeekBar.OnItemSelectionListener() {
            @Override
            public void onItemSelected(final int itemIndex, String itemString) {
                if (itemIndex > mCurrentRange) {
                    new AlertDialog.Builder(mView.getContext())
                            .setTitle(R.string.notice)
                            .setMessage(R.string.notice_narrow_range_change)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sendChangeRequest(itemIndex);

                                    mSeekBar.setProgressToIndex(mCurrentRange);

                                    new AlertDialog.Builder(mView.getContext())
                                            .setMessage(R.string.range_changed_narrow)
                                            .setPositiveButton(android.R.string.ok, null)
                                            .show();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mSeekBar.setProgressToIndex(mCurrentRange);
                                }
                            })
                            .show();

                } else if (itemIndex < mCurrentRange) {
                    new AlertDialog.Builder(mView.getContext())
                            .setTitle(R.string.notice)
                            .setMessage(R.string.notice_broad_range_change)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    changeRange(itemIndex);
                                    mCurrentRange = itemIndex;
                                    new AlertDialog.Builder(mView.getContext())
                                            .setMessage(R.string.range_changed_broad)
                                            .setPositiveButton(android.R.string.ok, null)
                                            .show();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mSeekBar.setProgressToIndex(mCurrentRange);
                                }
                            })
                            .show();
                }
            }
        });
    }

    public void bind(Friend friend, String key /* friend's key */) {
        mFriendId = key;
        setRange(friend.getRange());
        setName(friend.getDisplayName());
        setPicture(friend.getPicture());
    }

    private void setName(String name) {
        mName.setText(name);
    }

    private void setRange(int range) {
        mCurrentRange = LocationRange.toPosition(range);
        mSeekBar.setProgressToIndex(mCurrentRange);
    }

    private void setPicture(String url) {
        Picasso.with(MainApplication.getContext())
                .load(url)
                .transform(new CircleTransform())
                .resize(48, 48)
                .into(mPicture);
    }

    private void changeRange(int index) {
        try {
            int range = LocationRange.toRange(index);
            FB.changeRange(mFriendId, range);
        } catch (Exception e) {
        }
    }

    private void sendChangeRequest(int index) {
        try {
            int range = LocationRange.toRange(index);
            FB.sendChangeRequest(mFriendId, range);
        } catch (Exception e) {
        }
    }

}

