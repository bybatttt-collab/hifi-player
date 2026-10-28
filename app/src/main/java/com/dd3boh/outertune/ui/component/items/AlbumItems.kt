package com.dd3boh.outertune.ui.component.items

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Album
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.media3.exoplayer.offline.Download
import com.dd3boh.outertune.constants.PlayOrigin
import com.dd3boh.outertune.LocalDatabase
import com.dd3boh.outertune.LocalDownloadUtil
import com.dd3boh.outertune.LocalPlayerConnection
import com.dd3boh.outertune.constants.ListThumbnailSize
import com.dd3boh.outertune.constants.ThumbnailCornerRadius
import com.dd3boh.outertune.db.entities.Album
import com.dd3boh.outertune.db.entities.Song
import com.dd3boh.outertune.playback.queues.ListQueue
import com.dd3boh.outertune.ui.utils.getNSongsString
import com.dd3boh.outertune.utils.albumSongsToPlay
import com.dd3boh.outertune.utils.getDownloadState
import com.dd3boh.outertune.utils.joinByBullet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.ZoneOffset

@Composable
fun AlbumListItem(
    album: Album,
    modifier: Modifier = Modifier,
    showLikedIcon: Boolean = true,
    badges: @Composable RowScope.() -> Unit = {
        val database = LocalDatabase.current
        val downloadUtil = LocalDownloadUtil.current
        var songs by remember {
            mutableStateOf(emptyList<Song>())
        }

        LaunchedEffect(Unit) {
            database.albumSongs(album.id).collect {
                songs = it
            }
        }

        var downloadState by remember {
            mutableIntStateOf(Download.STATE_STOPPED)
        }

        LaunchedEffect(songs) {
            val songs = songs.filterNot { it.song.isLocal }
            if (songs.isEmpty()) return@LaunchedEffect
            downloadUtil.downloads.collect { downloads ->
                downloadState = getDownloadState(songs.map { downloads[it.id] })
            }
        }

        if (showLikedIcon && album.album.bookmarkedAt != null) {
        }

        Icon.Download(downloadState)
    },
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    trailingContent: @Composable RowScope.() -> Unit = {},
) = ListItem(
    title = album.album.title,
    subtitle = joinByBullet(
        album.artists.joinToString { it.name },
        album.takeIf { it.album.songCount != 0 }?.let { album ->
            getNSongsString(album.album.songCount, album.downloadCount)
        },
        album.album.year?.toString()
    ),
    badges = badges,
    thumbnailContent = {
        ItemThumbnail(
            thumbnailUrl = album.album.thumbnailUrl,
            placeholderIcon = Icons.Outlined.Album,
            isActive = isActive,
            isPlaying = isPlaying,
            shape = RoundedCornerShape(ThumbnailCornerRadius),
            modifier = Modifier.size(ListThumbnailSize)
        )
    },
    trailingContent = trailingContent,
    modifier = modifier
)

@Composable
fun AlbumGridItem(
    album: Album,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope,
    badges: @Composable RowScope.() -> Unit = {
        val database = LocalDatabase.current
        val downloadUtil = LocalDownloadUtil.current
        var songs by remember {
            mutableStateOf(emptyList<Song>())
        }

        LaunchedEffect(Unit) {
            database.albumSongs(album.id).collect {
                songs = it
            }
        }

        var downloadState by remember {
            mutableIntStateOf(Download.STATE_STOPPED)
        }

        LaunchedEffect(songs) {
            val songs = songs.filterNot { it.song.isLocal }
            if (songs.isEmpty()) return@LaunchedEffect
            downloadUtil.downloads.collect { downloads ->
                downloadState = getDownloadState(songs.map { downloads[it.id] })
            }
        }

        if (album.album.bookmarkedAt != null) {
        }

        Icon.Download(downloadState)
    },
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    fillMaxWidth: Boolean = false,
) = GridItem(
    title = { AlbumGridCaption(album.album.title, album.artists.joinToString { it.name }, badges) },
    subtitle = {},
    thumbnailContent = {
        ItemThumbnail(
            thumbnailUrl = album.album.thumbnailUrl,
            placeholderIcon = Icons.Outlined.Album,
            isActive = isActive,
            isPlaying = isPlaying,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(ThumbnailCornerRadius),
        )

    },
    fillMaxWidth = fillMaxWidth,
    modifier = modifier
)

/** Keep the caption's footprint while letting a one-line title give its spare line to the artist. */
@Composable
private fun AlbumGridCaption(title: String, artist: String, badges: @Composable RowScope.() -> Unit) {
    var titleLines by remember(title) { mutableIntStateOf(2) }
    val titleStyle = MaterialTheme.typography.bodyMedium
    val artistStyle = MaterialTheme.typography.bodySmall
    val minimumHeight = with(LocalDensity.current) {
        (titleStyle.lineHeight.value * 2 + artistStyle.lineHeight.value).sp.toDp()
    }
    Column(Modifier.fillMaxWidth().heightIn(min = minimumHeight)) {
        Text(
            text = title, style = titleStyle, fontWeight = FontWeight.Bold,
            maxLines = 2, overflow = TextOverflow.Ellipsis,
            onTextLayout = { result -> titleLines = result.lineCount },
            modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            badges()
            Text(
                text = artist, style = artistStyle, color = MaterialTheme.colorScheme.secondary,
                maxLines = if (titleLines <= 1) 2 else 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
