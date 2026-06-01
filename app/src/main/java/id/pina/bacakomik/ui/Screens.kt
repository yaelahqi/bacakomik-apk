package id.pina.bacakomik.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
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

private val FILTER_TYPES = listOf("All", "Manhwa", "Manga", "Manhua")
// GENRE_LIST moved to ApiService.GENRES

@Composable
fun HomeScreen(onMangaClick: (String) -> Unit) {
    var mangaList by remember { mutableStateOf<List<Manga>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf("All") }
    var currentPage by remember { mutableStateOf(1) }
    var hasMore by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    fun loadInitial(type: String) {
        error = null
        isLoading = true
        currentPage = 1
        hasMore = true
        scope.launch {
            try {
                val items = withContext(Dispatchers.IO) {
                    ApiService.fetchList(page = 1, type = type)
                }
                mangaList = items
                hasMore = items.isNotEmpty()
                isLoading = false
            } catch (e: Exception) {
                error = e.message ?: "Unknown error"
                isLoading = false
            }
        }
    }

    fun loadMore() {
        if (isLoadingMore || !hasMore || isLoading) return
        isLoadingMore = true
        scope.launch {
            try {
                val nextPage = currentPage + 1
                val items = withContext(Dispatchers.IO) {
                    ApiService.fetchList(page = nextPage, type = selectedType)
                }
                if (items.isEmpty()) {
                    hasMore = false
                } else {
                    val existing = mangaList.map { it.slug }.toSet()
                    val newItems = items.filter { it.slug !in existing }
                    mangaList = mangaList + newItems
                    currentPage = nextPage
                    if (newItems.isEmpty()) hasMore = false
                }
                isLoadingMore = false
            } catch (e: Exception) {
                isLoadingMore = false
            }
        }
    }

    LaunchedEffect(selectedType) { loadInitial(selectedType) }

    // Auto-load when scrolled near end
    LaunchedEffect(gridState, mangaList.size, hasMore, isLoadingMore) {
        snapshotFlow {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible to mangaList.size
        }.collect { (lastVisible, total) ->
            if (total > 0 && lastVisible >= total - 6 && hasMore && !isLoadingMore) {
                loadMore()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(PinaNavy)) {
        Text(
            "🏠 Komik Update",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        FilterChipsRow(
            types = FILTER_TYPES,
            selected = selectedType,
            onSelect = { selectedType = it }
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
                    Button(
                        onClick = { loadInitial(selectedType) },
                        colors = ButtonDefaults.buttonColors(containerColor = PinaRed)
                    ) { Text("Retry") }
                }
            }
            else -> MangaGrid(
                items = mangaList,
                gridState = gridState,
                isLoadingMore = isLoadingMore,
                onMangaClick = onMangaClick
            )
        }
    }
}

@Composable
fun FilterChipsRow(types: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        lazyRowItems(types) { type ->
            val active = type == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (active) PinaRed else PinaNavyCard)
                    .clickable { onSelect(type) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    type,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}


private val STATUS_LIST = listOf("All", "Ongoing", "Completed")
private val TYPE_LIST = listOf("All", "Manga", "Manhwa", "Manhua")

data class FilterState(
    val status: String = "All",
    val type: String = "All",
    val genre: String = "Semua"
)

@Composable
fun FilterBottomSheet(
    initial: FilterState,
    onApply: (FilterState) -> Unit,
    onDismiss: () -> Unit
) {
    var status by remember { mutableStateOf(initial.status) }
    var type by remember { mutableStateOf(initial.type) }
    var genre by remember { mutableStateOf(initial.genre) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1D27),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Saring", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                IconButton(onClick = onDismiss) {
                    Text("\u2715", fontSize = 20.sp, color = PinaGray)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Status
            Text("Status", color = PinaGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                lazyRowItems(STATUS_LIST) { s ->
                    val active = s == status
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (active) PinaRed else PinaNavyCard)
                            .clickable { status = s }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(s, color = Color.White, fontSize = 13.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Type
            Text("Type", color = PinaGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                lazyRowItems(TYPE_LIST) { t ->
                    val active = t == type
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (active) PinaRed else PinaNavyCard)
                            .clickable { type = t }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(t, color = Color.White, fontSize = 13.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Genre grid
            Text("Genre", color = PinaGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ApiService.GENRES.forEach { g ->
                    val active = g == genre
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (active) PinaRed else PinaNavyCard)
                            .clickable { genre = g }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(g, color = Color.White, fontSize = 12.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    onApply(FilterState(status = status, type = type, genre = genre))
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PinaRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Filter", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ExploreScreen(onMangaClick: (String) -> Unit) {
    var filterState by remember { mutableStateOf(FilterState()) }
    var showFilter by remember { mutableStateOf(false) }
    val hasActiveFilter = filterState.status != "All" || filterState.type != "All" || filterState.genre != "Semua"
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Manga>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var selectedGenre by remember { mutableStateOf("Semua") }
    var currentPage by remember { mutableStateOf(1) }
    var hasMore by remember { mutableStateOf(true) }
    var browseMode by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    fun runSearch() {
        if (query.isBlank()) return
        isLoading = true
        hasSearched = true
        browseMode = false
        scope.launch {
            try {
                results = withContext(Dispatchers.IO) { ApiService.search(query.trim()) }
                isLoading = false
            } catch (e: Exception) {
                results = emptyList()
                isLoading = false
            }
        }
    }

    fun applyFilter(fs: FilterState) {
        filterState = fs
        selectedGenre = fs.genre
        currentPage = 1
        hasMore = true
        isLoading = true
        hasSearched = true
        browseMode = true
        query = ""
        scope.launch {
            try {
                val items = withContext(Dispatchers.IO) {
                    ApiService.fetchList(
                        page = 1,
                        type = if (fs.type == "All") null else fs.type,
                        genre = if (fs.genre == "Semua") null else fs.genre,
                        status = if (fs.status == "All") null else fs.status
                    )
                }
                results = items
                hasMore = items.isNotEmpty()
                isLoading = false
            } catch (e: Exception) {
                results = emptyList()
                isLoading = false
            }
        }
    }

    fun loadGenre(genre: String) {
        selectedGenre = genre
        filterState = filterState.copy(genre = genre)
        browseMode = true
        hasSearched = true
        isLoading = true
        currentPage = 1
        hasMore = true
        scope.launch {
            try {
                val items = withContext(Dispatchers.IO) {
                    ApiService.fetchList(
                        page = 1,
                        type = if (filterState.type == "All") null else filterState.type,
                        genre = if (genre == "Semua") null else genre,
                        status = if (filterState.status == "All") null else filterState.status
                    )
                }
                results = items
                hasMore = items.isNotEmpty()
                isLoading = false
            } catch (e: Exception) {
                results = emptyList()
                isLoading = false
            }
        }
    }

    fun loadMore() {
        if (isLoadingMore || !hasMore || isLoading || !browseMode) return
        isLoadingMore = true
        scope.launch {
            try {
                val nextPage = currentPage + 1
                val items = withContext(Dispatchers.IO) {
                    ApiService.fetchList(
                        page = nextPage,
                        type = if (filterState.type == "All") null else filterState.type,
                        genre = if (selectedGenre == "Semua") null else selectedGenre,
                        status = if (filterState.status == "All") null else filterState.status
                    )
                }
                if (items.isEmpty()) {
                    hasMore = false
                } else {
                    val existing = results.map { it.slug }.toSet()
                    val newItems = items.filter { it.slug !in existing }
                    results = results + newItems
                    currentPage = nextPage
                    if (newItems.isEmpty()) hasMore = false
                }
                isLoadingMore = false
            } catch (e: Exception) {
                isLoadingMore = false
            }
        }
    }

    LaunchedEffect(gridState, results.size, hasMore, isLoadingMore, browseMode) {
        snapshotFlow {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible to results.size
        }.collect { (lastVisible, total) ->
            if (browseMode && total > 0 && lastVisible >= total - 6 && hasMore && !isLoadingMore) {
                loadMore()
            }
        }
    }

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
            placeholder = { Text("Search by name...", color = PinaGray) },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { runSearch() }) {
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(8.dp))

        // Filter button row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (hasActiveFilter) {
                val parts = mutableListOf<String>()
                if (filterState.status != "All") parts.add(filterState.status)
                if (filterState.type != "All") parts.add(filterState.type)
                if (filterState.genre != "Semua") parts.add(filterState.genre)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PinaRed.copy(alpha = 0.2f))
                        .clickable { showFilter = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        "\uD83C\uDFA2 ${parts.joinToString(" \u00B7 ")}",
                        color = PinaRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (hasActiveFilter) PinaRed else PinaNavyCard)
                    .clickable { showFilter = true }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    "\uD83C\uDFA2 Saring",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (showFilter) {
            FilterBottomSheet(
                initial = filterState,
                onApply = { applyFilter(it) },
                onDismiss = { showFilter = false }
            )
        }


        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PinaRed)
            }
            hasSearched && results.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tidak ada hasil", color = PinaGray, fontSize = 16.sp)
            }
            !hasSearched -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Cari berdasarkan nama", color = PinaGray, fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("atau gunakan tombol Saring", color = PinaGray, fontSize = 12.sp)
                }
            }
            else -> MangaGrid(
                items = results,
                gridState = gridState,
                isLoadingMore = isLoadingMore,
                onMangaClick = onMangaClick
            )
        }
    }
}

@Composable
fun MangaGrid(
    items: List<Manga>,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState = rememberLazyGridState(),
    isLoadingMore: Boolean = false,
    onMangaClick: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = gridState,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items, key = { it.slug }) { manga ->
            PosterCard(manga, onClick = { onMangaClick(manga.slug) })
        }
        if (isLoadingMore) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = PinaRed) }
            }
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
