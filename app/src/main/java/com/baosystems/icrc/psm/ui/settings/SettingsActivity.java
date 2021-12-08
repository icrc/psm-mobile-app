package com.baosystems.icrc.psm.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.databinding.ActivitySettingsBinding;
import com.baosystems.icrc.psm.ui.login.LoginActivity;
import com.baosystems.icrc.psm.ui.splashscreen.SplashActivity;
import com.baosystems.icrc.psm.utils.ActivityManager;
import com.baosystems.icrc.psm.utils.LocaleManager;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsActivity extends AppCompatActivity
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settings-title";
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }

        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current activity title so that it can be set back again after
        // a configuration change
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate())
            return true;

        return super.onSupportNavigateUp();
    }

    public static Intent getSettingsActivityIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment()
        );
        fragment.setArguments(pref.getExtras());
//        fragment.setTargetFragment(caller, 0);

        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(pref.getTitle());
        }

        return true;
    }

    /**
     * The root fragment
     */
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root, rootKey);
            addListeners();
        }

        private void addListeners() {
            // Add logout click listener
            Preference logoutPref = findPreference(getString(R.string.logout_pref_key));
            if (logoutPref != null) {
                logoutPref.setOnPreferenceClickListener(preference -> {
                    logout();
                    return true;
                });
            }
        }

        private void logout() {
            // TODO: Invoke logout
        }

        private void navigateToLogin() {
            Intent loginIntent = LoginActivity.getLoginActivityIntent(requireActivity());
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            ActivityManager.startActivity(requireActivity(), loginIntent, true);
        }
    }

    /**
     * The languages fragment
     */
    public static class LanguagePreferencesFragment extends PreferenceFragmentCompat
            implements Preference.OnPreferenceChangeListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.languages, rootKey);
            observeLanguagePreferenceChange();
        }

        private void observeLanguagePreferenceChange() {
            String languagePrefKey = getString(R.string.language_pref_key);
            Preference languagePreference = findPreference(languagePrefKey);
            if (languagePreference != null) {
                languagePreference.setOnPreferenceChangeListener(this);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            confirmAppRestart(newValue);
            return true;
        }

        private void confirmAppRestart(Object newValue) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setMessage(R.string.confirm_app_restart_message)
                    .setTitle(R.string.language_change_dialog_title)
                    .setPositiveButton(R.string.ok, (dialog, which) -> restartApp())
                    .setNegativeButton(R.string.restart_later, (dialog, which) -> dialog.cancel());

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void restartApp() {
            Intent intent = SplashActivity.getSplashActivityIntent(requireContext());
            ActivityManager.startActivity(requireActivity(), intent, true);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.setLocale(newBase));
    }
}