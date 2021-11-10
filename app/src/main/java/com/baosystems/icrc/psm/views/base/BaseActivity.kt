package com.baosystems.icrc.psm.views.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

/**
 * The base Activity
 * Has the menu set, and also sets the action bar.
 */
abstract class BaseActivity : AppCompatActivity() {
    private lateinit var viewModel: ViewModel
    private lateinit var binding: ViewDataBinding

    // TODO: Inject via DI if possible/necessary
    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = createViewModel(disposable)

        // Set the custom theme, if any,
        // before calling setContentView (which happens in ViewBinding)
        getCustomTheme(viewModel)?.let {
            theme.applyStyle(it, true)
        }

        binding = createViewBinding()

        getToolBar()?.let {
            setupToolbar(it)
        }
    }

    abstract fun createViewBinding(): ViewDataBinding

    /**
     * Initialize the ViewModel for this Activity
     */
    abstract fun createViewModel(disposable: CompositeDisposable): ViewModel

    /**
     * Subclasses should override this to use a custom theme
     */
    open fun getCustomTheme(viewModel: ViewModel): Int? = null

    fun getViewModel(): ViewModel = viewModel

    fun getViewBinding(): ViewDataBinding = binding

    override fun onDestroy() {
        Timber.d("About to clear existing 'disposables'")
        disposable.clear()
        super.onDestroy()
    }

    private fun setupToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)

        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        } else Timber.w("Support action bar is null")
    }

    /**
     * Get the Activity's toolbar.
     * No toolbar is created by default. Subclasses should override this as necessary
     */
    open fun getToolBar(): Toolbar? = null


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