package com.routeal.cocoger.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.ui.main.PanelMapActivity;
import com.routeal.cocoger.util.Utils;
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

        Intent intent = getIntent();
        String defaultDisplayName = intent.getStringExtra("displayName");

        if (defaultDisplayName != null && !defaultDisplayName.isEmpty()) {
            TextInputEditText displayName = (TextInputEditText) findViewById(R.id.display_name);
            displayName.setText(defaultDisplayName);
        }

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
                        .setRequestedSize(128, 128)
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

        TextInputEditText displayNameText = (TextInputEditText) findViewById(R.id.display_name);
        String displayName = displayNameText.getText().toString();
        if (displayName.isEmpty()) {
            displayNameText.setError("No name entered");
            valid = false;
        } else {
            displayNameText.setError(null);
        }

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
        final ProgressDialog dialog = Utils.getBusySpinner(this);

        // get the user in the memory
        final User user = MainApplication.getUser();
        user.setDisplayName(displayName);
        if (!displayName.isEmpty()) {
            user.setSearchedName(displayName.toLowerCase());
        }
        user.setGender(gender.toLowerCase());
        user.setBirthYear(yearBirth.toLowerCase());
        user.setCreated(System.currentTimeMillis());
        user.setLocale(getResources().getConfiguration().locale.getDisplayLanguage());
        user.setTimezone(TimeZone.getDefault().getID());

        FB.uploadImageFile(mProfilePictureUri, "profile.jpg", new FB.UploadImageListener() {
            @Override
            public void onSuccess(String url) {
                dialog.dismiss();

                // set the url to the user
                user.setPicture(url);

                // save the user to the database
                FB.initUser(user);

                // start the main map
                Intent intent = new Intent(getApplicationContext(), PanelMapActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFail(String err) {
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
                    //drawable.setBounds(0, 0, 512, 512);
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 128, 128, true));
                } catch (FileNotFoundException e) {
                }
                if (drawable != null) {
                    EditText editText = (EditText) findViewById(R.id.input_picture);
                    editText.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                    editText.setText(" ");
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping onFail: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
