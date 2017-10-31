package com.routeal.cocoger.ui.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.franmontiel.fullscreendialog.FullScreenDialogFragment;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.manager.FriendManager;
import com.routeal.cocoger.manager.UpdateListener;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.util.LoadImage;
import com.routeal.cocoger.util.LocationRange;
import com.routeal.cocoger.util.SnappingSeekBar;

import java.util.Set;

/**
 * Created by nabe on 7/22/17.
 */

public class FriendListFragment extends PagerFragment {
    private final static String TAG = "FriendListFragment";

    private RecyclerView mRecyclerView;
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

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(layoutManager);

        mEmptyTextView = (TextView) view.findViewById(R.id.empty_view);

        final FriendListAdapter friendListAdapter = new FriendListAdapter();

        FriendManager.setUpdateListener(new UpdateListener<Friend>() {
            @Override
            public void onAdded(String key, Friend object) {
                friendListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChanged(String key, Friend object) {
                friendListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onRemoved(String key) {
                friendListAdapter.notifyDataSetChanged();
            }
        });

        mRecyclerView.setAdapter(friendListAdapter);

        return view;
    }

    @Override
    void onViewPageSelected() {
    }

    class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {

        @Override
        public FriendListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_friend_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FriendListAdapter.ViewHolder holder, int position) {
            Set<String> keySet = FriendManager.getFriends().keySet();
            String[] keys = keySet.toArray(new String[0]);
            String key = keys[position];
            Friend friend = FriendManager.getFriend(key);
            holder.bind(friend, key);
        }

        @Override
        public int getItemCount() {
            int size = FriendManager.getFriends().size();
            if (size == 0) {
                mEmptyTextView.setVisibility(View.VISIBLE);
            } else {
                mEmptyTextView.setVisibility(View.GONE);
            }
            return size;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private final ImageView mPicture;
            private final TextView mName;
            private final SnappingSeekBar mSeekBar;
            private final View mView;
            private int mCurrentRange;
            private String mFriendId;

            public ViewHolder(final View itemView) {
                super(itemView);

                mView = itemView;
                mPicture = (ImageView) itemView.findViewById(R.id.friend_picture);
                mName = (TextView) itemView.findViewById(R.id.friend_title);
                mSeekBar = (SnappingSeekBar) itemView.findViewById(R.id.friend_range);

                mPicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(FB.FRIEND_MARKER_SHOW);
                        intent.putExtra(FB.KEY, mFriendId);
                        LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
                    }
                });

                mName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(FB.FRIEND_MARKER_SHOW);
                        intent.putExtra(FB.KEY, mFriendId);
                        LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
                    }
                });

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

                View view = itemView.findViewById(R.id.send_message);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendMessage();
                    }
                });

                view = itemView.findViewById(R.id.unfriend);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String msg = String.format(itemView.getResources().getString(R.string.confirm_unfriend),
                                mName.getText().toString());
                        new AlertDialog.Builder(mView.getContext())
                                .setTitle(R.string.unfriend)
                                .setMessage(msg)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        FB.deleteFriend(mFriendId);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                    }
                });

            }

            public void bind(Friend friend, String key /* friend's key */) {
                mFriendId = key;
                setRange(friend.getRange());
                setName(friend.getDisplayName());
                setPicture();
            }

            private void setName(String name) {
                mName.setText(name);
            }

            private void setRange(int range) {
                mCurrentRange = LocationRange.toPosition(range);
                mSeekBar.setProgressToIndex(mCurrentRange);
            }

            private void setPicture() {
                new LoadImage(mPicture).loadProfile(mFriendId);
            }

            private void changeRange(int index) {
                int range = LocationRange.toRange(index);
                FB.updateRange(mFriendId, range);
            }

            private void sendChangeRequest(int index) {
                int range = LocationRange.toRange(index);
                FB.sendRangeRequest(mFriendId, range);
            }

            private void sendMessage() {
                // TODO: not implemented yet
            }
        }
    };

}
