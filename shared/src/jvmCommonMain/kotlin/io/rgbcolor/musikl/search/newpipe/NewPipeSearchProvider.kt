package io.rgbcolor.musikl.search.newpipe

import io.rgbcolor.musikl.model.TrackResult
import io.rgbcolor.musikl.search.MusicSearchProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem

private object NewPipeInit {
    val ready: Unit by lazy { NewPipe.init(OkHttpDownloader()) }
}

class NewPipeSearchProvider : MusicSearchProvider {

    override suspend fun searchFirstSong(query: String): TrackResult =
        searchFirst(query, YoutubeSearchQueryHandlerFactory.MUSIC_SONGS)

    override suspend fun searchFirstVideo(query: String): TrackResult =
        searchFirst(query, YoutubeSearchQueryHandlerFactory.MUSIC_VIDEOS)

    override suspend fun searchSongs(query: String): List<TrackResult> =
        search(query, YoutubeSearchQueryHandlerFactory.MUSIC_SONGS)

    override suspend fun searchVideos(query: String): List<TrackResult> =
        search(query, YoutubeSearchQueryHandlerFactory.MUSIC_VIDEOS)

    override suspend fun resolveStreamUrl(pageUrl: String): String = withContext(Dispatchers.IO) {
        NewPipeInit.ready

        val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, pageUrl)
        val bestAudio = streamInfo.audioStreams.maxByOrNull { it.averageBitrate }
            ?: error("Nessuno stream audio disponibile per questo contenuto")

        bestAudio.content
    }

    private suspend fun searchFirst(query: String, contentFilter: String): TrackResult =
        search(query, contentFilter).firstOrNull()
            ?: error("Nessun risultato per \"$query\"")

    private suspend fun search(query: String, contentFilter: String): List<TrackResult> =
        withContext(Dispatchers.IO) {
            NewPipeInit.ready

            val extractor = ServiceList.YouTube.getSearchExtractor(
                query,
                listOf(contentFilter),
                "",
            )
            extractor.fetchPage()

            extractor.initialPage.items
                .filterIsInstance<StreamInfoItem>()
                .map { it.toTrackResult() }
        }

    private fun StreamInfoItem.toTrackResult(): TrackResult {
        val thumbnailUrl = thumbnails.maxByOrNull { it.height }?.url
            ?: error("Nessuna thumbnail disponibile per \"$name\"")
        return TrackResult(title = name, thumbnailUrl = thumbnailUrl, pageUrl = url)
    }
}