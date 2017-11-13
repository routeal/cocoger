package com.routeal.cocoger.ui.main;

import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.franmontiel.fullscreendialog.FullScreenDialogContent;
import com.franmontiel.fullscreendialog.FullScreenDialogController;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.manager.FriendManager;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.util.LoadImage;
import com.routeal.cocoger.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by nabe on 8/13/17.
 */

public class UserDialogFragment extends Fragment
        implements FullScreenDialogContent,
        View.OnClickListener,
        TextView.OnEditorActionListener {

    private final static String TAG = "UserDialogFragment";

    private FullScreenDialogController mDialogController;

    private TextView mEmptyText;
    private EditText mSearchText;
    private RecyclerView mRecyclerView;
    private UserListAdapter mAdapter;
    private SortedMap<String, User> mUsers = new TreeMap<>();

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        mEmptyText = (TextView) view.findViewById(R.id.empty_list_text);
        String msg = getResources().getString(R.string.user_search_empty_text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mEmptyText.setText(Html.fromHtml(msg, Html.FROM_HTML_MODE_LEGACY));
        } else {
            mEmptyText.setText(Html.fromHtml(msg));
        }

        mSearchText = (EditText) view.findViewById(R.id.search_text);
        mSearchText.setOnEditorActionListener(this);

        Button searchButton = (Button) view.findViewById(R.id.search_button);
        searchButton.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        layoutManager.setReverseLayout(false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.user_list);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new UserListAdapter();
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.showSoftKeyboard(getActivity(), mSearchText);
    }

    @Override
    public void onDialogCreated(FullScreenDialogController dialogController) {
        this.mDialogController = dialogController;
    }

    @Override
    public boolean onConfirmClick(FullScreenDialogController dialogController) {
        boolean isRequested = updateDatabaseForFriendRequest();
        if (isRequested) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.friend_request)
                    .setMessage(R.string.friend_request_sent)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
        Utils.hideSoftKeyboard(getActivity(), mSearchText);
        return false;
    }

    private boolean updateDatabaseForFriendRequest() {
        if (FB.checkFriendRequest(mAdapter.getSelected())) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.friend_request)
                    .setMessage(R.string.friend_request_failed)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return false;
        }
        return FB.sendFriendRequest(mAdapter.getSelected());
    }

    @Override
    public boolean onDiscardClick(FullScreenDialogController dialogController) {
        Utils.hideSoftKeyboard(getActivity(), mSearchText);
        return false;
    }

    @Override
    public void onClick(View v) {
        searchUser();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            searchUser();
        }
        return false;
    }

    private void searchUser() {
        String text = mSearchText.getText().toString();
        text = text.trim();
        if (text.isEmpty()) {
            return;
        } else {
            mSearchText.setText("");
        }

        FB.findUsers(text, new FB.UserListListener() {
            @Override
            public void onSuccess(SortedMap<String, User> users) {
                mUsers = users;
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(String err) {
                mUsers = new TreeMap<String, User>();
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

        @Override
        public UserListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_user_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(UserListAdapter.ViewHolder holder, int position) {
            Set<String> keySet = mUsers.keySet();
            String[] keys = keySet.toArray(new String[0]);
            String key = keys[position];
            User user = mUsers.get(key);
            holder.bind(user, key);
        }

        @Override
        public int getItemCount() {
            int size = mUsers.size();
            if (size == 0) {
                mEmptyText.setVisibility(View.VISIBLE);
            } else {
                mEmptyText.setVisibility(View.GONE);
            }
            return size;
        }

        List<String> getSelected() {
            List<String> keys = new ArrayList<>();
            for(Map.Entry<String, User> entry : mUsers.entrySet()) {
                String key = entry.getKey();
                User value = entry.getValue();
                if (value.getSelected()) {
                    keys.add(key);
                }
            }
            return keys;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private CheckBox mCheckbox;
            private ImageView mPicture;
            private TextView mName;
            private TextView mLocation;
            private View mView;

            ViewHolder(View itemView) {
                super(itemView);

                mView = itemView;
                mCheckbox = (CheckBox) itemView.findViewById(R.id.check_user);
                mPicture = (ImageView) itemView.findViewById(R.id.picture);
                mName = (TextView) itemView.findViewById(R.id.title);
                mLocation = (TextView) itemView.findViewById(R.id.location);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCheckbox.setChecked(!mCheckbox.isChecked());
                    }
                });
            }

            private void disableInput(boolean checkbox, int color) {
                mView.setBackgroundColor(ContextCompat.getColor(getContext(), color));
                mView.setEnabled(false);
                mView.setClickable(false);
                mCheckbox.setChecked(checkbox);
                mCheckbox.setClickable(false);
            }

            void bind(final User user, String key /* user's key */) {
                setName(user.getDisplayName());
                setPicture(key);

                // callback for checkbox
                mCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        user.setSelected(isChecked);
                    }
                });

                // FIXME:
                // disable myself, don't know how to remove myself from the searched list
                if (FB.isCurrentUser(key)) {
                    disableInput(false, R.color.grey_400);
                    setLocation(mView.getResources().getString(R.string.me));
                    return;
                }

                // check to see already invited
                User me = FB.getUser();
                Map<String, Long> invites = me.getInvites();
                if (invites != null && invites.containsKey(key)) {
                    // already invited
                    disableInput(true, R.color.blue_grey_50);
                    setLocation(mView.getResources().getString(R.string.pending));
                    return;
                }

                // check to see already being friend
                if (FriendManager.getFriend(key) != null) {
                    // already being friend
                    disableInput(true, R.color.teal100);
                    setLocation(mView.getResources().getString(R.string.friend));
                    return;
                }

                FB.getLocation(user.getLocation(), new FB.LocationListener() {
                    @Override
                    public void onSuccess(Location location, Address address) {
                        if (address != null && address.getCountryName() != null) {
                            setLocation(address.getCountryName());
                        }
                    }

                    @Override
                    public void onFail(String err) {
                    }
                });
            }

            private void setName(String name) {
                mName.setText(name);
            }

            private void setLocation(String location) {
                mLocation.setText(location);
            }

            private void setPicture(String key) {
                new LoadImage(mPicture).loadProfile(key);
            }
        }
    }

}
