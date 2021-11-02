package com.baosystems.icrc.psm.views.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
    private SplashViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySplashBinding binding = (ActivitySplashBinding) getViewBinding();

        binding.setLifecycleOwner(this);
    }

    public ViewModel createViewModel(@NotNull CompositeDisposable disposable) {
        // TODO: Inject SchedulerProvider using DI
        BaseSchedulerProvider schedulerProvider = new SchedulerProviderImpl();

        // TODO: Inject PreferenceProvider using DI
        PreferenceProvider preferenceProvider = new SecurePreferenceProviderImpl(this);

        viewModel = new ViewModelProvider(
                this,
                new SplashViewModelFactory(
                        getApplication(),
                        disposable,
                        schedulerProvider,
                        preferenceProvider
                )
        ).get(SplashViewModel.class);

        viewModel.isLoggedIn().observe(this, loggedIn -> {
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

        return viewModel;
    }

    @NonNull
    @Override
    public ViewDataBinding createViewBinding() {
        return DataBindingUtil.setContentView(this, R.layout.activity_splash);
    }
}