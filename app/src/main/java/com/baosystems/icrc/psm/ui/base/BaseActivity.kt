package com.baosystems.icrc.psm.ui.base

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.commons.Constants.INTENT_EXTRA_MESSAGE
import com.baosystems.icrc.psm.ui.settings.SettingsActivity
import com.baosystems.icrc.psm.utils.ActivityManager.Companion.showInfoMessage
import com.baosystems.icrc.psm.utils.LocaleManager
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

    override fun onStart() {
        super.onStart()
        showPendingMessages()
    }

    private fun showPendingMessages() {
        val message = intent.getStringExtra(INTENT_EXTRA_MESSAGE)
        message?.let {
            showInfoMessage(
                getViewBinding().root, it
            )
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
            supportActionBar!!.setDisplayShowTitleEnabled(true)
        } else Timber.w("Support action bar is null")
    }

    /**
     * Get the Activity's toolbar.
     * No toolbar is created by default. Subclasses should override this as necessary
     */
    open fun getToolBar(): Toolbar? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (showMoreOptions()) {
            menuInflater.inflate(R.menu.more_options, menu)
            return true
        }
        return true
    }

    /**
     * Indicates if the more options menu should be shown
     */
    open fun showMoreOptions(): Boolean = false

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Timber.d("Options clicked: %s", item.itemId)
        when(item.itemId) {
            R.id.action_settings -> {
                startActivity(SettingsActivity.getSettingsActivityIntent(this))
                return true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }
}