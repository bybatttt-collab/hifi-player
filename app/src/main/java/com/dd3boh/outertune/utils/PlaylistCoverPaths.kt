package com.dd3boh.outertune.utils

import java.io.File

const val PLAYLIST_COVER_DIRECTORY = "hifi-playlist-covers"

fun isCustomPlaylistCover(path: String?): Boolean =
    path != null && File(path).isAbsolute && File(path).parentFile?.name == PLAYLIST_COVER_DIRECTORY

/** Remote refreshes keep an existing personal image; a missing file falls back to the server. */
fun playlistCoverAfterSync(current: String?, remote: String?): String? =
    if (isCustomPlaylistCover(current) && File(current!!).isFile) current else remote

fun playlistCoverSize(width: Int, height: Int): Pair<Int, Int> {
    require(width > 0 && height > 0)
    val scale = minOf(1.0, 1024.0 / maxOf(width, height))
    return maxOf(1, (width * scale).toInt()) to maxOf(1, (height * scale).toInt())
}
