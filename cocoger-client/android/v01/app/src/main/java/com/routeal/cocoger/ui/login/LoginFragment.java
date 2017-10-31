package com.routeal.cocoger.ui.login;

import android.app.PendingIntent;
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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nabe on 8/21/17.
 */

public class LoginFragment extends Fragment {

    private static final int RC_HINT = 13;
    private static final AtomicInteger SAFE_ID = new AtomicInteger(10);
    private final String TAG = "LoginFragment";
    private TextInputEditText mEmailText;
    private TextInputEditText mPasswordText;
    private Button mLoginButton;
    private Button mSignupButton;
    private String mDisplayName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mEmailText = (TextInputEditText) view.findViewById(R.id.input_email);
        mPasswordText = (TextInputEditText) view.findViewById(R.id.input_password);

        TextView noteView = (TextView) view.findViewById(R.id.text_term_privacy);
        noteView.setMovementMethod(LinkMovementMethod.getInstance());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            noteView.setText(Html.fromHtml(getResources().getString(R.string.signup_note), Html.FROM_HTML_MODE_LEGACY));
        } else {
            noteView.setText(Html.fromHtml(getResources().getString(R.string.signup_note)));
        }

        mLoginButton = (Button) view.findViewById(R.id.action_login);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(v);
            }
        });

        mSignupButton = (Button) view.findViewById(R.id.action_signup);
        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup(v);
            }
        });

        return view;
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
                    Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                    if (credential != null) {
                        mEmailText.setText(credential.getId());
                        mDisplayName = credential.getName();
                    }
                }
                break;
        }
    }

    // FIXME: kind wired
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
        String password = mPasswordText.getText().toString();

        if (email.isEmpty()) {
            mEmailText.setError(getResources().getString(R.string.email_not_entered));
            return;
        }
        if (password.isEmpty()) {
            mPasswordText.setError(getResources().getString(R.string.password_not_entered));
            return;
        }

        mEmailText.setError(null);
        mPasswordText.setError(null);

        final Utils.ProgressBarView dialog = Utils.getProgressBar(getActivity());

        FB.signIn(getActivity(), email, password, new FB.SignInListener() {
            @Override
            public void onSuccess() {
                dialog.hide();
                Intent intent = new Intent(getContext(), PanelMapActivity.class);
                startActivity(intent);
                getActivity().finish();
            }

            @Override
            public void onFail(String err) {
                dialog.hide();
                onFailed(err);
            }
        });
    }

    private void signup(View view) {
        mLoginButton.setEnabled(false);
        mSignupButton.setEnabled(false);

        final String email = mEmailText.getText().toString();
        String password = mPasswordText.getText().toString();

        if (email.isEmpty()) {
            mEmailText.setError(getResources().getString(R.string.email_not_entered));
            return;
        }
        if (password.isEmpty()) {
            mPasswordText.setError(getResources().getString(R.string.password_not_entered));
            return;
        }

        mEmailText.setError(null);
        mPasswordText.setError(null);

        final Utils.ProgressBarView dialog = Utils.getProgressBar(getActivity());

        FB.createUser(getActivity(), email, password, new FB.SignUpListener() {
            @Override
            public void onSuccess(String key) {
                dialog.hide();

                // start the setup
                Intent intent = new Intent(getActivity(), AccountActivity.class);
                intent.putExtra(AccountActivity.EMAIL, email);
                if (mDisplayName != null && !mDisplayName.isEmpty()) {
                    intent.putExtra(AccountActivity.DISPLAY_NAME, mDisplayName);
                }
                startActivity(intent);

                // remove the activity
                getActivity().finish();

                DBUtil.saveMessage(key,
                        getResources().getString(R.string.welcome),
                        getResources().getString(R.string.welcome_message),
                        R.drawable.ic_person_pin_circle_white_48dp,
                        System.currentTimeMillis());
            }

            @Override
            public void onFail(String err) {
                dialog.hide();
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
