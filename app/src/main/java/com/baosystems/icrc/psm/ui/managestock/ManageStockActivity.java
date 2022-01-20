package com.baosystems.icrc.psm.ui.managestock;

import static com.baosystems.icrc.psm.commons.Constants.AUDIO_RECORDING_REQUEST_CODE;
import static com.baosystems.icrc.psm.commons.Constants.INTENT_EXTRA_TRANSACTION;
import static com.baosystems.icrc.psm.utils.Utils.isValidStockOnHand;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.text.Editable;
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
import com.baosystems.icrc.psm.data.SpeechRecognitionState;
import com.baosystems.icrc.psm.data.models.StockItem;
import com.baosystems.icrc.psm.data.models.Transaction;
import com.baosystems.icrc.psm.databinding.ActivityManageStockBinding;
import com.baosystems.icrc.psm.ui.base.BaseActivity;
import com.baosystems.icrc.psm.ui.base.BaseViewModel;
import com.baosystems.icrc.psm.ui.base.ItemWatcher;
import com.baosystems.icrc.psm.ui.base.SpeechController;
import com.baosystems.icrc.psm.ui.reviewstock.ReviewStockActivity;
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
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import timber.log.Timber;

@AndroidEntryPoint
public class ManageStockActivity extends BaseActivity {
    private ActivityManageStockBinding binding;
    private ManageStockViewModel viewModel;
    private ManageStockAdapter adapter;
    private boolean voiceInputEnabled;

    private final ItemWatcher<StockItem, String, String> itemWatcher =
            new ItemWatcher<StockItem, String, String>() {

        @Override
        public void updateFields(StockItem item, @Nullable String qty, int position,
                                 @NonNull List<? extends RuleEffect> ruleEffects) {
            // TODO: remove logging below (just for debugging)
            Timber.d(">>>>>>     Rule Effects");
            ruleEffects.forEach(System.out::println);

            ruleEffects.forEach(ruleEffect -> {
                if (ruleEffect.ruleAction() instanceof RuleActionAssign &&
                        (((RuleActionAssign) ruleEffect.ruleAction()).field()
                                .equals(viewModel.getConfig().getStockOnHand()))) {

                    String value = ruleEffect.data();
                    boolean isValid = isValidStockOnHand(value);
                    String stockOnHand = isValid ? value : item.getStockOnHand();

                    viewModel.addItem(item, qty, stockOnHand, !isValid);
                    if (!isValid) {
                        ActivityManager.showErrorMessage(binding.getRoot(),
                                getString(R.string.stock_on_hand_exceeded_message));
                    }

                    updateItemView(position);
                    updateNextButton();
                }
            });

            updateNextButton();
        }

        @Override
        public String getStockOnHand(StockItem item) {
            return viewModel.getStockOnHand(item);
        }

        @Override
        public void quantityChanged(StockItem item, int position, @Nullable String value,
                                    @Nullable OnQuantityValidated callback) {
            // If the qty is cleared, remove from cache if already present
            if (value == null || value.isEmpty()) {
                boolean outcome = viewModel.removeItemFromCache(item);
                if (outcome) {

                    updateItemView(position);
                    updateNextButton();
                }
                return;
            }

            viewModel.setQuantity(item, position, value, callback);
        }

        @Override
        public void removeItem(StockItem item) { }

        @Nullable
        @Override
        public String getQuantity(StockItem item) {
            return viewModel.getItemQuantity(item);
        }

        @Override
        public boolean hasError(StockItem item) {
            return viewModel.hasError(item);
        }
    };

    private final SpeechController speechController = new SpeechController() {
        private Function1<? super String, Unit> callback;

        @Override
        public void startListening(@NonNull Function1<? super String, Unit> callback) {
            this.callback = callback;

            viewModel.startListening();
        }

        @Override
        public void onResult(@Nullable String data) {
            callback.invoke(data);
        }

        @Override
        public void stopListening() {
            viewModel.stopListening();
        }
    };

    private void updateItemView(int position) {
        runOnUiThread(() -> adapter.notifyItemRangeChanged(position, 1));
    }

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

        voiceInputEnabled = viewModel.isVoiceInputEnabled(
                getResources().getString(R.string.use_mic_pref_key)
        );

        if (voiceInputEnabled) {
            ActivityManager.checkPermission(this, AUDIO_RECORDING_REQUEST_CODE);
        }

        // Set the activity title to the active transaction name
        // TODO: use localized name for the title
        setTitle(viewModel.getTransaction().getTransactionType().name());

        setupSearchInput();
        setupRecyclerView();
        setupObservers();
        configureScanner();
        updateNextButton();
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean currentVoiceInputState = viewModel.isVoiceInputEnabled(
                getResources().getString(R.string.use_mic_pref_key)
        );

        if (currentVoiceInputState != voiceInputEnabled) {
            adapter.setVoiceInputEnabled(currentVoiceInputState);
            voiceInputEnabled = currentVoiceInputState;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        ActivityManager.showBackButtonWarning(this, () -> {
                super.onBackPressed();
                return null;
        });
    }

    private void configureScanner() {
        ActivityResultLauncher<ScanOptions> barcodeLauncher =
                registerForActivityResult(new ScanContract(), scanIntentResult -> {
                    if (scanIntentResult.getContents() == null) {
                        ActivityManager.showInfoMessage(binding.getRoot(),
                                getString(R.string.scan_canceled));
                    } else {
                        String data = scanIntentResult.getContents();
                        viewModel.onScanCompleted(data);
                        binding.searchInputField.setText(data);
                    }
                });
        binding.scanButton.setOnClickListener(view -> scanBarcode(barcodeLauncher));
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

        viewModel.getShowGuide().observe(this,
                showGuide -> crossFade(binding.qtyGuide.getRoot(), showGuide,
                        getResources().getInteger(android.R.integer.config_shortAnimTime)));

        viewModel.getSpeechStatus().observe(this, status -> {
            if (status instanceof SpeechRecognitionState.Errored) {
                handleSpeechError(((SpeechRecognitionState.Errored)status).getCode());
            } else if (status instanceof SpeechRecognitionState.Completed) {
//                adapter.setSpeechResult(((SpeechRecognitionState.Completed)status).getData());
                speechController.onResult(((SpeechRecognitionState.Completed)status).getData());
            } else {

            }

//            speechController.onStateChange()
        });
    }

    private void handleSpeechError(int code) {
        String message;

        switch (code) {
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions. Request android.permission.RECORD_AUDIO";
                break;
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error. Maybe your internet connection is poor!";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network operation timed out";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No recognition result matched.";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "Server sends error status";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input detected";
                break;
            default:
                message = "Unhandled speech recognition error code";
                break;
        }

        Timber.d("Speech status error: code = %d, message = %s", code, message);
        ActivityManager.showErrorMessage(binding.getRoot(), message);
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

        boolean isVoiceInputEnabled = viewModel.isVoiceInputEnabled(
                getResources().getString(R.string.use_mic_pref_key));
        adapter = new ManageStockAdapter(
                itemWatcher,
                speechController,
                viewModel.getConfig(),
                isVoiceInputEnabled
        );
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        );

        // Set up listeners for the guide info box
        binding.stockEntriesTableHeader.qtyInfoIconButton.setOnClickListener(v -> viewModel.toggleGuideDisplay());
        binding.qtyGuide.closeGuideButton.setOnClickListener(v -> viewModel.toggleGuideDisplay());
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
    public BaseViewModel createViewModel(@NonNull CompositeDisposable disposable) {
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