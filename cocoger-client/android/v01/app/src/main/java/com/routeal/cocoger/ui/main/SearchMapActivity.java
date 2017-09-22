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
import android.widget.TextView;

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

    class CustomMapStyle {
        SwitchCompat view;
        int id;
        String resource;
    }

    CustomMapStyle mMapStyles[];

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

        mMapStyles = new CustomMapStyle[10];
        mMapStyles[0] = new CustomMapStyle();
        mMapStyles[0].id = R.raw.mapstyle_retro;
        mMapStyles[0].resource = "retro";
        mMapStyles[1] = new CustomMapStyle();
        mMapStyles[1].id = R.raw.mapstyle_night;
        mMapStyles[1].resource = "night";
        mMapStyles[2] = new CustomMapStyle();
        mMapStyles[2].id = R.raw.mapstyle_no_poi;
        mMapStyles[2].resource = "custom";
        mMapStyles[3] = new CustomMapStyle();
        mMapStyles[3].id = R.raw.mapstyle_blue_essence;
        mMapStyles[3].resource = "blue_essence";
        mMapStyles[4] = new CustomMapStyle();
        mMapStyles[4].id = R.raw.mapstyle_blue_ish;
        mMapStyles[4].resource = "blue_ish";
        mMapStyles[5] = new CustomMapStyle();
        mMapStyles[5].id = R.raw.mapstyle_grayscale;
        mMapStyles[5].resource = "grayscale";
        mMapStyles[6] = new CustomMapStyle();
        mMapStyles[6].id = R.raw.mapstyle_muted_blue;
        mMapStyles[6].resource = "blue";
        mMapStyles[7] = new CustomMapStyle();
        mMapStyles[7].id = R.raw.mapstyle_pale_dawn;
        mMapStyles[7].resource = "pale_down";
        mMapStyles[8] = new CustomMapStyle();
        mMapStyles[8].id = R.raw.mapstyle_paper;
        mMapStyles[8].resource = "paper";
        mMapStyles[9] = new CustomMapStyle();
        mMapStyles[9].id = R.raw.mapstyle_pinky;
        mMapStyles[9].resource = "pinky";
    }

    void onMapReady() {
        boolean traffic_value = MainApplication.getBool("traffic");
        mMap.setTrafficEnabled(traffic_value);

        String current_style = MainApplication.getString("style");
        if (current_style == null || current_style.equals("normal")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else {
            for (int i = 0; i < mMapStyles.length; i++) {
                if (mMapStyles[i].resource.equals(current_style)) {
                    MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(SearchMapActivity.this, mMapStyles[i].id);
                    mMap.setMapStyle(style);
                    break;
                }
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

                    mMapStyles[0].view = (SwitchCompat) view.findViewById(R.id.switch_retro);
                    mMapStyles[1].view = (SwitchCompat) view.findViewById(R.id.switch_night);
                    mMapStyles[2].view = (SwitchCompat) view.findViewById(R.id.switch_custom);
                    mMapStyles[3].view = (SwitchCompat) view.findViewById(R.id.switch_blue_essence);
                    mMapStyles[4].view = (SwitchCompat) view.findViewById(R.id.switch_blue_ish);
                    mMapStyles[5].view = (SwitchCompat) view.findViewById(R.id.switch_grayscale);
                    mMapStyles[6].view = (SwitchCompat) view.findViewById(R.id.switch_muted_blue);
                    mMapStyles[7].view = (SwitchCompat) view.findViewById(R.id.switch_pale_down);
                    mMapStyles[8].view = (SwitchCompat) view.findViewById(R.id.switch_paper);
                    mMapStyles[9].view = (SwitchCompat) view.findViewById(R.id.switch_pinky);

                    for (int i = 0; i < mMapStyles.length; i++) {
                        String current_style = MainApplication.getString("style");
                        if (!current_style.isEmpty()) {
                            if (mMapStyles[i].resource.equals(current_style)) {
                                mMapStyles[i].view.setChecked(true);
                            } else {
                                mMapStyles[i].view.setChecked(false);
                            }
                        }
                    }

                    for (int i = 0; i < mMapStyles.length; i++) {
                        final int n = i;
                        mMapStyles[n].view.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    CustomMapStyle cstyle = mMapStyles[n];
                                    for (int j = 0; j < mMapStyles.length; j++) {
                                        if (cstyle.view != mMapStyles[j].view) {
                                            mMapStyles[j].view.setChecked(false);
                                        }
                                    }
                                    MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(SearchMapActivity.this, cstyle.id);
                                    mMap.setMapStyle(style);
                                    MainApplication.putString("style", cstyle.resource);
                                } else {
                                    mMap.setMapStyle(null);
                                    MainApplication.putString("style", "normal");
                                }
                            }
                        });
                    }

                    final SwitchCompat traffic = (SwitchCompat) view.findViewById(R.id.switch_traffic);
                    traffic.setChecked(MainApplication.getBool("traffic"));

                    traffic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            mMap.setTrafficEnabled(isChecked);
                            MainApplication.putBool("traffic", isChecked);
                        }
                    });

                    AlertDialog dialog = new AlertDialog.Builder(SearchMapActivity.this)
                            .setView(view)
                            .setCancelable(true)
                            .setNegativeButton(R.string.close, null)
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
