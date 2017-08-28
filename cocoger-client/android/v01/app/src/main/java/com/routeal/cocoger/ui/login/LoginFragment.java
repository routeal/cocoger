package com.routeal.cocoger.ui.login;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.Device;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.ui.main.SlidingUpPanelMapActivity;
import com.routeal.cocoger.util.Utils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nabe on 8/21/17.
 */

public class LoginFragment extends Fragment {

    private final String TAG = "LoginFragment";

    private static final int RC_HINT = 13;

    private static final AtomicInteger SAFE_ID = new AtomicInteger(10);

    private Credential mLastCredential;
    private TextInputEditText mEmailText;
    private TextInputEditText mDisplayName;
    private TextInputEditText mPassword;
    private Button mLoginButton;
    private Button mSignupButton;

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
                        mDisplayName.setText(mLastCredential.getName());
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

    private void login(View view) {
        mLoginButton.setEnabled(false);
        mSignupButton.setEnabled(false);

        String email = mEmailText.getText().toString();
        String password = mPassword.getText().toString();

        final ProgressDialog dialog = ProgressDialog.show(getContext(), null, null, false, true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.progressbar_spinner);

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //onLoginSucceeded(task.getResult().getUser());

                            Intent intent = new Intent(getContext(), SlidingUpPanelMapActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        } else {
                            onFailed(task.getException().getLocalizedMessage());
                        }
                        dialog.dismiss();
                    }
                });

    }

    private void signup(View view) {
        mLoginButton.setEnabled(false);
        mSignupButton.setEnabled(false);

        String email = mEmailText.getText().toString();
        String password = mPassword.getText().toString();

        final ProgressDialog dialog = ProgressDialog.show(getContext(), null, null, false, true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.progressbar_spinner);

        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // save the display name
                            String name = mDisplayName.getText().toString();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name).build();
                            task.getResult().getUser().updateProfile(profileUpdates);

                            // start the setup
                            Intent intent = new Intent(getActivity(), SetupActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        } else {
                            onFailed(task.getException().getLocalizedMessage());
                        }
                        dialog.dismiss();
                    }
                });

    }

    private void onFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        mLoginButton.setEnabled(true);
        mSignupButton.setEnabled(true);
    }

    private void onLoginSucceeded(FirebaseUser fbUser) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
        userRef.child(fbUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                MainApplication.setUser(user);

                // the current device
                Device currentDevice = Utils.getDevice();

                String devKey = null;

                // get the device key to match the device id in the user database
                Map<String, String> devList = user.getDevices();
                if (devList != null && !devList.isEmpty()) {
                    for (Map.Entry<String, String> entry : devList.entrySet()) {
                        // the values is a device id
                        if (entry.getValue().equals(currentDevice.getDeviceId())) {
                            devKey = entry.getKey();
                        }
                    }
                }

                String uid = dataSnapshot.getKey();

                DatabaseReference devRef = FirebaseDatabase.getInstance().getReference().child("devices");

                if (devKey != null) {
                    // update the timestamp of the device
                    devRef.child(devKey).child("timestamp").setValue(currentDevice.getTimestamp());
                } else {
                    // set the uid to the device before save
                    currentDevice.setUid(uid);

                    // add it as a new device
                    String newKey = devRef.push().getKey();
                    devRef.child(newKey).setValue(currentDevice);

                    // also add it to the user database under 'devices'
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
                    userRef.child(uid).child("devices").child(newKey).setValue(currentDevice.getDeviceId());
                }

                Intent intent = new Intent(getContext(), SlidingUpPanelMapActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

}
