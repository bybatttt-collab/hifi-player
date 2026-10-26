package com.dd3boh.outertune.db

/** Unacknowledged saves made in this app stay ahead of the last remote snapshot. */
object AlbumSavedOrderSql {
    const val ASC = "(album.savedOrder IS NULL) ASC, album.savedOrder ASC, album.bookmarkedAt ASC, album.id ASC"
    const val DESC = "(album.savedOrder IS NULL) DESC, album.savedOrder DESC, album.bookmarkedAt DESC, album.id DESC"
    const val RECENT = "SELECT album.*, 0 AS downloadCount FROM album WHERE bookmarkedAt IS NOT NULL ORDER BY " + DESC + " LIMIT :limit"
}
