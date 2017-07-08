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
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.User;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    private int genderIndex = 0;

    @Bind(R.id.input_name)
    EditText nameText;
    @Bind(R.id.input_year_birth)
    EditText yearBirthText;
    @Bind(R.id.input_email)
    EditText emailText;
    @Bind(R.id.input_gender)
    EditText genderText;
    @Bind(R.id.input_password)
    EditText passwordText;
    @Bind(R.id.input_reEnterPassword)
    EditText reEnterPasswordText;
    @Bind(R.id.btn_signup)
    Button signupButton;
    @Bind(R.id.link_login)
    TextView loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);

        ButterKnife.bind(this);

        // execute to sign up
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        // switch to login screen
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_right_out, R.anim.push_right_in);
            }
        });

        // show a dialog for year selection
        yearBirthText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startYearBirthDialog();
            }
        });

        // show a dialog for gender selection
        genderText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGenderDialog();
            }
        });
    }

    private void startGenderDialog() {
        new MaterialDialog.Builder(this)
                .items(R.array.genders)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        String[] genders = getResources().getStringArray(R.array.genders);
                        genderText.setText(genders[which]);
                        genderText.setError(null);
                        genderIndex = which;
                        return true;
                    }
                })
                .show();
    }

    private void startYearBirthDialog() {
        new MaterialDialog.Builder(this)
                .items(R.array.years)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        String[] years = getResources().getStringArray(R.array.years);
                        yearBirthText.setText(years[which]);
                        yearBirthText.setError(null);
                        return true;
                    }
                })
                .show();
    }

    private void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            return;
        }

        final ProgressDialog dialog = ProgressDialog.show(this, null, null, false, true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.progress_bar);

        signupButton.setEnabled(false);

        String name = nameText.getText().toString();
        String yearBirth = yearBirthText.getText().toString();
        String gender = genderText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        int bod = Integer.parseInt(yearBirth);

        User user = new User();
        user.setName(name);
        user.setBod(bod);
        user.setGender(genderIndex);
        user.setEmail(email);
        user.setPassword(password);

        Call<User> login = MainApplication.getRestClient().service().signup(user);

        login.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                dialog.dismiss();
                if (response.isSuccessful()) {
                    onSignupSuccess();
                } else {
                    onSignupFailed(response.toString());
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                dialog.dismiss();
                onSignupFailed(t.getLocalizedMessage());
            }
        });
    }

    private void onSignupSuccess() {
        // Prompt to login just for testing
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void onSignupFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        signupButton.setEnabled(true);
    }

    private boolean validate() {
        boolean valid = true;

        String name = nameText.getText().toString();
        String yearBirth = yearBirthText.getText().toString();
        String email = emailText.getText().toString();
        String gender = genderText.getText().toString();
        String password = passwordText.getText().toString();
        String reEnterPassword = reEnterPasswordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            nameText.setError("Minimum 4 characters");
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (yearBirth.isEmpty()) {
            yearBirthText.setError("An empty year");
            valid = false;
        } else {
            yearBirthText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Invalid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (gender.isEmpty()) {
            genderText.setError("An empty gender");
            valid = false;
        } else {
            genderText.setError(null);
        }

        // FIXME: PASSWORD CHECKING MUST BE MORE DETAILED

        if (password.isEmpty()) {
            passwordText.setError("An empty password");
            valid = false;
        } else if (password.length() < 6) {
            passwordText.setError("Minimum 6 numbers");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || !(reEnterPassword.equals(password))) {
            reEnterPasswordText.setError("Password not match");
            valid = false;
        } else {
            reEnterPasswordText.setError(null);
        }

        return valid;
    }
}
