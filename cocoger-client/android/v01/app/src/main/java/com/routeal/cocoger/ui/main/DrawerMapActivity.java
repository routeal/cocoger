package com.routeal.cocoger.ui.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.service.LocationUpdate;
import com.routeal.cocoger.ui.login.LoginActivity;
import com.routeal.cocoger.util.LoadImage;

/**
 * Created by hwatanabe on 10/5/17.
 */

abstract class DrawerMapActivity extends MapActivity implements NavigationView.OnNavigationItemSelectedListener, DrawerLayout.DrawerListener {
    private final static String TAG = "DrawerMapActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // close the drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        // show the selected activities
        int id = item.getItemId();
        if (id == R.id.nav_sharing_timeline) {
            showShareTimeline();
        } else if (id == R.id.nav_account) {
            showAccount();
        } else if (id == R.id.nav_send_feedback) {
            sendFeedback();
        } else if (id == R.id.nav_logout) {
            logout();
        } else if (id == R.id.nav_term_services) {
        } else if (id == R.id.nav_privacy_policy) {
            Intent intent = new Intent(getApplicationContext(), WebActivity.class);
            intent.putExtra("url", "http://www.google.com");
            intent.putExtra("title", "Privacy Policy");
            startActivity(intent);
        }
        return true;
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        User user = FB.getUser();
        if (user == null) {
            return;
        }

        TextView textView = (TextView) findViewById(R.id.my_display_name);
        textView.setText(user.getDisplayName());

        textView = (TextView) findViewById(R.id.my_email);
        textView.setText(user.getEmail());

        ImageView imageView = (ImageView) findViewById(R.id.my_picture);
        new LoadImage(imageView).loadProfile(FB.getUid());
    }

    @Override
    public void onDrawerClosed(View drawerView) {
    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.confirm_logout)
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FB.signOut();
                        // start the login screen
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        // exit the current activity
                        DrawerMapActivity.this.exitApp();
                    }
                })
                .setPositiveButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    private void showShareTimeline() {
        Intent intent = new Intent(getApplicationContext(), TimelineActivity.class);
        startActivity(intent);
    }

    private void showAccount() {
        Intent intent = new Intent(getApplicationContext(), AccountActivity.class);
        startActivity(intent);
    }

    private void sendFeedback() {
        Intent intent = new Intent(getApplicationContext(), FeedbackActivity.class);
        startActivity(intent);
    }

    protected void showSettings() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);
        final View view = layoutInflaterAndroid.inflate(R.layout.dialog_setting, null);
        final RadioGroup fgIntervalGroup = (RadioGroup) view.findViewById(R.id.foreground_interval);
        final RadioGroup bgIntervalGroup = (RadioGroup) view.findViewById(R.id.background_interval);
        final TextView warning = (TextView) view.findViewById(R.id.location_update_warning);
        final SwitchCompat bgSwitch = (SwitchCompat) view.findViewById(R.id.switch_background);
        RadioButton fgbutton1 = (RadioButton) view.findViewById(R.id.fg_interval_1);
        RadioButton fgbutton3 = (RadioButton) view.findViewById(R.id.fg_interval_3);
        RadioButton fgbutton5 = (RadioButton) view.findViewById(R.id.fg_interval_5);
        final RadioButton bgbutton1 = (RadioButton) view.findViewById(R.id.bg_interval_1);
        final RadioButton bgbutton5 = (RadioButton) view.findViewById(R.id.bg_interval_5);
        final RadioButton bgbutton15 = (RadioButton) view.findViewById(R.id.bg_interval_15);
        final RadioButton bgbutton30 = (RadioButton) view.findViewById(R.id.bg_interval_30);
        final RadioButton bgbutton60 = (RadioButton) view.findViewById(R.id.bg_interval_60);
        bgbutton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    warning.setVisibility(View.VISIBLE);
                }
            }
        });
        bgbutton5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    warning.setVisibility(View.VISIBLE);
                }
            }
        });
        bgbutton15.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    warning.setVisibility(View.VISIBLE);
                }
            }
        });
        bgbutton30.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    warning.setVisibility(View.INVISIBLE);
                }
            }
        });
        bgbutton60.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    warning.setVisibility(View.INVISIBLE);
                }
            }
        });
        bgSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    bgbutton30.setChecked(true);
                    warning.setVisibility(View.INVISIBLE);
                    bgbutton1.setEnabled(true);
                    bgbutton5.setEnabled(true);
                    bgbutton15.setEnabled(true);
                    bgbutton30.setEnabled(true);
                    bgbutton60.setEnabled(true);
                } else {
                    bgbutton30.setChecked(true);
                    warning.setVisibility(View.INVISIBLE);
                    bgbutton1.setEnabled(false);
                    bgbutton5.setEnabled(false);
                    bgbutton15.setEnabled(false);
                    bgbutton30.setEnabled(false);
                    bgbutton60.setEnabled(false);
                }
            }
        });
        int serviceInterval = MainApplication.getInt(LocationUpdate.LOCATION_UPDATE_INTERVAL, LocationUpdate.DEFAULT_LOCATION_UPDATE_INTERVAL);
        serviceInterval /= 60 * 1000;
        if (serviceInterval == 1) {
            fgbutton1.setChecked(true);
        } else if (serviceInterval == 3) {
            fgbutton3.setChecked(true);
        } else if (serviceInterval == 5) {
            fgbutton5.setChecked(true);
        }
        int fgServiceInterval = MainApplication.getInt(LocationUpdate.FOREGROUND_LOCATION_UPDATE_INTERVAL, LocationUpdate.DEFAULT_FOREGROUND_LOCATION_UPDATE_INTERVAL);
        if (fgServiceInterval == 0) {
            bgSwitch.setChecked(false);
            bgbutton30.setChecked(true);
            warning.setVisibility(View.INVISIBLE);
            fgIntervalGroup.setEnabled(false);
        } else {
            fgIntervalGroup.setEnabled(true);
            bgSwitch.setChecked(true);
            fgServiceInterval /= 60 * 1000;
            if (fgServiceInterval == 1) {
                bgbutton1.setChecked(true);
            } else if (fgServiceInterval == 5) {
                bgbutton5.setChecked(true);
            } else if (fgServiceInterval == 15) {
                bgbutton15.setChecked(true);
            } else if (fgServiceInterval == 30) {
                bgbutton30.setChecked(true);
            } else if (fgServiceInterval == 60) {
                bgbutton60.setChecked(true);
            }
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int serviceInterval = MainApplication.getInt(LocationUpdate.LOCATION_UPDATE_INTERVAL, LocationUpdate.DEFAULT_LOCATION_UPDATE_INTERVAL);
                        int fgServiceInterval = MainApplication.getInt(LocationUpdate.FOREGROUND_LOCATION_UPDATE_INTERVAL, LocationUpdate.DEFAULT_FOREGROUND_LOCATION_UPDATE_INTERVAL);

                        RadioButton fg = (RadioButton) view.findViewById(fgIntervalGroup.getCheckedRadioButtonId());
                        int fgInterval = Integer.valueOf(fg.getText().toString()) * 60 * 1000;
                        if (serviceInterval != fgInterval) {
                            MainApplication.putInt(LocationUpdate.LOCATION_UPDATE_INTERVAL, fgInterval);
                            Log.d(TAG, "New Location update: fg=" + fgInterval);
                        }
                        if (bgSwitch.isChecked()) {
                            RadioButton bg = (RadioButton) view.findViewById(bgIntervalGroup.getCheckedRadioButtonId());
                            int bgInterval = Integer.valueOf(bg.getText().toString()) * 60 * 1000;
                            if (fgServiceInterval != bgInterval) {
                                MainApplication.putInt(LocationUpdate.FOREGROUND_LOCATION_UPDATE_INTERVAL, bgInterval);
                                Log.d(TAG, "New Location update: bg=" + bgInterval);
                            }
                        } else {
                            if (fgServiceInterval != 0) {
                                MainApplication.putInt(LocationUpdate.FOREGROUND_LOCATION_UPDATE_INTERVAL, 0);
                                Log.d(TAG, "New Location update: bg=0");
                            }
                        }
                    }
                })
                .show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }
}
