package com.zionhuang.innertube

import com.zionhuang.innertube.models.Artist
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.pages.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.net.URI

object FullAlbumSearch {
    val FILTER = YouTube.SearchFilter("hifi:youtube-full-album")

    suspend fun search(query: String): Result<SearchResult> = withContext(Dispatchers.IO) {
        com.zionhuang.innertube.utils.runCatchingCancellable {
            // Initialize the same downloader/proxy used by stream resolution.
            NewPipeUtils
            val text = if (query.contains("full album", ignoreCase = true)) query else "$query full album"
            val service = ServiceList.YouTube
            val handler = service.searchQHFactory.fromQuery(text, listOf("videos"), "")
            val info = SearchInfo.getInfo(service, handler)
            val songs = info.relatedItems.filterIsInstance<StreamInfoItem>().mapNotNull { item ->
                val raw = URI(item.url).rawQuery.orEmpty().split('&').firstOrNull { it.startsWith("v=") }?.substring(2)
                if (raw == null || !raw.matches(Regex("[A-Za-z0-9_-]{11}"))) return@mapNotNull null
                SongItem(
                    id = raw,
                    title = item.name,
                    artists = listOf(Artist(item.uploaderName.orEmpty(), null)),
                    duration = item.duration.takeIf { it > 0 && it < Int.MAX_VALUE }?.toInt(),
                    thumbnail = item.thumbnails.lastOrNull()?.url.orEmpty(),
                    musicVideoType = "MUSIC_VIDEO_TYPE_UGC",
                )
            }.distinctBy { it.id }
            if (songs.isEmpty() && info.errors.isNotEmpty()) throw info.errors.first()
            SearchResult(songs)
        }
    }
}
