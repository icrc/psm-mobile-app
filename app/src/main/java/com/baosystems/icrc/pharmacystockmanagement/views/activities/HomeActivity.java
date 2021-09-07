package com.baosystems.icrc.pharmacystockmanagement.views.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.baosystems.icrc.pharmacystockmanagement.R;

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