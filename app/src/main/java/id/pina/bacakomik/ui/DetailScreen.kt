package id.pina.bacakomik.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import coil.compose.AsyncImage
import id.pina.bacakomik.PinaGray
import id.pina.bacakomik.PinaNavy
import id.pina.bacakomik.PinaNavyCard
import id.pina.bacakomik.PinaNavyLight
import id.pina.bacakomik.PinaRed
import id.pina.bacakomik.data.ApiService
import id.pina.bacakomik.data.ChapterItem
import id.pina.bacakomik.data.FavoriteItem
import id.pina.bacakomik.data.LastRead
import id.pina.bacakomik.data.LocalStore
import id.pina.bacakomik.data.MangaDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    slug: String,
    onBack: () -> Unit,
    onChapterClick: (String, String) -> Unit
) {
    val ctx = LocalContext.current
    val store = remember { LocalStore(ctx) }

    var detail by remember { mutableStateOf<MangaDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isFav by remember { mutableStateOf(store.isFavorite(slug)) }
    var lastRead by remember { mutableStateOf<LastRead?>(store.getLastRead(slug)) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(slug) {
        scope.launch {
            try {
                val d = withContext(Dispatchers.IO) { ApiService.fetchManga(slug) }
                detail = d
                isLoading = false
            } catch (e: Exception) {
                error = e.message
                isLoading = false
            }
        }
    }

    // Refresh lastRead when returning from reader
    LaunchedEffect(slug) {
        lastRead = store.getLastRead(slug)
    }

    Scaffold(
        containerColor = PinaNavy,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        detail?.title ?: "Loading...",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val d = detail ?: return@IconButton
                        if (isFav) {
                            store.removeFavorite(slug)
                            isFav = false
                        } else {
                            store.addFavorite(FavoriteItem(
                                slug = d.slug,
                                title = d.title,
                                cover = d.cover,
                                type = d.type,
                                theme = d.theme,
                                latestChapter = d.chapters.firstOrNull()?.number ?: ""
                            ))
                            isFav = true
                        }
                    }) {
                        Icon(
                            if (isFav) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            "Favorite",
                            tint = if (isFav) PinaRed else Color.White
                        )
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
                    Text("❌ Error loading", color = PinaRed, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(error ?: "", color = PinaGray, fontSize = 12.sp)
                }
            }

            detail != null -> {
                val d = detail!!
                val firstCh = d.chapters.lastOrNull()  // chapter list = newest first → oldest = last
                val resumeCh = lastRead

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { DetailHeader(d) }
                    item {
                        ActionButtons(
                            firstChapter = firstCh,
                            lastRead = resumeCh,
                            onStart = { ch ->
                                onChapterClick(ch.slug, ch.number)
                            },
                            onResume = { lr ->
                                onChapterClick(lr.chapterSlug, lr.chapterLabel)
                            }
                        )
                    }
                    item { DetailDescription(d) }
                    item {
                        Text(
                            "Chapter (${d.chapters.size})",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(d.chapters) { ch ->
                        ChapterRow(
                            ch = ch,
                            isLastRead = resumeCh?.chapterSlug == ch.slug,
                            onClick = { onChapterClick(ch.slug, ch.number) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButtons(
    firstChapter: ChapterItem?,
    lastRead: LastRead?,
    onStart: (ChapterItem) -> Unit,
    onResume: (LastRead) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (firstChapter != null) {
            Button(
                onClick = { onStart(firstChapter) },
                colors = ButtonDefaults.buttonColors(containerColor = PinaRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "▶ Mulai Baca",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        if (lastRead != null) {
            Button(
                onClick = { onResume(lastRead) },
                colors = ButtonDefaults.buttonColors(containerColor = PinaNavyCard),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "↻ Lanjut " + (lastRead.chapterLabel.ifBlank { "Terakhir" }),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PinaRed,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DetailHeader(d: MangaDetail) {
    Row(verticalAlignment = Alignment.Top) {
        AsyncImage(
            model = d.cover,
            contentDescription = d.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 120.dp, height = 180.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PinaNavyCard)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(d.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 3, overflow = TextOverflow.Ellipsis)
            if (d.altTitle.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(d.altTitle, color = PinaGray, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(8.dp))
            InfoLine("Type", d.type)
            InfoLine("Status", d.status)
            InfoLine("Author", d.author)
            InfoLine("Theme", d.theme)
            if (d.rating.isNotBlank()) InfoLine("Rating", d.rating)
        }
    }
}

@Composable
fun InfoLine(label: String, value: String) {
    if (value.isBlank()) return
    Row {
        Text("$label:", color = PinaGray, fontSize = 11.sp, modifier = Modifier.width(60.dp))
        Text(value, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun DetailDescription(d: MangaDetail) {
    Column {
        if (d.genres.isNotEmpty()) {
            Text("Genre", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            FlowChips(d.genres)
            Spacer(Modifier.height(12.dp))
        }
        if (d.description.isNotBlank()) {
            Text("Sinopsis", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(d.description, color = Color(0xFFCFCFD8), fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
fun FlowChips(items: List<String>) {
    val chunked = items.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        chunked.forEach { group ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                group.forEach { g ->
                    Text(
                        g,
                        color = PinaRed,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .background(PinaRed.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChapterRow(ch: ChapterItem, isLastRead: Boolean, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isLastRead) PinaRed.copy(alpha = 0.15f) else PinaNavyCard
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(ch.number, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    if (isLastRead) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "📍 Terakhir dibaca",
                            color = PinaRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (ch.date.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(ch.date, color = PinaGray, fontSize = 10.sp)
                }
            }
            Text("›", color = PinaRed, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
