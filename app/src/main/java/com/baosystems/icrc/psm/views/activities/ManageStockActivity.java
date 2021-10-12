package com.baosystems.icrc.psm.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.data.models.UserIntent;
import com.baosystems.icrc.psm.databinding.ActivityManageStockBinding;
import com.baosystems.icrc.psm.viewmodels.ManageStockViewModel;
import com.baosystems.icrc.psm.viewmodels.factories.ManageStockViewModelFactory;

import java.util.Objects;

public class ManageStockActivity extends BaseActivity {
    private ActivityManageStockBinding binding;
//    private ManageStockViewModel manageStockViewModel;

    private static final String TAG = "ManageStockActivity";
    private static final String INTENT_DATA = "STOCK_CHOICES";

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
//        super.onCreate(savedInstanceState, persistentState);
//
//
//
//        manageStockViewModel = new ViewModelProvider(this).get(ManageStockViewModel.class);
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_stock);
//        binding.setViewModel(manageStockViewModel);
//        binding.setLifecycleOwner(this);
//
////        binding.
////        setSupportActionBar(binding);
//
//        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
//    }

    public static Intent getManageStockActivityIntent(Context context, UserIntent bundle) {
        Intent intent = new Intent(context, ManageStockActivity.class);
        intent.putExtra(INTENT_DATA, bundle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_stock);

//        ManageStockViewModel viewModel =
//                new ViewModelProvider(this).get(ManageStockViewModel.class);
//        updateViewModel(getIntent().getParcelableExtra(INTENT_DATA));

        UserIntent intentExtra = getIntent().getParcelableExtra(INTENT_DATA);
        ManageStockViewModel viewModel = new ViewModelProvider(
                this,
                new ManageStockViewModelFactory(
                        intentExtra.getTransactionType(),
                        intentExtra.getFacility(),
                        intentExtra.getTransactionDate(),
                        intentExtra.getDistributedTo()
                )
        ).get(ManageStockViewModel.class);

        Log.d(TAG, intentExtra.toString());

        ActivityManageStockBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_manage_stock);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

//        binding.
        setSupportActionBar(binding.toolbarContainer.toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        else
            Log.w(TAG, "Support action bar is null");
    }

    //    private void updateViewModel(UserIntent data) {
//        if (data != null) {
//            manageStockViewModel.setTransactionType(data.getTransactionType());
//            manageStockViewModel.setFacility(data.getFacility());
//            manageStockViewModel.setTransactionDate(data.getTransactionDate());
//
//            if (data.getDistributedTo() != null)
//                manageStockViewModel.setDistributedTo(data.getDistributedTo());
//        }
//    }
}