package com.routeal.cocoger.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.Device;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.provider.DBUtil;
import com.routeal.cocoger.ui.main.SlidingUpPanelMapActivity;
import com.routeal.cocoger.util.Utils;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        Button loginButton = (Button) findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        TextView signupLink = (TextView) findViewById(R.id.link_signup);
        signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        TextView resetPasswordLink = (TextView) findViewById(R.id.link_reset_password);
        resetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ResetPasswordActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
            }
        });

        String previousEmail = MainApplication.getLoginEmail();
        if (previousEmail != null) {
            EditText emailText = (EditText) findViewById(R.id.input_email);
            emailText.setText(previousEmail);
        }
    }

    private void login() {
        Button loginButton = (Button) findViewById(R.id.btn_login);
        loginButton.setEnabled(false);

        boolean valid = true;

        EditText emailText = (EditText) findViewById(R.id.input_email);
        String email = emailText.getText().toString();

        EditText passwordText = (EditText) findViewById(R.id.input_password);
        String password = passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError(getResources().getString(R.string.invalid_email));
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 6) {
            passwordText.setError(getResources().getString(R.string.min_password));
            valid = false;
        } else {
            passwordText.setError(null);
        }

        if (!valid) return;

        final ProgressDialog dialog = ProgressDialog.show(this, null, null, false, true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.progressbar_spinner);

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            onLoginSuccess(task.getResult().getUser());
                        } else {
                            onLoginFailed(getResources().getString(R.string.login_failed));
                        }
                        dialog.dismiss();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void onLoginSuccess(FirebaseUser firebaseUser) {
        final String uid = firebaseUser.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
        userRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                // save the user info to the local database
                DBUtil.deleteUser();
                DBUtil.saveUser(user);

                // save the email address to the local preference
                MainApplication.setLoginEmail(user.getEmail());

                // the current device
                final Device current_device = Utils.getDevice();

                String devKey = null;

                // get the device key to match the device id in the user
                Map<String, String> devList = user.getDevices();
                if (devList != null && !devList.isEmpty()) {
                    for (Map.Entry<String, String> entry : devList.entrySet()) {
                        // the values is a device id
                        if (entry.getValue().equals(current_device.getDeviceId())) {
                            devKey = entry.getKey();
                        }
                    }
                }

                if (devKey != null) {
                    // update the timestamp of the device
                    DatabaseReference devRef = FirebaseDatabase.getInstance().getReference().child("devices");
                    devRef.child(devKey).child("timestamp").setValue(current_device.getTimestamp());
                } else {
                    // add a new device
                    addDevice(uid, current_device);
                }

                startMain();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addDevice(String uid, Device newDevice) {
        // add this new device to the database
        DatabaseReference devRef = FirebaseDatabase.getInstance().getReference().child("devices");
        String newKey = devRef.push().getKey();
        newDevice.setUid(uid);
        devRef.child(newKey).setValue(newDevice);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
        userRef.child(uid).child("devices").child(newKey).setValue(newDevice.getDeviceId());
    }

    private void startMain() {
        Intent intent = new Intent(getApplicationContext(), SlidingUpPanelMapActivity.class);
        startActivity(intent);
        finish();
    }

    private void onLoginFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Button loginButton = (Button) findViewById(R.id.btn_login);
        loginButton.setEnabled(true);
    }
}
