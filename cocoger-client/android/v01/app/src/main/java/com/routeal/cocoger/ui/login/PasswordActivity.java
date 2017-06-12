package com.routeal.cocoger.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.routeal.cocoger.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by nabe on 6/11/17.
 */

public class PasswordActivity extends AppCompatActivity {
    private static final String TAG = "PasswordActivity";

    @Bind(R.id.input_email)
    EditText emailText;
    @Bind(R.id.btn_reset_password)
    Button resetButton;
    @Bind(R.id.link_login)
    TextView loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        ButterKnife.bind(this);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_out, R.anim.push_left_in);
            }
        });
    }

    private void resetPassword() {
        Log.d(TAG, "resetPassword");

        String email = emailText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Invalid email address");
            return;
        } else {
            emailText.setError(null);
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }
}
