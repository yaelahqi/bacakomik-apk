package id.pina.bacakomik.data

import android.content.ContentResolver
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class BackupEntry(val title: String, val type: String, val chapter: String?)

data class ImportProgress(
    val phase: String,
    val current: Int,
    val total: Int,
    val matched: Int,
    val lastTitle: String
)

object ImportService {
    private const val BATCH_SIZE = 8

    fun copyUriToCache(ctx: Context, uri: Uri): File {
        val cr: ContentResolver = ctx.contentResolver
        val fileName = "import_backup_${System.currentTimeMillis()}.db"
        val out = File(ctx.cacheDir, fileName)
        cr.openInputStream(uri)?.use { input ->
            FileOutputStream(out).use { output ->
                input.copyTo(output)
            }
        } ?: throw Exception("Cannot read selected file")
        return out
    }

    fun readBackup(file: File): Pair<List<BackupEntry>, List<BackupEntry>> {
        val db = SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        val favs = mutableListOf<BackupEntry>()
        val hist = mutableListOf<BackupEntry>()
        try {
            db.rawQuery("SELECT title, type FROM favorit", null).use { c ->
                while (c.moveToNext()) {
                    val t = c.getString(0) ?: continue
                    val tp = c.getString(1) ?: ""
                    if (t.isNotBlank()) favs.add(BackupEntry(t, tp, null))
                }
            }
            try {
                db.rawQuery("SELECT title, type, chapter FROM history", null).use { c ->
                    while (c.moveToNext()) {
                        val t = c.getString(0) ?: continue
                        val tp = c.getString(1) ?: ""
                        val ch = c.getString(2)
                        if (t.isNotBlank()) hist.add(BackupEntry(t, tp, ch))
                    }
                }
            } catch (_: Exception) {}
        } finally {
            db.close()
        }
        return favs to hist
    }

    private fun keywords(title: String): String {
        val stop = setOf("the","a","an","of","and","with","to","in","on","is","my","i","ii","iii","s")
        val words = title.lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() && it !in stop && it.length > 1 }
        return words.take(3).joinToString(" ")
    }

    private suspend fun searchOne(entry: BackupEntry): Manga? = withContext(Dispatchers.IO) {
        val q = keywords(entry.title).ifBlank { entry.title }
        try {
            ApiService.search(q).firstOrNull()
        } catch (_: Exception) { null }
    }

    suspend fun runImport(
        ctx: Context,
        uri: Uri,
        store: LocalStore,
        onProgress: (ImportProgress) -> Unit
    ): Triple<Int, Int, Int> {
        val cacheFile = withContext(Dispatchers.IO) { copyUriToCache(ctx, uri) }
        try {
            val (favs, hist) = withContext(Dispatchers.IO) { readBackup(cacheFile) }
            val total = favs.size + hist.size
            var processed = 0
            var matchedFav = 0
            var matchedHist = 0
            onProgress(ImportProgress("reading", 0, total, 0, ""))

            // Process favorites in parallel batches
            favs.chunked(BATCH_SIZE).forEach { batch ->
                val results = coroutineScope {
                    batch.map { entry -> async { entry to searchOne(entry) } }.awaitAll()
                }
                for ((entry, match) in results) {
                    if (match != null) {
                        store.addFavorite(FavoriteItem(
                            slug = match.slug,
                            title = match.title,
                            cover = match.cover,
                            type = match.type,
                            theme = match.theme,
                            latestChapter = match.latestChapterTitle
                        ))
                        matchedFav++
                    }
                    processed++
                    onProgress(ImportProgress("matching", processed, total, matchedFav + matchedHist, entry.title))
                }
            }

            // Process history in parallel batches
            hist.chunked(BATCH_SIZE).forEach { batch ->
                val results = coroutineScope {
                    batch.map { entry -> async { entry to searchOne(entry) } }.awaitAll()
                }
                for ((entry, match) in results) {
                    if (match != null && entry.chapter != null) {
                        val chSlug = match.slug + "-chapter-" + entry.chapter
                        val chLabel = "Chapter " + entry.chapter
                        store.setLastRead(match.slug, chSlug, chLabel)
                        matchedHist++
                    }
                    processed++
                    onProgress(ImportProgress("matching", processed, total, matchedFav + matchedHist, entry.title))
                }
            }

            onProgress(ImportProgress("done", total, total, matchedFav + matchedHist, ""))
            return Triple(favs.size, hist.size, matchedFav + matchedHist)
        } finally {
            cacheFile.delete()
        }
    }
}
