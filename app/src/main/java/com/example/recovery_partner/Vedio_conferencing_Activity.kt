package com.example.recovery_partner
import android.Manifest
import android.content.pm.PackageManager

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Checks if the RECORD_AUDIO and CAMERA permissions are granted.
 *
 * If not granted, will request them. Will call onPermissionGranted if/when
 * the permissions are granted.
 */
fun ComponentActivity.requireNeededPermissions(onPermissionsGranted: (() -> Unit)? = null) {
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { grants ->
            // Check if any permissions weren't granted.
            for (grant in grants.entries) {
                if (!grant.value) {
                    Toast.makeText(
                        this,
                        "Missing permission: ${grant.key}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }

            // If all granted, notify if needed.
            if (onPermissionsGranted != null && grants.all { it.value }) {
                onPermissionsGranted()
            }
        }

    val neededPermissions = listOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
        .filter { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_DENIED }
        .toTypedArray()

    if (neededPermissions.isNotEmpty()) {
        requestPermissionLauncher.launch(neededPermissions)
    } else {
        onPermissionsGranted?.invoke()
    }
}