package id.pina.bacakomik.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import id.pina.bacakomik.data.api.KomikApi
import id.pina.bacakomik.data.model.ListItem
import id.pina.bacakomik.data.repo.LibraryRepo
import id.pina.bacakomik.ui.components.ErrorBox
import id.pina.bacakomik.ui.components.LoadingBox
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    slug: String,
    onBack: () -> Unit,
    onReadChapter: (chapterSlug: String) -> Unit,
) {
    val vm: DetailViewModel = viewModel()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(slug) {
        vm.load(slug)
        vm.setSaved(LibraryRepo.isSaved(ctx, slug))
    }
    val state by vm.state.collectAsStateWithLifecycle()
    val manga = state.manga

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        manga?.title ?: "Detail",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    if (manga != null) {
                        IconButton(onClick = {
                            scope.launch {
                                val item = ListItem(
                                    slug = manga.slug,
                                    title = manga.title,
                                    cover = manga.cover,
                                    type = manga.type.ifBlank { "Komik" },
                                )
                                val added = LibraryRepo.toggle(ctx, item)
                                vm.setSaved(added)
                            }
                        }) {
                            Icon(
                                if (state.saved) Icons.Filled.Bookmark
                                else Icons.Outlined.BookmarkBorder,
                                null,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.loading -> LoadingBox()
                state.error != null -> ErrorBox("Gagal: ${state.error}")
                manga != null -> DetailContent(manga, onReadChapter)
            }
        }
    }
}

@Composable
private fun DetailContent(
    manga: id.pina.bacakomik.data.model.MangaDetail,
    onReadChapter: (chapterSlug: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier
                        .width(120.dp)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = KomikApi.imageUrl(manga.cover),
                        contentDescription = manga.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                Column(Modifier.padding(start = 14.dp).fillMaxWidth()) {
                    Text(manga.title, style = MaterialTheme.typography.titleMedium)
                    if (!manga.altTitle.isNullOrBlank())
                        Text(manga.altTitle, style = MaterialTheme.typography.bodySmall)
                    Text("${manga.type} • ${manga.status}", style = MaterialTheme.typography.bodySmall)
                    if (manga.author.isNotBlank())
                        Text("Author: ${manga.author}", style = MaterialTheme.typography.bodySmall)
                    if (manga.rating.isNotBlank())
                        Text("⭐ ${manga.rating}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (manga.genres.isNotEmpty()) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .fillMaxWidth(),
                ) {
                    manga.genres.forEach { g ->
                        AssistChip(onClick = {}, label = { Text(g) })
                    }
                }
            }
        }
        if (manga.description.isNotBlank()) {
            item {
                Text(
                    manga.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
        item {
            Text(
                "Chapters (${manga.chapters.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        items(manga.chapters, key = { it.slug }) { ch ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onReadChapter(ch.slug) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Text(
                    "Chapter ${ch.number}",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                if (!ch.date.isNullOrBlank()) {
                    Text(ch.date, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
