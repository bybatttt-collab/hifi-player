package com.dd3boh.outertune.ui.menu

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.dd3boh.outertune.LocalDatabase
import com.dd3boh.outertune.R
import com.dd3boh.outertune.db.entities.PlaylistEntity
import com.dd3boh.outertune.utils.*
import com.zionhuang.innertube.YouTube
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

data class PlaylistCoverActions(val choose: () -> Unit, val reset: () -> Unit, val canReset: Boolean)

@Composable
fun rememberPlaylistCoverActions(playlist: PlaylistEntity?, scope: CoroutineScope): PlaylistCoverActions {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val latest = rememberUpdatedState(playlist)
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val id = latest.value?.id
        if (uri != null && id != null) scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val file = storePlaylistCover(context, uri)
                    try {
                        val current = database.playlist(id).first()?.playlist ?: error("Playlist removed")
                        database.updatePlaylistCover(id, file.absolutePath)
                        removeStoredPlaylistCover(context, current.thumbnailUrl)
                    } catch (error: Exception) { file.delete(); throw error }
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { Toast.makeText(context, R.string.hifi_cover_error, Toast.LENGTH_SHORT).show() }
        }
    }
    return PlaylistCoverActions(
        choose = { picker.launch("image/*") },
        reset = {
            latest.value?.let { selected -> scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val original = selected.browseId?.let { YouTube.playlist(it).getOrNull()?.playlist?.thumbnail }
                        val current = database.playlist(selected.id).first()?.playlist ?: return@withContext
                        database.updatePlaylistCover(selected.id, original)
                        removeStoredPlaylistCover(context, current.thumbnailUrl)
                    }
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { Toast.makeText(context, R.string.hifi_cover_error, Toast.LENGTH_SHORT).show() }
            } }
        },
        canReset = isCustomPlaylistCover(playlist?.thumbnailUrl),
    )
}
