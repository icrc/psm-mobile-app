package com.baosystems.icrc.psm.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.data.models.Transaction;
import com.baosystems.icrc.psm.databinding.ActivityManageStockBinding;
import com.baosystems.icrc.psm.service.StockManager;
import com.baosystems.icrc.psm.service.StockManagerImpl;
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider;
import com.baosystems.icrc.psm.service.scheduler.SchedulerProviderImpl;
import com.baosystems.icrc.psm.utils.Sdk;
import com.baosystems.icrc.psm.viewmodels.stock.ManageStockViewModel;
import com.baosystems.icrc.psm.viewmodels.stock.ManageStockViewModelFactory;
import com.baosystems.icrc.psm.views.adapters.ItemWatcher;
import com.baosystems.icrc.psm.views.adapters.ManageStockAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class ManageStockActivity extends BaseActivity {
    private static final String INTENT_DATA = "STOCK_ITEMS";

    private ActivityManageStockBinding binding;
    private ManageStockViewModel viewModel;
    private ManageStockAdapter adapter;

    private TextInputEditText searchInputField;
    private TextInputLayout searchInputContainer;

    public static Intent getManageStockActivityIntent(Context context, Transaction bundle) {
        Intent intent = new Intent(context, ManageStockActivity.class);
        intent.putExtra(INTENT_DATA, bundle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        getTheme().applyStyle(R.style.Theme_PharmacyStockManagement_Distribution, true);

        viewModel = (ManageStockViewModel) getViewModel();

        binding = (ActivityManageStockBinding) getViewBinding();
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        binding.fabManageStock.setOnClickListener(view -> navigateToReviewStock());

        // Set the toolbar title to the active transaction name
        binding.toolbarContainer.tvToolbar.setText(
                viewModel.getTransaction().getTransactionType().name());

        setupSearchInput();
        setupRecyclerView();
        updateColorTheme();

        viewModel.getStockItems().observe(this, pagedListLiveData -> {
            Timber.d("Updating recyclerview pagedlist");
            adapter.submitList(pagedListLiveData);
            // TODO: Scroll back to the top of the recyclerview if a new pagedlist is added

            // TODO: Handle empty results state

            // TODO: Handle error states
        });
    }

    private void updateColorTheme() {
        Integer resId;

        switch (viewModel.getTransaction().getTransactionType()) {
            case DISTRIBUTION:
                resId = R.color.distribution_color;
                break;
            case DISCARD:
                resId = R.color.discard_color;
                break;
            case CORRECTION:
                resId = R.color.correction_color;
                break;
            default:
                resId = null;
                break;
        }

        if (resId != null) {
            binding.toolbarContainer.toolbar.setBackgroundResource(resId);
//            Paris.styleBuilder(binding.fabManageStock).backgroundRes()
//            Paris.style().apply()
        }

    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.stockItemsList;
//        recyclerView.setHasFixedSize(true);

        ItemWatcher<TrackedEntityInstance, Long> qtyChangeListener =
                new ItemWatcher<TrackedEntityInstance, Long>() {
            @Override
            public void quantityChanged(@Nullable TrackedEntityInstance item, Long value) {
                viewModel.setItemQuantity(item, value);
            }

            @Nullable
            @Override
            public Long getValue(TrackedEntityInstance item) {
                return viewModel.getItemQuantity(item);
            }
        };
        adapter = new ManageStockAdapter(qtyChangeListener);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        );
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
//                searchStock(editable.toString());
                viewModel.onSearchQueryChanged(editable.toString());
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

    private void showClearSearchIconStatus(TextInputLayout textInputLayout, boolean show) {
        textInputLayout.setEndIconVisible(show);
    }

    @NonNull
    @Override
    public ViewModel createViewModel(@NonNull CompositeDisposable disposable) {
        // TODO: Inject SchedulerProvider using DI
        BaseSchedulerProvider schedulerProvider = new SchedulerProviderImpl();

        // TODO: Inject StockManager
        // TODO: Inject D2
        StockManager stockManager = new StockManagerImpl(Sdk.d2(this));

        Timber.d("Parcelable extra = %s", getIntent().getParcelableExtra(INTENT_DATA));
        Transaction transaction = getIntent().getParcelableExtra(INTENT_DATA);
        ManageStockViewModel viewModel = new ViewModelProvider(
                this,
                new ManageStockViewModelFactory(
                        disposable,
                        schedulerProvider,
                        stockManager,
                        transaction
                )
        ).get(ManageStockViewModel.class);

        Timber.d(getIntent().getParcelableExtra(INTENT_DATA).toString());

        return viewModel;
    }

    @NonNull
    @Override
    public ViewDataBinding createViewBinding() {
        return DataBindingUtil.setContentView(this, R.layout.activity_manage_stock);
    }

    private void navigateToReviewStock() {
        Timber.d("About to start review activity with payload: %s", viewModel.getData());
        startActivity(
                ReviewStockActivity.getReviewStockActivityIntent(this, viewModel.getData())
        );
    }

    @Nullable
    @Override
    public Toolbar getToolBar() {
        return ((ActivityManageStockBinding) getViewBinding()).toolbarContainer.toolbar;
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