package org.metabrainz.mobile.presentation.features.settings;

import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.metabrainz.mobile.R;
import org.metabrainz.mobile.presentation.IntentFactory;
import org.metabrainz.mobile.presentation.UserPreferences;
import org.metabrainz.mobile.presentation.features.suggestion.SuggestionProvider;
import org.metabrainz.mobile.presentation.features.tagger.FileSelectActivity;
import org.metabrainz.mobile.util.TaggerUtils;

import static android.app.Activity.RESULT_OK;
import static org.metabrainz.mobile.App.DIRECTORY_SELECT_REQUEST_CODE;
import static org.metabrainz.mobile.App.STORAGE_PERMISSION_REQUEST_CODE;
import static org.metabrainz.mobile.presentation.UserPreferences.PREFERENCE_CLEAR_SUGGESTIONS;
import static org.metabrainz.mobile.presentation.UserPreferences.PREFERENCE_TAGGER_DIRECTORY;
import static org.metabrainz.mobile.presentation.features.tagger.FileSelectActivity.ACTION_SELECT_DIRECTORY;
import static org.metabrainz.mobile.presentation.features.tagger.FileSelectActivity.EXTRA_FILE_PATH;
import static org.metabrainz.mobile.presentation.features.tagger.FileSelectActivity.FILE_SELECT_TYPE;

public class SettingsFragment extends PreferenceFragmentCompat implements androidx.preference.Preference.OnPreferenceClickListener {

    private Preference taggerDirectoryPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        findPreference(PREFERENCE_CLEAR_SUGGESTIONS).setOnPreferenceClickListener(this);
        taggerDirectoryPreference = findPreference(PREFERENCE_TAGGER_DIRECTORY);
        taggerDirectoryPreference.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(androidx.preference.Preference preference) {
        if (preference.getKey().equals(PREFERENCE_CLEAR_SUGGESTIONS)) {
            clearSuggestionHistory();
            return true;
        } else if (preference.getKey().equals(PREFERENCE_TAGGER_DIRECTORY)) {
            String[] permissions = TaggerUtils.getPermissionsList(getContext());
            if (permissions.length > 0) ActivityCompat.requestPermissions(getActivity(),
                    permissions, STORAGE_PERMISSION_REQUEST_CODE);
            else chooseDirectory();
            return true;
        }
        return false;
    }

    private void clearSuggestionHistory() {
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(), SuggestionProvider.AUTHORITY,
                SuggestionProvider.MODE);
        suggestions.clearHistory();
        Toast.makeText(getActivity(), R.string.toast_search_cleared, Toast.LENGTH_SHORT).show();
    }

    private void chooseDirectory() {
        Intent intent = new Intent(getActivity(), FileSelectActivity.class);
        intent.putExtra(FILE_SELECT_TYPE, ACTION_SELECT_DIRECTORY);
            startActivityForResult(intent, DIRECTORY_SELECT_REQUEST_CODE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(IntentFactory.getDashboard(getActivity()));
                return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DIRECTORY_SELECT_REQUEST_CODE && resultCode == RESULT_OK) {
            String path = data.getExtras().getString(EXTRA_FILE_PATH);
            if (path != null)
                UserPreferences.setPreferenceTaggerDirectory(path);
        }
    }
}
