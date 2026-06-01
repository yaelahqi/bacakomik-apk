package id.pina.bacakomik.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import id.pina.bacakomik.PinaGray
import id.pina.bacakomik.PinaNavy
import id.pina.bacakomik.PinaNavyCard
import id.pina.bacakomik.PinaNavyLight
import id.pina.bacakomik.PinaRed
import id.pina.bacakomik.data.ApiService
import id.pina.bacakomik.data.ChapterItem
import id.pina.bacakomik.data.MangaDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    slug: String,
    onBack: () -> Unit,
    onChapterClick: (String) -> Unit
) {
    var detail by remember { mutableStateOf<MangaDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(slug) {
        scope.launch {
            try {
                detail = withContext(Dispatchers.IO) { ApiService.fetchManga(slug) }
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { DetailHeader(d) }
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
                        ChapterRow(ch) { onChapterClick(ch.slug) }
                    }
                }
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
                .size(width = 110.dp, height = 160.dp)
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
    // Simple wrapping row using FlowRow alternative — Column of Rows
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
fun ChapterRow(ch: ChapterItem, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PinaNavyCard),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(ch.number, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                if (ch.date.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(ch.date, color = PinaGray, fontSize = 10.sp)
                }
            }
            Text("›", color = PinaRed, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
