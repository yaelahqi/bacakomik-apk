package id.pina.bacakomik.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.pina.bacakomik.PinaGray
import id.pina.bacakomik.PinaNavy
import id.pina.bacakomik.PinaNavyCard
import id.pina.bacakomik.PinaRed
import id.pina.bacakomik.data.ImportProgress
import id.pina.bacakomik.data.ImportService
import id.pina.bacakomik.data.LocalStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ImportPhase {
    object Idle : ImportPhase()
    object Running : ImportPhase()
    data class Done(val favsTotal: Int, val histTotal: Int, val matched: Int) : ImportPhase()
    data class Error(val message: String) : ImportPhase()
}

@Composable
fun MeScreen() {
    val ctx = LocalContext.current
    val store = remember { LocalStore(ctx) }
    val scope = rememberCoroutineScope()
    var favCount by remember { mutableStateOf(store.listFavorites().size) }
    var importState by remember { mutableStateOf<ImportPhase>(ImportPhase.Idle) }
    var importProgress by remember { mutableStateOf<ImportProgress?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        importState = ImportPhase.Running
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    ImportService.runImport(ctx, uri, store) { progress ->
                        importProgress = progress
                    }
                }
                importState = ImportPhase.Done(result.first, result.second, result.third)
                favCount = store.listFavorites().size
            } catch (e: Exception) {
                importState = ImportPhase.Error(e.message ?: "Unknown error")
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(PinaNavy).padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("\uD83D\uDC64", fontSize = 48.sp)
        Spacer(Modifier.height(8.dp))
        Text("Pina Komik", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(2.dp))
        Text("v2.2.0", fontSize = 13.sp, color = PinaGray)
        Spacer(Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = PinaNavyCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$favCount", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Favorit", color = PinaGray, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("3", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Tipe", color = PinaGray, fontSize = 11.sp)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        when (val st = importState) {
            is ImportPhase.Idle -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PinaNavyCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Import dari BacaKomik.my", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text("Pilih file backup .db dari BacaKomik.my", color = PinaGray, fontSize = 11.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "File > Backup > backup.db > Pilih",
                            color = PinaGray,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .background(Color(0xFF22263A), shape = MaterialTheme.shapes.small)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { filePicker.launch(arrayOf("application/*", "application/octet-stream", "*/*")) },
                            colors = ButtonDefaults.buttonColors(containerColor = PinaRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Pilih backup.db", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            is ImportPhase.Running -> {
                val p = importProgress
                Card(
                    colors = CardDefaults.cardColors(containerColor = PinaNavyCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Importing...", color = PinaRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        if (p != null) {
                            val pct = if (p.total > 0) p.current.toFloat() / p.total else 0f
                            LinearProgressIndicator(
                                progress = { pct },
                                color = PinaRed,
                                trackColor = Color(0xFF22263A),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(6.dp))
                            val progressText = "Matched ${p.matched} / ${p.total}"
                            Text(progressText, color = PinaGray, fontSize = 11.sp)
                            if (p.lastTitle.isNotBlank()) {
                                Text(
                                    p.lastTitle,
                                    color = PinaGray, fontSize = 10.sp,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        } else {
                            CircularProgressIndicator(color = PinaRed, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            is ImportPhase.Done -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PinaNavyCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Import selesai!", color = Color(0xFF43AA8B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        val favText = "Favorit: ${st.favsTotal} total"
                        Text(favText, color = Color.White, fontSize = 12.sp)
                        val histText = "History: ${st.histTotal} total, ${st.matched} matched"
                        Text(histText, color = PinaGray, fontSize = 11.sp)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { importState = ImportPhase.Idle },
                            colors = ButtonDefaults.buttonColors(containerColor = PinaNavyCard),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Import lagi", color = PinaRed, fontSize = 12.sp)
                        }
                    }
                }
            }

            is ImportPhase.Error -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PinaNavyCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Import gagal", color = PinaRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(st.message, color = PinaGray, fontSize = 11.sp)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { importState = ImportPhase.Idle },
                            colors = ButtonDefaults.buttonColors(containerColor = PinaRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Coba lagi", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        val helpText = "Cara import: Buka BacaKomik.my > Profil > Backup > pilih file backup.db"
        Text(helpText, color = PinaGray, fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}
