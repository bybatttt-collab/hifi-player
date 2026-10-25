package com.dd3boh.outertune.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dd3boh.outertune.constants.*
import com.dd3boh.outertune.db.MusicDatabase
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.AlbumItem
import com.zionhuang.innertube.models.PlaylistItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HifiHomeViewModel @Inject constructor(val database: MusicDatabase) : ViewModel() {
    val recentAlbums = database.recentSavedAlbums(24).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recentSongs = database.recentSavedSongs(3).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val playlists = database.playlistInLibraryAsc().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
