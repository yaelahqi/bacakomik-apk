package id.pina.bacakomik.data

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object ApiService {
    private const val BASE_URL = "https://komik.pina.my.id/api/v1"
    private const val USER_AGENT = "Mozilla/5.0"

    private fun get(path: String): String {
        val conn = URL("$BASE_URL$path").openConnection() as HttpURLConnection
        conn.setRequestProperty("User-Agent", USER_AGENT)
        conn.connectTimeout = 15_000
        conn.readTimeout = 20_000
        val json = conn.inputStream.bufferedReader().readText()
        conn.disconnect()
        return json
    }

    private fun parseItems(json: String): List<Manga> {
        val obj = JSONObject(json)
        val arr = obj.getJSONArray("items")
        return (0 until arr.length()).map { i ->
            val item = arr.getJSONObject(i)
            Manga(
                slug = item.getString("slug"),
                title = item.getString("title"),
                cover = item.getString("cover"),
                type = item.getString("type"),
                theme = item.getString("theme"),
                upCount = item.optString("upCount", ""),
                url = item.getString("url"),
                latestChapterTitle = item.getString("latestChapterTitle"),
                latestChapterUrl = item.getString("latestChapterUrl"),
                totalChapters = item.getString("totalChapters")
            )
        }
    }

    fun fetchList(): List<Manga> = parseItems(get("/list"))

    fun search(query: String): List<Manga> {
        val q = URLEncoder.encode(query, "UTF-8")
        return parseItems(get("/search?q=$q"))
    }

    fun fetchManga(slug: String): MangaDetail {
        val json = get("/manga/$slug")
        val obj = JSONObject(json)
        if (obj.has("error")) throw Exception(obj.getString("error"))

        val genresArr = obj.optJSONArray("genres")
        val genres = if (genresArr != null) {
            (0 until genresArr.length()).map { genresArr.getString(it) }
        } else emptyList()

        val chaptersArr = obj.optJSONArray("chapters")
        val chapters = if (chaptersArr != null) {
            (0 until chaptersArr.length()).map { i ->
                val c = chaptersArr.getJSONObject(i)
                ChapterItem(
                    number = c.optString("number", ""),
                    title = c.optString("title", ""),
                    url = c.optString("url", ""),
                    slug = c.optString("slug", ""),
                    date = c.optString("date", "")
                )
            }
        } else emptyList()

        return MangaDetail(
            slug = obj.getString("slug"),
            title = obj.optString("title", ""),
            altTitle = obj.optString("altTitle", ""),
            cover = obj.optString("cover", ""),
            description = obj.optString("description", ""),
            type = obj.optString("type", ""),
            status = obj.optString("status", ""),
            rating = obj.optString("rating", ""),
            author = obj.optString("author", ""),
            genres = genres,
            theme = obj.optString("theme", ""),
            views = obj.optString("views", ""),
            chapters = chapters
        )
    }

    fun fetchChapter(chapterSlug: String): ChapterRead {
        val json = get("/chapter/$chapterSlug")
        val obj = JSONObject(json)
        if (obj.has("error")) throw Exception(obj.getString("error"))

        val imgArr = obj.optJSONArray("images")
        val images = if (imgArr != null) {
            (0 until imgArr.length()).map { imgArr.getString(it) }
        } else emptyList()

        return ChapterRead(
            chapterSlug = obj.optString("chapterSlug", chapterSlug),
            title = obj.optString("title", ""),
            mangaSlug = obj.optString("mangaSlug", ""),
            images = images
        )
    }
}
