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

import com.franmontiel.fullscreendialog.FullScreenDialogContent;
import com.franmontiel.fullscreendialog.FullScreenDialogController;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;

/**
 * Created by nabe on 8/13/17.
 */

public class UserListFragment extends Fragment
        implements FullScreenDialogContent,
        View.OnClickListener,
        TextView.OnEditorActionListener {

    private FullScreenDialogController dialogController;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_list, container, false);
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
        return FB.sendFriendRequest(recyclerView.getAdapter());
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
        try {
            RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.user_list);
            recyclerView.setAdapter(FB.getUserRecyclerAdapter(text, getView()));
        } catch (Exception e) {
        }
    }

}
