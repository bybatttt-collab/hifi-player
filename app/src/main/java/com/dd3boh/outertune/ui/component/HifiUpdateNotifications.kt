package com.dd3boh.outertune.ui.component

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.dd3boh.outertune.BuildConfig
import com.dd3boh.outertune.utils.dataStore
import kotlinx.coroutines.flow.first

/** Only after the person has enabled update checks during setup; a refusal is respected. */
@Composable
fun HifiUpdateNotifications(ready: Boolean) {
    if (BuildConfig.DEBUG || !ready || Build.VERSION.SDK_INT < 33) return
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        val askedKey = com.dd3boh.outertune.utils.HifiAskedUpdateNotificationsKey
        if (context.dataStore.data.first()[askedKey] == true) return@LaunchedEffect
        context.dataStore.edit { it[askedKey] = true }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
