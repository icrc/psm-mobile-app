package com.baosystems.icrc.pharmacystockmanagement.views.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.baosystems.icrc.pharmacystockmanagement.R

/**
 * The base Activity
 * Has the menu set, and also sets the action bar.
 */
open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

//        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        // TODO: Figure out a way to not do this.
        //  Removing the line without fixing the issue adds the activity name
        //  to the left of the toolbar
//        toolbar.title = ""
//        setSupportActionBar(toolbar)
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        val inflater: MenuInflater = menuInflater
//        inflater.inflate(R.menu.more_options, menu)
//        Log.d("BaseActivity", "About creating settings menu")
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
//        R.id.action_settings -> {
//            // TODO: Show the app settings UI
//            Log.d("BaseActivity", "Settings clicked")
//            true
//        }
//
//        else -> super.onOptionsItemSelected(item)
//    }
}