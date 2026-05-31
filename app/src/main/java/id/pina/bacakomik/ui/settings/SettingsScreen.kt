package id.pina.bacakomik.ui.settings

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import id.pina.bacakomik.BuildConfig
import id.pina.bacakomik.data.repo.HistoryRepo
import id.pina.bacakomik.data.repo.LibraryRepo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.settingsDataStore by preferencesDataStore("settings")
private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
private val READING_MODE_KEY = intPreferencesKey("reading_mode") // 0=vertical, 1=horizontal

object SettingsDataStore {
    val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    val READING_MODE_KEY = intPreferencesKey("reading_mode")

    suspend fun isDarkMode(context: Context): Boolean {
        val prefs = context.settingsDataStore.data.first()
        return prefs[DARK_MODE_KEY] ?: false
    }

    suspend fun getReadingMode(context: Context): Int {
        val prefs = context.settingsDataStore.data.first()
        return prefs[READING_MODE_KEY] ?: 0
    }

    suspend fun setDarkMode(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { it[DARK_MODE_KEY] = enabled }
    }

    suspend fun setReadingMode(context: Context, mode: Int) {
        context.settingsDataStore.edit { it[READING_MODE_KEY] = mode }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onImport: () -> Unit,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var darkMode by remember { mutableStateOf(false) }
    var readingMode by remember { mutableStateOf(0) } // 0=vertical, 1=horizontal
    var showClearDialog by remember { mutableStateOf(false) }

    // Load prefs
    androidx.compose.runtime.LaunchedEffect(Unit) {
        darkMode = SettingsDataStore.isDarkMode(ctx)
        readingMode = SettingsDataStore.getReadingMode(ctx)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan") },
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // Section 1: Umum
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Umum",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Mode Gelap", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Gunakan tema gelap",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = darkMode,
                            onCheckedChange = { enabled ->
                                darkMode = enabled
                                scope.launch { SettingsDataStore.setDarkMode(ctx, enabled) }
                            },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Versi Aplikasi", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            BuildConfig.VERSION_NAME,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Section 2: Reader
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Reader",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Mode Baca", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                if (readingMode == 0) "Vertikal (scroll)" else "Horizontal (swipe)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = readingMode == 1,
                            onCheckedChange = { horizontal ->
                                readingMode = if (horizontal) 1 else 0
                                scope.launch { SettingsDataStore.setReadingMode(ctx, readingMode) }
                            },
                        )
                    }
                }
            }

            // Section 3: Data
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Data",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(
                        onClick = onImport,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Import dari Pina Komik (.db)")
                    }
                    Spacer(Modifier.height(4.dp))
                    TextButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text("Hapus semua data")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showClearDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Hapus Semua Data?") },
            text = { Text("Semua data favorit dan riwayat baca akan dihapus. Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    scope.launch {
                        LibraryRepo.clear(ctx)
                        HistoryRepo.clear(ctx)
                    }
                }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Batal")
                }
            },
        )
    }
}
