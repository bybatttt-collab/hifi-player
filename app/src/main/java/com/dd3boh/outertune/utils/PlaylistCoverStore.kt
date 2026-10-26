package com.dd3boh.outertune.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import java.io.File
import java.util.UUID

/** Copy the selected image into app storage, so no lasting access to the user's gallery is needed. */
fun storePlaylistCover(context: Context, uri: Uri): File {
    val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
        val (width, height) = playlistCoverSize(info.size.width, info.size.height)
        decoder.setTargetSize(width, height)
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
    }
    val directory = File(context.filesDir, PLAYLIST_COVER_DIRECTORY).apply { check(isDirectory || mkdirs()) }
    val file = File(directory, UUID.randomUUID().toString() + ".jpg")
    try {
        file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)) }
        return file
    } catch (error: Exception) {
        file.delete()
        throw error
    } finally { bitmap.recycle() }
}

fun removeStoredPlaylistCover(context: Context, path: String?) {
    if (!isCustomPlaylistCover(path)) return
    val file = File(path!!)
    if (file.parentFile?.canonicalFile == File(context.filesDir, PLAYLIST_COVER_DIRECTORY).canonicalFile) file.delete()
}
