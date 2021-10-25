package com.baosystems.icrc.psm.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.data.models.UserIntent;
import com.baosystems.icrc.psm.databinding.ActivityManageStockBinding;
import com.baosystems.icrc.psm.service.MetadataManager;
import com.baosystems.icrc.psm.service.MetadataManagerImpl;
import com.baosystems.icrc.psm.utils.ConfigUtils;
import com.baosystems.icrc.psm.utils.Sdk;
import com.baosystems.icrc.psm.viewmodels.stock.ManageStockViewModel;
import com.baosystems.icrc.psm.viewmodels.stock.ManageStockViewModelFactory;
import com.baosystems.icrc.psm.views.adapters.ManageStockAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class ManageStockActivity extends BaseActivity {
    private static final String INTENT_DATA = "STOCK_CHOICES";

    private ActivityManageStockBinding binding;
    private ManageStockViewModel viewModel;
    private ManageStockAdapter adapter;

    private TextInputEditText searchInputField;
    private TextInputLayout searchInputContainer;

    public static Intent getManageStockActivityIntent(Context context, UserIntent bundle) {
        Intent intent = new Intent(context, ManageStockActivity.class);
        intent.putExtra(INTENT_DATA, bundle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = (ManageStockViewModel) getViewModel();

//        ManageStockViewModel viewModel =
//                new ViewModelProvider(this).get(ManageStockViewModel.class);
//        updateViewModel(getIntent().getParcelableExtra(INTENT_DATA));

        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_stock);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        setSupportActionBar(binding.toolbarContainer.toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        else
            Timber.w("Support action bar is null");

        // configure the search input
        setupSearchInput();

        // configure the recyclerview
        RecyclerView recyclerView = binding.stockItemsList;
        adapter = new ManageStockAdapter();
        recyclerView.setAdapter(adapter);

        searchStock("");
    }

    private void setupSearchInput() {
        searchInputField = binding.searchInputField;
        searchInputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (searchInputField.getText() != null) {
                    showClearSearchIconStatus(searchInputContainer,
                            searchInputField.getText().length() > 0);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchStock(editable.toString());
            }
        });

        // Clear the search field when the clear text icon is clicked
        searchInputContainer = binding.searchFieldLayout;
        // TODO: Clicking of the end icon does not currently work,
        //  it seems the listener is not being invoked. Fix it
//        searchInputContainer.setEndIconOnClickListener(view -> {
//            Log.d(TAG, "Clear search field icon was clicked");
//
//            if (searchInputField.getText() != null)
//                searchInputField.getText().clear();
//        });

//        searchInputContainer.addOnEditTextAttachedListener(textInputLayout -> {
//            EditText textInput = textInputLayout.getEditText();
//            boolean show = textInput != null && !textInput.getText().toString().isEmpty();
//            showClearSearchIconStatus(textInputLayout, show);
//        });
    }

    private void searchStock(String query) {
        // Clear the existing list
        adapter.submitList(null);

        // Invalidate existing observers
        viewModel.getStockItems().removeObservers(this);
        viewModel.getSearch().setValue(query);

        // Observe the new PagedList returned
        viewModel.getStockItems().observe(this, list -> {
            if (list != null)
                Timber.d("Stock items: list size: %d, , loaded count: %d",
                        list.size(), list.getLoadedCount());

            adapter.submitList(list);
        });
    }

    private void showClearSearchIconStatus(TextInputLayout textInputLayout, boolean show) {
        textInputLayout.setEndIconVisible(show);
    }

    @Override
    public ViewModel createViewModel(@NotNull CompositeDisposable disposable) {
        // TODO: Inject MetadataManager
        // TODO: Inject D2
        MetadataManager metadataManager = new MetadataManagerImpl(
                Sdk.d2(this),
                ConfigUtils.loadConfigFile(getResources())
        );

        UserIntent intentExtra = getIntent().getParcelableExtra(INTENT_DATA);
        ManageStockViewModel viewModel = new ViewModelProvider(
                this,
                new ManageStockViewModelFactory(
                        metadataManager,
                        intentExtra.getTransactionType(),
                        intentExtra.getFacility(),
                        intentExtra.getTransactionDate(),
                        intentExtra.getDistributedTo()
                )
        ).get(ManageStockViewModel.class);

        Timber.d(intentExtra.toString());

        return viewModel;
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