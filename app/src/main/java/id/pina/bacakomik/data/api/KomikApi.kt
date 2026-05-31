package id.pina.bacakomik.data.api

import id.pina.bacakomik.BuildConfig
import id.pina.bacakomik.data.model.ChapterPages
import id.pina.bacakomik.data.model.ListResponse
import id.pina.bacakomik.data.model.MangaDetail
import id.pina.bacakomik.data.model.SearchResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KomikApi {
    private const val BASE = BuildConfig.API_BASE
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            requestTimeoutMillis = 25_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 25_000
        }
    }

    suspend fun list(
        page: Int = 1,
        order: String = "update",
        type: String = "all",
        genre: String? = null,
    ): ListResponse = client.get("$BASE/api/v1/list") {
        parameter("page", page)
        parameter("order", order)
        parameter("type", type)
        if (genre != null) parameter("genre", genre)
    }.body()

    suspend fun search(query: String): SearchResponse =
        client.get("$BASE/api/v1/search") { parameter("q", query) }.body()

    suspend fun manga(slug: String): MangaDetail =
        client.get("$BASE/api/v1/manga/$slug").body()

    suspend fun chapter(slug: String): ChapterPages =
        client.get("$BASE/api/v1/chapter/$slug").body()

    /** Proxy CDN images through our origin to bypass referer checks. */
    fun imageUrl(remote: String?): String? {
        if (remote.isNullOrBlank()) return null
        return "$BASE/api/img?u=" + java.net.URLEncoder.encode(remote, "UTF-8")
    }
}
