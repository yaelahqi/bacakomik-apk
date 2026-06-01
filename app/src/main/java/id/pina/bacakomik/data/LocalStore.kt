package id.pina.bacakomik.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Lightweight persistence using SharedPreferences (no Room/db overhead).
 * - Favorites: list of MangaDetail-summary (slug, title, cover, type, theme, latestChapter)
 * - Last read: per-slug map { slug -> { chapterSlug, chapterNumber, ts } }
 */
class LocalStore(ctx: Context) {
    private val prefs: SharedPreferences =
        ctx.applicationContext.getSharedPreferences("pinakomik_store", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FAVORITES = "favorites_v1"
        private const val KEY_LASTREAD = "lastread_v1"
    }

    // ---------- Favorites ----------

    fun isFavorite(slug: String): Boolean {
        val arr = JSONArray(prefs.getString(KEY_FAVORITES, "[]") ?: "[]")
        for (i in 0 until arr.length()) {
            if (arr.getJSONObject(i).getString("slug") == slug) return true
        }
        return false
    }

    fun addFavorite(item: FavoriteItem) {
        val arr = JSONArray(prefs.getString(KEY_FAVORITES, "[]") ?: "[]")
        // remove if exists
        val newArr = JSONArray()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.getString("slug") != item.slug) newArr.put(o)
        }
        // prepend new
        val obj = JSONObject().apply {
            put("slug", item.slug)
            put("title", item.title)
            put("cover", item.cover)
            put("type", item.type)
            put("theme", item.theme)
            put("latestChapter", item.latestChapter)
            put("ts", System.currentTimeMillis())
        }
        val merged = JSONArray()
        merged.put(obj)
        for (i in 0 until newArr.length()) merged.put(newArr.getJSONObject(i))
        prefs.edit().putString(KEY_FAVORITES, merged.toString()).apply()
    }

    fun removeFavorite(slug: String) {
        val arr = JSONArray(prefs.getString(KEY_FAVORITES, "[]") ?: "[]")
        val newArr = JSONArray()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.getString("slug") != slug) newArr.put(o)
        }
        prefs.edit().putString(KEY_FAVORITES, newArr.toString()).apply()
    }

    fun listFavorites(): List<FavoriteItem> {
        val arr = JSONArray(prefs.getString(KEY_FAVORITES, "[]") ?: "[]")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            FavoriteItem(
                slug = o.getString("slug"),
                title = o.optString("title", ""),
                cover = o.optString("cover", ""),
                type = o.optString("type", ""),
                theme = o.optString("theme", ""),
                latestChapter = o.optString("latestChapter", "")
            )
        }
    }

    // ---------- Last read ----------

    fun setLastRead(mangaSlug: String, chapterSlug: String, chapterLabel: String) {
        val obj = JSONObject(prefs.getString(KEY_LASTREAD, "{}") ?: "{}")
        obj.put(mangaSlug, JSONObject().apply {
            put("chapterSlug", chapterSlug)
            put("chapterLabel", chapterLabel)
            put("ts", System.currentTimeMillis())
        })
        prefs.edit().putString(KEY_LASTREAD, obj.toString()).apply()
    }

    fun getLastRead(mangaSlug: String): LastRead? {
        val obj = JSONObject(prefs.getString(KEY_LASTREAD, "{}") ?: "{}")
        if (!obj.has(mangaSlug)) return null
        val o = obj.getJSONObject(mangaSlug)
        return LastRead(
            chapterSlug = o.getString("chapterSlug"),
            chapterLabel = o.optString("chapterLabel", ""),
            ts = o.optLong("ts", 0L)
        )
    }
}

data class FavoriteItem(
    val slug: String,
    val title: String,
    val cover: String,
    val type: String,
    val theme: String,
    val latestChapter: String
)

data class LastRead(
    val chapterSlug: String,
    val chapterLabel: String,
    val ts: Long
)
