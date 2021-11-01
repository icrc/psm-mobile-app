package com.baosystems.icrc.psm.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.data.TransactionType;
import com.baosystems.icrc.psm.databinding.ActivityHomeBinding;
import com.baosystems.icrc.psm.service.MetadataManager;
import com.baosystems.icrc.psm.service.MetadataManagerImpl;
import com.baosystems.icrc.psm.service.UserManager;
import com.baosystems.icrc.psm.service.UserManagerImpl;
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider;
import com.baosystems.icrc.psm.service.scheduler.SchedulerProviderImpl;
import com.baosystems.icrc.psm.utils.ActivityManager;
import com.baosystems.icrc.psm.utils.ConfigUtils;
import com.baosystems.icrc.psm.utils.Sdk;
import com.baosystems.icrc.psm.viewmodels.home.HomeViewModel;
import com.baosystems.icrc.psm.viewmodels.home.HomeViewModelFactory;
import com.baosystems.icrc.psm.views.adapters.GenericListAdapter;
import com.baosystems.icrc.psm.views.adapters.RecentActivityAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class HomeActivity extends BaseActivity {
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

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);

        facilityTextView = (AutoCompleteTextView) binding.selectedFacilityTextView.getEditText();
        distributedToTextView = (AutoCompleteTextView) binding.distributedToTextView.getEditText();
        recentActivitiesRecyclerView = binding.recentActivityList;

        attachObservers();

//        try {
//            initViewModel(d2);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//
//            ActivityManager.showErrorMessage(binding.getRoot(),
//                    getResources().getString(R.string.config_file_error));
//        }

        setupComponents();
    }

    private void attachObservers() {
        // TODO: Optimize facilityListAdapter (It also crashes when the list item is selected)
        // TODO: Inject FacilityListAdapter with DI
        viewModel.getFacilities().observe(this, facilitiesList -> {
            facilityTextView.setAdapter(new GenericListAdapter<>(
                    this, R.layout.list_item, facilitiesList));
        });

        viewModel.getDestinationsList().observe(this, destinations -> distributedToTextView.setAdapter(new GenericListAdapter<>(
                this, R.layout.list_item, destinations
        )));

        viewModel.getTransactionType().observe(this, transactionType -> {
            Timber.d( "New transaction selected: " + transactionType.name());

            // TODO: Add a border around the selected button, and reset the
            //  other buttons to the default
//            ColorStateList backgroundTintList = ContextCompat.getColorStateList(
//                    this, R.color.selector_distribution_button_background);
//            Paris.style(buttonsMap.get(transactionType)).apply(R.style.SelectedButtonStyle);
        });

        viewModel.getRecentActivityList().observe(this, recentActivities -> {
            recentActivityAdapter.submitList(recentActivities);
        });

        viewModel.getError().observe(this, message -> {
            Timber.d("Error: " + message);
            if (message != null) {
                ActivityManager.showErrorMessage(binding.getRoot(), message);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.more_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            // TODO: Show the app settings UI
            Timber.d("Settings clicked");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupComponents() {
        setupToolbar();
        setupButtons();

        facilityTextView.setOnItemClickListener((adapterView, view, position, row_id) ->
                {
                    viewModel.setFacility(
                            (OrganisationUnit) facilityTextView.getAdapter().getItem(position));
//                    facilityTextView.setText(homeViewModel.getFacility().getValue().displayName());
                }
        );

        distributedToTextView.setOnItemClickListener((adapterView, view, position, row_id) ->
                viewModel.setDestination(
                        (Option) distributedToTextView.getAdapter().getItem(position))
        );

        setupTransactionDateField();

        binding.fabManageStock.setOnClickListener(view -> {
            navigateToManageStock();
        });

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

            button.setOnClickListener(view -> selectTransaction(view, type));
        });
    }

    private void selectTransaction(View button, TransactionType buttonTransaction) {
        viewModel.selectTransaction(buttonTransaction);
    }

    private void setupTransactionDateField() {
        // TODO: Theme datepicker, if necessary
        // TODO: Set the initial date to show in the pciker, which should be equal to the model date
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().build();
        picker.addOnPositiveButtonClickListener(viewModel::setTransactionDate);

        // Show the date picker when the calendar icon is clicked
        binding.transactionDateTextView.setEndIconOnClickListener(view -> {
            picker.show(
                    getSupportFragmentManager(),
                    MaterialDatePicker.class.getCanonicalName()
            );
        });
    }

    private void setupToolbar() {
        // TODO: Figure out a way to fix the toolbar title that shows the activity name on the left

        //  Removing the line without fixing the issue adds the activity name
        //  to the left of the toolbar
        binding.toolbar.setTitle("");
        setSupportActionBar(binding.toolbar);
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

//        UserIntent data = new UserIntent(
//                homeViewModel.getTransactionType().getValue(),
//                homeViewModel.get
//        );
        startActivity(
                ManageStockActivity.getManageStockActivityIntent(this,
                        viewModel.getData())
        );
    }

    public static Intent getHomeActivityIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }

    @NonNull
    @Override
    public ViewModel createViewModel(@NonNull CompositeDisposable disposable) {
//        assert d2 != null; // TODO: Remove once d2 has been injected
        // TODO: Inject D2
        D2 d2 = Sdk.d2(this);

        // TODO: Inject MetadataManager
        MetadataManager metadataManager = new MetadataManagerImpl(
                d2,
                ConfigUtils.loadConfigFile(getResources())
        );

        // TODO: Inject UserManager using DI
        UserManager userManager = new UserManagerImpl(d2);

        // TODO: Inject SchedulerProvider using DI
        BaseSchedulerProvider schedulerProvider = new SchedulerProviderImpl();

        // TODO: Handle situations where d2 is probably null

        // TODO: Handle errors that can occur if expected configuration properties
        //  (e.g. program id, item code id etc) weren't found.
        //  The application cannot proceed without them
        return new ViewModelProvider(this,
                new HomeViewModelFactory(
                        disposable,
                        schedulerProvider,
                        metadataManager,
                        userManager
                )).get(HomeViewModel.class);
    }
}