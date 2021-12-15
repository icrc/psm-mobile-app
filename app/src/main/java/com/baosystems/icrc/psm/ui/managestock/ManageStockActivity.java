package com.baosystems.icrc.psm.ui.managestock;

import static com.baosystems.icrc.psm.commons.Constants.INTENT_EXTRA_TRANSACTION;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import androidx.activity.result.ActivityResultLauncher;
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
import com.baosystems.icrc.psm.data.models.StockItem;
import com.baosystems.icrc.psm.data.models.Transaction;
import com.baosystems.icrc.psm.databinding.ActivityManageStockBinding;
import com.baosystems.icrc.psm.ui.base.BaseActivity;
import com.baosystems.icrc.psm.ui.base.ItemWatcher;
import com.baosystems.icrc.psm.ui.reviewstock.ReviewStockActivity;
import com.baosystems.icrc.psm.ui.scanner.ScannerActivity;
import com.baosystems.icrc.psm.utils.ActivityManager;
import com.google.android.material.textfield.TextInputEditText;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.hisp.dhis.rules.models.RuleActionAssign;
import org.hisp.dhis.rules.models.RuleEffect;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

@AndroidEntryPoint
public class ManageStockActivity extends BaseActivity {
    private ActivityManageStockBinding binding;
    private ManageStockViewModel viewModel;
    private ManageStockAdapter adapter;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), scanIntentResult -> {
                if (scanIntentResult.getContents() == null) {
                    ActivityManager.showInfoMessage(binding.getRoot(),
                            getString(R.string.scan_canceled));
                } else {
                    String data = scanIntentResult.getContents();
                    Timber.i("Result: %s", data);
//                    viewModel.onScanCompleted("AFORMEDFPF2");
                    // TODO: Set the name of the item in the search field
                    viewModel.onScanCompleted(data);
                }
            });

    private final ItemWatcher<StockItem, Long, String> itemWatcher =
            new ItemWatcher<StockItem, Long, String>() {
                @Override
        public void updateFields(StockItem item, @Nullable Long qty, int position,
                                 @NonNull List<? extends RuleEffect> ruleEffects) {
            ruleEffects.forEach(ruleEffect -> {
                if (ruleEffect.ruleAction() instanceof RuleActionAssign &&
                        (((RuleActionAssign) ruleEffect.ruleAction()).field()
                                .equals(viewModel.getConfig().getStockOnHand()))) {

                    String value = ruleEffect.data();
                    boolean isValid = isValidStockOnHand(value);
                    String stockOnHand = isValid ? value : item.getStockOnHand();

                    viewModel.addItem(item, qty, stockOnHand, !isValid);
                    if (!isValid) {
                        flagError(item);
                    }

                    updateItemView(position);
                }
            });

            updateNextButton();
        }

        private boolean isValidStockOnHand(String value) {
            if (value == null || TextUtils.isEmpty(value)) {
                Timber.w("Stock on hand value received is empty");
                return false;
            }

            return Long.parseLong(value) >= 0;
        }

        private void updateItemView(int position) {
            runOnUiThread(() -> adapter.notifyItemRangeChanged(position, 1));
        }

        private void flagError(StockItem item) {
            ActivityManager.showErrorMessage(binding.getRoot(),
                    getString(R.string.stock_on_hand_exceeded_message));
        }

        @Override
        public String getStockOnHand(StockItem item) {
            return viewModel.getStockOnHand(item);
        }

        @Override
        public void quantityChanged(StockItem item, @Nullable Long value,
                                    @Nullable OnQuantityValidated callback) {
            viewModel.setItemQuantity(item, value, callback);
        }

        @Override
        public void removeItem(StockItem item) { }

        @Nullable
        @Override
        public Long getQuantity(StockItem item) {
            return viewModel.getItemQuantity(item);
        }

        @Override
        public boolean hasError(StockItem item) {
            return viewModel.hasError(item);
        }
    };

    private void updateNextButton() {
        runOnUiThread(() -> binding.fabManageStock.setEnabled(viewModel.canReview()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = (ManageStockViewModel) getViewModel();

        binding = (ActivityManageStockBinding) getViewBinding();
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        binding.fabManageStock.setOnClickListener(view -> navigateToReviewStock());

        // Set the activity title to the active transaction name
        // TODO: use localized name for the title
        setTitle(viewModel.getTransaction().getTransactionType().name());

        setupSearchInput();
        setupRecyclerView();
        setupObservers();

        binding.scanButton.setOnClickListener(view -> scanBarcode());
        updateNextButton();
    }

    private void scanBarcode() {
        ScanOptions scanOptions = new ScanOptions()
                .setBeepEnabled(true)
                .setCaptureActivity(ScannerActivity.class);
        barcodeLauncher.launch(scanOptions);
    }

    private void setupObservers() {
        viewModel.getStockItems().observe(this, pagedListLiveData -> {
            adapter.submitList(pagedListLiveData);

            // Scroll back to the top of the recyclerview if a new pagedlist is added
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
        return new ViewModelProvider(this).get(ManageStockViewModel.class);
    }

    @NonNull
    @Override
    public ViewDataBinding createViewBinding() {
        return DataBindingUtil.setContentView(this, R.layout.activity_manage_stock);
    }

    private void navigateToReviewStock() {
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
        intent.putExtra(INTENT_EXTRA_TRANSACTION, bundle);
        return intent;
    }
}