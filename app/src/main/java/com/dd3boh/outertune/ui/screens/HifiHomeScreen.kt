package com.dd3boh.outertune.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.dd3boh.outertune.*
import com.dd3boh.outertune.R
import com.dd3boh.outertune.models.toMediaMetadata
import com.dd3boh.outertune.playback.queues.ListQueue
import com.dd3boh.outertune.ui.component.LibraryAlbumGridItem
import com.dd3boh.outertune.ui.component.LibraryPlaylistGridItem
import com.dd3boh.outertune.ui.component.NavigationTitle
import com.dd3boh.outertune.ui.menu.SongMenu
import com.dd3boh.outertune.ui.menu.YouTubeAlbumMenu
import com.dd3boh.outertune.ui.menu.YouTubePlaylistMenu
import com.dd3boh.outertune.ui.screens.walkthrough.WelcomeCard
import com.dd3boh.outertune.ui.theme.hifiPanel
import com.dd3boh.outertune.utils.*
import com.dd3boh.outertune.viewmodels.HifiHomeViewModel
import com.zionhuang.innertube.models.AlbumItem
import com.zionhuang.innertube.models.PlaylistItem
import androidx.compose.ui.platform.LocalContext
import com.dd3boh.outertune.constants.SongSortType
import com.dd3boh.outertune.constants.SongSortTypeKey
import com.dd3boh.outertune.constants.SongSortDescendingKey
import com.dd3boh.outertune.constants.PlayOrigin
import com.dd3boh.outertune.extensions.toEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(navController: NavController, welcomeCard: WelcomeCard? = null, onWelcomeSee: () -> Unit = {}, onWelcomeDismiss: () -> Unit = {}, viewModel: HifiHomeViewModel = hiltViewModel()) {
    val albums by viewModel.recentAlbums.collectAsState()
    val songs by viewModel.recentSongs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val pinnedIds by rememberPreference(PinnedPlaylistsKey, emptySet<String>())
    val pinned = remember(playlists, pinnedIds) { playlists.filter { playlistPinKey(it.id, it.playlist.browseId) in pinnedIds } }

    val menu = LocalMenuState.current
    val context = LocalContext.current
    val player = LocalPlayerConnection.current ?: return
    val scope = rememberCoroutineScope()
    val metadata by player.mediaMetadata.collectAsState()
    val playing by player.isPlaying.collectAsState()


    LazyColumn(contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(), modifier = Modifier.fillMaxSize().hifiPanel()) {
        if (pinned.isNotEmpty()) {
            item(key = "pinned_title") { NavigationTitle(stringResource(R.string.hifi_pinned_playlists)) }
            item(key = "pinned_playlists") {
                LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(pinned, key = { it.id }) { playlist ->
                        LibraryPlaylistGridItem(navController, menu, scope, playlist, Modifier.width(homeCardWidth()))
                    }
                }
            }
        }
        item(key = "recent_albums_title") { NavigationTitle(stringResource(R.string.hifi_recent_albums)) }
        item(key = "recent_albums") {
            if (albums.isEmpty()) QuietEmpty()
            else LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(albums, key = { it.id }) { album -> LibraryAlbumGridItem(navController = navController, menuState = menu, coroutineScope = scope, album = album, isActive = metadata?.album?.id == album.id, isPlaying = playing, modifier = Modifier.width(homeCardWidth())) }
            }
        }
        item(key = "recent_songs_title") { NavigationTitle(stringResource(R.string.hifi_recent_songs)) }
        if (songs.isEmpty()) item { QuietEmpty() }
        items(songs.take(3), key = { "recent_song_${it.id}" }) { song ->
            ListItem(
                headlineContent = { Text(song.song.title, maxLines = 1) },
                supportingContent = { Text(song.artists.joinToString { it.name }, maxLines = 1) },
                leadingContent = { AsyncImage(model = song.song.thumbnailUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(48.dp)) },
                trailingContent = { IconButton(onClick = { menu.show { SongMenu(originalSong = song, navController = navController, onDismiss = menu::dismiss) } }) { Icon(Icons.Rounded.MoreVert, "Menu") } },
                modifier = Modifier.clickable {
                    scope.launch {
                        val queue = withContext(Dispatchers.IO) {
                            val settings = context.dataStore.data.first()
                            val sort = settings[SongSortTypeKey].toEnum(SongSortType.CREATE_DATE)
                            val all = viewModel.database.likedSongs(sort, settings[SongSortDescendingKey] != false).first()
                            val index = all.indexOfFirst { it.id == song.id }
                            if (index < 0) null else ListQueue(
                                title = context.getString(R.string.liked_songs),
                                items = all.map { it.toMediaMetadata() },
                                startIndex = index,
                            )
                        }
                        queue?.let { player.playQueue(it, origin = PlayOrigin.LIBRARY) }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable private fun QuietEmpty() { Text("Nessun elemento salvato", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium) }
@Composable private fun homeCardWidth(): androidx.compose.ui.unit.Dp =
    ((androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp - 44.dp) / 3).coerceIn(88.dp, 144.dp)
