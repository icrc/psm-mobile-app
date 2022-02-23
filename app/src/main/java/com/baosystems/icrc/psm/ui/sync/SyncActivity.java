package com.baosystems.icrc.psm.ui.sync;

import static com.baosystems.icrc.psm.commons.Constants.INITIAL_SYNC;
import static com.baosystems.icrc.psm.commons.Constants.INSTANT_DATA_SYNC;
import static com.baosystems.icrc.psm.commons.Constants.INSTANT_METADATA_SYNC;
import static com.baosystems.icrc.psm.commons.Constants.SCREEN_TRANSITION_DELAY;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkInfo;

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
        sync();
    }

    @Override
    protected void onResume() {
        super.onResume();

        viewModel.getSyncStatus(INITIAL_SYNC).observe(this, workInfoList ->
                workInfoList.forEach(workInfo -> {

            if (workInfo.getTags().contains(INSTANT_METADATA_SYNC)) {
                handleMetadataSyncResponse(workInfo);
            } else if (workInfo.getTags().contains(INSTANT_DATA_SYNC)) {
                handleDataSyncResponse(workInfo);
            }
        }));
    }

    private void handleDataSyncResponse(WorkInfo workInfo) {
        if (workInfo.getState() == WorkInfo.State.RUNNING) {
            showProgress(R.string.data_sync_in_progress);
        } else if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
            showSuccess(R.string.sync_completed);
            navigateToHomeAfterDelay();
        } else if (workInfo.getState() == WorkInfo.State.FAILED) {
            showError(R.string.data_sync_error);
        }
    }

    private void handleMetadataSyncResponse(WorkInfo workInfo) {
        if (workInfo.getState() == WorkInfo.State.RUNNING) {
            showProgress(R.string.metadata_sync_in_progress);
        } else if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
            showSuccess(R.string.metadata_sync_completed);
        } else if (workInfo.getState() == WorkInfo.State.FAILED) {
            showError(R.string.metadata_sync_error);
        }
    }

    private void showError(@StringRes Integer messageRes) {
        binding.progressBar.setVisibility(View.GONE);
        binding.infoTextView.setText(messageRes);

        binding.infoIcon.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.ic_outline_error_36));
        binding.infoIcon.setVisibility(View.VISIBLE);

        binding.resyncButton.setVisibility(View.VISIBLE);
    }

    private void showProgress(@StringRes Integer messageRes) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.infoIcon.setVisibility(View.GONE);
        binding.resyncButton.setVisibility(View.GONE);
        binding.infoTextView.setText(messageRes);
    }

    private void showSuccess(@StringRes Integer messageRes) {
        binding.progressBar.setVisibility(View.GONE);

        binding.infoIcon.setImageDrawable(ContextCompat.getDrawable(
                this, R.drawable.ic_outline_check_circle_36));
        binding.infoIcon.setVisibility(View.VISIBLE);

        binding.infoTextView.setText(messageRes);
    }

    private void navigateToHomeAfterDelay() {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(this::navigateToHomeScreen, SCREEN_TRANSITION_DELAY);
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