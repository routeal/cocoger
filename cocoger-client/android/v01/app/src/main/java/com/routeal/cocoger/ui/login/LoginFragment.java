package com.routeal.cocoger.ui.login;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.routeal.cocoger.R;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nabe on 8/21/17.
 */

public class LoginFragment extends Fragment {

    private final String TAG = "LoginFragment";

    private static final int RC_HINT = 13;

    private Credential mLastCredential;
    private TextInputEditText mEmailText;
    private TextInputEditText mDisplayName;
    private TextInputEditText mPassword;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        mEmailText = (TextInputEditText) v.findViewById(R.id.input_email);
        mDisplayName = (TextInputEditText) v.findViewById(R.id.display_name);
        mPassword = (TextInputEditText) v.findViewById(R.id.input_password);

        TextView textView = (TextView) v.findViewById(R.id.term_privacy);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(Html.fromHtml(getResources().getString(R.string.signup_note)));

        Button loginButton = (Button) v.findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(v);
            }
        });

        Button signupButton = (Button) v.findViewById(R.id.btn_signup);
        signupButton.setOnClickListener(new View.OnClickListener() {
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
                        mDisplayName.setText(mLastCredential.getName());
                    }
                }
                break;
        }
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
                .enableAutoManage(getActivity(), getSafeAutoManageId(),
                        new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                                Log.e(TAG, "Client connection failed: " + connectionResult.getErrorMessage());
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

    private static final AtomicInteger SAFE_ID = new AtomicInteger(10);

    private static int getSafeAutoManageId() {
        return SAFE_ID.getAndIncrement();
    }

    private void login(View view) {
    }

    private void signup(View view) {
        Intent intent = new Intent(getActivity(), SetupActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}
