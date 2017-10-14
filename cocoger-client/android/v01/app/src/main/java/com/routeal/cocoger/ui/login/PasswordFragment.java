package com.routeal.cocoger.ui.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
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
        View v = inflater.inflate(R.layout.fragment_passwd_reset, container, false);
        mEmailText = (TextInputEditText) v.findViewById(R.id.input_email);
        AppCompatButton resetButton = (AppCompatButton) v.findViewById(R.id.reset_passwd);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
        return v;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            ViewPager pager = (ViewPager) getActivity().findViewById(R.id.viewPager);
            Fragment fragment = ((FragmentPagerAdapter) pager.getAdapter()).getItem(0);
            mEmailText.setText(((LoginFragment) fragment).getEmail());
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

        FB.resetPassword(email, new FB.ResetPasswordListener() {
            void backToLogin(String msg) {
                // show the message
                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                // back to the main login page
                ViewPager pager = (ViewPager) getActivity().findViewById(R.id.viewPager);
                if (pager != null) {
                    pager.setCurrentItem(0, false);
                }
            }

            @Override
            public void onSuccess() {
                String message = getResources().getString(R.string.password_reset_email_sent);
                backToLogin(message);
            }

            @Override
            public void onFail(String err) {
                backToLogin(err);
            }
        });
    }
}
