package com.baosystems.icrc.psm.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.data.models.StockEntry;
import com.baosystems.icrc.psm.databinding.ActivityReviewStockBinding;
import com.baosystems.icrc.psm.service.StockManager;
import com.baosystems.icrc.psm.service.StockManagerImpl;
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider;
import com.baosystems.icrc.psm.service.scheduler.SchedulerProviderImpl;
import com.baosystems.icrc.psm.utils.Sdk;
import com.baosystems.icrc.psm.viewmodels.review.ReviewStockViewModel;
import com.baosystems.icrc.psm.viewmodels.review.ReviewStockViewModelFactory;
import com.baosystems.icrc.psm.viewmodels.stock.ManageStockViewModel;
import com.baosystems.icrc.psm.views.adapters.ItemWatcher;
import com.baosystems.icrc.psm.views.adapters.ReviewStockAdapter;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class ReviewStockActivity extends BaseActivity {
    private static final String INTENT_DATA = "REVIEW_STOCK_ITEMS";

    private ReviewStockViewModel viewModel;
    private ActivityReviewStockBinding binding;
    private ReviewStockAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = (ReviewStockViewModel) getViewModel();
        binding = (ActivityReviewStockBinding) getViewBinding();
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        // Set the toolbar title
        binding.tvToolbar.setText(viewModel.getTransaction().getTransactionType().name());

        setupSearchInput();
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.stockItemsList;
        recyclerView.setHasFixedSize(true);

        ItemWatcher<StockEntry, Long> itemWatcher = new ItemWatcher<StockEntry, Long>() {
            @Override
            public void removeItem(StockEntry item) {
                viewModel.removeItem(item);
            }

            @Nullable
            @Override
            public Long getValue(StockEntry item) {
                return viewModel.getItemQuantity(item);
            }

            @Override
            public void quantityChanged(StockEntry item, Long value) {
                viewModel.updateQuantity(item, value);
            }
        };
        adapter = new ReviewStockAdapter(itemWatcher);
        recyclerView.setAdapter(adapter);

        viewModel.getStockItems().observe(this, stockItems -> {
            Timber.d("Stock item entries: %s", stockItems);
            adapter.submitList(stockItems);
        });
    }

    private void setupSearchInput() {

    }

    @Nullable
    @Override
    public Integer getCustomTheme(@NonNull ViewModel viewModel) {
        switch (((ReviewStockViewModel)viewModel).getTransaction().getTransactionType()) {
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

    public static Intent getReviewStockActivityIntent(Context context, Parcelable bundle) {
        Intent intent = new Intent(context, ReviewStockActivity.class);
        intent.putExtra(INTENT_DATA, bundle);
        return intent;
    }

    @NonNull
    @Override
    public ViewModel createViewModel(@NonNull CompositeDisposable disposable) {
        // TODO: Inject SchedulerProvider using DI
        BaseSchedulerProvider schedulerProvider = new SchedulerProviderImpl();

        // TODO: Inject StockManager
        // TODO: Inject D2
        StockManager stockManager = new StockManagerImpl(Sdk.d2(this));

        ReviewStockViewModel viewModel = new ViewModelProvider(
                this,
                new ReviewStockViewModelFactory(
                        disposable,
                        schedulerProvider,
                        stockManager,
                        getIntent().getParcelableExtra(INTENT_DATA)
                )
        ).get(ReviewStockViewModel.class);



        return viewModel;
    }

    @NonNull
    @Override
    public ViewDataBinding createViewBinding() {
        return DataBindingUtil.setContentView(this, R.layout.activity_review_stock);
    }

    @Nullable
    @Override
    public Toolbar getToolBar() {
        return ((ActivityReviewStockBinding) getViewBinding()).toolbar;
    }
}
