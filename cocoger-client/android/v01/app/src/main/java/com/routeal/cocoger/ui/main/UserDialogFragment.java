package com.routeal.cocoger.ui.main;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.franmontiel.fullscreendialog.FullScreenDialogContent;
import com.franmontiel.fullscreendialog.FullScreenDialogController;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.util.Utils;

/**
 * Created by nabe on 8/13/17.
 */

public class UserDialogFragment extends Fragment
        implements FullScreenDialogContent,
        View.OnClickListener,
        TextView.OnEditorActionListener {

    private final static String TAG = "UserDialogFragment";

    private FullScreenDialogController mDialogController;

    private EditText mSearchText;
    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter mAdapter;

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        TextView emptyText = (TextView) view.findViewById(R.id.empty_list_text);
        String msg = getResources().getString(R.string.user_search_empty_text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            emptyText.setText(Html.fromHtml(msg, Html.FROM_HTML_MODE_LEGACY));
        } else {
            emptyText.setText(Html.fromHtml(msg));
        }

        mSearchText = (EditText) view.findViewById(R.id.search_text);
        mSearchText.setOnEditorActionListener(this);

        Button searchButton = (Button) view.findViewById(R.id.search_button);
        searchButton.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        layoutManager.setReverseLayout(false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.user_list);
        mRecyclerView.setLayoutManager(layoutManager);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.showSoftKeyboard(getActivity(), mSearchText);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public void onDialogCreated(FullScreenDialogController dialogController) {
        this.mDialogController = dialogController;
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
        if (FB.checkFriendRequest(mRecyclerView.getAdapter())) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.friend_request)
                    .setMessage(R.string.friend_request_failed)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return false;
        }
        return FB.sendFriendRequest(mRecyclerView.getAdapter());
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
        if (!text.isEmpty()) {
            if (mAdapter != null) {
                mAdapter.stopListening();
            }
            // TOOD: should not use FB UI for this
            mAdapter = FB.getUserRecyclerAdapter(text, getView());
            mAdapter.startListening();
            mRecyclerView.setAdapter(mAdapter);
            mSearchText.setText("");
        }
    }

}
