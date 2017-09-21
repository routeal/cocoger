package com.routeal.cocoger.ui.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.arlib.floatingsearchview.util.Util;
import com.franmontiel.fullscreendialog.FullScreenDialogFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.ui.login.LoginActivity;
import com.routeal.cocoger.util.LoadImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchMapActivity extends MapActivity {
    private final static String TAG = "SearchMapActivity";

    private FloatingSearchView mSearchView;

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
        navigationView.setNavigationItemSelectedListener(mNavigationItemSelectedListener);
    }

    void onMapReady() {

        boolean traffic_value = MainApplication.getBool("traffic");
        mMap.setTrafficEnabled(traffic_value);

        String style_value = MainApplication.getString("style");
        if (style_value == null || style_value.equals("normal")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else {
            if (style_value.equals("satellite")) {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            } else if (style_value.equals("terrain")) {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            } else if (style_value.equals("retro")) {
                MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(SearchMapActivity.this, R.raw.mapstyle_retro);
                mMap.setMapStyle(style);
            } else if (style_value.equals("night")) {
                MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(SearchMapActivity.this, R.raw.mapstyle_night);
                mMap.setMapStyle(style);
            } else if (style_value.equals("custom")) {
                MapStyleOptions style = new MapStyleOptions("[" +
                        "  {" +
                        "    \"featureType\":\"poi.business\"," +
                        "    \"elementType\":\"all\"," +
                        "    \"stylers\":[" +
                        "      {" +
                        "        \"visibility\":\"off\"" +
                        "      }" +
                        "    ]" +
                        "  }," +
                        "  {" +
                        "    \"featureType\":\"transit\"," +
                        "    \"elementType\":\"all\"," +
                        "    \"stylers\":[" +
                        "      {" +
                        "        \"visibility\":\"off\"" +
                        "      }" +
                        "    ]" +
                        "  }" +
                        "]");
                mMap.setMapStyle(style);
            }
        }
    }

    private void setupDrawerHead() {
        User user = MainApplication.getUser();
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

    private void showOpensourceLibraries() {
        new LibsBuilder()
                //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                //start the activity
                .start(this);
    }

    private void showShareTimeline() {
        Intent intent = new Intent(getApplicationContext(), TimelineActivity.class);
        startActivity(intent);
    }

    private void showAccount() {
        Intent intent = new Intent(getApplicationContext(), AccountActivity.class);
        startActivity(intent);
    }

    private void showSettings() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    private void showYourVoice() {
        FullScreenDialogFragment dialogFragment = new FullScreenDialogFragment.Builder(this)
                .setTitle(R.string.your_feedback)
                .setConfirmButton(R.string.send)
                .setContent(UserListFragment.class, new Bundle())
                .build();
        dialogFragment.show(getSupportFragmentManager(), "user-dialog");
    }

    NavigationView.OnNavigationItemSelectedListener mNavigationItemSelectedListener =
            new NavigationView.OnNavigationItemSelectedListener() {
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
                    } else if (id == R.id.nav_settings) {
                        showSettings();
                    } else if (id == R.id.nav_send_feedback) {
                        showYourVoice();
                    } else if (id == R.id.nav_logout) {
                        logout();
                    } else if (id == R.id.nav_term_services) {
                    } else if (id == R.id.nav_privacy_policy) {
                        Intent intent = new Intent(getApplicationContext(), WebActivity.class);
                        intent.putExtra("url", "http://www.google.com");
                        intent.putExtra("title", "Privacy Policy");
                        startActivity(intent);
                    } else if (id == R.id.nav_open_source) {
                        showOpensourceLibraries();
                    }

                    return true;
                }
            };

    private static List<NameSuggestion> sNameSuggestions =
            new ArrayList<>(Arrays.asList(
                    new NameSuggestion("green"),
                    new NameSuggestion("blue"),
                    new NameSuggestion("pink")));
                    /*
                    new NameSuggestion("purple"),
                    new NameSuggestion("brown"),
                    new NameSuggestion("gray"),
                    new NameSuggestion("Granny Smith Apple"),
                    new NameSuggestion("Indigo"),
                    new NameSuggestion("Periwinkle"),
                    new NameSuggestion("Mahogany"),
                    new NameSuggestion("Maize"),
                    new NameSuggestion("Mahogany"),
                    new NameSuggestion("Outer Space"),
                    new NameSuggestion("Melon"),
                    new NameSuggestion("Yellow"),
                    new NameSuggestion("Orange"),
                    new NameSuggestion("Red"),
                    new NameSuggestion("Orchid")));
                    */

    private String mLastQuery = "green";

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
                if (item.getItemId() == R.id.action_map_layer) {
                    LayoutInflater layoutInflaterAndroid = LayoutInflater.from(SearchMapActivity.this);
                    View view = layoutInflaterAndroid.inflate(R.layout.dialog_layer, null);
                    final SwitchCompat satellite = (SwitchCompat) view.findViewById(R.id.switch_satellite);
                    final SwitchCompat terrain = (SwitchCompat) view.findViewById(R.id.switch_terrain);
                    final SwitchCompat traffic = (SwitchCompat) view.findViewById(R.id.switch_traffic);
                    final SwitchCompat retro = (SwitchCompat) view.findViewById(R.id.switch_retro);
                    final SwitchCompat night = (SwitchCompat) view.findViewById(R.id.switch_night);
                    final SwitchCompat custom = (SwitchCompat) view.findViewById(R.id.switch_custom);

                    boolean traffic_value = MainApplication.getBool("traffic");
                    traffic.setChecked(traffic_value);

                    String style = MainApplication.getString("style");
                    if (style == null || style.equals("normal")) {
                        satellite.setChecked(false);
                        terrain.setChecked(false);
                        retro.setChecked(false);
                        night.setChecked(false);
                        custom.setChecked(false);
                    } else {
                        if (style.equals("satellite")) {
                            satellite.setChecked(true);
                        } else if (style.equals("terrain")) {
                            terrain.setChecked(true);
                        } else if (style.equals("retro")) {
                            retro.setChecked(true);
                        } else if (style.equals("night")) {
                            night.setChecked(true);
                        } else if (style.equals("custom")) {
                            custom.setChecked(true);
                        }
                    }

                    satellite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                terrain.setChecked(false);
                                retro.setChecked(false);
                                night.setChecked(false);
                                custom.setChecked(false);
                                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                MainApplication.putString("style", "satellite");
                            } else {
                                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                MainApplication.putString("style", "normal");
                            }
                        }
                    });
                    terrain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                satellite.setChecked(false);
                                retro.setChecked(false);
                                night.setChecked(false);
                                custom.setChecked(false);
                                satellite.setChecked(false);
                                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                MainApplication.putString("style", "terrain");
                            } else {
                                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                MainApplication.putString("style", "normal");
                            }
                        }
                    });
                    traffic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            mMap.setTrafficEnabled(isChecked);
                            if (isChecked) {
                                MainApplication.putBool("traffic", true);
                            } else {
                                MainApplication.putBool("traffic", false);
                            }
                        }
                    });
                    retro.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                satellite.setChecked(false);
                                terrain.setChecked(false);
                                night.setChecked(false);
                                custom.setChecked(false);
                                MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(SearchMapActivity.this, R.raw.mapstyle_retro);
                                mMap.setMapStyle(style);
                                MainApplication.putString("style", "retro");
                            } else {
                                mMap.setMapStyle(null);
                                MainApplication.putString("style", "normal");
                            }
                        }
                    });
                    night.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                satellite.setChecked(false);
                                terrain.setChecked(false);
                                retro.setChecked(false);
                                custom.setChecked(false);
                                MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(SearchMapActivity.this, R.raw.mapstyle_night);
                                mMap.setMapStyle(style);
                                MainApplication.putString("style", "night");
                            } else {
                                mMap.setMapStyle(null);
                                MainApplication.putString("style", "normal");
                            }
                        }
                    });
                    custom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                satellite.setChecked(false);
                                terrain.setChecked(false);
                                retro.setChecked(false);
                                night.setChecked(false);
                                MapStyleOptions style = new MapStyleOptions("[" +
                                        "  {" +
                                        "    \"featureType\":\"poi.business\"," +
                                        "    \"elementType\":\"all\"," +
                                        "    \"stylers\":[" +
                                        "      {" +
                                        "        \"visibility\":\"off\"" +
                                        "      }" +
                                        "    ]" +
                                        "  }," +
                                        "  {" +
                                        "    \"featureType\":\"transit\"," +
                                        "    \"elementType\":\"all\"," +
                                        "    \"stylers\":[" +
                                        "      {" +
                                        "        \"visibility\":\"off\"" +
                                        "      }" +
                                        "    ]" +
                                        "  }" +
                                        "]");
                                mMap.setMapStyle(style);
                                MainApplication.putString("style", "custom");
                            } else {
                                mMap.setMapStyle(null);
                                MainApplication.putString("style", "normal");
                            }
                        }
                    });
                    AlertDialog dialog = new AlertDialog.Builder(SearchMapActivity.this)
                            .setView(view)
                            .setCancelable(true)
                            .show();
                    dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
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
}
