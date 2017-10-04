package com.routeal.cocoger.ui.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.text.Html;
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

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.arlib.floatingsearchview.util.Util;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.service.LocationUpdate;
import com.routeal.cocoger.ui.login.LoginActivity;
import com.routeal.cocoger.util.LoadImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchMapActivity extends MapActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final static String TAG = "SearchMapActivity";
    private static List<NameSuggestion> sNameSuggestions =
            new ArrayList<>(Arrays.asList(
                    new NameSuggestion("green"),
                    new NameSuggestion("blue"),
                    new NameSuggestion("pink")));
    private FloatingSearchView mSearchView;
    private String mLastQuery = "green";

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);

        setupFloatingSearch();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                setupDrawerHead();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        mSearchView.attachNavigationDrawerToMenuButton(drawer);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupDrawerHead() {
        User user = FB.getUser();
        if (user == null) {
            return;
        }

        TextView textView = (TextView) findViewById(R.id.my_display_name);
        textView.setText(user.getDisplayName());

        textView = (TextView) findViewById(R.id.my_email);
        textView.setText(user.getEmail());

        ImageView imageView = (ImageView) findViewById(R.id.my_picture);
        new LoadImage.LoadImageView(imageView).execute(user.getPicture());
    }

    private void logout() {
        new AlertDialog.Builder(mSearchView.getContext())
                .setTitle(R.string.logout)
                .setMessage(R.string.confirm_logout)
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FB.signOut();

                        // start the login screen
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
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

    private void showSettings() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);
        final View view = layoutInflaterAndroid.inflate(R.layout.dialog_setting, null);
        final RadioGroup fgIntervalGroup = (RadioGroup) view.findViewById(R.id.foreground_interval);
        final RadioGroup bgIntervalGroup = (RadioGroup) view.findViewById(R.id.background_interval);
        final TextView warning = (TextView) view.findViewById(R.id.location_update_warning);
        RadioButton fgbutton1 = (RadioButton) view.findViewById(R.id.fg_interval_1);
        RadioButton fgbutton3 = (RadioButton) view.findViewById(R.id.fg_interval_3);
        RadioButton fgbutton5 = (RadioButton) view.findViewById(R.id.fg_interval_5);
        RadioButton bgbutton1 = (RadioButton) view.findViewById(R.id.bg_interval_1);
        bgbutton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    warning.setVisibility(View.VISIBLE);
                }
            }
        });
        RadioButton bgbutton5 = (RadioButton) view.findViewById(R.id.bg_interval_5);
        bgbutton5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    warning.setVisibility(View.VISIBLE);
                }
            }
        });
        RadioButton bgbutton15 = (RadioButton) view.findViewById(R.id.bg_interval_15);
        bgbutton15.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    warning.setVisibility(View.VISIBLE);
                }
            }
        });
        RadioButton bgbutton30 = (RadioButton) view.findViewById(R.id.bg_interval_30);
        bgbutton30.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    warning.setVisibility(View.INVISIBLE);
                }
            }
        });
        RadioButton bgbutton60 = (RadioButton) view.findViewById(R.id.bg_interval_60);
        bgbutton60.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    warning.setVisibility(View.INVISIBLE);
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
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RadioButton fg = (RadioButton) view.findViewById(fgIntervalGroup.getCheckedRadioButtonId());
                        int fgInterval = Integer.valueOf(fg.getText().toString()) * 60 * 1000;
                        RadioButton bg = (RadioButton) view.findViewById(bgIntervalGroup.getCheckedRadioButtonId());
                        int bgInterval = Integer.valueOf(bg.getText().toString()) * 60 * 1000;
                        Log.d(TAG, "New Location update: fg=" + fgInterval + " bg=" + bgInterval);

                        int serviceInterval = MainApplication.getInt(LocationUpdate.LOCATION_UPDATE_INTERVAL, LocationUpdate.DEFAULT_LOCATION_UPDATE_INTERVAL);
                        if (serviceInterval != fgInterval) {
                            MainApplication.putInt(LocationUpdate.LOCATION_UPDATE_INTERVAL, fgInterval);
                        }
                        int fgServiceInterval = MainApplication.getInt(LocationUpdate.FOREGROUND_LOCATION_UPDATE_INTERVAL, LocationUpdate.DEFAULT_FOREGROUND_LOCATION_UPDATE_INTERVAL);
                        if (fgServiceInterval != bgInterval) {
                            MainApplication.putInt(LocationUpdate.FOREGROUND_LOCATION_UPDATE_INTERVAL, bgInterval);
                        }
                    }
                })
                .show();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    private void setupFloatingSearch() {
        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {
                if (!oldQuery.equals("") && newQuery.equals("")) {
                    mSearchView.clearSuggestions();
                } else {
                    mSearchView.showProgress();

                    // Retrieves a list of the suggestion for the new query string
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            mSearchView.swapSuggestions(sNameSuggestions);
                            mSearchView.hideProgress();
                        }
                    }, 500);
                }

                Log.d(TAG, "onSearchTextChanged()");
            }
        });

        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final SearchSuggestion searchSuggestion) {

                NameSuggestion nameSuggestion = (NameSuggestion) searchSuggestion;

                Log.d(TAG, "onSuggestionClicked(): " + nameSuggestion.getBody());

/*
                mSearchView.setSearchBarTitle(nameSuggestion.getBody());

                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getView().requestFocus();

                //mSearchView.clearSuggestions();
*/
                mSearchView.setSearchText(nameSuggestion.getBody());

                mLastQuery = searchSuggestion.getBody();
            }

            @Override
            public void onSearchAction(String query) {
                mLastQuery = query;

                Log.d(TAG, "onSearchAction(): " + mLastQuery);
            }
        });

        mSearchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                mSearchView.swapSuggestions(sNameSuggestions);
                Log.d(TAG, "onFocus()");
            }

            @Override
            public void onFocusCleared() {
                Log.d(TAG, "onFocusCleared()");

                //set the title of the bar so that when focus is returned a new query begins
                //mSearchView.setSearchBarTitle(mLastQuery);

                //you can also set setSearchText(...) to make keep the query there when not focused and when focus returns
                //mSearchView.setSearchText(searchSuggestion.getBody());
            }
        });

        mSearchView.setOnSuggestionsListHeightChanged(new FloatingSearchView.OnSuggestionsListHeightChanged() {
            @Override
            public void onSuggestionsListHeightChanged(float newHeight) {
                //mSearchResultsList.setTranslationY(newHeight);
            }
        });

        mSearchView.setOnClearSearchActionListener(new FloatingSearchView.OnClearSearchActionListener() {
            @Override
            public void onClearSearchClicked() {

                Log.d(TAG, "onClearSearchClicked()");
            }
        });

        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                if (item.getItemId() == R.id.action_setting) {
                    showSettings();
                }
            }
        });

        mSearchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon,
                                         TextView textView, SearchSuggestion item, int itemPosition) {
                NameSuggestion colorSuggestion = (NameSuggestion) item;

                boolean mIsDarkSearchTheme = false;
                String textColor = mIsDarkSearchTheme ? "#ffffff" : "#000000";
                String textLight = mIsDarkSearchTheme ? "#bfbfbf" : "#787878";

                if (colorSuggestion.getIsHistory()) {
                    leftIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                            R.drawable.ic_history_black_24dp, null));

                    Util.setIconColor(leftIcon, Color.parseColor(textColor));
                    leftIcon.setAlpha(.36f);
                } else {
                    leftIcon.setAlpha(0.0f);
                    leftIcon.setImageDrawable(null);
                }

                textView.setTextColor(Color.parseColor(textColor));
                String text = colorSuggestion.getBody()
                        .replaceFirst(mSearchView.getQuery(),
                                "<font color=\"" + textLight + "\">" + mSearchView.getQuery() + "</font>");
                textView.setText(Html.fromHtml(text));
            }

        });

    }

    static class NameSuggestion implements SearchSuggestion {
        public static final Creator<NameSuggestion> CREATOR = new Creator<NameSuggestion>() {
            @Override
            public NameSuggestion createFromParcel(Parcel in) {
                return new NameSuggestion(in);
            }

            @Override
            public NameSuggestion[] newArray(int size) {
                return new NameSuggestion[size];
            }
        };
        private String mColorName;
        private boolean mIsHistory = true;

        public NameSuggestion(String suggestion) {
            this.mColorName = suggestion.toLowerCase();
        }

        public NameSuggestion(Parcel source) {
            this.mColorName = source.readString();
            this.mIsHistory = source.readInt() != 0;
        }

        public boolean getIsHistory() {
            return this.mIsHistory;
        }

        public void setIsHistory(boolean isHistory) {
            this.mIsHistory = isHistory;
        }

        @Override
        public String getBody() {
            return mColorName;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mColorName);
            dest.writeInt(mIsHistory ? 1 : 0);
        }
    }

}
