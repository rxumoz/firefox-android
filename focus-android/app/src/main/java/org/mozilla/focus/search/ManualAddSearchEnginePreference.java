/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import android.widget.ProgressBar;
import org.mozilla.focus.R;
import org.mozilla.focus.utils.UrlUtils;

public class ManualAddSearchEnginePreference extends Preference {
    private EditText engineNameEditText;
    private EditText searchQueryEditText;
    private TextInputLayout engineNameErrorLayout;
    private TextInputLayout searchQueryErrorLayout;

    private ProgressBar progressView;

    private String savedSearchEngineName;
    private String savedSearchQuery;

    public ManualAddSearchEnginePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ManualAddSearchEnginePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        final View view = super.onCreateView(parent);

        engineNameErrorLayout = view.findViewById(R.id.edit_engine_name_layout);
        searchQueryErrorLayout = view.findViewById(R.id.edit_search_string_layout);

        engineNameEditText = view.findViewById(R.id.edit_engine_name);
        engineNameEditText.addTextChangedListener(buildTextWatcherForErrorLayout(engineNameErrorLayout));
        searchQueryEditText = view.findViewById(R.id.edit_search_string);
        searchQueryEditText.addTextChangedListener(buildTextWatcherForErrorLayout(searchQueryErrorLayout));

        progressView = view.findViewById(R.id.progress);

        updateState();
        return view;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        savedSearchEngineName = savedState.searchEngineName;
        savedSearchQuery = savedState.searchQuery;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.searchEngineName = engineNameEditText.getText().toString();
        savedState.searchQuery = searchQueryEditText.getText().toString();
        return savedState;
    }

    private void updateState() {
        if (engineNameEditText != null) {
            engineNameEditText.setText(savedSearchEngineName);
        }
        if (searchQueryEditText != null) {
            searchQueryEditText.setText(savedSearchQuery);
        }
    }

    private boolean engineNameIsUnique(String engineName) {
        final SharedPreferences sharedPreferences = getContext().getSharedPreferences(SearchEngineManager.PREF_FILE_SEARCH_ENGINES, Context.MODE_PRIVATE);
        return !sharedPreferences.contains(engineName);
    }

    public boolean validateEngineNameAndShowError(String engineName) {
        if (TextUtils.isEmpty(engineName) || !engineNameIsUnique(engineName)) {
            engineNameErrorLayout.setError(getContext().getString(R.string.search_add_error_empty_name));
            return false;
        } else {
            engineNameErrorLayout.setError(null);
            return true;
        }
    }

    public boolean validateSearchQueryAndShowError(String searchQuery) {
        if (TextUtils.isEmpty(searchQuery)) {
            searchQueryErrorLayout.setError(getContext().getString(R.string.search_add_error_empty_search));
            return false;
        } else if (!UrlUtils.isValidSearchQueryUrl(searchQuery)) {
            searchQueryErrorLayout.setError(getContext().getString(R.string.search_add_error_format));
            return false;
        } else {
            searchQueryErrorLayout.setError(null);
            return true;
        }
    }

    private TextWatcher buildTextWatcherForErrorLayout(final TextInputLayout errorLayout) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                errorLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        };
    }

    public void setSearchQueryErrorText(final String err) {
        searchQueryErrorLayout.setError(err);
    }

    public void setProgressViewShown(final boolean isShown) {
        progressView.setVisibility(isShown ? View.VISIBLE : View.GONE);
    }

    static class SavedState extends BaseSavedState {
        String searchEngineName;
        String searchQuery;

        SavedState(Parcel source) {
            super(source);
            searchEngineName = source.readString();
            searchQuery = source.readString();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(searchEngineName);
            dest.writeString(searchQuery);
        }

        @Override
        public String toString() {
            return "ManualAddSearchEnginePreference.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " search_engine_name=" + searchEngineName
                    + " search_query=" + searchQuery
                    + "}";
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel parcel) {
                        return new SavedState(parcel);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
