package com.baosystems.icrc.pharmacystockmanagement.views.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.baosystems.icrc.pharmacystockmanagement.ManageStockActivity;
import com.baosystems.icrc.pharmacystockmanagement.R;
import com.baosystems.icrc.pharmacystockmanagement.data.TransactionType;
import com.baosystems.icrc.pharmacystockmanagement.data.models.Destination;
import com.baosystems.icrc.pharmacystockmanagement.data.models.Facility;
import com.baosystems.icrc.pharmacystockmanagement.databinding.ActivityHomeBinding;
import com.baosystems.icrc.pharmacystockmanagement.viewmodels.HomeViewModel;
import com.baosystems.icrc.pharmacystockmanagement.views.adapters.RecentActivityAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    private HomeViewModel hvm;
    private RecentActivityAdapter recentActivityAdapter;

    private AutoCompleteTextView facilityTextView;
    private AutoCompleteTextView distributedToTextView;
    private AutoCompleteTextView transactionDateTextView;
    private RecyclerView recentActivitiesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hvm = new ViewModelProvider(this).get(HomeViewModel.class);
        ActivityHomeBinding binding = configureBinding(hvm);
        setupComponents(binding);
    }

    private ActivityHomeBinding configureBinding(HomeViewModel hvm) {
        ActivityHomeBinding binding = DataBindingUtil.setContentView(
                this, R.layout.activity_home);

        facilityTextView = binding.selectedFacilityTextView;
        transactionDateTextView = binding.transactionDateTextView;
        distributedToTextView = binding.distributedToTextView;
        recentActivitiesRecyclerView = binding.recentActivityList;

        binding.setViewModel(hvm);
        binding.setLifecycleOwner(this);

        return binding;
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

    private void setupComponents(ActivityHomeBinding binding) {
        setupToolbar();

        setupButtons(binding);

        hvm.getFacilitiesList().observe(this, facilitiesList -> {
            facilityTextView.setAdapter(new ArrayAdapter<>(
                    this, R.layout.list_item, facilitiesList));
        });

        hvm.getDestinationsList().observe(this, destinations -> {
            distributedToTextView.setAdapter(new ArrayAdapter<Destination>(
                    this, R.layout.list_item, destinations
            ));
        });

        facilityTextView.setOnItemClickListener((adapterView, view, position, row_id) ->
                hvm.setFacility((Facility) facilityTextView.getAdapter().getItem(position))
        );

        distributedToTextView.setOnItemClickListener((adapterView, view, position, row_id) ->
                hvm.setDestination(
                        (Destination) distributedToTextView.getAdapter().getItem(position))
        );

        setupTransactionDateField(binding);

        binding.extendedNextFab.setOnClickListener(view -> {

            // TODO: Show light alert if all the fields haven't been filled, otherwise
            //  navigate to the next activity
            Log.d("HA", "Selected transaction: " +
                    hvm.getTransactionType().getValue());
            Log.d("HA", "Selected facility: " + hvm.getFacility().getValue());
            Log.d("HA", "Selected date: " + hvm.getTransactionDate().getValue());
            Log.d("HA", "Selected distributed to: " + hvm.getDestination().getValue());

            if (!canProceed()) {
                Toast.makeText(this,
                        this.getString(R.string.cannot_proceed_from_home_message),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            navigateToManageStock();
        });

        setupRecentActivities(hvm);
    }

    private void setupButtons(ActivityHomeBinding binding) {
        // Add listeners to the buttons
        Map<TransactionType, MaterialButton> buttonsMap =
                new HashMap<TransactionType, MaterialButton>() {
            {
                put(TransactionType.DISTRIBUTION, binding.distributionButton);
                put(TransactionType.DISCARD, binding.discardButton);
                put(TransactionType.CORRECTION, binding.correctionButton);
            }
        };

        hvm.getTransactionType().observe(this, transactionType -> {
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

            button.setOnClickListener(view -> selectTransaction(((MaterialButton)view), type, hvm));
        });
    }

    private void selectTransaction(
            View button, TransactionType buttonTransaction, HomeViewModel hvm) {
        hvm.selectTransaction(buttonTransaction);
    }

    private void setupTransactionDateField(ActivityHomeBinding binding) {
        // Add a listener to the calendar icon
        binding.transactionDateTextInputLayout.setEndIconOnClickListener(view -> {
            // TODO: Show the datepicker when the calendar icon is clicked
            Log.d("HomeActivity", "Show the datepicker");
        });
    }

    private void setupToolbar() {
        // TODO: Figure out a way to fix the toolbar title that shows the activity name on the left
//        Toolbar toolbar = findViewById(R.id.toolbar);
//
//        //  Removing the line without fixing the issue adds the activity name
//        //  to the left of the toolbar
//        toolbar.setTitle("");
//        setSupportActionBar(toolbar);
    }

    private void setupRecentActivities(HomeViewModel hvm) {
        recentActivityAdapter = new RecentActivityAdapter();
        hvm.getRecentActivityList().observe(this, recentActivities -> {
            recentActivityAdapter.submitList(recentActivities);
        });
        recentActivitiesRecyclerView.setAdapter(recentActivityAdapter);

        // TODO: Use a custom divider decoration
//        DividerItemDecoration decoration =
//                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
//        recentActivitiesRecyclerView.addItemDecoration(decoration);
    }


    private void navigateToManageStock() {
        Intent intent = ManageStockActivity.getManageStockActivityIntent(this, hvm);
        startActivity(intent);
    }

    private boolean canProceed() {
        if (hvm.getTransactionType().getValue() == null)
            return false;

        if (hvm.isDistribution().getValue() && hvm.getDestination().getValue() == null)
            return false;

        return hvm.getFacility().getValue() != null &&
                hvm.getTransactionDate().getValue() != null;
    }
}