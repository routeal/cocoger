package com.routeal.cocoger.ui.login;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.provider.DBUtil;
import com.routeal.cocoger.ui.main.AccountActivity;
import com.routeal.cocoger.ui.main.PanelMapActivity;
import com.routeal.cocoger.util.Utils;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nabe on 8/21/17.
 */

public class LoginFragment extends Fragment {

    private static final int RC_HINT = 13;
    private static final AtomicInteger SAFE_ID = new AtomicInteger(10);
    private final String TAG = "LoginFragment";
    private Credential mLastCredential;
    private TextInputEditText mEmailText;
    //private TextInputEditText mDisplayName;
    private TextInputEditText mPassword;
    private Button mLoginButton;
    private Button mSignupButton;

    private String mDisplayName;

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        mEmailText = (TextInputEditText) v.findViewById(R.id.input_email);
        //mDisplayName = (TextInputEditText) v.findViewById(R.id.display_name);
        mPassword = (TextInputEditText) v.findViewById(R.id.input_password);

        TextView textView = (TextView) v.findViewById(R.id.term_privacy);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(getResources().getString(R.string.signup_note), Html.FROM_HTML_MODE_LEGACY));
        } else {
            textView.setText(Html.fromHtml(getResources().getString(R.string.signup_note)));
        }

        mLoginButton = (Button) v.findViewById(R.id.btn_login);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(v);
            }
        });

        mSignupButton = (Button) v.findViewById(R.id.btn_signup);
        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup(v);
            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showEmailAutoCompleteHint();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_HINT:
                if (data != null) {
                    mLastCredential = data.getParcelableExtra(Credential.EXTRA_KEY);
                    if (mLastCredential != null) {
                        mEmailText.setText(mLastCredential.getId());
                        //mDisplayName.setText(mLastCredential.getName());
                        mDisplayName = mLastCredential.getName();
                    }
                }
                break;
        }
    }

    String getEmail() {
        return mEmailText.getText().toString();
    }

    private void showEmailAutoCompleteHint() {
        try {
            startIntentSenderForResult(getEmailHintIntent().getIntentSender(), RC_HINT);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Unable to start hint intent", e);
        }
    }

    private PendingIntent getEmailHintIntent() {
        GoogleApiClient client = new GoogleApiClient.Builder(getContext())
                .addApi(Auth.CREDENTIALS_API)
                .enableAutoManage(getActivity(), SAFE_ID.getAndIncrement(),
                        new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(
                                    @NonNull ConnectionResult connectionResult) {
                                Log.e(TAG, "Client connection onFail: " +
                                        connectionResult.getErrorMessage());
                            }
                        })
                .build();

        HintRequest hintRequest = new HintRequest.Builder()
                .setHintPickerConfig(new CredentialPickerConfig.Builder()
                        .setShowCancelButton(true)
                        .build())
                .setEmailAddressIdentifierSupported(true)
                .build();

        return Auth.CredentialsApi.getHintPickerIntent(client, hintRequest);
    }

    private void startIntentSenderForResult(IntentSender sender, int requestCode)
            throws IntentSender.SendIntentException {
        startIntentSenderForResult(sender, requestCode, null, 0, 0, 0, null);
    }

    private void login(View view) {
        mLoginButton.setEnabled(false);
        mSignupButton.setEnabled(false);

        String email = mEmailText.getText().toString();
        String password = mPassword.getText().toString();

        if (email.isEmpty()) {
            mEmailText.setError("Email not entered");
            return;
        }
        if (password.isEmpty()) {
            mPassword.setError("Password not entered");
            return;
        }

        mEmailText.setError(null);
        mPassword.setError(null);

        final ProgressDialog dialog = Utils.getBusySpinner(getContext());

        FB.signIn(getActivity(), email, password, new FB.SignInListener() {
            @Override
            public void onSuccess() {
                dialog.dismiss();
                Intent intent = new Intent(getContext(), PanelMapActivity.class);
                startActivity(intent);
                getActivity().finish();
            }

            @Override
            public void onFail(String err) {
                dialog.dismiss();
                onFailed(err);
            }
        });
    }

    private void signup(View view) {
        mLoginButton.setEnabled(false);
        mSignupButton.setEnabled(false);

        final String email = mEmailText.getText().toString();
        String password = mPassword.getText().toString();

        final ProgressDialog dialog = Utils.getBusySpinner(getContext());

        FB.createUser(getActivity(), email, password, new FB.CreateUserListener() {
            @Override
            public void onSuccess(String key) {
                dialog.dismiss();
                // start the setup
                Intent intent = new Intent(getActivity(), AccountActivity.class);
                if (mDisplayName != null && !mDisplayName.isEmpty()) {
                    intent.putExtra("displayName", mDisplayName);
                }
                intent.putExtra("email", email);
                startActivity(intent);
                getActivity().finish();

                DBUtil.saveMessage(key, "Welcome", "Hope that you enjoy cocoger!!!",
                        R.drawable.ic_person_pin_circle_white_48dp, new Date());
            }

            @Override
            public void onFail(String err) {
                dialog.dismiss();
                onFailed(err);
            }
        });
    }

    private void onFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        mLoginButton.setEnabled(true);
        mSignupButton.setEnabled(true);
    }
}
