package id.pina.bacakomik.data.repo

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.historyDataStore by preferencesDataStore("history")

@Serializable
data class HistoryEntry(
    val slug: String,
    val title: String,
    val cover: String? = null,
    val type: String = "Komik",
    val chapterSlug: String? = null,
    val chapterTitle: String? = null,
    val readAt: Long = System.currentTimeMillis(),
)

object HistoryRepo {
    private const val MAX_ENTRIES = 500
    private val KEY = stringPreferencesKey("entries")
    private val json = Json { ignoreUnknownKeys = true }

    fun observe(ctx: Context): Flow<List<HistoryEntry>> =
        ctx.historyDataStore.data.map { p ->
            p[KEY]?.let { runCatching { json.decodeFromString<List<HistoryEntry>>(it) }.getOrNull() }
                ?: emptyList()
        }

    suspend fun all(ctx: Context): List<HistoryEntry> = observe(ctx).first()

    suspend fun getHistory(ctx: Context): List<HistoryEntry> =
        all(ctx).sortedByDescending { it.readAt }

    suspend fun addHistory(
        ctx: Context,
        slug: String,
        title: String,
        cover: String?,
        type: String,
        chapterSlug: String?,
        chapterTitle: String?,
    ) {
        ctx.historyDataStore.edit { p ->
            val cur = p[KEY]?.let {
                runCatching { json.decodeFromString<List<HistoryEntry>>(it) }.getOrNull()
            } ?: emptyList()
            // Remove existing entry for same slug+chapter to deduplicate
            val filtered = cur.filterNot { it.slug == slug && it.chapterSlug == chapterSlug }
            val entry = HistoryEntry(
                slug = slug,
                title = title,
                cover = cover,
                type = type,
                chapterSlug = chapterSlug,
                chapterTitle = chapterTitle,
                readAt = System.currentTimeMillis(),
            )
            val next = (filtered + entry).sortedByDescending { it.readAt }.take(MAX_ENTRIES)
            p[KEY] = json.encodeToString(ListSerializer(HistoryEntry.serializer()), next)
        }
    }

    suspend fun removeHistory(ctx: Context, slug: String, chapterSlug: String?) {
        ctx.historyDataStore.edit { p ->
            val cur = p[KEY]?.let {
                runCatching { json.decodeFromString<List<HistoryEntry>>(it) }.getOrNull()
            } ?: emptyList()
            val next = cur.filterNot { it.slug == slug && it.chapterSlug == chapterSlug }
            p[KEY] = json.encodeToString(ListSerializer(HistoryEntry.serializer()), next)
        }
    }

    suspend fun removeBySlug(ctx: Context, slug: String) {
        ctx.historyDataStore.edit { p ->
            val cur = p[KEY]?.let {
                runCatching { json.decodeFromString<List<HistoryEntry>>(it) }.getOrNull()
            } ?: emptyList()
            val next = cur.filterNot { it.slug == slug }
            p[KEY] = json.encodeToString(ListSerializer(HistoryEntry.serializer()), next)
        }
    }

    suspend fun clear(ctx: Context) {
        ctx.historyDataStore.edit { it.clear() }
    }
}
