package com.routeal.cocoger.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.franmontiel.fullscreendialog.FullScreenDialogContent;
import com.franmontiel.fullscreendialog.FullScreenDialogController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nabe on 8/13/17.
 */

public class AddFriendFragment extends Fragment
        implements FullScreenDialogContent,
        View.OnClickListener,
        TextView.OnEditorActionListener {

    private FullScreenDialogController dialogController;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_friend, container, false);
    }

    @Override
    public void onDialogCreated(FullScreenDialogController dialogController) {
        this.dialogController = dialogController;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();

        EditText searchText = (EditText) view.findViewById(R.id.search_text);
        searchText.setOnEditorActionListener(this);

        Button searchButton = (Button) view.findViewById(R.id.search_button);
        searchButton.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        layoutManager.setReverseLayout(false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.user_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean onConfirmClick(FullScreenDialogController dialogController) {
        final boolean isRequested = updateDatabaseForFriendRequest();

        if (isRequested) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Friend Request")
                    .setMessage("Has been successfully sent.")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }

        EditText searchText = (EditText) getView().findViewById(R.id.search_text);
        hideKeyboard(searchText);

        return false;
    }

    private boolean updateDatabaseForFriendRequest() {
        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.user_list);

        FirebaseRecyclerAdapter<User, AddFriendViewHolder> adapter =
                (FirebaseRecyclerAdapter<User, AddFriendViewHolder>) recyclerView.getAdapter();

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference userRef = db.getReference().child("users");

        for (int i = 0; i < adapter.getItemCount(); i++) {
            DatabaseReference friendRef = adapter.getRef(i);
            if (friendRef.getKey().equals(uid)) {
                // trying to add myself to friends
                continue;
            }

            // set the timestamp when the friend request is added.
            // When the requested user approved, the timestamp will be changed to true.
            long timestamp = System.currentTimeMillis();

            Map<String, Long> friend = new HashMap<>();

            // add myself to friend
            friend.clear();
            friend.put(uid, timestamp);
            friendRef.child("invitees").setValue(friend);

            // add friends to myself
            String key = adapter.getRef(i).getKey();
            friend.clear();
            friend.put(key, timestamp);
            userRef.child(uid).child("invites").setValue(friend);
        }

        return true;
    }

    @Override
    public boolean onDiscardClick(FullScreenDialogController dialogController) {
        return false;
    }

    @Override
    public void onClick(View v) {
        EditText searchText = (EditText) getView().findViewById(R.id.search_text);
        String text = searchText.getText().toString();
        if (!text.isEmpty()) {
            searchUser(text);
            searchText.setText("");
            hideKeyboard(searchText);
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            String text = v.getText().toString();
            if (!text.isEmpty()) {
                searchUser(text);
                v.setText("");
            }
        }
        return false;
    }

    private void searchUser(String text) {
        // TODO:
        // Exclude 1) myself, 2) current friends but being added to friend

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");

        Query query = userRef
                .orderByChild("searchedName")
                .startAt(text)
                .endAt(text + "~");

        FirebaseRecyclerAdapter<User, AddFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<User, AddFriendViewHolder>(
                        User.class,
                        R.layout.listview_add_friend,
                        AddFriendViewHolder.class,
                        query) {

                    @Override
                    public void populateViewHolder(AddFriendViewHolder holder, User user, int position) {
                        holder.bind(user, getRef(position).getKey());
                    }

                    @Override
                    public void onDataChanged() {
                        // NOTE: this gets called when new people are added to the friends
                        View view = getView();
                        if (view != null) {
                            TextView emptyListMessage = (TextView) view.findViewById(R.id.empty_list_text);
                            emptyListMessage.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
                        }
                    }
                };

        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.user_list);
        recyclerView.setAdapter(adapter);
    }

}
