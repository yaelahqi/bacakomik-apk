package id.pina.bacakomik.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import id.pina.bacakomik.data.ApiService
import id.pina.bacakomik.data.Manga
import id.pina.bacakomik.PinaNavy
import id.pina.bacakomik.PinaNavyCard
import id.pina.bacakomik.PinaRed
import id.pina.bacakomik.PinaGray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen() {
    var mangaList by remember { mutableStateOf<List<Manga>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                mangaList = withContext(Dispatchers.IO) { ApiService.fetchList() }
                isLoading = false
            } catch (e: Exception) {
                error = e.message ?: "Unknown error"
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PinaNavy)
            .padding(top = 16.dp)
    ) {
        // Header
        Text(
            text = "🏠 Home",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PinaRed)
                }
            }
            error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("❌ Error", fontSize = 18.sp, color = PinaRed)
                        Spacer(Modifier.height(8.dp))
                        Text(error ?: "", fontSize = 14.sp, color = PinaGray)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {
                            error = null
                            isLoading = true
                            scope.launch {
                                try {
                                    mangaList = withContext(Dispatchers.IO) { ApiService.fetchList() }
                                    isLoading = false
                                } catch (e: Exception) {
                                    error = e.message
                                    isLoading = false
                                }
                            }
                        }, colors = ButtonDefaults.buttonColors(containerColor = PinaRed)) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(mangaList) { manga ->
                        MangaCard(manga)
                    }
                }
            }
        }
    }
}

@Composable
fun ExploreScreen() {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Manga>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PinaNavy)
            .padding(top = 16.dp)
    ) {
        // Header
        Text(
            text = "🔍 Explore",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Search bar
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search manga...", color = PinaGray) },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = {
                    if (query.isNotBlank()) {
                        isLoading = true
                        hasSearched = true
                        scope.launch {
                            try {
                                results = withContext(Dispatchers.IO) {
                                    ApiService.search(query.trim())
                                }
                                isLoading = false
                            } catch (e: Exception) {
                                results = emptyList()
                                isLoading = false
                            }
                        }
                    }
                }) {
                    Icon(Icons.Filled.Search, "Search", tint = PinaRed)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PinaRed,
                unfocusedBorderColor = PinaGray,
                cursorColor = PinaRed,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PinaRed)
                }
            }
            hasSearched && results.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No results found", color = PinaGray, fontSize = 16.sp)
                }
            }
            !hasSearched -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Type a manga name to search", color = PinaGray, fontSize = 14.sp)
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(results) { manga ->
                        MangaCard(manga)
                    }
                }
            }
        }
    }
}

@Composable
fun MangaCard(manga: Manga) {
    val typeColor = when (manga.type) {
        "Manhwa" -> Color(0xFFE63946)
        "Manga" -> Color(0xFF4EA8DE)
        "Manhua" -> Color(0xFF43AA8B)
        else -> PinaGray
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = PinaNavyCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover image
            AsyncImage(
                model = manga.cover,
                contentDescription = manga.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 90.dp, height = 60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2A2D3A))
            )

            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = manga.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                // Type chip + Theme
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = manga.type,
                        fontSize = 10.sp,
                        color = typeColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(typeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = manga.theme,
                        fontSize = 10.sp,
                        color = PinaGray
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Chapter + UpCount
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = manga.latestChapterTitle,
                        fontSize = 11.sp,
                        color = PinaGray
                    )
                    if (!manga.upCount.isNullOrEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "🆕 ${manga.upCount}",
                            fontSize = 10.sp,
                            color = PinaRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
