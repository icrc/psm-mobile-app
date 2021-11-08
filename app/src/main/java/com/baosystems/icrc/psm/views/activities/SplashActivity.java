package com.baosystems.icrc.psm.views.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.databinding.ActivitySplashBinding;
import com.baosystems.icrc.psm.service.PreferenceProvider;
import com.baosystems.icrc.psm.service.SecurePreferenceProviderImpl;
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider;
import com.baosystems.icrc.psm.service.scheduler.SchedulerProviderImpl;
import com.baosystems.icrc.psm.utils.ActivityManager;
import com.baosystems.icrc.psm.viewmodels.splash.SplashViewModel;
import com.baosystems.icrc.psm.viewmodels.splash.SplashViewModelFactory;

import org.jetbrains.annotations.NotNull;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class SplashActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySplashBinding binding = (ActivitySplashBinding) getViewBinding();
        binding.setLifecycleOwner(this);

        SplashViewModel viewModel = ((SplashViewModel)getViewModel());
        // Ensure the required configuration parameters have been set
        if (!viewModel.getConfigurationIsValid()) {
            // Inform the user about the error and do nothing else
            binding.splashIcon.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_missing)
            );
            binding.label.setText(getString(R.string.missing_configuration));
            binding.label.setTextColor(getColorStateList(R.color.error));
            return;
        }

        viewModel.getLoggedIn().observe(this, loggedIn -> {
            Timber.d("Login check -> viewModel.getLoggedIn(): %s", loggedIn);
            Intent intent;
            if (loggedIn) {
                Timber.d("User is logged in. Has metadata being synced? %s",
                        viewModel.hasSyncedMetadata());

                if (viewModel.hasSyncedMetadata())
                    intent = HomeActivity.getHomeActivityIntent(this);
                else
                    intent = SyncActivity.getSyncActivityIntent(this);
            } else {
                intent = LoginActivity.getLoginActivityIntent(this);
            }

            ActivityManager.startActivity(this, intent, true);
        });
    }

    @NonNull
    public ViewModel createViewModel(@NotNull CompositeDisposable disposable) {
        // TODO: Inject SchedulerProvider using DI
        BaseSchedulerProvider schedulerProvider = new SchedulerProviderImpl();

        // TODO: Inject PreferenceProvider using DI
        PreferenceProvider preferenceProvider = new SecurePreferenceProviderImpl(this);

        return new ViewModelProvider(
                this,
                new SplashViewModelFactory(
                        getApplication(),
                        disposable,
                        schedulerProvider,
                        preferenceProvider
                )
        ).get(SplashViewModel.class);
    }

    @NonNull
    @Override
    public ViewDataBinding createViewBinding() {
        return DataBindingUtil.setContentView(this, R.layout.activity_splash);
    }
}