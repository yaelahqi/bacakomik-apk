package id.pina.bacakomik.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import id.pina.bacakomik.PinaGray
import id.pina.bacakomik.PinaNavy
import id.pina.bacakomik.PinaNavyCard
import id.pina.bacakomik.PinaRed
import id.pina.bacakomik.data.FavoriteItem
import id.pina.bacakomik.data.HistoryItem
import id.pina.bacakomik.data.LocalStore
import id.pina.bacakomik.data.Manga
import java.text.SimpleDateFormat
import java.util.*

private enum class FavTab(val label: String) { FAVORIT("Favorit"), HISTORI("Histori") }
private enum class SortMode(val label: String) { RECENT("Terakhir ditambahkan"), AZ("A → Z"), ZA("Z → A") }

@Composable
fun FavoritScreen(onMangaClick: (String) -> Unit) {
    val ctx = LocalContext.current
    val store = remember { LocalStore(ctx) }
    var favs by remember { mutableStateOf(listOf<Pair<Long, FavoriteItem>>()) }
    var history by remember { mutableStateOf(listOf<HistoryItem>()) }
    var selectedTab by remember { mutableStateOf(FavTab.FAVORIT) }
    var sortMode by remember { mutableStateOf(SortMode.RECENT) }

    LaunchedEffect(Unit) {
        favs = store.listFavoritesWithTimestamp()
        history = store.listAllLastRead()
    }

    Column(modifier = Modifier.fillMaxSize().background(PinaNavy)) {
        Text(
            "📚 Koleksi",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        // Tab row
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = PinaNavy,
            contentColor = PinaRed,
        ) {
            FavTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            tab.label,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == tab) Color.White else PinaGray
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            FavTab.FAVORIT -> {
                // Sort chips
                SortChipsRow(selected = sortMode, onSelect = { sortMode = it })
                val sorted = when (sortMode) {
                    SortMode.RECENT -> favs
                    SortMode.AZ -> favs.sortedBy { it.second.title.lowercase() }
                    SortMode.ZA -> favs.sortedByDescending { it.second.title.lowercase() }
                }
                if (sorted.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Belum ada favorit.", color = PinaGray, fontSize = 14.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Tap 🔖 di detail komik untuk bookmark.",
                                color = PinaGray,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        gridItems(sorted, key = { it.second.slug }) { (_, fav) ->
                            PosterCard(
                                manga = Manga(
                                    slug = fav.slug,
                                    title = fav.title,
                                    cover = fav.cover,
                                    type = fav.type,
                                    theme = fav.theme,
                                    upCount = "",
                                    url = "",
                                    latestChapterTitle = fav.latestChapter,
                                    latestChapterUrl = "",
                                    totalChapters = ""
                                ),
                                onClick = { onMangaClick(fav.slug) }
                            )
                        }
                    }
                }
            }

            FavTab.HISTORI -> {
                if (history.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Belum ada histori baca.", color = PinaGray, fontSize = 14.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Buka chapter komik untuk mulai histori.",
                                color = PinaGray,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(history, key = { it.slug }) { item ->
                            HistoryCard(
                                item = item,
                                store = store,
                                onClick = { onMangaClick(item.slug) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SortChipsRow(selected: SortMode, onSelect: (SortMode) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SortMode.entries.forEach { mode ->
            val active = mode == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (active) PinaRed else PinaNavyCard)
                    .clickable { onSelect(mode) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    mode.label,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun HistoryCard(item: HistoryItem, store: LocalStore, onClick: () -> Unit) {
    val dateStr = remember(item.ts) {
        if (item.ts > 0) {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            sdf.format(Date(item.ts))
        } else ""
    }
    val displayTitle = item.title.ifBlank {
        item.slug.replace("-", " ").split(" ")
            .joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }
    }
    val typeColor = when (item.type) {
        "Manhwa" -> Color(0xFFE63946)
        "Manga" -> Color(0xFF4EA8DE)
        "Manhua" -> Color(0xFF43AA8B)
        else -> PinaGray
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = PinaNavyCard),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover thumbnail
            Box(
                modifier = Modifier
                    .size(width = 60.dp, height = 80.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(PinaNavy)
            ) {
                if (item.cover.isNotBlank()) {
                    AsyncImage(
                        model = item.cover,
                        contentDescription = displayTitle,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("📖", fontSize = 24.sp)
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    displayTitle,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                if (item.type.isNotBlank()) {
                    Text(
                        item.type,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(typeColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    item.chapterLabel,
                    color = PinaRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                if (dateStr.isNotBlank()) {
                    Text(
                        dateStr,
                        color = PinaGray,
                        fontSize = 10.sp
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Text("›", color = PinaGray, fontSize = 24.sp, fontWeight = FontWeight.Light)
        }
    }
}
