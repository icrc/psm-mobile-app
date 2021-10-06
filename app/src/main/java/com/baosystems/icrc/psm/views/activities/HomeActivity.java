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
import com.baosystems.icrc.psm.utils.Sdk;
import com.baosystems.icrc.psm.viewmodels.HomeViewModel;
import com.baosystems.icrc.psm.viewmodels.factories.HomeViewModelFactory;
import com.baosystems.icrc.psm.views.adapters.GenericListAdapter;
import com.baosystems.icrc.psm.views.adapters.RecentActivityAdapter;
import com.google.android.material.button.MaterialButton;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.HashMap;
import java.util.Map;

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

        // TODO: Inject D2
        D2 d2 = Sdk.d2();

        // TODO: Inject MetadataManager
        assert d2 != null;
        MetadataManager metadataManager = new MetadataManagerImpl(d2);

        // TODO: Inject UserManager using DI
        UserManager userManager = new UserManagerImpl(d2);

        // TODO: Inject SchedulerProvider using DI
        BaseSchedulerProvider schedulerProvider = new SchedulerProviderImpl();

//        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // TODO: Handle situations where d2 is probably null
        homeViewModel = new ViewModelProvider(this,
                new HomeViewModelFactory(
                        schedulerProvider,
                        metadataManager,
                        userManager
                )).get(HomeViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        binding.setViewModel(homeViewModel);
        binding.setLifecycleOwner(this);

        facilityTextView = binding.selectedFacilityTextView;
        transactionDateTextView = binding.transactionDateTextView;
        distributedToTextView = binding.distributedToTextView;
        recentActivitiesRecyclerView = binding.recentActivityList;

        setupComponents();
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

            // TODO: Show light alert if all the fields haven't been filled, otherwise
            //  navigate to the next activity
            Log.d("HA", "Selected transaction: " +
                    homeViewModel.getTransactionType().getValue());
            Log.d("HA", "Selected facility: " + homeViewModel.getFacility().getValue());
            Log.d("HA", "Selected date: " + homeViewModel.getTransactionDate().getValue());
            Log.d("HA", "Selected distributed to: " + homeViewModel.getDestination().getValue());

            if (!canProceed()) {
                Toast.makeText(this,
                        this.getString(R.string.cannot_proceed_from_home_warning),
                        Toast.LENGTH_SHORT).show();
                return;
            }

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

        homeViewModel.getTransactionType().observe(this, transactionType -> {
            Log.d("HA", "New transaction selected: " + transactionType.name());

            // TODO: Add a border around the selected button, and reset the
            //  other buttons to the default
//            ColorStateList backgroundTintList = ContextCompat.getColorStateList(
//                    this, R.color.selector_distribution_button_background);
//            Paris.style(buttonsMap.get(transactionType)).apply(R.style.SelectedButtonStyle);
        });

        buttonsMap.entrySet().iterator().forEachRemaining(entry -> {
            TransactionType type = entry.getKey();
            MaterialButton button = entry.getValue();

            button.setOnClickListener(view -> selectTransaction(((MaterialButton)view), type, homeViewModel));
        });
    }

    private void selectTransaction(
            View button, TransactionType buttonTransaction, HomeViewModel hvm) {
        hvm.selectTransaction(buttonTransaction);
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
        homeViewModel.getRecentActivityList().observe(this, recentActivities -> {
            recentActivityAdapter.submitList(recentActivities);
        });
        recentActivitiesRecyclerView.setAdapter(recentActivityAdapter);

        // TODO: Use a custom divider decoration
//        DividerItemDecoration decoration =
//                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
//        recentActivitiesRecyclerView.addItemDecoration(decoration);
    }


    private void navigateToManageStock() {
        startActivity(
                ManageStockActivity.getManageStockActivityIntent(this, homeViewModel)
        );
    }

    private boolean canProceed() {
        if (homeViewModel.getTransactionType().getValue() == null)
            return false;


        // TODO: Can bring about NullPointerException
        if (homeViewModel.isDistribution().getValue() && homeViewModel.getDestination().getValue() == null)
            return false;

        return homeViewModel.getFacility().getValue() != null &&
                homeViewModel.getTransactionDate().getValue() != null;
    }

    public static Intent getHomeActivityIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }
}