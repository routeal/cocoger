package com.routeal.cocoger.ui.main;

import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.franmontiel.fullscreendialog.FullScreenDialogContent;
import com.franmontiel.fullscreendialog.FullScreenDialogController;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.User;

/**
 * Created by nabe on 8/13/17.
 */

public class UserAddFragment extends Fragment
        implements FullScreenDialogContent,
        View.OnClickListener,
        TextView.OnEditorActionListener {

    private FullScreenDialogController dialogController;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_users, container, false);
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

        return false;
    }

    private boolean updateDatabaseForFriendRequest() {
        /*
        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.user_list);

        FirebaseRecyclerAdapter<User, UserViewHolder> adapter =
                (FirebaseRecyclerAdapter<User, UserViewHolder>) recyclerView.getAdapter();

        for (int i = 0; i < adapter.getItemCount(); i++) {
            DatabaseReference ref = adapter.getRef(i);
        }

        return false;
        */

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
                .orderByChild("name")
                .startAt(text)
                .endAt(text+"~");

        FirebaseRecyclerAdapter<User, UserViewHolder> adapter =
                new FirebaseRecyclerAdapter<User, UserViewHolder>(
                        User.class,
                        R.layout.listview_add_users,
                        UserViewHolder.class,
                        query) {

                    @Override
                    public void populateViewHolder(UserViewHolder holder, User user, int position) {
                        holder.bind(user);
                    }

                    @Override
                    public void onDataChanged() {
                        TextView emptyListMessage = (TextView) getView().findViewById(R.id.empty_list_text);
                        emptyListMessage.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
                    }
                };

        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.user_list);
        recyclerView.setAdapter(adapter);
    }

}
