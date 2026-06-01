package id.pina.bacakomik.data

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ApiService {
    private const val BASE_URL = "https://komik.pina.my.id/api/v1"
    private const val USER_AGENT = "Mozilla/5.0"

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

    fun fetchList(): List<Manga> {
        val conn = URL("$BASE_URL/list").openConnection() as HttpURLConnection
        conn.setRequestProperty("User-Agent", USER_AGENT)
        conn.connectTimeout = 15_000
        conn.readTimeout = 15_000
        val json = conn.inputStream.bufferedReader().readText()
        conn.disconnect()
        return parseItems(json)
    }

    fun search(query: String): List<Manga> {
        val conn = URL("$BASE_URL/search?q=$query").openConnection() as HttpURLConnection
        conn.setRequestProperty("User-Agent", USER_AGENT)
        conn.connectTimeout = 15_000
        conn.readTimeout = 15_000
        val json = conn.inputStream.bufferedReader().readText()
        conn.disconnect()
        return parseItems(json)
    }
}
