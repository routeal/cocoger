package com.routeal.cocoger.ui.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.arlib.floatingsearchview.util.Util;
import com.facebook.login.LoginManager;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.net.RestClient;
import com.routeal.cocoger.provider.DBUtil;
import com.routeal.cocoger.ui.login.FacebookLoginActivity;
import com.routeal.cocoger.util.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchMapActivity extends MapActivity
        implements NavigationView.OnNavigationItemSelectedListener {
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
                ImageView imageView = (ImageView) findViewById(R.id.my_picture);
                Picasso.with(getApplicationContext())
                        .load(DBUtil.getUser().getPicture())
                        .resize(128, 128)
                        .into(imageView);

                TextView textView = (TextView) findViewById(R.id.my_display_name);
                textView.setText(DBUtil.getUser().getName());

                textView = (TextView) findViewById(R.id.my_email);
                textView.setText(DBUtil.getUser().getEmail());

                Address addr = getAddress();
                if (addr != null) {
                    textView = (TextView) findViewById(R.id.my_address);
                    textView.setText(addr.toString());
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSearchView.attachNavigationDrawerToMenuButton(drawer);

    }

    private void logoutImpl() {
        // logout anyway
        LoginManager.getInstance().logOut();

        // cleanup the databases
        DBUtil.deleteUser();
        DBUtil.deleteLocations();

        // cleanup the app config
        MainApplication.permitLocation(false);

        // start the login screen
        Intent intent = new Intent(getApplicationContext(), FacebookLoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void logoutFacebook() {
        final ProgressDialog dialog = Utils.spinBusyCursor(this);

        Call<Void> logout = RestClient.service().logout(RestClient.token(), Utils.getDevice());

        logout.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                dialog.dismiss();
                logoutImpl();
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                dialog.dismiss();
                logoutImpl();
            }
        });
    }

    private void showOpensourceLibraries() {
        new LibsBuilder()
                //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                //start the activity
                .start(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_logout) {
            logoutFacebook();
        } else if (id == R.id.nav_open_source) {
            showOpensourceLibraries();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

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
                //just print action
                Toast.makeText(getApplicationContext(), item.getTitle(),
                        Toast.LENGTH_SHORT).show();

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
