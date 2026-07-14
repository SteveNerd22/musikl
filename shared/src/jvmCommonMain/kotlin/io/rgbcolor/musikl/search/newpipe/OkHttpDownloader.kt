package io.rgbcolor.musikl.search.newpipe

import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import okhttp3.Request as OkRequest

internal class OkHttpDownloader(
    private val client: OkHttpClient = OkHttpClient(),
) : Downloader() {

    override fun execute(request: Request): Response {
        val builder = OkRequest.Builder().url(request.url())
        request.headers().forEach { (name, values) ->
            values.forEach { value -> builder.addHeader(name, value) }
        }
        val body = request.dataToSend()?.toRequestBody()
        builder.method(request.httpMethod(), body)

        client.newCall(builder.build()).execute().use { response ->
            return Response(
                response.code,
                response.message,
                response.headers.toMultimap(),
                response.body.string(),
                response.request.url.toString(),
            )
        }
    }
}