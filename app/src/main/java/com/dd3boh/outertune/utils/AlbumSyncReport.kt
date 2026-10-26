package com.dd3boh.outertune.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.text.DateFormat
import java.util.Date

val SyncUploadsKey = booleanPreferencesKey("hifiSyncPersonalUploads")
val HifiAskedUpdateNotificationsKey = booleanPreferencesKey("hifiAskedUpdateNotifications")

val AlbumSyncStateKey = stringPreferencesKey("hifiAlbumSyncState")
val AlbumSavedOrderReadyKey = androidx.datastore.preferences.core.booleanPreferencesKey("hifiAlbumSavedOrderReady")
val AlbumSyncCountKey = intPreferencesKey("hifiAlbumSyncCount")
val AlbumSyncAttemptKey = longPreferencesKey("hifiAlbumSyncAttempt")

@Composable
fun AlbumSyncReport(syncing: Boolean) {
    val state by rememberPreference(AlbumSyncStateKey, "NEVER")
    val count by rememberPreference(AlbumSyncCountKey, 0)
    val time by rememberPreference(AlbumSyncAttemptKey, 0L)
    val message = when {
        syncing -> "YouTube Music · Sincronizzazione album in corso…"
        state == "COMPLETE" -> "YouTube Music · $count album letti · Importazione completa"
        state == "PARTIAL" -> "YouTube Music · $count album letti · Importazione incompleta"
        state == "NEVER" -> "YouTube Music · Album non ancora sincronizzati"
        else -> "YouTube Music · Sincronizzazione non completata. Riprova."
    }
    Text(
        message + if (time > 0L && !syncing) "\nUltimo tentativo: ${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(time))}" else "",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
