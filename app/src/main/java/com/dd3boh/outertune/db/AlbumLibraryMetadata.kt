package com.dd3boh.outertune.db

import com.dd3boh.outertune.db.entities.AlbumEntity
import com.zionhuang.innertube.models.AlbumItem

/** A library card can repair metadata, but must not replace save state or full album details. */
fun mergeAlbumLibraryMetadata(existing: AlbumEntity?, item: AlbumItem): AlbumEntity =
    (existing ?: AlbumEntity(id = item.browseId, title = item.title, songCount = 0, duration = 0)).let { old ->
        old.copy(
            playlistId = item.playlistId.takeIf { it.isNotBlank() } ?: old.playlistId,
            title = item.title.takeIf { it.isNotBlank() } ?: old.title,
            year = item.year ?: old.year,
            thumbnailUrl = item.thumbnail.takeIf { it.isNotBlank() } ?: old.thumbnailUrl,
        )
    }
