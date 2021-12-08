package com.baosystems.icrc.psm.ui.sync;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.data.AppConfig;
import com.baosystems.icrc.psm.data.NetworkState;
import com.baosystems.icrc.psm.databinding.ActivitySyncBinding;
import com.baosystems.icrc.psm.ui.base.BaseActivity;
import com.baosystems.icrc.psm.ui.home.HomeActivity;
import com.baosystems.icrc.psm.utils.ActivityManager;
import com.baosystems.icrc.psm.utils.ConfigUtils;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;

@AndroidEntryPoint
public class SyncActivity extends BaseActivity {
    private ActivitySyncBinding binding;
    private SyncViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = (SyncViewModel) getViewModel();

        binding = (ActivitySyncBinding) getViewBinding();
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);

        binding.resyncButton.setOnClickListener(view -> sync());

        addObservers();
        sync();
    }

    private void addObservers() {
        viewModel.getSyncStatus().observe(this, networkState -> {
            if (networkState == NetworkState.Loading.INSTANCE) {
                binding.infoTextView.setText(R.string.sync_in_progress);
                return;
            }

            if (networkState.getClass() == NetworkState.Error.class) {
                NetworkState.Error errorState = ((NetworkState.Error) networkState);

                binding.progressBar.setVisibility(View.GONE);
                binding.infoTextView.setText(errorState.getErrorStringRes());
                binding.infoIcon.setVisibility(View.VISIBLE);
                binding.infoIcon.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.ic_outline_error_36));
                binding.resyncButton.setVisibility(View.VISIBLE);
                return;
            }

            if (networkState.getClass() == NetworkState.Success.class) {
                NetworkState.Success<Boolean> successState = ((NetworkState.Success<Boolean>) networkState);

                if (successState.getResult()) {
                    navigateToHomeScreen();
                } else {
                    binding.infoIcon.setImageDrawable(ContextCompat.getDrawable(
                            this, R.drawable.ic_outline_check_circle_36));
                    binding.infoIcon.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.GONE);
                    binding.infoTextView.setText(R.string.sync_completed);
                }
            }
        });
    }

    private void sync() {
        viewModel.startSync();
    }

    private void navigateToHomeScreen() {
        AppConfig config = ConfigUtils.getAppConfig(getResources());
        ActivityManager.startActivity(this,
                HomeActivity.getHomeActivityIntent(this, config), true
        );
    }

    public static Intent getSyncActivityIntent(Context context) {
        return new Intent(context, SyncActivity.class);
    }

    @NonNull
    @Override
    public ViewModel createViewModel(@NonNull CompositeDisposable disposable) {
        return new ViewModelProvider(this).get(SyncViewModel.class);
    }

    @NonNull
    @Override
    public ViewDataBinding createViewBinding() {
        return DataBindingUtil.setContentView(this, R.layout.activity_sync);
    }
}