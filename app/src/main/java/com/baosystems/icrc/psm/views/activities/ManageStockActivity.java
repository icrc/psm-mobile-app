package com.baosystems.icrc.psm.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.data.TransactionType;
import com.baosystems.icrc.psm.data.models.Destination;
import com.baosystems.icrc.psm.databinding.ActivityManageStockBinding;
import com.baosystems.icrc.psm.viewmodels.HomeViewModel;
import com.baosystems.icrc.psm.viewmodels.ManageStockViewModel;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.time.LocalDateTime;

public class ManageStockActivity extends BaseActivity {
    private ActivityManageStockBinding binding;
    private ManageStockViewModel manageStockViewModel;

    private enum IntentExtra {
        TRANSACTION_TYPE,
        FACILITY,
        DATETIME,
        DISTRIBUTED_TO
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        manageStockViewModel = new ViewModelProvider(this).get(ManageStockViewModel.class);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_stock);
        binding.setViewModel(manageStockViewModel);
        binding.setLifecycleOwner(this);

//        binding.
//        setSupportActionBar(binding);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static Intent getManageStockActivityIntent(
            Context context,
            HomeViewModel homeViewModel
            ) {
        Bundle bundle = new Bundle();

        TransactionType type = homeViewModel.getTransactionType().getValue();
        if (type != null)
            bundle.putString(IntentExtra.TRANSACTION_TYPE.name(), type.name());

        OrganisationUnit facility = homeViewModel.getFacility().getValue();
        if (facility != null)
            bundle.putString(IntentExtra.FACILITY.name(), facility.name());

        Destination destination = homeViewModel.getDestination().getValue();
        if (destination != null)
            bundle.putString(IntentExtra.DISTRIBUTED_TO.name(), destination.getName());

        LocalDateTime transactionDate = homeViewModel.getTransactionDate().getValue();
        if (transactionDate != null)
            bundle.putString(IntentExtra.DATETIME.name(), transactionDate.toString());


        Intent intent = new Intent(context, ManageStockActivity.class);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_stock);

        ManageStockViewModel viewModel =
                new ViewModelProvider(this).get(ManageStockViewModel.class);

        ActivityManageStockBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_manage_stock);
    }
}