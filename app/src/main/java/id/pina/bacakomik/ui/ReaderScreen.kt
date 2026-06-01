package id.pina.bacakomik.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import id.pina.bacakomik.PinaGray
import id.pina.bacakomik.PinaNavy
import id.pina.bacakomik.PinaNavyLight
import id.pina.bacakomik.PinaRed
import id.pina.bacakomik.data.ApiService
import id.pina.bacakomik.data.ChapterRead
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    chapterSlug: String,
    onBack: () -> Unit
) {
    var chapter by remember { mutableStateOf<ChapterRead?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(chapterSlug) {
        scope.launch {
            try {
                chapter = withContext(Dispatchers.IO) { ApiService.fetchChapter(chapterSlug) }
                isLoading = false
            } catch (e: Exception) {
                error = e.message
                isLoading = false
            }
        }
    }

    Scaffold(
        containerColor = PinaNavy,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        chapter?.title ?: "Loading...",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PinaNavyLight)
            )
        }
    ) { padding ->
        when {
            isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = PinaRed) }

            error != null -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("❌ Failed to load", color = PinaRed, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(error ?: "", color = PinaGray, fontSize = 12.sp)
                }
            }

            chapter != null -> {
                val imgs = chapter!!.images
                if (imgs.isEmpty()) {
                    Box(
                        Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) { Text("No pages", color = PinaGray) }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .background(Color.Black)
                    ) {
                        items(imgs) { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = "page",
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black)
                            )
                        }
                        item {
                            Text(
                                "— End of chapter —",
                                color = PinaGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
