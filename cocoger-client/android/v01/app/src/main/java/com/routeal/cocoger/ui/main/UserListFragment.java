package com.routeal.cocoger.ui.main;

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

import com.franmontiel.fullscreendialog.FullScreenDialogContent;
import com.franmontiel.fullscreendialog.FullScreenDialogController;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.util.Utils;

/**
 * Created by nabe on 8/13/17.
 */

public class UserListFragment extends Fragment
        implements FullScreenDialogContent,
        View.OnClickListener,
        TextView.OnEditorActionListener {

    private FullScreenDialogController mDialogController;

    private EditText mSearchText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_list, container, false);
    }

    @Override
    public void onDialogCreated(FullScreenDialogController dialogController) {
        this.mDialogController = dialogController;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();

        mSearchText = (EditText) view.findViewById(R.id.search_text);
        mSearchText.setOnEditorActionListener(this);
        Utils.showSoftKeyboard(getActivity(), mSearchText);

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
                    .setTitle(R.string.friend_request)
                    .setMessage(R.string.friend_request_sent)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
        Utils.hideSoftKeyboard(getActivity(), mSearchText);
        return false;
    }

    private boolean updateDatabaseForFriendRequest() {
        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.user_list);
        if (FB.checkFriendRequest(recyclerView.getAdapter())) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.friend_request)
                    .setMessage(R.string.friend_request_failed)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return false;
        }
        return FB.sendFriendRequest(recyclerView.getAdapter());
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
        text.trim();
        if (!text.isEmpty()) {
            RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.user_list);
            recyclerView.setAdapter(FB.getUserRecyclerAdapter(text, getView()));
            mSearchText.setText("");
        }
        Utils.showSoftKeyboard(getActivity(), mSearchText);
    }

}
