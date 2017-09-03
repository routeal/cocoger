package com.routeal.cocoger.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;

/**
 * Created by nabe on 8/21/17.
 */

public class PasswordFragment extends Fragment {
    private final String TAG = "PasswordFragment";

    private TextInputEditText mEmailText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_password, container, false);
        mEmailText = (TextInputEditText) v.findViewById(R.id.input_email);
        AppCompatButton resetButton = (AppCompatButton) v.findViewById(R.id.btn_reset_password);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
        return v;
    }

    private LoginFragment login;

    void setLoginFragment(LoginFragment login) {
        this.login = login;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            mEmailText.setText(login.getEmail());
        }
    }

    private void resetPassword() {
        Log.d(TAG, "resetPassword");

        final String email = mEmailText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailText.setError(getResources().getString(R.string.invalid_email));
            return;
        } else {
            mEmailText.setError(null);
        }

        FB.resetPassword(email, new FB.ResetPasswordListener() {
            void wrapup(String msg) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            }

            @Override
            public void onSuccess() {
                String message = getResources().getString(R.string.password_reset_email_sent);
                wrapup(message);
            }

            @Override
            public void onFail(String err) {
                wrapup(err);
            }
        });
    }
}
