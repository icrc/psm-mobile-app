package com.baosystems.icrc.psm.views.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.widget.TextView;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.databinding.ActivitySyncBinding;
import com.baosystems.icrc.psm.viewmodels.SyncViewModel;

public class SyncActivity extends AppCompatActivity {

    private TextView syncInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SyncViewModel syncModel = new ViewModelProvider(this).get(SyncViewModel.class);

        ActivitySyncBinding binding = DataBindingUtil.setContentView(
                this, R.layout.activity_sync);
        binding.setLifecycleOwner(this);
        binding.setViewModel(syncModel);

        syncInfoTextView = binding.syncInfoTextView;
        syncModel.startSync();
    }
}