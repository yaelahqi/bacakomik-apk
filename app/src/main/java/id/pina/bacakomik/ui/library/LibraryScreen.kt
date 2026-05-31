package id.pina.bacakomik.ui.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import id.pina.bacakomik.data.api.KomikApi
import id.pina.bacakomik.data.repo.LibraryEntry
import id.pina.bacakomik.data.repo.HistoryEntry
import id.pina.bacakomik.ui.components.EmptyBox
import id.pina.bacakomik.ui.components.GridSkeleton
import id.pina.bacakomik.ui.theme.PinaNavy
import id.pina.bacakomik.ui.theme.PinaNavyCard
import id.pina.bacakomik.ui.theme.PinaNavyElev
import id.pina.bacakomik.ui.theme.PinaRed
import id.pina.bacakomik.ui.theme.PinaTextPrimary
import id.pina.bacakomik.ui.theme.PinaTextSecondary
import id.pina.bacakomik.ui.theme.PinaTextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(onOpen: (slug: String) -> Unit) {
    val vm: LibraryViewModel = viewModel()
    val ctx = LocalContext.current
    LaunchedEffect(Unit) { vm.load(ctx) }
    val state by vm.state.collectAsStateWithLifecycle()
    val pullState = rememberPullToRefreshState()
    var query by remember { mutableStateOf("") }

    val activeTab = state.tab

    Column(
        modifier = Modifier.fillMaxSize().background(PinaNavy),
    ) {
        // ── Title ──
        Text(
            text = "Favorit",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
            color = PinaTextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        )

        // ── Pill toggle: Favorit | Riwayat ──
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(PinaNavyCard),
        ) {
            listOf(Tab.FAVORITES to "Favorit", Tab.HISTORY to "Riwayat").forEach { (tab, label) ->
                val isActive = activeTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isActive) Color.White else Color.Transparent)
                        .clickable { vm.setTab(tab) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isActive) PinaNavy else PinaTextSecondary,
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Search bar ──
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(PinaNavyCard)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Search, null, tint = PinaTextMuted, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Search...", style = MaterialTheme.typography.bodyMedium, color = PinaTextMuted)
        }

        // ── Delete All button (History tab only) ──
        if (activeTab == Tab.HISTORY) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(PinaNavyCard)
                        .clickable { vm.clearHistory(ctx) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Delete, null, tint = PinaTextMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Hapus Semua", style = MaterialTheme.typography.bodySmall, color = PinaTextMuted)
                }
            }
        }

        // ── Section header ──
        val sectionTitle = if (activeTab == Tab.FAVORITES) "Daftar Favorit" else "Riwayat Baca"
        val sectionSub = if (activeTab == Tab.FAVORITES) "Baca kembali manga favoritmu" else "Komik terakhir yang kamu baca"
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.titleMedium,
            color = PinaTextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
        Text(
            text = sectionSub,
            style = MaterialTheme.typography.bodySmall,
            color = PinaTextSecondary,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(Modifier.height(8.dp))

        // ── Content ──
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = { vm.refresh() },
            state = pullState,
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                state.loading -> GridSkeleton(itemCount = 6)
                activeTab == Tab.FAVORITES && state.all.isEmpty() ->
                    EmptyBox("Belum ada komik tersimpan.\nTap bookmark di halaman komik.")
                activeTab == Tab.HISTORY && state.historyItems.isEmpty() ->
                    EmptyBox("Belum ada riwayat baca.")
                activeTab == Tab.FAVORITES -> {
                    val filtered = state.all.filter {
                        query.isBlank() || it.title.contains(query, ignoreCase = true)
                    }
                    FavoritesGrid(filtered, onOpen, onLongPress = { vm.removeFromFavorites(ctx, it.slug) })
                }
                else -> {
                    val filtered = state.historyItems.filter {
                        query.isBlank() || it.title.contains(query, ignoreCase = true)
                    }
                    HistoryList(filtered, onOpen, onLongPress = { vm.removeFromHistory(ctx, it) })
                }
            }
        }
    }
}

@Composable
private fun FavoritesGrid(
    items: List<LibraryEntry>,
    onOpen: (String) -> Unit,
    onLongPress: (LibraryEntry) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        itemsIndexed(items, key = { _, it -> it.slug }) { _, item ->
            Box {
                // Card
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(PinaNavyCard)
                        .combinedClickable(
                            onClick = { onOpen(item.slug) },
                            onLongClick = { onLongPress(item) },
                        )
                        .padding(8.dp),
                ) {
                    AsyncImage(
                        model = KomikApi.imageUrl(item.cover),
                        contentDescription = item.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = PinaTextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp,
                    )
                    item.lastChapterTitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = PinaRed,
                            maxLines = 1,
                        )
                    }
                }
                // UP badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(PinaRed)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text("UP", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryList(
    items: List<HistoryEntry>,
    onOpen: (String) -> Unit,
    onLongPress: (HistoryEntry) -> Unit,
) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items, key = { "${it.slug}_${it.chapterSlug}" }) { entry ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PinaNavyCard)
                    .combinedClickable(
                        onClick = { onOpen(entry.slug) },
                        onLongClick = { onLongPress(entry) },
                    )
                    .padding(10.dp),
            ) {
                AsyncImage(
                    model = KomikApi.imageUrl(entry.cover),
                    contentDescription = entry.title,
                    modifier = Modifier
                        .size(56.dp, 76.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        entry.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = PinaTextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Terakhir di baca ${entry.chapterTitle ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = PinaTextSecondary,
                    )
                }
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Baru saja"
        diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} menit lalu"
        diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} jam lalu"
        diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)} hari lalu"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
