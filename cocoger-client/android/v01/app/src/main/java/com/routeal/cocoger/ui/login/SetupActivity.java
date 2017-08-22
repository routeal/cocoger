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
import android.util.Log;
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
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.ui.main.SlidingUpPanelMapActivity;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.TimeZone;

public class SetupActivity extends AppCompatActivity {
    private static final String TAG = "SetupActivity";

    private final int MIN_NAME_LENGTH = 4;

    private final int MIN_PASSWORD_LENGTH = 6;

    private User mUser = new User();

    private Uri mProfilePictureUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup);

        // execute to sign up
        Button startButton = (Button) findViewById(R.id.btn_start_app);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startApp();
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
                        .start(SetupActivity.this);
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

    private void startApp() {
        EditText nameText = (EditText) findViewById(R.id.input_name);
        String name = nameText.getText().toString();
        EditText yearBirthText = (EditText) findViewById(R.id.input_year_birth);
        String yearBirth = yearBirthText.getText().toString();
        EditText genderText = (EditText) findViewById(R.id.input_gender);
        String gender = genderText.getText().toString();
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

        if (gender.isEmpty()) {
            genderText.setError(getResources().getString(R.string.no_gender));
            valid = false;
        } else {
            genderText.setError(null);
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

        // save the user in the memory
        MainApplication.setUser(mUser);

/*
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    onSignupSuccess(task.getResult().getUser());
                    dialog.dismiss();
                } else {
                    Log.d(TAG, task.getException().getLocalizedMessage());
                    dialog.dismiss();
                    onSignupFailed(getResources().getString(R.string.signup_failed));
                }
            }
        });
*/
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

    private void onSignupSuccess(final FirebaseUser fbUser) {
        Toast.makeText(getApplicationContext(),
                getResources().getString(R.string.signup_success),
                Toast.LENGTH_LONG).show();

        // Save the profile picture to the server
        String refName = "users/" + fbUser.getUid() + "/image/profile.jpg";

        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

        StorageReference profileRef = mStorageRef.child(refName);

        profileRef.putFile(mProfilePictureUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests")
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        String picture = downloadUrl.toString();
                        mUser.setPicture(picture);

                        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
                        String uid = fbUser.getUid();

                        FirebaseDatabase db = FirebaseDatabase.getInstance();
                        DatabaseReference userRef = db.getReference().child("users");
                        userRef.child(uid).child("picture").setValue(picture);

                        Intent intent = new Intent(getApplicationContext(), SlidingUpPanelMapActivity.class);
                        startActivity(intent);
                        finish();
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


/*
    private PendingIntent getEmailHintIntent() {
        GoogleApiClient client = new GoogleApiClient.Builder(this)
                .addApi(Auth.CREDENTIALS_API)
                .enableAutoManage(this, GoogleApiHelper.getSafeAutoManageId(),
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
*/

}
