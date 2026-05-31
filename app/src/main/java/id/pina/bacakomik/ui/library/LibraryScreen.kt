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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import id.pina.bacakomik.data.api.KomikApi
import id.pina.bacakomik.data.repo.HistoryEntry
import id.pina.bacakomik.ui.components.EmptyBox
import id.pina.bacakomik.ui.components.ListSkeleton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(onOpen: (slug: String) -> Unit) {
    val vm: LibraryViewModel = viewModel()
    val ctx = LocalContext.current
    LaunchedEffect(Unit) { vm.load(ctx) }
    val state by vm.state.collectAsStateWithLifecycle()
    val pullState = rememberPullToRefreshState()

    Column(Modifier.fillMaxSize()) {
        Text(
            text = "Library",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp),
        )

        // Tab Card Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val activeTab = state.tab
            listOf(Tab.FAVORITES to "Favorit", Tab.HISTORY to "History").forEach { (tab, label) ->
                val isActive = activeTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else Color.Transparent
                        )
                        .clickable { vm.setTab(tab) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isActive) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }
        }

        when {
            state.loading -> ListSkeleton(itemCount = 6)
            state.tab == Tab.FAVORITES && state.all.isEmpty() ->
                EmptyBox("Belum ada komik tersimpan.\nTap ikon bookmark di halaman komik.")
            state.tab == Tab.HISTORY && state.historyItems.isEmpty() ->
                EmptyBox("Belum ada riwayat baca.\nMulai baca komik untuk melihat riwayat.")
            else -> {
                PullToRefreshBox(
                    isRefreshing = state.refreshing,
                    onRefresh = { vm.refresh() },
                    state = pullState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val listState = rememberLazyListState()
                    val nearBottom by remember {
                        derivedStateOf {
                            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            val visibleSize = if (state.tab == Tab.FAVORITES) state.visible.size else state.visibleHistory.size
                            last >= visibleSize - 3
                        }
                    }
                    LaunchedEffect(nearBottom, state.visible.size, state.hasMore, state.visibleHistory.size, state.hasMoreHistory) {
                        if (nearBottom) vm.loadMore()
                    }

                    if (state.tab == Tab.FAVORITES) {
                        FavoritesList(
                            state = state,
                            listState = listState,
                            onOpen = onOpen,
                            onLongPress = { slug ->
                                vm.removeFromFavorites(ctx, slug)
                            },
                        )
                    } else {
                        HistoryList(
                            state = state,
                            listState = listState,
                            onOpen = onOpen,
                            onLongPress = { entry ->
                                vm.removeFromHistory(ctx, entry)
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoritesList(
    state: LibraryUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onOpen: (String) -> Unit,
    onLongPress: (String) -> Unit,
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(state.visible, key = { it.slug }) { entry ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .combinedClickable(
                        onClick = { onOpen(entry.slug) },
                        onLongClick = { onLongPress(entry.slug) },
                    )
                    .padding(8.dp),
            ) {
                Box(
                    Modifier
                        .size(width = 56.dp, height = 80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    AsyncImage(
                        model = KomikApi.imageUrl(entry.cover),
                        contentDescription = entry.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .fillMaxWidth(),
                ) {
                    Text(
                        entry.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        entry.type,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    entry.lastChapterTitle?.let {
                        Text(
                            "Terakhir: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
        if (state.hasMore) {
            item {
                Box(
                    Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryList(
    state: LibraryUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onOpen: (String) -> Unit,
    onLongPress: (HistoryEntry) -> Unit,
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(state.visibleHistory, key = { "${it.slug}_${it.chapterSlug}" }) { entry ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .combinedClickable(
                        onClick = { onOpen(entry.slug) },
                        onLongClick = { onLongPress(entry) },
                    )
                    .padding(8.dp),
            ) {
                Box(
                    Modifier
                        .size(width = 56.dp, height = 80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    AsyncImage(
                        model = KomikApi.imageUrl(entry.cover),
                        contentDescription = entry.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .fillMaxWidth(),
                ) {
                    Text(
                        entry.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    entry.chapterTitle?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        formatRelativeTime(entry.readAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            }
        }
        if (state.hasMoreHistory) {
            item {
                Box(
                    Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
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
        else -> {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
