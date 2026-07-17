package io.rgbcolor.musikl.search.newpipe

import io.rgbcolor.musikl.model.TrackResult
import io.rgbcolor.musikl.search.MusicSearchProvider
import io.rgbcolor.musikl.search.MusicSearchProvider.Companion.DEFAULT_PAGE_SIZE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem

private object NewPipeInit {
    val ready: Unit by lazy { NewPipe.init(OkHttpDownloader()) }
}

class NewPipeSearchProvider(
    private val pageSize: Int = DEFAULT_PAGE_SIZE,
    private val maxCachedQueries: Int = 100,
) : MusicSearchProvider {

    private class SearchCursor {
        val mutex = Mutex()
        val items = mutableListOf<TrackResult>()
        var nextPage: Page? = null
        var exhausted = false
    }

    private val cursorsLock = Mutex()
    private val cursors = object : LinkedHashMap<String, SearchCursor>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, SearchCursor>?) =
            size > maxCachedQueries
    }

    override suspend fun warmUp() = withContext(Dispatchers.IO) {
        NewPipeInit.ready
    }

    override suspend fun searchSongs(query: String, page: Int): List<TrackResult> =
        search(query, YoutubeSearchQueryHandlerFactory.MUSIC_SONGS, page)

    override suspend fun searchVideos(query: String, page: Int): List<TrackResult> =
        search(query, YoutubeSearchQueryHandlerFactory.MUSIC_VIDEOS, page)

    override suspend fun resolveStreamUrlInternal(pageUrl: String): String {
        NewPipeInit.ready
        val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, pageUrl)
        return streamInfo.audioStreams.maxByOrNull { it.averageBitrate }?.content
            ?: throw Exception("Nessuno stream audio disponibile")
    }

    private suspend fun search(query: String, contentFilter: String, page: Int): List<TrackResult> {
        val cursor = cursorFor(query, contentFilter)

        return cursor.mutex.withLock {
            val requiredCount = (page + 1) * pageSize

            while (cursor.items.size < requiredCount && !cursor.exhausted) {
                fetchNextRawPage(query, contentFilter, cursor)
            }

            val from = (page * pageSize).coerceAtMost(cursor.items.size)
            val to = requiredCount.coerceAtMost(cursor.items.size)
            cursor.items.subList(from, to).toList()
        }
    }

    private suspend fun fetchNextRawPage(query: String, contentFilter: String, cursor: SearchCursor) =
        withContext(Dispatchers.IO) {
            NewPipeInit.ready

            val extractor = ServiceList.YouTube.getSearchExtractor(
                query,
                listOf(contentFilter),
                "",
            )

            val itemsPage = if (cursor.items.isEmpty() && cursor.nextPage == null) {
                extractor.fetchPage()
                extractor.initialPage
            } else {
                val next = cursor.nextPage
                if (next == null) {
                    cursor.exhausted = true
                    return@withContext
                }
                extractor.getPage(next)
            }

            // --- SOLUZIONE: Filtra i duplicati qui ---
            val newItems = itemsPage.items
                .filterIsInstance<StreamInfoItem>()
                .map { it.toTrackResult() }

            val existingUrls = cursor.items.map { it.pageUrl }.toSet()
            val uniqueNewItems = newItems.filter { it.pageUrl !in existingUrls }

            cursor.items += uniqueNewItems
            // ----------------------------------------

            cursor.nextPage = itemsPage.nextPage
            if (itemsPage.nextPage == null) cursor.exhausted = true
        }

    private suspend fun cursorFor(query: String, contentFilter: String): SearchCursor {
        val key = "$contentFilter::$query"
        return cursorsLock.withLock {
            cursors.getOrPut(key) { SearchCursor() }
        }
    }


    private fun StreamInfoItem.toTrackResult(): TrackResult {
        val thumbnailUrl = thumbnails.maxByOrNull { it.height }?.url
            ?: error("Nessuna thumbnail disponibile per \"$name\"")
        return TrackResult(title = name, thumbnailUrl = thumbnailUrl, pageUrl = url)
    }
}