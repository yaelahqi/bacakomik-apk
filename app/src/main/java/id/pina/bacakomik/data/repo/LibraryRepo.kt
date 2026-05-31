package id.pina.bacakomik.data.repo

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import id.pina.bacakomik.data.model.ListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore("library")

@Serializable
data class LibraryEntry(
    val slug: String,
    val title: String,
    val cover: String? = null,
    val type: String = "Komik",
    val savedAt: Long = System.currentTimeMillis(),
    val lastChapterSlug: String? = null,
    val lastChapterTitle: String? = null,
    val lastReadAt: Long = 0L,
)

object LibraryRepo {
    private val KEY = stringPreferencesKey("entries")
    private val json = Json { ignoreUnknownKeys = true }

    fun observe(ctx: Context): Flow<List<LibraryEntry>> =
        ctx.dataStore.data.map { p ->
            p[KEY]?.let { runCatching { json.decodeFromString<List<LibraryEntry>>(it) }.getOrNull() }
                ?: emptyList()
        }

    suspend fun all(ctx: Context): List<LibraryEntry> = observe(ctx).first()

    suspend fun toggle(ctx: Context, item: ListItem): Boolean {
        var added = false
        ctx.dataStore.edit { p ->
            val cur = p[KEY]?.let {
                runCatching { json.decodeFromString<List<LibraryEntry>>(it) }.getOrNull()
            } ?: emptyList()
            val exists = cur.any { it.slug == item.slug }
            val next = if (exists) {
                cur.filterNot { it.slug == item.slug }
            } else {
                added = true
                cur + LibraryEntry(
                    slug = item.slug,
                    title = item.title,
                    cover = item.cover,
                    type = item.type,
                )
            }
            p[KEY] = json.encodeToString(ListSerializer(LibraryEntry.serializer()), next)
        }
        return added
    }

    suspend fun isSaved(ctx: Context, slug: String): Boolean = all(ctx).any { it.slug == slug }

    suspend fun markRead(ctx: Context, mangaSlug: String, chapterSlug: String, title: String) {
        ctx.dataStore.edit { p ->
            val cur = p[KEY]?.let {
                runCatching { json.decodeFromString<List<LibraryEntry>>(it) }.getOrNull()
            } ?: emptyList()
            val exists = cur.any { it.slug == mangaSlug }
            if (!exists) return@edit
            val next = cur.map {
                if (it.slug == mangaSlug)
                    it.copy(
                        lastChapterSlug = chapterSlug,
                        lastChapterTitle = title,
                        lastReadAt = System.currentTimeMillis(),
                    )
                else it
            }
            p[KEY] = json.encodeToString(ListSerializer(LibraryEntry.serializer()), next)
        }
    }

    suspend fun addEntries(ctx: Context, entries: List<LibraryEntry>) {
        ctx.dataStore.edit { p ->
            val cur = p[KEY]?.let {
                runCatching { json.decodeFromString<List<LibraryEntry>>(it) }.getOrNull()
            } ?: emptyList()
            val slugs = entries.map { it.slug }.toSet()
            val filtered = cur.filter { it.slug !in slugs }
            p[KEY] = json.encodeToString(ListSerializer(LibraryEntry.serializer()), filtered + entries)
        }
    }

    suspend fun clear(ctx: Context) {
        ctx.dataStore.edit { it.clear() }
    }
}
