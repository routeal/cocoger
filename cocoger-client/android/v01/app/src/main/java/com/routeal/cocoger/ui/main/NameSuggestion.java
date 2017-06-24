package com.routeal.cocoger.ui.main;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

/**
 * Created by nabe on 6/22/17.
 */

public class NameSuggestion implements SearchSuggestion {
    private String mColorName;
    private boolean mIsHistory = true;

    public NameSuggestion(String suggestion) {
        this.mColorName = suggestion.toLowerCase();
    }

    public NameSuggestion(Parcel source) {
        this.mColorName = source.readString();
        this.mIsHistory = source.readInt() != 0;
    }

    public void setIsHistory(boolean isHistory) {
        this.mIsHistory = isHistory;
    }

    public boolean getIsHistory() {
        return this.mIsHistory;
    }

    @Override
    public String getBody() {
        return mColorName;
    }

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
