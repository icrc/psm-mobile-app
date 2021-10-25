package com.baosystems.icrc.psm.views.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

/**
 * The base Activity
 * Has the menu set, and also sets the action bar.
 */
abstract class BaseActivity : AppCompatActivity() {
    private lateinit var viewModel: ViewModel

    // TODO: Inject via DI if possible/necessary
    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        // TODO: Figure out a way to not do this.
        //  Removing the line without fixing the issue adds the activity name
        //  to the left of the toolbar
//        toolbar.title = ""
//        setSupportActionBar(toolbar)

        viewModel = createViewModel(disposable)
    }

    fun getViewModel(): ViewModel = viewModel

    /**
     * Initialize the ViewModel for this Activity
     */
    abstract fun createViewModel(disposable: CompositeDisposable): ViewModel

    override fun onDestroy() {
        Timber.d("About to clear existing 'disposables'")
        disposable.clear()
        super.onDestroy()
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