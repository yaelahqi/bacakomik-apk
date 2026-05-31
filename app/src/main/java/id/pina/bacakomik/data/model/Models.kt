package id.pina.bacakomik.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListItem(
    val slug: String,
    val title: String,
    val cover: String? = null,
    val type: String = "Komik",
    val theme: String = "",
    val upCount: String? = null,
    val url: String = "",
    val latestChapterTitle: String? = null,
    val latestChapterUrl: String? = null,
    val totalChapters: String? = null,
)

@Serializable
data class ListResponse(
    val items: List<ListItem> = emptyList(),
    val hasNext: Boolean = false,
    val page: Int = 1,
)

@Serializable
data class SearchResponse(
    val items: List<ListItem> = emptyList(),
    val query: String = "",
)

@Serializable
data class ChapterRef(
    val number: String,
    val title: String,
    val url: String,
    val slug: String,
    val date: String? = null,
)

@Serializable
data class MangaDetail(
    val slug: String,
    val title: String,
    val altTitle: String? = null,
    val cover: String? = null,
    val description: String = "",
    val type: String = "",
    val status: String = "",
    val rating: String = "",
    val author: String = "",
    val genres: List<String> = emptyList(),
    val theme: String = "",
    val views: String = "",
    val chapters: List<ChapterRef> = emptyList(),
)

@Serializable
data class ChapterPages(
    @SerialName("chapterSlug") val slug: String = "",
    val title: String = "",
    val mangaSlug: String? = null,
    @SerialName("images") val pages: List<String> = emptyList(),
    val prev: String? = null,
    val next: String? = null,
)
