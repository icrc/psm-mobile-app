package com.baosystems.icrc.psm.views.managestock;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.data.models.AppConfig;
import com.baosystems.icrc.psm.data.models.StockEntry;
import com.baosystems.icrc.psm.data.models.Transaction;
import com.baosystems.icrc.psm.databinding.ActivityManageStockBinding;
import com.baosystems.icrc.psm.service.StockManager;
import com.baosystems.icrc.psm.service.StockManagerImpl;
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider;
import com.baosystems.icrc.psm.service.scheduler.SchedulerProviderImpl;
import com.baosystems.icrc.psm.utils.ConfigUtils;
import com.baosystems.icrc.psm.utils.Sdk;
import com.baosystems.icrc.psm.views.base.BaseActivity;
import com.baosystems.icrc.psm.views.base.ItemWatcher;
import com.baosystems.icrc.psm.views.reviewstock.ReviewStockActivity;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class ManageStockActivity extends BaseActivity {
    private static final String INTENT_DATA = "STOCK_ITEMS";

    private ActivityManageStockBinding binding;
    private ManageStockViewModel viewModel;
    private ManageStockAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        setupObservers();

        // TODO: Temporarily set to a particular code pending when actual scan is implemented
        binding.scanButton.setOnClickListener(
                view -> viewModel.onScanCompleted("AFORMEDFPF2"));
    }

    private void setupObservers() {
        viewModel.getStockItems().observe(this, pagedListLiveData -> {
            Timber.d("Updating recyclerview pagedlist");
            adapter.submitList(pagedListLiveData);
            // TODO: Scroll back to the top of the recyclerview if a new pagedlist is added

            LinearLayoutManager layoutManager =
                    (LinearLayoutManager) binding.stockItemsList.getLayoutManager();
            if (layoutManager != null) {
                int position = layoutManager.findFirstCompletelyVisibleItemPosition();
                if (position != RecyclerView.NO_POSITION) {
                    binding.stockItemsList.scrollToPosition(position);
                }
            }

            // TODO: Handle empty results state

            // TODO: Handle error states
        });
    }

    @Override
    public boolean showMoreOptions() {
        return true;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public Integer getCustomTheme(@NotNull ViewModel viewModel) {
        switch (((ManageStockViewModel)viewModel).getTransaction().getTransactionType()) {
            case DISTRIBUTION:
                return R.style.Theme_App_Distribution;
            case DISCARD:
                return R.style.Theme_App_Discard;
            case CORRECTION:
                return R.style.Theme_App_Correction;
            default:
                return null;
        }
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.stockItemsList;
//        recyclerView.setHasFixedSize(true);

        ItemWatcher<StockEntry, Long> itemWatcher =
                new ItemWatcher<StockEntry, Long>() {
            @Override
            public void removeItem(StockEntry item) {

            }

            @Override
            public void quantityChanged(StockEntry item, Long value) {
                viewModel.setItemQuantity(item, value);
            }

            @Nullable
            @Override
            public Long getValue(StockEntry item) {
                return viewModel.getItemQuantity(item);
            }
        };
        adapter = new ManageStockAdapter(itemWatcher, viewModel.getConfig());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        );
    }

    private void setupSearchInput() {
        TextInputEditText searchInputField = binding.searchInputField;
        searchInputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                viewModel.onSearchQueryChanged(editable.toString());
            }
        });
    }

    @NonNull
    @Override
    public ViewModel createViewModel(@NonNull CompositeDisposable disposable) {
        // TODO: Inject SchedulerProvider using DI
        BaseSchedulerProvider schedulerProvider = new SchedulerProviderImpl();

        // TODO: Inject StockManager
        // TODO: Inject D2
        StockManager stockManager = new StockManagerImpl(Sdk.d2(this));
        Transaction transaction = getIntent().getParcelableExtra(INTENT_DATA);
        AppConfig config = ConfigUtils.getAppConfig(getResources());

        return new ViewModelProvider(
                this,
                new ManageStockViewModelFactory(
                        disposable,
                        schedulerProvider,
                        stockManager,
                        config,
                        transaction
                )
        ).get(ManageStockViewModel.class);
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

    public static Intent getManageStockActivityIntent(Context context, Transaction bundle) {
        Intent intent = new Intent(context, ManageStockActivity.class);
        intent.putExtra(INTENT_DATA, bundle);
        return intent;
    }
}