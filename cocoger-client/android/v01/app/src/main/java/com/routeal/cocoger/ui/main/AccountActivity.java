package com.routeal.cocoger.ui.main;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.util.LoadImage;
import com.routeal.cocoger.util.Utils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.TimeZone;

/**
 * Created by nabe on 7/25/17.
 */

public class AccountActivity extends AppCompatActivity {

    private Uri mProfilePictureUri;
    private String mName;
    private String mEmail;
    private String mBod;
    private String mGender;
    private boolean mIsSetup = false;
    private TextView mNameView;
    private TextView mEmailView;
    private TextView mBodView;
    private TextView mGenderView;
    private Button mStartApp;
    private ImageView mPictureView;
    private FloatingActionButton mPhotoCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        mName = intent.getStringExtra("displayName");
        mEmail = intent.getStringExtra("email");

        mIsSetup = (mEmail != null && !mEmail.isEmpty());

        mNameView = (TextView) findViewById(R.id.name);
        mEmailView = (TextView) findViewById(R.id.email);
        mBodView = (TextView) findViewById(R.id.bod);
        mGenderView = (TextView) findViewById(R.id.gender);
        mStartApp = (Button) findViewById(R.id.start_app);
        mPictureView = (ImageView) findViewById(R.id.profile_picture);
        mPhotoCameraView = (FloatingActionButton) findViewById(R.id.photo_camera);

        final User user = MainApplication.getUser();

        if (!mIsSetup) {
            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setDisplayHomeAsUpEnabled(true);
                ab.setDisplayShowTitleEnabled(true);
                if (user != null && user.getDisplayName() != null) {
                    ab.setTitle(user.getDisplayName());
                }
            }
        }

        if (user != null && user.getPicture() != null) {
            new LoadImage.LoadImageAsync(true, new LoadImage.LoadImageListener() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    mPictureView.setImageBitmap(bitmap);
                }
            }).execute(user.getPicture());
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.photo_camera);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable drawable = Utils.getIconDrawable(AccountActivity.this, R.drawable.ic_photo_camera_white_48dp);
                mPhotoCameraView.setImageDrawable(drawable);
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setActivityTitle(getResources().getString(R.string.picture))
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setRequestedSize(128, 128)
                        .setFixAspectRatio(true)
                        .setAspectRatio(100, 100)
                        .start(AccountActivity.this);
            }
        });

        if (mName == null) {
            if (user != null && user.getDisplayName() != null) {
                mNameView.setText(user.getDisplayName());
            }
        } else {
            mNameView.setText(mName);
        }
        View view = findViewById(R.id.name_container);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNameView.setError(null);
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(AccountActivity.this);
                View view = layoutInflaterAndroid.inflate(R.layout.dialog_input, null);
                TextView title = (TextView) view.findViewById(R.id.title);
                title.setText("Display Name");
                final TextView text = (TextView) view.findViewById(R.id.text);
                text.setText(mNameView.getText().toString());
                new AlertDialog.Builder(AccountActivity.this)
                        .setView(view)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mName = text.getText().toString();
                                mNameView.setText(mName);
                                ActionBar ab = AccountActivity.this.getSupportActionBar();
                                if (ab != null) {
                                    ab.setTitle(mName);
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();

            }
        });

        if (mEmail != null) {
            mEmailView.setText(mEmail);
        } else {
            if (user != null && user.getEmail() != null) {
                mEmailView.setText(user.getEmail());
            }
        }

        if (mBod == null) {
            if (user != null && user.getBirthYear() != null) {
                mBodView.setText(user.getBirthYear());
            }
        } else {
            mBodView.setText(mBod);
        }
        view = findViewById(R.id.bod_container);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBodView.setError(null);
                final String[] years = getResources().getStringArray(R.array.years);
                String current_bod = mBodView.getText().toString();
                int i = 0;
                for (; i < years.length; i++) {
                    if (current_bod.equalsIgnoreCase(years[i])) {
                        break;
                    }
                }
                final int index = (i == years.length) ? -1 : i;
                new AlertDialog.Builder(AccountActivity.this)
                        .setTitle("Birth Year")
                        .setSingleChoiceItems(R.array.years, index, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String[] years = getResources().getStringArray(R.array.years);
                                mBod = years[which];
                            }
                        })
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mBodView.setText(mBod);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });

        if (mGender == null) {
            if (user != null && user.getGender() != null) {
                mGenderView.setText(user.getGender());
            }
        } else {
            mGenderView.setText(mGender);
        }
        view = findViewById(R.id.gender_container);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGenderView.setError(null);
                final String[] genders = getResources().getStringArray(R.array.genders);
                String current_gender = mGenderView.getText().toString();
                int i = 0;
                for (; i < genders.length; i++) {
                    if (current_gender.equalsIgnoreCase(genders[i])) {
                        break;
                    }
                }
                final int index = (i == genders.length) ? -1 : i;
                new AlertDialog.Builder(AccountActivity.this)
                        .setTitle("Gender")
                        .setSingleChoiceItems(R.array.genders, index, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String[] genders = getResources().getStringArray(R.array.genders);
                                mGender = genders[which];
                            }
                        })
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mGenderView.setText(mGender);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });

        if (!mIsSetup) {
            mStartApp.setVisibility(View.GONE);
        } else {
            mStartApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setup();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (MainApplication.getUser() != null) {
            getMenuInflater().inflate(R.menu.menu_save, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            save();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                try {
                    mProfilePictureUri = result.getUri();
                    InputStream inputStream = getContentResolver().openInputStream(mProfilePictureUri);
                    Drawable drawable = Drawable.createFromStream(inputStream, result.getUri().toString());
                    //drawable.setBounds(0, 0, 512, 512);
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    Bitmap cropped = Utils.cropCircle(bitmap);
                    ImageView imageView = (ImageView) findViewById(R.id.profile_picture);
                    imageView.setImageBitmap(cropped);
                } catch (FileNotFoundException e) {
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping onFail: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void save() {
        final User user = MainApplication.getUser();

        if (mName != null && mName.equals(user.getDisplayName())) {
            mName = null;
        }
        if (mBod != null && mBod.equalsIgnoreCase(user.getBirthYear())) {
            mBod = null;
        }
        if (mGender != null && mGender.equalsIgnoreCase(user.getGender())) {
            mGender = null;
        }

        if (mName != null || mBod != null || mGender != null) {
            FB.updateUser(mName, mGender, mBod);
        }

        if (mProfilePictureUri != null) {
            FB.uploadImageFile(mProfilePictureUri, "profile.jpg", new FB.UploadImageListener() {
                @Override
                public void onSuccess(String url) {
                    // set the url to the user
                    user.setPicture(url);
                    AccountActivity.this.finish();
                }

                @Override
                public void onFail(String err) {
                }
            });
        }

        finish();
    }

    private void setup() {
        boolean validated = true;
        if (mName == null || mName.isEmpty()) {
            validated = false;
            mNameView.setError("No name entered");
        }
        if (mBod == null || mBod.isEmpty()) {
            validated = false;
            mBodView.setError("No birth year selected");
        }
        if (mGender == null || mGender.isEmpty()) {
            validated = false;
            mGenderView.setError("No gender selected");
        }
        if (mProfilePictureUri == null) {
            validated = false;
            Drawable drawable = Utils.getIconDrawable(this, R.drawable.ic_photo_camera_white_48dp, R.color.red);
            mPhotoCameraView.setImageDrawable(drawable);
        }
        if (!validated) {
            return;
        }

        // show the busy cursor
        final ProgressDialog dialog = Utils.getBusySpinner(this);

        // get the user in the memory
        final User user = MainApplication.getUser();
        user.setDisplayName(mName);
        user.setSearchedName(mName.toLowerCase());
        user.setGender(mGender.toLowerCase());
        user.setBirthYear(mBod.toLowerCase());
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
                try {
                    FB.initUser(user);
                } catch (Exception e) {
                }

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
}
