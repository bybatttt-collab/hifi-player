package com.zionhuang.innertube.pages

import com.zionhuang.innertube.models.SectionListRenderer
import com.zionhuang.innertube.models.YTItem
import com.zionhuang.innertube.models.getContinuation

/** Headers may precede the actual library shelf. An unknown shape must never mean an empty library. */
fun decodeLibrarySections(sections: List<SectionListRenderer.Content>?): LibraryPage {
    val shelves = sections.orEmpty().filter { it.gridRenderer != null || it.musicShelfRenderer != null }
    require(shelves.isNotEmpty()) { "Library response has no recognized shelf" }
    val items = mutableListOf<YTItem>()
    var complete = true
    val continuations = linkedSetOf<String>()
    shelves.forEach { section ->
        section.gridRenderer?.let { grid ->
            grid.items.mapNotNull { it.musicTwoRowItemRenderer }.forEach { renderer ->
                val item = LibraryPage.fromMusicTwoRowItemRenderer(renderer)
                if (item == null) {
                    if (complete) println("HiFiLibraryShape grid: album=${renderer.isAlbum}, playlist=${renderer.isPlaylist}, song=${renderer.isSong}, title=${!renderer.title.runs.isNullOrEmpty()}, overlay=${renderer.thumbnailOverlay != null}, libraryNavigation=${renderer.navigationEndpoint.browseEndpoint?.browseId?.startsWith("FEmusic_") == true}, hasBrowse=${renderer.navigationEndpoint.browseEndpoint != null}")
                    complete = false
                } else items += item
            }
            grid.continuations?.getContinuation()?.let(continuations::add)
        }
        section.musicShelfRenderer?.let { shelf ->
            shelf.contents.orEmpty().mapNotNull { it.musicResponsiveListItemRenderer }.forEach { renderer ->
                val item = LibraryPage.fromMusicResponsiveListItemRenderer(renderer)
                if (item == null) complete = false else items += item
            }
            shelf.continuations?.getContinuation()?.let(continuations::add)
            shelf.contents?.getContinuation()?.let(continuations::add)
        }
    }
    require(continuations.size <= 1) { "Library contains multiple independent continuations" }
    return LibraryPage(items.distinctBy { it.id }, continuations.singleOrNull(), complete)
}
