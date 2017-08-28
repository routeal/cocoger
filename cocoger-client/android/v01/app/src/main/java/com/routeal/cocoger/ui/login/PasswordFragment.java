package com.routeal.cocoger.ui.login;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.routeal.cocoger.R;

/**
 * Created by nabe on 8/21/17.
 */

public class PasswordFragment extends Fragment {
    private final String TAG = "PasswordFragment";

    private TextInputEditText mEmailText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

        String email = mEmailText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailText.setError(getResources().getString(R.string.invalid_email));
            return;
        } else {
            mEmailText.setError(null);
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String message;
                        if (task.isSuccessful()) {
                            message = getResources().getString(R.string.password_reset_email_sent);
                        } else {
                            message = task.getException().getLocalizedMessage();
                        }
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                        getActivity().finish();
                    }
                });
    }
}
