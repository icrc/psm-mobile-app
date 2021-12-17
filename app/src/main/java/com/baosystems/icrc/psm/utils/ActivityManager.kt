package com.baosystems.icrc.psm.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.baosystems.icrc.psm.R
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar

class ActivityManager {
    companion object {
        @JvmStatic
        fun startActivity(activity: Activity, intent: Intent,
                          closeCurrentActivity: Boolean) {
            activity.startActivity(intent)

            if (closeCurrentActivity)
                activity.finish()
        }

        @JvmStatic
        private fun showMessage(view: View, message: String, isError: Boolean) {
            val color = if (isError) {
                R.color.error
            } else {
                R.color.primaryColor
            }

            // TODO: Style the backgroundTint of an snackbar in case of errors
            if (message.isNotEmpty())
            // TODO: Move the error and success Snackbar styling to the theme or styles (whichever is appropriate)
                Snackbar.make(view, message, LENGTH_LONG).setBackgroundTint(
                    ContextCompat.getColor(view.context, color)
                ).show()
        }

        @JvmStatic
        fun showErrorMessage(view: View, message: String) {
            showMessage(view, message, true)
        }

        @JvmStatic
        fun showInfoMessage(view: View, message: String) {
            showMessage(view, message, false)
        }

        @JvmStatic
        fun hasFlash(context: Context): Boolean {
            return context.packageManager
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        }

        @JvmStatic
        fun showBackButtonWarning(context: Context, confirmationCallback: () -> Unit) {
            AlertDialog.Builder(context)
                .setMessage(R.string.previous_page_lose_data_warning)
                .setTitle(R.string.confirmation)
                .setPositiveButton(android.R.string.ok) { dialog, which -> confirmationCallback() }
                .setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                .create()
                .show()
        }
    }
}