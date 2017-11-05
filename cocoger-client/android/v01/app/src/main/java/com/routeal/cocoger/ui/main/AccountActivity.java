package com.routeal.cocoger.ui.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.provider.DBUtil;
import com.routeal.cocoger.util.LoadImage;
import com.routeal.cocoger.util.Utils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.TimeZone;

/**
 * Created by nabe on 7/25/17.
 */

/**
 * AccountActivity is an activity for the users to set up their profile at sign-up and also
 * to update their profile after sign-in.
 * When it is used for the profile setup at sign-up, both name and email are given
 * from the login activity through the intent.
 */
public class AccountActivity extends AppCompatActivity {

    public final static String DISPLAY_NAME = "displayName";
    public final static String EMAIL = "email";
    private final static String TAG = "AccountActivity";
    private boolean mIsInitialSetup = false;
    private String mName;
    private String mEmail;
    private String mBod;
    private String mGender;
    private TextView mNameView;
    private TextView mEmailView;
    private TextView mBodView;
    private TextView mGenderView;
    private Button mStartApp;
    private ImageView mPictureView;
    private FloatingActionButton mPhotoCameraView;
    private Uri mProfilePictureUri;
    private CollapsingToolbarLayout mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mToolBar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        mNameView = (TextView) findViewById(R.id.name);
        mEmailView = (TextView) findViewById(R.id.email);
        mBodView = (TextView) findViewById(R.id.bod);
        mGenderView = (TextView) findViewById(R.id.gender);
        mStartApp = (Button) findViewById(R.id.start_app);
        mPictureView = (ImageView) findViewById(R.id.profile_picture);
        mPhotoCameraView = (FloatingActionButton) findViewById(R.id.photo_camera);
        View nameContainer = findViewById(R.id.name_container);
        View bodContainer = findViewById(R.id.bod_container);
        View genderContainer = findViewById(R.id.gender_container);

        mPhotoCameraView.setOnClickListener(new View.OnClickListener() {
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

        nameContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNameView.setError(null);
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(AccountActivity.this);
                View view = layoutInflaterAndroid.inflate(R.layout.dialog_input, null);
                TextView title = (TextView) view.findViewById(R.id.title);
                title.setText(view.getResources().getString(R.string.display_name));
                final TextView text = (TextView) view.findViewById(R.id.text);
                text.setText(mNameView.getText().toString());
                AlertDialog dialog = new AlertDialog.Builder(AccountActivity.this)
                        .setView(view)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mName = text.getText().toString();
                                mNameView.setText(mName);
                                mToolBar.setTitle(mName);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                if (dialog.getWindow() != null) {
                    dialog.getWindow().clearFlags(
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                }
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

        bodContainer.setOnClickListener(new View.OnClickListener() {
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
                final int index = (i < years.length) ? i : -1;
                new AlertDialog.Builder(AccountActivity.this)
                        .setTitle(R.string.birth_year)
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

        genderContainer.setOnClickListener(new View.OnClickListener() {
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
                final int index = (i < genders.length) ? i : -1;
                new AlertDialog.Builder(AccountActivity.this)
                        .setTitle(R.string.gender)
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

        // user may or may not be available when it is started after the first sign-up.
        User user = FB.getUser();

        // name and email from the login
        Intent intent = getIntent();
        mName = intent.getStringExtra(DISPLAY_NAME);
        mEmail = intent.getStringExtra(EMAIL);
        mIsInitialSetup = (mEmail != null && !mEmail.isEmpty());

        if (mIsInitialSetup) {
            mStartApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setup();
                }
            });
        } else {
            // disable the start app button
            mStartApp.setVisibility(View.GONE);

            // set the user name to the action bar
            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setDisplayHomeAsUpEnabled(true);
                ab.setDisplayShowTitleEnabled(true);
            }

            if (user != null) {
                new LoadImage(new LoadImage.LoadImageListener() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        mPictureView.setImageBitmap(bitmap);
                    }
                }).loadProfile(FB.getUid());
            }
        }

        if (mName != null) {
            mNameView.setText(mName);
            mToolBar.setTitle(mName);
        } else {
            if (user != null && user.getDisplayName() != null) {
                mNameView.setText(user.getDisplayName());
                mToolBar.setTitle(user.getDisplayName());
            }
        }

        if (mEmail != null) {
            mEmailView.setText(mEmail);
        } else {
            if (user != null && user.getEmail() != null) {
                mEmailView.setText(user.getEmail());
            }
        }

        if (user != null && user.getBirthYear() != null) {
            mBodView.setText(user.getBirthYear());
        }

        if (user != null && user.getGender() != null) {
            mGenderView.setText(user.getGender());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mIsInitialSetup) {
            getMenuInflater().inflate(R.menu.menu_save, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_send) {
            update();
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
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProfilePictureUri = result.getUri();
                Bitmap bitmap = Utils.getBitmap(this, mProfilePictureUri);
                if (bitmap != null) {
                    Bitmap cropped = Utils.cropCircle(bitmap);
                    mPictureView.setImageBitmap(cropped);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping onFail: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void update() {
        User user = FB.getUser();

        if (mName != null && mName.equals(user.getDisplayName())) {
            mName = null;
        }
        if (mBod != null && mBod.equalsIgnoreCase(user.getBirthYear())) {
            mBod = null;
        }
        if (mGender != null && mGender.equalsIgnoreCase(user.getGender())) {
            mGender = null;
        }

        if (mProfilePictureUri == null) {
            if (mName == null && mBod == null && mGender == null) {
                // nothing to update
                AccountActivity.this.finish();
                return;
            }
            FB.updateUser(mName, mGender, mBod, new FB.CompleteListener() {
                @Override
                public void onSuccess() {
                    AccountActivity.this.finish();
                    Intent intent = new Intent(FB.USER_UPDATED);
                    LocalBroadcastManager.getInstance(AccountActivity.this).sendBroadcast(intent);
                }

                @Override
                public void onFail(String err) {
                    Log.d(TAG, err);
                }
            });
        } else {
            byte[] bytes = Utils.getBitmapBytes(this, mProfilePictureUri);
            if (bytes == null) {
                Log.d(TAG, "Failed to convert a profile image into byte");
                return;
            }

            FB.uploadProfileImage(bytes, new FB.UploadDataListener() {
                @Override
                public void onSuccess(String url) {
                    String dbName = FB.getUid() + "_" + FB.PROFILE_IMAGE;
                    DBUtil.deleteImage(dbName);

                    // set the url to the user
                    if (mName == null && mBod == null && mGender == null) {
                        AccountActivity.this.finish();
                        Intent intent = new Intent(FB.USER_UPDATED);
                        LocalBroadcastManager.getInstance(AccountActivity.this).sendBroadcast(intent);
                        return;
                    }
                    FB.updateUser(mName, mGender, mBod, new FB.CompleteListener() {
                        @Override
                        public void onSuccess() {
                            AccountActivity.this.finish();
                            Intent intent = new Intent(FB.USER_UPDATED);
                            LocalBroadcastManager.getInstance(AccountActivity.this).sendBroadcast(intent);
                        }

                        @Override
                        public void onFail(String err) {
                            Log.d(TAG, err);
                        }
                    });
                }

                @Override
                public void onFail(String err) {
                    Log.d(TAG, err);
                }
            });
        }
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
            Drawable drawable = Utils.getIconDrawable(this, R.drawable.ic_photo_camera_white_48dp, R.color.red_500);
            mPhotoCameraView.setImageDrawable(drawable);
        }
        if (!validated) {
            return;
        }

        // show the busy cursor
        final Utils.ProgressBarView dialog = Utils.getProgressBar(this);

        // get the user in the memory
        User tmp = FB.getUser();

        if (tmp == null) {
            // sometime when the network is slow, the user object may not be created
            // in the background by the time, normally FB.getUser() works
            tmp = new User();
        }

        final User user = tmp;
        user.setDisplayName(mName);
        user.setSearchedName(mName.toLowerCase());
        user.setGender(mGender.toLowerCase());
        user.setBirthYear(mBod.toLowerCase());
        user.setCreated(System.currentTimeMillis());
        user.setLocale(Utils.getLanguage());
        user.setTimezone(TimeZone.getDefault().getID());

        byte bytes[] = Utils.getBitmapBytes(this, mProfilePictureUri);

        if (bytes == null) {
            dialog.hide();
            return;
        }

        FB.uploadProfileImage(bytes, new FB.UploadDataListener() {
            @Override
            public void onSuccess(String url) {
                dialog.hide();

                // save the user to the database
                FB.saveUser(user);

                // start the main map
                Intent intent = new Intent(getApplicationContext(), PanelMapActivity.class);
                startActivity(intent);

                AccountActivity.this.finish();
            }

            @Override
            public void onFail(String err) {
                dialog.hide();
            }
        });
    }
}
