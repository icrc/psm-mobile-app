package com.baosystems.icrc.psm.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.os.Build;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.data.TransactionType;
import com.baosystems.icrc.psm.data.models.AppConfig;
import com.baosystems.icrc.psm.databinding.ActivityHomeBinding;
import com.baosystems.icrc.psm.ui.adapters.RecentActivityAdapter;
import com.baosystems.icrc.psm.ui.base.BaseActivity;
import com.baosystems.icrc.psm.ui.base.GenericListAdapter;
import com.baosystems.icrc.psm.ui.managestock.ManageStockActivity;
import com.baosystems.icrc.psm.utils.ActivityManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;

import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.HashMap;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

@AndroidEntryPoint
public class HomeActivity extends BaseActivity {
    private static final String INTENT_DATA = "APP-CONFIG";

    private ActivityHomeBinding binding;
    private RecentActivityAdapter recentActivityAdapter;

    private AutoCompleteTextView facilityTextView;
    private AutoCompleteTextView distributedToTextView;
    private RecyclerView recentActivitiesRecyclerView;
    private HomeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = (HomeViewModel) getViewModel();

        binding = (ActivityHomeBinding) getViewBinding();
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);

        facilityTextView = (AutoCompleteTextView) binding.selectedFacilityTextView.getEditText();
        distributedToTextView = (AutoCompleteTextView) binding.distributedToTextView.getEditText();
        recentActivitiesRecyclerView = binding.recentActivityList;

        attachObservers();
        setupComponents();

        // Cannot go up the stack
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public boolean showMoreOptions() {
        return true;
    }

    private void attachObservers() {
        // TODO: Optimize facilityListAdapter (It also crashes when the list item is selected)
        // TODO: Inject FacilityListAdapter with DI
        viewModel.getFacilities().observe(this, facilitiesList ->
                facilityTextView.setAdapter(
                        new GenericListAdapter<>(this, R.layout.list_item, facilitiesList)
                )
        );

        viewModel.getDestinationsList().observe(this, destinations -> distributedToTextView.setAdapter(new GenericListAdapter<>(
                this, R.layout.list_item, destinations
        )));

        viewModel.getTransactionType().observe(this, transactionType -> {
            // TODO: Add a border around the selected button, and reset the
            //  other buttons to the default
            Timber.d("Transaction type: %s", transactionType);
        });

        viewModel.getRecentActivityList().observe(this, recentActivities ->
                recentActivityAdapter.submitList(recentActivities));

        viewModel.getError().observe(this, message -> {
            if (message != null) {
                Timber.d("Error: %s", message);
                ActivityManager.showErrorMessage(binding.getRoot(), message);
            }
        });
    }

    private void setupComponents() {
        setupButtons();

        facilityTextView.setOnItemClickListener((adapterView, view, position, row_id) ->
                viewModel.setFacility((OrganisationUnit) facilityTextView.getAdapter().getItem(position))
        );

        distributedToTextView.setOnItemClickListener((adapterView, view, position, row_id) ->
                viewModel.setDestination(
                        (Option) distributedToTextView.getAdapter().getItem(position))
        );

        binding.fabNext.setOnClickListener(view -> navigateToManageStock());
        setupTransactionDateField();
        setupRecentActivities();
    }

    private void setupButtons() {
        // Add listeners to the buttons
        Map<TransactionType, MaterialButton> buttonsMap =
                new HashMap<TransactionType, MaterialButton>() {
            {
                put(TransactionType.DISTRIBUTION, binding.distributionButton);
                put(TransactionType.DISCARD, binding.discardButton);
                put(TransactionType.CORRECTION, binding.correctionButton);
            }
        };

        buttonsMap.entrySet().iterator().forEachRemaining(entry -> {
            TransactionType type = entry.getKey();
            MaterialButton button = entry.getValue();

            button.setOnClickListener(view -> selectTransaction(type));
        });
    }

    private void selectTransaction(TransactionType buttonTransaction) {
        viewModel.selectTransaction(buttonTransaction);

        updateTheme(buttonTransaction);
    }

    private void updateTheme(TransactionType type) {
        int color;

        switch (type) {
            case DISTRIBUTION:
                color = R.color.distribution_color;
                break;
            case DISCARD:
                color = R.color.discard_color;
                break;
            case CORRECTION:
                color = R.color.correction_color;
                break;
            default:
                color = -1;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && color != -1) {
            ColorStateList colorStateList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, color)
            );
            binding.fabNext.setBackgroundTintBlendMode(BlendMode.SRC_OVER);
            binding.toolbar.setBackgroundTintBlendMode(BlendMode.SRC_OVER);
            binding.fabNext.setBackgroundTintList(colorStateList);
            binding.toolbar.setBackgroundTintList(colorStateList);
        }
    }

    private void setupTransactionDateField() {
        // TODO: Theme datepicker, if necessary
        // TODO: Set the initial date to show in the pciker, which should be equal to the model date
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().build();
        picker.addOnPositiveButtonClickListener(viewModel::setTransactionDate);

        // Show the date picker when the calendar icon is clicked
        binding.transactionDateTextView.setEndIconOnClickListener(view ->
                picker.show(getSupportFragmentManager(), MaterialDatePicker.class.getCanonicalName())
        );
    }

    private void setupRecentActivities() {
        recentActivityAdapter = new RecentActivityAdapter();
        recentActivitiesRecyclerView.setAdapter(recentActivityAdapter);

        // TODO: Use a custom divider decoration
//        DividerItemDecoration decoration =
//                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
//        recentActivitiesRecyclerView.addItemDecoration(decoration);
    }


    private void navigateToManageStock() {
        if (!viewModel.readyManageStock()) {
            Toast.makeText(this,
                    this.getString(R.string.cannot_proceed_from_home_warning),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(
                ManageStockActivity.getManageStockActivityIntent(this, viewModel.getData())
        );
    }

    public static Intent getHomeActivityIntent(Context context, AppConfig config) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra(INTENT_DATA, config);
        return intent;
    }

    @NonNull
    @Override
    public ViewModel createViewModel(@NonNull CompositeDisposable disposable) {
        // TODO: Handle situations where d2 is probably null

        // TODO: Handle errors that can occur if expected configuration properties
        //  (e.g. program id, item code id etc) weren't found.
        //  The application cannot proceed without them
        return new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @NonNull
    @Override
    public ViewDataBinding createViewBinding() {
        return DataBindingUtil.setContentView(this, R.layout.activity_home);
    }

    @Nullable
    @Override
    public Toolbar getToolBar() {
        return ((ActivityHomeBinding) getViewBinding()).toolbar;
    }
}