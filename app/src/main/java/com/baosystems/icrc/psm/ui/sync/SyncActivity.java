package com.baosystems.icrc.psm.ui.sync;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.data.AppConfig;
import com.baosystems.icrc.psm.databinding.ActivitySyncBinding;
import com.baosystems.icrc.psm.ui.base.BaseActivity;
import com.baosystems.icrc.psm.ui.home.HomeActivity;
import com.baosystems.icrc.psm.utils.ActivityManager;
import com.baosystems.icrc.psm.utils.ConfigUtils;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;

@AndroidEntryPoint
public class SyncActivity extends BaseActivity {
    private SyncViewModel viewModel;
    private ProgressBar progressBar;
    private ImageView infoIcon;
    private Button resyncButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = (SyncViewModel) getViewModel();

        ActivitySyncBinding binding = (ActivitySyncBinding) getViewBinding();
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);

        progressBar = binding.progressBar;
        infoIcon = binding.infoIcon;
        resyncButton = binding.resyncButton;

        resyncButton.setOnClickListener(view -> sync());

        addObservers();
        sync();
    }

    private void addObservers() {
        viewModel.getSyncResult().observe(this, result -> {
            if (result != null) {
                if (result.getDrawableRes() != null) {
                    infoIcon.setImageDrawable(ContextCompat.getDrawable(
                            this, result.getDrawableRes()));
                    infoIcon.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }

                // TODO: Ensure you test for the failure and retry sync
                if (result.getError() != null) {
                    resyncButton.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.getSyncCompleted().observe(this, status -> {
            if (status)
                navigateToHomeScreen();
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