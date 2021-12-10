package com.baosystems.icrc.psm.ui.reviewstock;

import static com.baosystems.icrc.psm.commons.Constants.INTENT_EXTRA_MESSAGE;
import static com.baosystems.icrc.psm.commons.Constants.INTENT_EXTRA_STOCK_ENTRIES;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;

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
import com.baosystems.icrc.psm.ui.base.BaseActivity;
import com.baosystems.icrc.psm.ui.base.ItemWatcher;
import com.baosystems.icrc.psm.ui.home.HomeActivity;
import com.baosystems.icrc.psm.utils.ActivityManager;
import com.baosystems.icrc.psm.utils.ConfigUtils;
import com.google.android.material.textfield.TextInputEditText;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;

@AndroidEntryPoint
public class ReviewStockActivity extends BaseActivity {
    private ReviewStockViewModel viewModel;
    private ActivityReviewStockBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = (ReviewStockViewModel) getViewModel();
        binding = (ActivityReviewStockBinding) getViewBinding();
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        // Set the activity title to the active transaction name
        // TODO: use localized name for the title
        setTitle(viewModel.getTransaction().getTransactionType().name());

        setupSearchInput();
        setupRecyclerView();

        binding.fabCommitStock.setOnClickListener(view -> viewModel.commitTransaction());
        viewModel.getCommitStatus().observe(this, status -> {
            if (status)
                navigateToHome();
        });
    }

    private void navigateToHome() {
        Intent intent = HomeActivity.getHomeActivityIntent(
                this,
                ConfigUtils.getAppConfig(getResources())
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(INTENT_EXTRA_MESSAGE, getString(R.string.transaction_completed));
        ActivityManager.startActivity(this, intent, true);
    }

    @Override
    public boolean showMoreOptions() {
        return true;
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.stockItemsList;
        recyclerView.setHasFixedSize(true);

        ItemWatcher<StockEntry, Long> itemWatcher = new ItemWatcher<StockEntry, Long>() {
            @Override
            public void removeItem(StockEntry item) {
                viewModel.removeItem(item);
            }

            @Override
            public Long getValue(StockEntry item) {
                return viewModel.getItemQuantity(item);
            }

            @Override
            public void quantityChanged(StockEntry item, Long value) {
                viewModel.updateQuantity(item, value);
            }
        };
        ReviewStockAdapter adapter = new ReviewStockAdapter(itemWatcher);
        recyclerView.setAdapter(adapter);

        viewModel.getReviewedItems().observe(this, adapter::submitList);
    }

    private void setupSearchInput() {
        TextInputEditText searchField = binding.searchInputField;
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(
                    CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                viewModel.onSearchQueryChanged(editable.toString());
            }
        });
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
        intent.putExtra(INTENT_EXTRA_STOCK_ENTRIES, bundle);
        return intent;
    }

    @NonNull
    @Override
    public ViewModel createViewModel(@NonNull CompositeDisposable disposable) {
        return new ViewModelProvider(this).get(ReviewStockViewModel.class);
    }

    @NonNull
    @Override
    public ViewDataBinding createViewBinding() {
        return DataBindingUtil.setContentView(this, R.layout.activity_review_stock);
    }

    @Nullable
    @Override
    public Toolbar getToolBar() {
        return ((ActivityReviewStockBinding) getViewBinding()).toolbarContainer.toolbar;
    }
}
