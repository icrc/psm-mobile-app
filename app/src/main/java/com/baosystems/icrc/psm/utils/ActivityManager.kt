package com.baosystems.icrc.psm.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
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
        fun showToast(context: Context, messageRes: Int) {
            Toast.makeText(context, context.getString(messageRes), Toast.LENGTH_SHORT).show()
        }

        @JvmStatic
        fun hasFlash(context: Context): Boolean {
            return context.packageManager
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        }

        @JvmStatic
        fun showDialog(context: Context, titleRes: Int, messageRes: String,
                       confirmationCallback: () -> Unit) {
            AlertDialog.Builder(context)
                .setMessage(messageRes)
                .setTitle(titleRes)
                .setPositiveButton(android.R.string.ok) { _, _ -> confirmationCallback() }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show()
        }

        @JvmStatic
        fun showBackButtonWarning(context: Context, confirmationCallback: () -> Unit) {
            showDialog(context, R.string.confirmation,
                context.resources.getString(R.string.previous_page_lose_data_warning),
                confirmationCallback)
        }

        @JvmStatic
        fun checkPermission(activity: Activity, requestCode: Int) {
            if (ContextCompat.checkSelfPermission(activity.applicationContext,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        requestCode
                    )
                }
            }
        }
    }
}