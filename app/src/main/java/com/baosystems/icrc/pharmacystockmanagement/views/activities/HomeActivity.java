package com.baosystems.icrc.pharmacystockmanagement.views.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.baosystems.icrc.pharmacystockmanagement.R;
import com.baosystems.icrc.pharmacystockmanagement.data.TransactionType;
import com.baosystems.icrc.pharmacystockmanagement.data.models.Destination;
import com.baosystems.icrc.pharmacystockmanagement.databinding.ActivityHomeBinding;
import com.baosystems.icrc.pharmacystockmanagement.viewmodels.HomeViewModel;
import com.baosystems.icrc.pharmacystockmanagement.views.adapters.RecentActivityAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;

public class HomeActivity extends AppCompatActivity {
    private RecentActivityAdapter recentActivityAdapter;

    private AutoCompleteTextView facilitiesDropdown;
    private AutoCompleteTextView distributedToDropdown;
    private AutoCompleteTextView transactionDateTextView;
    private RecyclerView recentActivitiesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HomeViewModel hvm = new ViewModelProvider(this).get(HomeViewModel.class);
        ActivityHomeBinding binding = configureBinding(hvm);
        setupComponents(binding, hvm);
    }

    private ActivityHomeBinding configureBinding(HomeViewModel hvm) {
        ActivityHomeBinding binding = DataBindingUtil.setContentView(
                this, R.layout.activity_home);

        facilitiesDropdown = binding.tvFacilitiesMenu;
        transactionDateTextView = binding.tvTransactionDate;
        distributedToDropdown = binding.tvDistributedTo;
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
            Log.d(this.getLocalClassName(), "Settings clicked");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupComponents(ActivityHomeBinding binding, HomeViewModel hvm) {
        setupToolbar();

        setupButtons(binding, hvm);

        hvm.getFacilitiesList().observe(this, facilitiesList -> {
            facilitiesDropdown.setAdapter(new ArrayAdapter<>(
                    this, R.layout.list_item, facilitiesList));
        });

        hvm.getDestinationsList().observe(this, destinations -> {
            distributedToDropdown.setAdapter(new ArrayAdapter<Destination>(
                    this, R.layout.list_item, destinations
            ));
        });

        setupRecentActivities(hvm);
        setupTransactionDateField();
    }

    private void setupButtons(ActivityHomeBinding binding, HomeViewModel hvm) {
        // Add listeners to the buttons
        MaterialButton[] buttons = Arrays.asList(
                binding.distributionButton,
                binding.discardButton,
                binding.correctionButton
        ).toArray(new MaterialButton[0]);

        TransactionType[] buttonTransactions = Arrays.asList(
                TransactionType.DISTRIBUTION,
                TransactionType.DISCARD,
                TransactionType.CORRECTION
        ).toArray(new TransactionType[0]);

        for (int i = 0; i < buttons.length; i++) {
            int btnIndex = i;
            buttons[i].setOnClickListener(button -> {
                hvm.setSelectedTransaction(buttonTransactions[btnIndex]);
            });
        }
    }

    private void setupTransactionDateField() {
        // Add a listener to the calendar icon
        TextInputLayout transactionDateBox = findViewById(R.id.transaction_date_wrapper);
        transactionDateBox.setEndIconOnClickListener(view -> {
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
}