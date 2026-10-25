package com.dd3boh.outertune.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dd3boh.outertune.db.MusicDatabase
import com.dd3boh.outertune.db.entities.PlaylistEntity
import com.dd3boh.outertune.models.toMediaMetadata
import com.zionhuang.innertube.FullAlbumSearch
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.utils.walkSongs
import com.dd3boh.outertune.utils.*
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.AlbumItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class RymAlbumActionsViewModel @Inject constructor(private val database: MusicDatabase) : ViewModel() {
    val busy = MutableStateFlow(false)
    val notice = MutableStateFlow<String?>(null)
    val choices = MutableStateFlow<List<AlbumItem>>(emptyList())
    private var saving = false

    fun resolve(context: RymAlbumContext, save: Boolean) {
        if (busy.value) return
        busy.value = true
        notice.value = null
        saving = save
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = (context.artists + context.title).joinToString(" ").take(600)
                val albums = YouTube.search(query, YouTube.SearchFilter.FILTER_ALBUM).getOrThrow()
                    .items.filterIsInstance<AlbumItem>().distinctBy { it.id }.take(8)
                val first = albums.firstOrNull()
                if (first == null) {
                    val video = FullAlbumSearch.search(query).getOrThrow().items.filterIsInstance<SongItem>().firstOrNull()
                    if (video == null) notice.value = "Nessun album trovato, anche nella ricerca YouTube full album."
                    else performFullAlbum(video, save)
                }
                else perform(first, save)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                notice.value = "Ricerca non riuscita. Puoi riprovare."
            } finally { busy.value = false }
        }
    }
    private suspend fun performFullAlbum(video: SongItem, save: Boolean) {
        if (!save) { RymBrowser.openFullAlbum(video); return }
        val remote = mayPushToYouTube()
        var playlist = database.playlistInLibraryAsc().first().firstOrNull {
            it.playlist.name.trim().equals("full album", ignoreCase = true) && it.playlist.isEditable
        }
        val existingPlaylist = playlist != null
        if (playlist == null) {
            val browseId = if (remote) YouTube.createPlaylist("full album").getOrThrow() else null
            val entity = PlaylistEntity(name = "full album", browseId = browseId,
                bookmarkedAt = LocalDateTime.now(), isLocal = browseId == null)
            database.transactionNow { insert(entity) }
            playlist = requireNotNull(database.playlist(entity.id).first())
        }
        val target = playlist
        if (database.playlistSongMap(target.id, video.id).first() != null) {
            notice.value = "Già nella playlist full album."
            return
        }
        val browseId = target.playlist.browseId.takeUnless { target.playlist.isLocal }
        var alreadyRemote = false
        if (browseId != null) {
            check(remote) { "Account writes disabled" }
            if (existingPlaylist) {
                val songs = YouTube.playlist(browseId).getOrThrow().walkSongs()
                check(songs.complete) { "Playlist check incomplete; refusing a possible duplicate" }
                alreadyRemote = songs.items.any { it.id == video.id }
            }
            if (!alreadyRemote) {
                val response = YouTube.addToPlaylist(browseId, video.id).getOrThrow()
                check(response.status.value in 200..299) { "YouTube rejected the playlist save" }
            }
        }
        database.transactionNow {
            insert(video.toMediaMetadata())
            addSongToPlaylist(target, listOf(video.id))
        }
        notice.value = if (alreadyRemote) "Già nella playlist full album."
            else if (browseId != null) "Album salvato nella playlist full album di YouTube Music."
            else "Album salvato nella playlist full album locale."
    }
    fun choose(album: AlbumItem) {
        if (busy.value) return
        choices.value = emptyList()
        busy.value = true
        val save = saving
        viewModelScope.launch(Dispatchers.IO) {
            try { perform(album, save) }
            catch (_: Exception) { notice.value = "Operazione non riuscita. Puoi riprovare." }
            finally { busy.value = false }
        }
    }
    private suspend fun perform(album: AlbumItem, save: Boolean) {
        if (!save) { RymBrowser.openAlbum(album.id); return }
        if (database.albumById(album.id)?.bookmarkedAt != null) {
            notice.value = "Già nella raccolta."
            return
        }
        val page = YouTube.album(album.id).getOrThrow()
        val remote = mayPushToYouTube()
        if (remote) {
            val response = YouTube.likePlaylist(album.playlistId, true).getOrThrow()
            check(response.status.value in 200..299) { "YouTube Music rejected the save" }
        }
        database.transactionNow {
            insert(page)
            albumById(album.id)?.let { entity ->
                if (entity.bookmarkedAt == null) update(entity.copy(bookmarkedAt = LocalDateTime.now()))
            }
        }
        notice.value = if (remote) "Album salvato nella raccolta YouTube Music." else "Album salvato nell’app. Scrittura su YouTube Music disattivata."
    }
}
