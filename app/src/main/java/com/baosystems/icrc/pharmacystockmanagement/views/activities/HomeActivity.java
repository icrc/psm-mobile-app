package com.baosystems.icrc.pharmacystockmanagement.views.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.baosystems.icrc.pharmacystockmanagement.R;
import com.google.android.material.textfield.TextInputLayout;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.d("HomeActivity", "onCreate HomeActivity");

//        Toolbar toolbar = findViewById(R.id.toolbar);
//
//        // TODO: Figure out a way to not do this.
//        //  Removing the line without fixing the issue adds the activity name
//        //  to the left of the toolbar
//        toolbar.setTitle("");
//        setSupportActionBar(toolbar);

        setupComponents();
    }

    private void setupComponents() {
        populateFacilities();
//        populateDistributedToList();

        // Add a listener to the calendar icon
        TextInputLayout transactionDateBox = findViewById(R.id.transaction_date_wrapper);
        transactionDateBox.setEndIconOnClickListener(view -> {
            // TODO: Show the datepicker when the calendar icon is clicked
            Log.d("HomeActivity", "Show the datepicker");
        });
    }

    private void populateDistributedToList() {
//        ArrayList items = (ArrayList<String>) Arrays.asList(
//                "Akobo Hospital - [HOS], Borno State Specialist Hospital - [HOS]",
//                "Jnah Rafic Hariri University Hospital - [HOS]","Kaga Bandoro Hopital - [HOS]"
//        );
//        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.list_item, items);
//        ((AutoCompleteTextView)findViewById(R.id.tv_facilities_menu)).setAdapter(adapter);
    }

    private void populateFacilities() {
        String[] items = new String[]{"Banadir", "Borno State",
                "Central African Republic Generic"};

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                R.layout.list_item, items);
        AutoCompleteTextView textView = findViewById(R.id.tv_facilities_menu);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1, items);
        textView.setAdapter(adapter);
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
}