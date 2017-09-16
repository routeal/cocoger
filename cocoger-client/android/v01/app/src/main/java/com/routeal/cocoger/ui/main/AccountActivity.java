package com.routeal.cocoger.ui.main;

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

/**
 * Created by nabe on 7/25/17.
 */

public class AccountActivity extends AppCompatActivity {

    private Uri mProfilePictureUri;
    private String mName;
    private String mBod;
    private String mGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final User user = MainApplication.getUser();

        new LoadImage.LoadImageAsync(true, new LoadImage.LoadImageListener() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                ImageView imageView = (ImageView) findViewById(R.id.profile_picture);
                imageView.setImageBitmap(bitmap);
            }
        }).execute(user.getPicture());

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(true);
            ab.setTitle(user.getDisplayName());
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.photo_camera);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setActivityTitle(getResources().getString(R.string.picture))
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setRequestedSize(128, 128)
                        .setFixAspectRatio(true)
                        .setAspectRatio(100, 100)
                        .start(AccountActivity.this);
                /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                        */
            }
        });

        final TextView name = (TextView) findViewById(R.id.name);
        if (mName == null) {
            name.setText(user.getDisplayName());
        } else {
            name.setText(mName);
        }
        View view = findViewById(R.id.name_container);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(AccountActivity.this);
                View view = layoutInflaterAndroid.inflate(R.layout.dialog_input, null);
                TextView title = (TextView) view.findViewById(R.id.title);
                title.setText("Display Name");
                final TextView text = (TextView) view.findViewById(R.id.text);
                text.setText(name.getText().toString());
                new AlertDialog.Builder(AccountActivity.this)
                        .setView(view)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mName = text.getText().toString();
                                name.setText(mName);
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

        TextView email = (TextView) findViewById(R.id.email);
        email.setText(user.getEmail());

        final TextView bod = (TextView) findViewById(R.id.bod);
        if (mBod == null) {
            bod.setText(user.getBirthYear());
        } else {
            bod.setText(mBod);
        }
        view = findViewById(R.id.bod_container);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] years = getResources().getStringArray(R.array.years);
                String current_bod = bod.getText().toString();
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
                                bod.setText(mBod);
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

        final TextView gender = (TextView) findViewById(R.id.gender);
        if (mGender == null) {
            gender.setText(user.getGender());
        } else {
            gender.setText(mGender);
        }
        view = findViewById(R.id.gender_container);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] genders = getResources().getStringArray(R.array.genders);
                String current_gender = gender.getText().toString();
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
                                gender.setText(mGender);
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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
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

        if (user == null) {
            setup();
            return;
        }

        if (!mName.equals(user.getDisplayName())) {
            mName = null;
        }
        if (!mBod.equalsIgnoreCase(user.getBirthYear())) {
            mBod = null;
        }
        if (!mGender.equalsIgnoreCase(user.getGender())) {
            mGender = null;
        }

        if (mProfilePictureUri != null || mName != null || mBod != null || mGender != null) {
            FB.updateUser(mName, mGender, mBod);

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

            Toast.makeText(this, "Profile has been updated.", Toast.LENGTH_LONG).show();
        }
    }

    private void setup() {
        Toast.makeText(this, "Changes saved", Toast.LENGTH_LONG).show();
    }
}
