package id.pina.bacakomik.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
fun HomeScreen(onMangaClick: (String) -> Unit) {
    var mangaList by remember { mutableStateOf<List<Manga>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun load() {
        error = null
        isLoading = true
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

    LaunchedEffect(Unit) { load() }

    Column(modifier = Modifier.fillMaxSize().background(PinaNavy)) {
        Text(
            "🏠 Komik Update",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PinaRed)
            }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("❌ Error", fontSize = 18.sp, color = PinaRed)
                    Spacer(Modifier.height(8.dp))
                    Text(error ?: "", fontSize = 14.sp, color = PinaGray)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { load() }, colors = ButtonDefaults.buttonColors(containerColor = PinaRed)) {
                        Text("Retry")
                    }
                }
            }
            else -> MangaGrid(mangaList, onMangaClick)
        }
    }
}

@Composable
fun ExploreScreen(onMangaClick: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Manga>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(PinaNavy)) {
        Text(
            "🔍 Explore",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )

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
                }) { Icon(Icons.Filled.Search, "Search", tint = PinaRed) }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PinaRed,
                unfocusedBorderColor = PinaGray,
                cursorColor = PinaRed,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PinaRed)
            }
            hasSearched && results.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No results found", color = PinaGray, fontSize = 16.sp)
            }
            !hasSearched -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Type a manga name to search", color = PinaGray, fontSize = 14.sp)
            }
            else -> MangaGrid(results, onMangaClick)
        }
    }
}

@Composable
fun MangaGrid(items: List<Manga>, onMangaClick: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items, key = { it.slug }) { manga ->
            PosterCard(manga, onClick = { onMangaClick(manga.slug) })
        }
    }
}

@Composable
fun PosterCard(manga: Manga, onClick: () -> Unit) {
    val typeColor = when (manga.type) {
        "Manhwa" -> Color(0xFFE63946)
        "Manga" -> Color(0xFF4EA8DE)
        "Manhua" -> Color(0xFF43AA8B)
        else -> PinaGray
    }

    Column(modifier = Modifier.clickable { onClick() }) {
        // Poster cover with type badge overlay
        Box {
            AsyncImage(
                model = manga.cover,
                contentDescription = manga.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PinaNavyCard)
            )
            // Bottom dark gradient + chapter overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                        ),
                        RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(
                    manga.latestChapterTitle.replace("Chapter ", "Ch "),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // Type badge top-left
            Text(
                manga.type,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .background(typeColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
            // UpCount badge top-right
            if (!manga.upCount.isNullOrEmpty()) {
                Text(
                    "🆕",
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(PinaRed, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            manga.title,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 13.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(
            manga.theme,
            color = PinaGray,
            fontSize = 9.sp,
            maxLines = 1
        )
    }
}
