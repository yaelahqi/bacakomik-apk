package id.pina.bacakomik.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import id.pina.bacakomik.data.api.KomikApi
import id.pina.bacakomik.data.repo.LibraryRepo
import id.pina.bacakomik.ui.components.ErrorBox
import id.pina.bacakomik.ui.components.LoadingBox
import id.pina.bacakomik.ui.settings.SettingsDataStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    slug: String,
    onBack: () -> Unit,
    onNavigateChapter: (slug: String) -> Unit,
) {
    val vm: ReaderViewModel = viewModel()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(slug) { vm.load(slug) }
    val state by vm.state.collectAsStateWithLifecycle()
    val data = state.data

    // Reading mode: 0 = vertical, 1 = horizontal
    var readingMode by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        readingMode = SettingsDataStore.getReadingMode(ctx)
    }

    LaunchedEffect(data) {
        val d = data ?: return@LaunchedEffect
        val mangaSlug = d.mangaSlug ?: return@LaunchedEffect
        LibraryRepo.markRead(ctx, mangaSlug, d.slug, d.title)
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        data?.title ?: "Reader",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            null,
                            tint = Color.White,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xCC000000),
                    titleContentColor = Color.White,
                ),
            )
        },
        bottomBar = {
            if (data != null) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xCC000000))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    OutlinedButton(
                        onClick = { data.prev?.let(onNavigateChapter) },
                        enabled = !data.prev.isNullOrBlank(),
                    ) { Text("Sebelumnya") }
                    Button(
                        onClick = { data.next?.let(onNavigateChapter) },
                        enabled = !data.next.isNullOrBlank(),
                    ) { Text("Selanjutnya") }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    readingMode = if (readingMode == 0) 1 else 0
                    scope.launch { SettingsDataStore.setReadingMode(ctx, readingMode) }
                },
                containerColor = Color(0x99333333),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(0.dp),
            ) {
                Icon(
                    if (readingMode == 0) Icons.Default.SwapHoriz else Icons.Default.ViewColumn,
                    contentDescription = "Toggle reading mode",
                )
            }
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding),
        ) {
            when {
                state.loading -> LoadingBox()
                state.error != null -> ErrorBox("Gagal memuat: ${state.error}")
                data != null -> {
                    if (readingMode == 1) {
                        // Horizontal pager mode
                        val pagerState = rememberPagerState(pageCount = { data.pages.size })
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                        ) { page ->
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(KomikApi.imageUrl(data.pages[page]))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.FillHeight,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(600.dp),
                                )
                            }
                        }
                    } else {
                        // Vertical mode (original)
                        LazyColumn(Modifier.fillMaxSize()) {
                            items(data.pages, key = { it }) { page ->
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(KomikApi.imageUrl(page))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            item {
                                Text(
                                    text = "— Akhir chapter —",
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
