package com.dd3boh.outertune.utils

import androidx.datastore.preferences.core.stringSetPreferencesKey

val PinnedPlaylistsKey = stringSetPreferencesKey("hifiPinnedPlaylists")
fun playlistPinKey(localId: String, browseId: String?): String = browseId?.let { "yt:$it" } ?: "local:$localId"

val HifiNavigationMigratedKey = androidx.datastore.preferences.core.booleanPreferencesKey("hifiNavigationV3")
