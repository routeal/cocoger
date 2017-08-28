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
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
        boolean valid = true;

        EditText yearBirthText = (EditText) findViewById(R.id.input_year_birth);
        String yearBirth = yearBirthText.getText().toString();
        if (yearBirth.isEmpty()) {
            yearBirthText.setError(getResources().getString(R.string.no_year_birth));
            valid = false;
        } else {
            yearBirthText.setError(null);
        }

        EditText genderText = (EditText) findViewById(R.id.input_gender);
        String gender = genderText.getText().toString();
        if (gender.isEmpty()) {
            genderText.setError(getResources().getString(R.string.no_gender));
            valid = false;
        } else {
            genderText.setError(null);
        }

        EditText pictureText = (EditText) findViewById(R.id.input_picture);
        if (mProfilePictureUri == null) {
            pictureText.setError(getResources().getString(R.string.no_picture));
            valid = false;
        } else {
            pictureText.setError(null);
        }

        if (!valid) return;

        // show the busy cursor
        final ProgressDialog dialog = ProgressDialog.show(this, null, null, false, true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.progressbar_spinner);

        // get the user in the memory
        final User user = MainApplication.getUser();
        /// get the displayname from the firebase user
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        user.setDisplayName(fbUser.getDisplayName());
        String displayName = user.getDisplayName();
        if (displayName != null) {
            user.setSearchedName(displayName.toLowerCase());
        }
        user.setGender(gender.toLowerCase());
        user.setBirthYear(yearBirth.toLowerCase());
        user.setCreated(System.currentTimeMillis());
        user.setLocale(getResources().getConfiguration().locale.getDisplayLanguage());
        user.setTimezone(TimeZone.getDefault().getID());

        // save the profile picture to the server
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String refName = "users/" + uid + "/image/profile.jpg";
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profileRef = mStorageRef.child(refName);
        profileRef.putFile(mProfilePictureUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests")
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        String picture = downloadUrl.toString();
                        user.setPicture(picture);

                        // update the user in the firebase database
                        FirebaseDatabase db = FirebaseDatabase.getInstance();
                        DatabaseReference userRef = db.getReference().child("users").child(uid);
                        userRef.child("picture").setValue(user.getPicture());
                        userRef.child("searchedName").setValue(user.getSearchedName());
                        userRef.child("gender").setValue(user.getGender());
                        userRef.child("birthYear").setValue(user.getBirthYear());
                        userRef.child("created").setValue(user.getCreated());
                        userRef.child("locale").setValue(user.getLocale());
                        userRef.child("timezone").setValue(user.getTimezone());

                        // start the main map
                        Intent intent = new Intent(getApplicationContext(), SlidingUpPanelMapActivity.class);
                        startActivity(intent);
                        finish();

                        dialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // TODO: what to do???
                        dialog.dismiss();
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
}
