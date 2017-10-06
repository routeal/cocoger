package com.routeal.cocoger.ui.main;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.arlib.floatingsearchview.util.Util;
import com.routeal.cocoger.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SearchMapActivity extends DrawerMapActivity implements
        FloatingSearchView.OnQueryChangeListener,
        FloatingSearchView.OnSearchListener,
        FloatingSearchView.OnFocusChangeListener,
        FloatingSearchView.OnSuggestionsListHeightChanged,
        FloatingSearchView.OnClearSearchActionListener,
        FloatingSearchView.OnMenuItemClickListener,
        SearchSuggestionsAdapter.OnBindSuggestionCallback {
    private final static String TAG = "SearchMapActivity";
    private static List<NameSuggestion> sNameSuggestions =
            new ArrayList<>(Arrays.asList(
                    new NameSuggestion("green"),
                    new NameSuggestion("blue"),
                    new NameSuggestion("pink")));
    private FloatingSearchView mSearchView;
    private String mLastQuery = "green";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mSearchView.attachNavigationDrawerToMenuButton(drawer);
        mSearchView.setOnQueryChangeListener(this);
        mSearchView.setOnSearchListener(this);
        mSearchView.setOnFocusChangeListener(this);
        mSearchView.setOnSuggestionsListHeightChanged(this);
        mSearchView.setOnClearSearchActionListener(this);
        mSearchView.setOnMenuItemClickListener(this);
        mSearchView.setOnBindSuggestionCallback(this);
    }

    @Override
    public void onSearchTextChanged(String oldQuery, final String newQuery) {
        Log.d(TAG, "onSearchTextChanged()");

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
    }

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
        Log.d(TAG, "onSearchAction(): " + query);
        mLastQuery = query;
    }

    @Override
    public void onFocus() {
        Log.d(TAG, "onFocus()");
        mSearchView.swapSuggestions(sNameSuggestions);

        FloatingActionButton myLocation = (FloatingActionButton) findViewById(R.id.my_location);
        FloatingActionButton mapLayer = (FloatingActionButton) findViewById(R.id.map_layer);
        myLocation.setVisibility(View.INVISIBLE);
        mapLayer.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onFocusCleared() {
        Log.d(TAG, "onFocusCleared()");

        FloatingActionButton myLocation = (FloatingActionButton) findViewById(R.id.my_location);
        FloatingActionButton mapLayer = (FloatingActionButton) findViewById(R.id.map_layer);
        myLocation.setVisibility(View.VISIBLE);
        mapLayer.setVisibility(View.VISIBLE);

        //set the title of the bar so that when focus is returned a new query begins
        //mSearchView.setSearchBarTitle(mLastQuery);

        //you can also set setSearchText(...) to make keep the query there when not focused and when focus returns
        //mSearchView.setSearchText(searchSuggestion.getBody());
    }

    @Override
    public void onSuggestionsListHeightChanged(float newHeight) {
        Log.d(TAG, "onSuggestionsListHeightChanged()");
        //mSearchResultsList.setTranslationY(newHeight);
    }

    @Override
    public void onClearSearchClicked() {
        Log.d(TAG, "onClearSearchClicked()");
    }

    @Override
    public void onActionMenuItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_setting) {
            showSettings();
        }
    }

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
