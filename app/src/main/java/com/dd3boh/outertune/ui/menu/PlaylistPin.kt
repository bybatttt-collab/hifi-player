package com.dd3boh.outertune.ui.menu

import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import com.dd3boh.outertune.LocalDatabase
import com.dd3boh.outertune.R
import com.dd3boh.outertune.db.entities.PlaylistEntity
import com.dd3boh.outertune.utils.*
import com.zionhuang.innertube.models.PlaylistItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Composable
fun rememberPinAction(localId: String, browseId: String?): Pair<Boolean, () -> Unit> {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pins by rememberPreference(PinnedPlaylistsKey, emptySet<String>())
    val key = playlistPinKey(localId, browseId)
    return (key in pins) to {
        scope.launch(kotlinx.coroutines.NonCancellable) { context.dataStore.edit { settings ->
            val current = settings[PinnedPlaylistsKey].orEmpty()
            settings[PinnedPlaylistsKey] = if (key in current) current - key else current + key
        } }
    }
}
