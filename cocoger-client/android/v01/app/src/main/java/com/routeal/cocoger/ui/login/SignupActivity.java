package com.routeal.cocoger.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.Device;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.provider.DBUtil;
import com.routeal.cocoger.ui.main.SlidingUpPanelMapActivity;
import com.routeal.cocoger.util.Utils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    private final int MIN_NAME_LENGTH = 4;

    private final int MIN_PASSWORD_LENGTH = 6;

    private User mUser = new User();

    private Uri mProfilePictureUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);

        // execute to sign up
        Button signupButton = (Button) findViewById(R.id.btn_signup);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeSignup();
            }
        });

        // switch to login screen
        TextView loginLink = (TextView) findViewById(R.id.link_login);
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
        EditText yearBirthText = (EditText) findViewById(R.id.input_year_birth);
        yearBirthText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startYearBirthDialog();
            }
        });

        // show a dialog for gender selection
        EditText genderText = (EditText) findViewById(R.id.input_gender);
        genderText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGenderDialog();
            }
        });

        // show the picture app
        EditText pictureInput = (EditText) findViewById(R.id.input_picture);
        pictureInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setActivityTitle(getResources().getString(R.string.picture))
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setRequestedSize(512, 512)
                        .setFixAspectRatio(true)
                        .setAspectRatio(100, 100)
                        .start(SignupActivity.this);
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
                        EditText genderText = (EditText) findViewById(R.id.input_gender);
                        genderText.setText(genders[which]);
                        genderText.setError(null);
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
                        EditText yearBirthText = (EditText) findViewById(R.id.input_year_birth);
                        yearBirthText.setText(years[which]);
                        yearBirthText.setError(null);
                        return true;
                    }
                })
                .show();
    }

    private void executeSignup() {
        EditText nameText = (EditText) findViewById(R.id.input_name);
        String name = nameText.getText().toString();
        EditText yearBirthText = (EditText) findViewById(R.id.input_year_birth);
        String yearBirth = yearBirthText.getText().toString();
        EditText genderText = (EditText) findViewById(R.id.input_gender);
        String gender = genderText.getText().toString();
        EditText emailText = (EditText) findViewById(R.id.input_email);
        String email = emailText.getText().toString();
        EditText passwordText = (EditText) findViewById(R.id.input_password);
        String password = passwordText.getText().toString();
        EditText reEnterPasswordText = (EditText) findViewById(R.id.input_reEnterPassword);
        String reEnterPassword = reEnterPasswordText.getText().toString();
        EditText pictureText = (EditText) findViewById(R.id.input_picture);

        boolean valid = true;

        if (name.isEmpty() || name.length() < MIN_NAME_LENGTH) {
            nameText.setError(getResources().getString(R.string.min_name));
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (yearBirth.isEmpty()) {
            yearBirthText.setError(getResources().getString(R.string.no_year_birth));
            valid = false;
        } else {
            yearBirthText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError(getResources().getString(R.string.invalid_email));
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (gender.isEmpty()) {
            genderText.setError(getResources().getString(R.string.no_gender));
            valid = false;
        } else {
            genderText.setError(null);
        }

        if (password.isEmpty()) {
            passwordText.setError(getResources().getString(R.string.no_password));
            valid = false;
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            passwordText.setError(getResources().getString(R.string.min_password));
            valid = false;
        } else {
            passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || !(reEnterPassword.equals(password))) {
            reEnterPasswordText.setError(getResources().getString(R.string.no_password_match));
            valid = false;
        } else {
            reEnterPasswordText.setError(null);
        }

        if (mProfilePictureUri == null) {
            pictureText.setError(getResources().getString(R.string.no_picture));
            valid = false;
        } else {
            pictureText.setError(null);
        }

        if (!valid) return;

        mUser.setName(name);
        mUser.setSearchedName(name.toLowerCase());
        mUser.setEmail(email);
        mUser.setGender(gender.toLowerCase());
        mUser.setBirthYear(yearBirth.toLowerCase());
        mUser.setCreated(System.currentTimeMillis());
        mUser.setLocale(getResources().getConfiguration().locale.getDisplayLanguage());
        mUser.setTimezone(TimeZone.getDefault().getID());

        final ProgressDialog dialog = ProgressDialog.show(this, null, null, false, true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.progressbar_spinner);

        Button signupButton = (Button) findViewById(R.id.btn_signup);
        signupButton.setEnabled(false);

        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    onSignupSuccess(task.getResult().getUser());
                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                    onSignupFailed(getResources().getString(R.string.signup_failed));
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Drawable drawable = null;
                try {
                    mProfilePictureUri = result.getUri();
                    InputStream inputStream = getContentResolver().openInputStream(mProfilePictureUri);
                    drawable = Drawable.createFromStream(inputStream, result.getUri().toString());
                } catch (FileNotFoundException e) {
                }
                if (drawable != null) {
                    EditText editText = (EditText) findViewById(R.id.input_picture);
                    editText.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                    editText.setText(" ");
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void onSignupSuccess(final FirebaseUser firebaseUser) {
        // Send email verification to the signup email address
        sendEmailVerification();

        // Save the profile picture to the server
        String refName = "users/" + firebaseUser.getUid() + "/image/profile.jpg";

        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

        StorageReference profileRef = mStorageRef.child(refName);

        profileRef.putFile(mProfilePictureUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests")
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        mUser.setPicture(downloadUrl.toString());
                        saveUserInfo(firebaseUser);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // TODO: what to do???
                    }
                });
    }

    private void onSignupFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Button signupButton = (Button) findViewById(R.id.btn_signup);
        signupButton.setEnabled(true);
    }

    private void sendEmailVerification() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.verification_email_sent),
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void saveUserInfo(FirebaseUser firebaseUser) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        String uid = firebaseUser.getUid();

        // save the device info
        Device device = Utils.getDevice();
        device.setUid(uid);
        DatabaseReference devRef = db.getReference().child("devices");
        String mKey = devRef.push().getKey();
        devRef.child(mKey).setValue(device);

        // save the user info to the remote database
        DatabaseReference userRef = db.getReference().child("users");
        userRef.child(uid).setValue(mUser);

        // add the device key to the user info
        Map<String, String> devices = new HashMap<>();
        devices.put(mKey, device.getDeviceId());
        userRef.child(uid).child("devices").setValue(devices);

        // save the user info to the local database
        DBUtil.deleteUser();
        DBUtil.saveUser(mUser);

        // save the email address to the local preference
        MainApplication.setLoginEmail(mUser.getEmail());

        Intent intent = new Intent(getApplicationContext(), SlidingUpPanelMapActivity.class);
        startActivity(intent);
        finish();
    }
}
