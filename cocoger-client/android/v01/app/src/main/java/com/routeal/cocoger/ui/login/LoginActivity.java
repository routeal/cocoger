package com.routeal.cocoger.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.github.johnpersano.supertoasts.library.SuperToast;
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.Device;
import com.routeal.cocoger.model.Test;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.ui.main.MapsActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    @Bind(R.id.input_email)
    EditText emailText;
    @Bind(R.id.input_password)
    EditText passwordText;
    @Bind(R.id.btn_login)
    Button loginButton;
    @Bind(R.id.link_signup)
    TextView signupLink;
    @Bind(R.id.link_reset_password)
    TextView resetPasswordLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        resetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PasswordActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
            }
        });

        String email = MainApplication.getString("email");
        emailText.setText(email);
    }

    private void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            return;
        }

        loginButton.setEnabled(false);

        final ProgressDialog dialog = ProgressDialog.show(this, null, null, false, true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.progress_bar);

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        Device device = new Device();
        device.setId("1234");
        device.setPlatform("windows");
        device.setCountry("france");

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setDevice(device);

        Call<User> login = MainApplication.getRestClient().getService().login(user);

        login.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                dialog.dismiss();
                if (response.isSuccessful()) {
                    onLoginSuccess(response.body().getAuthToken());
                } else {
                    onLoginFailed(response.errorBody().toString());
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d(TAG, "retrofit2 enqueue error:", t);
                dialog.dismiss();
                onLoginFailed(t.getLocalizedMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void onLoginSuccess(String token) {
        String jwtToken = String.format("JWT %s", token);
        MainApplication.putString("token", jwtToken);

        String email = emailText.getText().toString();
        MainApplication.putString("email", email);

        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(intent);
        finish();
    }

    private void onLoginFailed(String message) {
        SuperActivityToast.create(this)
                .setText(message)
                .setFrame(Style.FRAME_STANDARD)
                .setDuration(Style.DURATION_MEDIUM)
                .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_RED))
                .setAnimations(Style.ANIMATIONS_POP)
                .show();
        loginButton.setEnabled(true);
    }

    private boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }
}
