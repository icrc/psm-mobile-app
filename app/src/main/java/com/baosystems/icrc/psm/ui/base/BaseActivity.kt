package com.baosystems.icrc.psm.ui.base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.commons.Constants
import com.baosystems.icrc.psm.commons.Constants.AUDIO_RECORDING_REQUEST_CODE
import com.baosystems.icrc.psm.commons.Constants.INTENT_EXTRA_MESSAGE
import com.baosystems.icrc.psm.data.SpeechRecognitionState
import com.baosystems.icrc.psm.data.SpeechRecognitionState.Errored
import com.baosystems.icrc.psm.ui.scanner.ScannerActivity
import com.baosystems.icrc.psm.ui.settings.SettingsActivity
import com.baosystems.icrc.psm.utils.ActivityManager.Companion.checkPermission
import com.baosystems.icrc.psm.utils.ActivityManager.Companion.showErrorMessage
import com.baosystems.icrc.psm.utils.ActivityManager.Companion.showInfoMessage
import com.baosystems.icrc.psm.utils.ActivityManager.Companion.showToast
import com.baosystems.icrc.psm.utils.LocaleManager
import com.journeyapps.barcodescanner.ScanOptions
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

/**
 * The base Activity
 * Has the menu set, and also sets the action bar.
 */
abstract class BaseActivity : AppCompatActivity() {
    private lateinit var viewModel: ViewModel
    private lateinit var binding: ViewDataBinding
    var speechController: SpeechController? = null

    private val disposable = CompositeDisposable()

    var voiceInputEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = createViewModel(disposable)

        if (viewModel is BaseViewModel) {
            voiceInputEnabled = isVoiceInputEnabled(viewModel)

            // Request Audio permission if required
            if (voiceInputEnabled) {
                checkPermission(this, AUDIO_RECORDING_REQUEST_CODE)
            }
        }

        if (viewModel is SpeechRecognitionAwareViewModel) {
            val speechAwareViewModel = viewModel as SpeechRecognitionAwareViewModel
            speechController = SpeechControllerImpl(speechAwareViewModel)

            registerSpeechRecognitionStatusObserver(
                speechAwareViewModel.getSpeechStatus(), speechController)
        }

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

    override fun onResume() {
        super.onResume()

        if (viewModel is BaseViewModel) {
            val currentVoiceInputState: Boolean = isVoiceInputEnabled(viewModel)

            if (voiceInputEnabled != currentVoiceInputState) {
                voiceInputEnabled = currentVoiceInputState

                onVoiceInputStateChanged()
            }
        }
    }

    /**
     * Should be overriden by subclasses that require custom logic
     */
    open fun onVoiceInputStateChanged() {}

    private fun isVoiceInputEnabled(viewModel: ViewModel) =
        (viewModel as BaseViewModel).isVoiceInputEnabled(resources.getString(R.string.use_mic_pref_key))

    override fun onDestroy() {
        disposable.clear()
        super.onDestroy()
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

    open fun scanBarcode(launcher: ActivityResultLauncher<ScanOptions>) {
        val scanOptions = ScanOptions()
            .setBeepEnabled(true)
            .setCaptureActivity(ScannerActivity::class.java)
        launcher.launch(scanOptions)
    }

    open fun crossFade(view: View, show: Boolean, duration: Long) {
        if (show) {
            view.alpha = 0f
            view.visibility = View.VISIBLE
            view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(null)
        } else {
            view.animate()
                .alpha(0f)
                .setDuration(duration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = View.GONE
                    }
                })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AUDIO_RECORDING_REQUEST_CODE && grantResults.isNotEmpty()) {
            var messageRes: Int = R.string.permission_denied

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                messageRes = R.string.permission_granted
            else if (grantResults[0] == PackageManager.PERMISSION_DENIED)
                // Permission denial may occur for different reasons.
                // For more information, see
                // https://developer.android.com/training/permissions/requesting#handle-denial
                messageRes = R.string.permission_denied

            showToast(this, messageRes)
        }
    }

    open fun registerSpeechRecognitionStatusObserver(
        speechStatus: LiveData<SpeechRecognitionState>,
        speechController: SpeechController?
    ) {
        speechStatus.observe(this) { state: SpeechRecognitionState ->
            if (state is Errored) {
                handleSpeechError(state.code, state.data)
            }
            speechController?.onStateChange(state)
        }
    }

    open fun handleSpeechError(code: Int, data: String?) {
        val resId: Int = when (code) {
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> R.string.insufficient_speech_permissions_error
            SpeechRecognizer.ERROR_AUDIO -> R.string.speech_audio_error
            SpeechRecognizer.ERROR_CLIENT -> R.string.speech_client_error
            SpeechRecognizer.ERROR_NETWORK -> R.string.speech_network_error
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> R.string.speech_network_timeout_error
            SpeechRecognizer.ERROR_NO_MATCH -> R.string.no_speech_match_error
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> R.string.speech_recognition_service_busy_error
            SpeechRecognizer.ERROR_SERVER -> R.string.speech_server_error
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> R.string.speech_timeout_error
            Constants.NON_NUMERIC_SPEECH_INPUT_ERROR -> R.string.non_numeric_speech_input_error
            else -> R.string.unknown_speech_error
        }

        val message =
            if (code == Constants.NON_NUMERIC_SPEECH_INPUT_ERROR)
                getString(resId, data ?: "")
            else
                getString(resId)

        Timber.d("Speech status error: code = %d, message = %s", code, message)
        showErrorMessage(binding.root, message)
    }
}