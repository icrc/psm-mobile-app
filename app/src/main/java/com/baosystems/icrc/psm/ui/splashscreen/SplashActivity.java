package com.baosystems.icrc.psm.ui.splashscreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.data.AppConfig;
import com.baosystems.icrc.psm.databinding.ActivitySplashBinding;
import com.baosystems.icrc.psm.ui.base.BaseActivity;
import com.baosystems.icrc.psm.ui.home.HomeActivity;
import com.baosystems.icrc.psm.ui.login.LoginActivity;
import com.baosystems.icrc.psm.ui.sync.SyncActivity;
import com.baosystems.icrc.psm.utils.ActivityManager;
import com.baosystems.icrc.psm.utils.ConfigUtils;

import org.jetbrains.annotations.NotNull;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

@AndroidEntryPoint
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
                Timber.d("User is logged in.\nHas metadata being synced? %s\nHas data being synced? %s",
                        viewModel.hasSyncedMetadata(), viewModel.hasSyncedData());

                if (viewModel.hasSyncedMetadata() && viewModel.hasSyncedData()) {
                    AppConfig config = ConfigUtils.getAppConfig(getResources());
                    intent = HomeActivity.getHomeActivityIntent(this, config);
                } else
                    intent = SyncActivity.getSyncActivityIntent(this);
            } else {
                intent = LoginActivity.getLoginActivityIntent(this);
            }

            ActivityManager.startActivity(this, intent, true);
        });
    }

    public static Intent getSplashActivityIntent(Context context) {
        Intent intent = new Intent(context, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    @NonNull
    public ViewModel createViewModel(@NotNull CompositeDisposable disposable) {
        return new ViewModelProvider(this).get(SplashViewModel.class);
    }

    @NonNull
    @Override
    public ViewDataBinding createViewBinding() {
        return DataBindingUtil.setContentView(this, R.layout.activity_splash);
    }
}