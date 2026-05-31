package id.pina.bacakomik.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.pina.bacakomik.data.api.KomikApi
import id.pina.bacakomik.data.model.ListItem
import id.pina.bacakomik.data.repo.HistoryEntry
import id.pina.bacakomik.data.repo.HistoryRepo
import id.pina.bacakomik.data.repo.LibraryEntry
import id.pina.bacakomik.data.repo.LibraryRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ImportResult(
    val favoritesImported: Int = 0,
    val historyImported: Int = 0,
    val notFound: List<String> = emptyList(),
    val totalProcessed: Int = 0,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onBack: () -> Unit,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var status by remember { mutableStateOf("Pilih file database BacaKomik (.db) untuk import.") }
    var progress by remember { mutableIntStateOf(0) }
    var total by remember { mutableIntStateOf(0) }
    var isImporting by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<ImportResult?>(null) }
    var notFoundList by remember { mutableStateOf<List<String>>(emptyList()) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) {
            status = "Tidak ada file dipilih."
            return@rememberLauncherForActivityResult
        }
        isImporting = true
        status = "Membaca database..."
        scope.launch {
            try {
                val importResult = importFromDatabase(ctx, uri) { current, totalItems, msg ->
                    progress = current
                    total = totalItems
                    status = msg
                }
                result = importResult
                notFoundList = importResult.notFound
                status = "✅ Selesai! ${importResult.favoritesImported} favorit, " +
                    "${importResult.historyImported} riwayat diimport. " +
                    "${importResult.notFound.size} tidak ditemukan."
            } catch (e: Exception) {
                status = "❌ Error: ${e.message}"
            } finally {
                isImporting = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Data") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Import dari BacaKomik",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Pilih file database (.db) dari aplikasi BacaKomik lama untuk mengimpor favorit dan riwayat baca.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(
                        onClick = { filePickerLauncher.launch(arrayOf("application/octet-stream")) },
                        enabled = !isImporting,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Pilih File Database")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Status
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(status, style = MaterialTheme.typography.bodyMedium)
                    if (isImporting && total > 0) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.height(24.dp))
                            Text(
                                "  Importing $progress/$total...",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            // Not found list
            if (notFoundList.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Tidak Ditemukan (${notFoundList.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.height(200.dp)) {
                            items(notFoundList) { title ->
                                Text(
                                    "• $title",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(vertical = 2.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun importFromDatabase(
    context: Context,
    uri: Uri,
    onProgress: (current: Int, total: Int, message: String) -> Unit,
): ImportResult = withContext(Dispatchers.IO) {
    val tempFile = java.io.File(context.cacheDir, "import_${System.currentTimeMillis()}.db")
    try {
        // Copy URI content to temp file
        context.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw Exception("Gagal membuka file")

        val db = SQLiteDatabase.openDatabase(
            tempFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY
        )

        val favorites = mutableListOf<DbFavorite>()
        val histories = mutableListOf<DbHistory>()

        // Read favorites
        try {
            val cursor = db.rawQuery("SELECT title, type, url, image, score, lastupdatetime FROM favorit", null)
            cursor.use {
                while (it.moveToNext()) {
                    favorites.add(
                        DbFavorite(
                            title = it.getString(0) ?: "",
                            type = it.getString(1) ?: "Komik",
                            url = it.getString(2) ?: "",
                            image = it.getString(3) ?: "",
                            score = it.getString(4) ?: "",
                            lastUpdateTime = it.getString(5) ?: "",
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Table might not exist
        }

        // Read history
        try {
            val cursor = db.rawQuery("SELECT title, type, url, image, chapter, currenttime FROM history", null)
            cursor.use {
                while (it.moveToNext()) {
                    histories.add(
                        DbHistory(
                            title = it.getString(0) ?: "",
                            type = it.getString(1) ?: "Komik",
                            url = it.getString(2) ?: "",
                            image = it.getString(3) ?: "",
                            chapter = it.getString(4) ?: "",
                            currentTime = it.getString(5) ?: "",
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Table might not exist
        }

        db.close()

        val totalItems = favorites.size + histories.size
        var current = 0
        var favImported = 0
        var histImported = 0
        val notFound = mutableListOf<String>()

        // Import favorites
        for (fav in favorites) {
            current++
            onProgress(current, totalItems, "Mencari: ${fav.title}")
            try {
                val searchResult = KomikApi.search(fav.title)
                val matchedItem = searchResult.items.firstOrNull()
                if (matchedItem != null) {
                    LibraryRepo.addEntries(
                        context,
                        listOf(
                            LibraryEntry(
                                slug = matchedItem.slug,
                                title = matchedItem.title,
                                cover = matchedItem.cover,
                                type = matchedItem.type,
                            )
                        )
                    )
                    favImported++
                } else {
                    notFound.add(fav.title)
                }
            } catch (_: Exception) {
                notFound.add(fav.title)
            }
        }

        // Import history
        for (hist in histories) {
            current++
            onProgress(current, totalItems, "Mencari: ${hist.title}")
            try {
                val searchResult = KomikApi.search(hist.title)
                val matchedItem = searchResult.items.firstOrNull()
                if (matchedItem != null) {
                    HistoryRepo.addHistory(
                        context,
                        slug = matchedItem.slug,
                        title = matchedItem.title,
                        cover = matchedItem.cover,
                        type = matchedItem.type,
                        chapterSlug = hist.chapter,
                        chapterTitle = hist.chapter,
                    )
                    histImported++
                } else {
                    if (hist.title !in notFound) notFound.add(hist.title)
                }
            } catch (_: Exception) {
                if (hist.title !in notFound) notFound.add(hist.title)
            }
        }

        ImportResult(
            favoritesImported = favImported,
            historyImported = histImported,
            notFound = notFound,
            totalProcessed = totalItems,
        )
    } finally {
        tempFile.delete()
    }
}

private data class DbFavorite(
    val title: String,
    val type: String,
    val url: String,
    val image: String,
    val score: String,
    val lastUpdateTime: String,
)

private data class DbHistory(
    val title: String,
    val type: String,
    val url: String,
    val image: String,
    val chapter: String,
    val currentTime: String,
)
