package id.pina.bacakomik.ui.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import id.pina.bacakomik.BuildConfig
import id.pina.bacakomik.data.repo.HistoryRepo
import id.pina.bacakomik.data.repo.LibraryRepo
import id.pina.bacakomik.ui.theme.PinaNavy
import id.pina.bacakomik.ui.theme.PinaNavyCard
import id.pina.bacakomik.ui.theme.PinaRed
import id.pina.bacakomik.ui.theme.PinaTextPrimary
import id.pina.bacakomik.ui.theme.PinaTextSecondary
import id.pina.bacakomik.ui.theme.PinaTextMuted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.settingsDataStore by preferencesDataStore("settings")
private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
private val READING_MODE_KEY = intPreferencesKey("reading_mode")

object SettingsDataStore {
    val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    val READING_MODE_KEY = intPreferencesKey("reading_mode")

    suspend fun isDarkMode(context: Context): Boolean =
        context.settingsDataStore.data.first()[DARK_MODE_KEY] ?: false

    suspend fun getReadingMode(context: Context): Int =
        context.settingsDataStore.data.first()[READING_MODE_KEY] ?: 0

    suspend fun setDarkMode(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { it[DARK_MODE_KEY] = enabled }
    }

    suspend fun setReadingMode(context: Context, mode: Int) {
        context.settingsDataStore.edit { it[READING_MODE_KEY] = mode }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit, onImport: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var readingMode by remember { mutableStateOf(0) }
    var previewMode by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        readingMode = SettingsDataStore.getReadingMode(ctx)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PinaNavy)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
    ) {
        // ── Title ──
        Text(
            text = "Pengaturan",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
            color = PinaTextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        )

        // ── Profile card ──
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(PinaNavyCard)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(PinaRed),
                contentAlignment = Alignment.Center,
            ) {
                Text("P", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = "Pina Komik",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = PinaTextPrimary,
                )
                Text(
                    text = "pina.my.id",
                    style = MaterialTheme.typography.bodySmall,
                    color = PinaTextSecondary,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Konfigurasi section ──
        Text(
            text = "Konfigurasi",
            style = MaterialTheme.typography.titleMedium,
            color = PinaTextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PinaNavyCard),
        ) {
            // Bersihkan Cache
            SettingsRow(
                icon = Icons.Outlined.Refresh,
                title = "Bersihkan Cache",
                subtitle = "Menghapus cache tanpa menghapus favorit dan history",
                onClick = { /* cache clear */ },
            )

            // Backup Data
            SettingsRow(
                icon = Icons.Outlined.CloudUpload,
                title = "Back Up Data",
                subtitle = "Cadangkan favorit dan riwayat baca",
                onClick = { /* backup */ },
            )

            // Pulihkan Data
            SettingsRow(
                icon = Icons.Outlined.CloudDownload,
                title = "Pulihkan Data",
                subtitle = "Pulihkan favorit dan riwayat membaca",
                onClick = onImport,
            )

            // Preview Mode toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.Visibility,
                    contentDescription = null,
                    tint = PinaTextMuted,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Preview Mode", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = PinaTextPrimary)
                    Text("Aktifkan Preview mode untuk melihat preview komik", style = MaterialTheme.typography.bodySmall, color = PinaTextSecondary)
                }
                Switch(
                    checked = previewMode,
                    onCheckedChange = { previewMode = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = PinaRed),
                )
            }

            // Default mode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.PhoneAndroid,
                    contentDescription = null,
                    tint = PinaTextMuted,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Default", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = PinaTextPrimary)
                    Text("Pilih mode", style = MaterialTheme.typography.bodySmall, color = PinaTextSecondary)
                }
                Text(
                    text = if (readingMode == 0) "Scroll" else "Swipe",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PinaTextSecondary,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Support section ──
        Text(
            text = "Dukungan",
            style = MaterialTheme.typography.titleMedium,
            color = PinaTextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PinaNavyCard),
        ) {
            SettingsRowSimple(title = "Beri rating") { /* rate */ }
            SettingsRowSimple(title = "Lapor Bug dan Saran") { /* report */ }
        }

        Spacer(Modifier.height(20.dp))

        // ── Information ──
        Text(
            text = "Informasi",
            style = MaterialTheme.typography.titleMedium,
            color = PinaTextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PinaNavyCard),
        ) {
            // Version
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Pina Komik - v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PinaTextPrimary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "FREE!",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = PinaRed,
                )
            }

            // Divider
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF1E2335))
            )

            SettingsRowSimple(title = "Kebijakan Privasi") { /* privacy */ }
            SettingsRowSimple(title = "Terms & Conditions") { /* terms */ }
            SettingsRowSimple(title = "Tentang Kami") { /* about */ }
        }

        Spacer(Modifier.height(12.dp))

        // ── Logout button ──
        Text(
            text = "LOG OUT",
            color = PinaRed,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(PinaNavyCard)
                .clickable { /* logout */ }
                .padding(vertical = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Spacer(Modifier.height(16.dp))

        // Version at bottom
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodySmall,
            color = PinaTextMuted,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }

    // Clear data dialog
    if (showClearDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = PinaNavyCard,
            title = { Text("Hapus Semua Data?", color = PinaTextPrimary) },
            text = { Text("Semua data favorit dan riwayat baca akan dihapus.", color = PinaTextSecondary) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showClearDialog = false
                    scope.launch {
                        LibraryRepo.clear(ctx)
                        HistoryRepo.clear(ctx)
                    }
                }) {
                    Text("Hapus", color = PinaRed)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showClearDialog = false }) {
                    Text("Batal", color = PinaTextSecondary)
                }
            },
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = PinaTextMuted, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = PinaTextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = PinaTextSecondary)
        }
    }
}

@Composable
private fun SettingsRowSimple(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = PinaTextPrimary,
            modifier = Modifier.weight(1f),
        )
        Text("\u203A", style = MaterialTheme.typography.titleLarge, color = PinaTextMuted)
    }
}
