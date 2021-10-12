package com.baosystems.icrc.psm.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
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
import com.baosystems.icrc.psm.utils.Sdk;
import com.baosystems.icrc.psm.viewmodels.HomeViewModel;
import com.baosystems.icrc.psm.viewmodels.factories.HomeViewModelFactory;
import com.baosystems.icrc.psm.views.adapters.GenericListAdapter;
import com.baosystems.icrc.psm.views.adapters.RecentActivityAdapter;
import com.google.android.material.button.MaterialButton;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HomeActivity extends AppCompatActivity {
    private HomeViewModel homeViewModel;
    private ActivityHomeBinding binding;
    private RecentActivityAdapter recentActivityAdapter;

    private AutoCompleteTextView facilityTextView;
    private AutoCompleteTextView distributedToTextView;
    private AutoCompleteTextView transactionDateTextView;
    private RecyclerView recentActivitiesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        binding.setLifecycleOwner(this);

        facilityTextView = binding.selectedFacilityTextView;
        transactionDateTextView = binding.transactionDateTextView;
        distributedToTextView = binding.distributedToTextView;
        recentActivitiesRecyclerView = binding.recentActivityList;

        // TODO: Inject D2
        D2 d2 = Sdk.d2();

        // TODO: Inject MetadataManager
        assert d2 != null; // TODO: Remove once d2 has been injected
        try {
            initViewModel(d2);
            setupComponents();
        } catch (IOException e) {
            e.printStackTrace();

            ActivityManager.showErrorMessage(binding.getRoot(),
                    getResources().getString(R.string.config_file_error));
        }
    }

    private void initViewModel(D2 d2) throws IOException {
        MetadataManager metadataManager = new MetadataManagerImpl(d2, loadConfigFile());

        // TODO: Inject UserManager using DI
        UserManager userManager = new UserManagerImpl(d2);

        // TODO: Inject SchedulerProvider using DI
        BaseSchedulerProvider schedulerProvider = new SchedulerProviderImpl();

        // TODO: Handle situations where d2 is probably null

        // TODO: Handle errors that can occur if expected configuration properties
        //  (e.g. program id, item code id etc) weren't found.
        //  The application cannot proceed without them
        homeViewModel = new ViewModelProvider(this,
                new HomeViewModelFactory(
                        schedulerProvider,
                        metadataManager,
                        userManager
                )).get(HomeViewModel.class);

        binding.setViewModel(homeViewModel);

        // TODO: remove later. temporarily used for testing
        homeViewModel.loadTestStockItems("").observe(this, teis -> {
            teis.forEach(tei -> {
                Log.d("HA", tei.trackedEntityAttributeValues().toString());
            });
        });

        // TODO: Optimize facilityListAdapter (It also crashes when the list item is selected)
        // TODO: Inject FacilityListAdapter with DI
        homeViewModel.getFacilities().observe(this, facilitiesList -> {
            facilityTextView.setAdapter(new GenericListAdapter<>(
                    this, R.layout.list_item, facilitiesList));
        });

        homeViewModel.getDestinationsList().observe(this, destinations -> {
            distributedToTextView.setAdapter(new GenericListAdapter<>(
                    this, R.layout.list_item, destinations
            ));
        });

        homeViewModel.getTransactionType().observe(this, transactionType -> {
            Log.d("HA", "New transaction selected: " + transactionType.name());

            // TODO: Add a border around the selected button, and reset the
            //  other buttons to the default
//            ColorStateList backgroundTintList = ContextCompat.getColorStateList(
//                    this, R.color.selector_distribution_button_background);
//            Paris.style(buttonsMap.get(transactionType)).apply(R.style.SelectedButtonStyle);
        });

        homeViewModel.getRecentActivityList().observe(this, recentActivities -> {
            recentActivityAdapter.submitList(recentActivities);
        });
    }

    private Properties loadConfigFile() throws IOException {
        Properties configProps = new Properties();
        configProps.load(getResources().openRawResource(R.raw.config));

        return configProps;
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
            Log.d("HA", "Settings clicked");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupComponents() {
        setupToolbar();
        setupButtons();

        facilityTextView.setOnItemClickListener((adapterView, view, position, row_id) ->
                {
                    homeViewModel.setFacility(
                            (OrganisationUnit) facilityTextView.getAdapter().getItem(position));
//                    facilityTextView.setText(homeViewModel.getFacility().getValue().displayName());
                }
        );

        distributedToTextView.setOnItemClickListener((adapterView, view, position, row_id) ->
                homeViewModel.setDestination(
                        (Option) distributedToTextView.getAdapter().getItem(position))
        );

        setupTransactionDateField();

        binding.extendedNextFab.setOnClickListener(view -> {
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

            button.setOnClickListener(view -> selectTransaction(
                    ((MaterialButton)view), type));
        });
    }

    private void selectTransaction(View button, TransactionType buttonTransaction) {
        homeViewModel.selectTransaction(buttonTransaction);
    }

    private void setupTransactionDateField() {
        // Add a listener to the calendar icon
        binding.transactionDateTextInputLayout.setEndIconOnClickListener(view -> {
            // TODO: Show the datepicker when the calendar icon is clicked
            Log.d("HomeActivity", "Show the datepicker");
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
        if (!homeViewModel.readyManageStock()) {
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
                        homeViewModel.getData())
        );
    }

    public static Intent getHomeActivityIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }
}