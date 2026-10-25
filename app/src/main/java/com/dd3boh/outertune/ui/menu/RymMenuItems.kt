package com.dd3boh.outertune.ui.menu

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.foundation.lazy.grid.LazyGridScope
import com.dd3boh.outertune.R
import com.dd3boh.outertune.utils.RymLinks

fun LazyGridScope.rymMenuItems(
    context: Context,
    artists: List<String>,
    albumTitle: String?,
    songTitle: String? = null,
    onDismiss: () -> Unit,
) {
    if (!albumTitle.isNullOrBlank()) {
        GridMenuItem(icon = Icons.Rounded.Search, title = R.string.hifi_rym_album) {
            onDismiss()
            RymLinks.open(context, albumTitle, artists, album = true)
        }
    }
    if (!songTitle.isNullOrBlank()) {
        GridMenuItem(icon = Icons.Rounded.Search, title = R.string.hifi_rym_song) {
            onDismiss()
            RymLinks.open(context, songTitle, artists, album = false)
        }
    }
}
