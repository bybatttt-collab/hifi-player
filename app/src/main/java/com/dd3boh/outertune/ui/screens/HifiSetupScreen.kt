package com.dd3boh.outertune.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavController
import com.dd3boh.outertune.BuildConfig
import com.dd3boh.outertune.LocalUpdateChecker
import com.dd3boh.outertune.constants.*
import com.dd3boh.outertune.ui.theme.HifiThemeSettings
import com.dd3boh.outertune.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun HifiSetupScreen(navController: NavController, appScope: CoroutineScope) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val updateChecker = LocalUpdateChecker.current
    var step by rememberSaveable { mutableIntStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var waitingPermission by remember { mutableStateOf(false) }
    var chosenUpdates by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var error by remember { mutableStateOf(false) }
    val cookie by rememberPreference(InnerTubeCookieKey, "")
    val signedIn = remember(cookie) { isAccountCookie(cookie) }
    val updates by rememberPreference(UpdateCheckEnabledKey, true)

    LaunchedEffect(Unit) { context.dataStore.edit { markFirstSetup(it) } }
    BackHandler { if (step > 0 && !busy && !waitingPermission) step-- }

    fun chooseUpdates(enabled: Boolean) {
        if (busy) return
        busy = true
        scope.launch {
            try {
                context.dataStore.edit {
                    it[UpdateCheckEnabledKey] = enabled
                    it[HifiAskedUpdateNotificationsKey] = true
                }
                chosenUpdates = enabled
                step = 1
                error = false
            } catch (_: java.io.IOException) { error = true }
            finally { busy = false }
        }
    }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        waitingPermission = false
        chooseUpdates(true)
    }
    fun finish() {
        if (busy) return
        busy = true
        appScope.launch {
            try {
                val enableUpdates = chosenUpdates ?: updates
                context.dataStore.edit { completeHifiSetup(it, enableUpdates, BuildConfig.VERSION_CODE) }
                context.dataStore.edit { it[OobeStatusKey] = OOBE_VERSION }
                BackgroundCheckWorker.schedule(context)
                if (enableUpdates) appScope.launch { updateChecker.check(force = true) }
                if (navController.previousBackStackEntry != null) navController.popBackStack("setup_wizard", inclusive = true)
                else navController.navigate("home") { popUpTo("setup_wizard") { inclusive = true }; launchSingleTop = true }
            } catch (_: java.io.IOException) {
                error = true
                busy = false
            }
        }
    }

    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(24.dp)) {
        Text("HiFi Player", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text((step + 1).toString() + " / 3", color = MaterialTheme.colorScheme.onSurfaceVariant)
        LinearProgressIndicator(progress = { (step + 1) / 3f }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        Spacer(Modifier.height(28.dp))
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
            when (step) {
                0 -> {
                    Text("Aggiornamenti", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(12.dp))
                    Text("Ricevi un avviso quando arriva una nuova versione.")
                }
                1 -> {
                    Text("Tema e colore", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(12.dp))
                    HifiThemeSettings(showTitle = false)
                }
                2 -> {
                    Text("YouTube Music", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(12.dp))
                    Text(if (signedIn) "Account collegato." else "Ritrova i tuoi album, brani e playlist.")
                    Spacer(Modifier.height(20.dp))
                    OutlinedButton(onClick = { navController.navigate("login") }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                        Text(if (signedIn) "Cambia account" else "Collega account")
                    }
                }
            }
        }
        if (error) Text("Impossibile salvare. Riprova.", color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        when (step) {
            0 -> {
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        waitingPermission = true
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else chooseUpdates(true)
                }, enabled = !busy && !waitingPermission, modifier = Modifier.fillMaxWidth()) { Text("Attiva aggiornamenti") }
                TextButton(onClick = { chooseUpdates(false) }, enabled = !busy && !waitingPermission, modifier = Modifier.fillMaxWidth()) { Text("Non ora") }
            }
            1 -> Button(onClick = { step = 2 }, modifier = Modifier.fillMaxWidth()) { Text("Continua") }
            2 -> Button(onClick = ::finish, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Text(if (signedIn) "Apri HiFi" else "Per ora senza account")
            }
        }
        if (step > 0) TextButton(onClick = { step-- }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Indietro") }
    }
}
