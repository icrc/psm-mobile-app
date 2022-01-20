package com.baosystems.icrc.psm.ui.settings;

import static com.baosystems.icrc.psm.commons.Constants.AUDIO_RECORDING_REQUEST_CODE;
import static com.baosystems.icrc.psm.commons.Constants.INSTANT_DATA_SYNC;
import static com.baosystems.icrc.psm.commons.Constants.INTENT_EXTRA_MESSAGE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.work.WorkInfo;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.data.NetworkState;
import com.baosystems.icrc.psm.databinding.ActivitySettingsBinding;
import com.baosystems.icrc.psm.ui.base.BaseActivity;
import com.baosystems.icrc.psm.ui.login.LoginActivity;
import com.baosystems.icrc.psm.ui.splashscreen.SplashActivity;
import com.baosystems.icrc.psm.utils.ActivityManager;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;

@AndroidEntryPoint
public class SettingsActivity extends BaseActivity
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settings-title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsViewModel viewModel = (SettingsViewModel) getViewModel();
        com.baosystems.icrc.psm.databinding.ActivitySettingsBinding binding = (ActivitySettingsBinding) getViewBinding();
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.settings_container, new SettingsFragment())
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

    @NonNull
    @Override
    public ViewDataBinding createViewBinding() {
        return DataBindingUtil.setContentView(this, R.layout.activity_settings);
    }

    @NonNull
    @Override
    public ViewModel createViewModel(@NonNull CompositeDisposable disposable) {
        return new ViewModelProvider(this).get(SettingsViewModel.class);
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
        fragment.setTargetFragment(caller, 0);

        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, fragment)
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
        private SettingsViewModel sViewModel;

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            sViewModel.getLogoutStatus().observe(getViewLifecycleOwner(), networkState -> {
                if (networkState.getClass() == NetworkState.Error.class) {
                    ActivityManager.showErrorMessage(view,
                            getString(((NetworkState.Error) networkState).getErrorStringRes()));

                    return;
                }

                if (networkState.getClass() == NetworkState.Success.class) {
                    navigateToLogin();
                }
            });
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            sViewModel = new ViewModelProvider(requireActivity())
                    .get(SettingsViewModel.class);

            getPreferenceManager().setPreferenceDataStore(
                    sViewModel.preferenceDataStore(getActivity().getApplicationContext())
            );

            setPreferencesFromResource(R.xml.preferences, rootKey);
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

            Preference forceSyncPref = findPreference(getString(R.string.force_sync_pref_key));
            if (forceSyncPref != null) {
                forceSyncPref.setOnPreferenceClickListener(preference -> {
                    sViewModel.syncData();
                    sViewModel.getSyncDataStatus().observe(getViewLifecycleOwner(), workInfoList ->
                            workInfoList.forEach(workInfo -> {
                                if (workInfo.getTags().contains(INSTANT_DATA_SYNC)) {
                                    handleDataSyncResponse(workInfo);
                                }
                            })
                    );
                    return true;
                });
            }

            Preference useMicPref = findPreference(getString(R.string.use_mic_pref_key));
            if (useMicPref != null) {
                useMicPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    if (newValue instanceof Boolean && ((Boolean)newValue)) {
                        ActivityManager.checkPermission(
                                requireActivity(), AUDIO_RECORDING_REQUEST_CODE);
                    }

                    return true;
                });
            }
        }

        private void handleDataSyncResponse(WorkInfo workInfo) {
            if (workInfo.getState() == WorkInfo.State.RUNNING) {
                if (getView() != null) {
                    ActivityManager.showInfoMessage(
                            getView(), getString(R.string.data_sync_in_progress));
                }
            } else if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                if (getView() != null) {
                    ActivityManager.showInfoMessage(getView(), getString(R.string.sync_completed));
                }
            } else if (workInfo.getState() == WorkInfo.State.FAILED) {
                if (getView() != null) {
                    ActivityManager.showErrorMessage(getView(), getString(R.string.data_sync_error));
                }
            }
        }

        private void logout() {
            sViewModel.logout();
        }

        private void navigateToLogin() {
            Intent loginIntent = LoginActivity.getLoginActivityIntent(requireActivity());
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            loginIntent.putExtra(INTENT_EXTRA_MESSAGE, getString(R.string.logout_success_message));

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
            confirmAppRestart();
            return true;
        }

        private void confirmAppRestart() {
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
}