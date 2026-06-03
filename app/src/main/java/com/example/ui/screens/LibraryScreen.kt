@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.model.Book
import com.example.viewmodel.ReadTrackerViewModel
import com.example.ui.theme.AccentOrange

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    viewModel: ReadTrackerViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onOpenShareSheet: () -> Unit
) {
    val books by viewModel.books.collectAsState()
    val showCovers by viewModel.showCovers.collectAsState()
    val showShareButton by viewModel.showShareButton.collectAsState()
    val showBookmarks by viewModel.showBookmarks.collectAsState()
    val bookmarkPosition by viewModel.bookmarkPosition.collectAsState()
    val enableRating by viewModel.enableRating.collectAsState()
    val ratingScale by viewModel.ratingScale.collectAsState()
    val badgeLayoutMode by viewModel.badgeLayoutMode.collectAsState()
    val enableAdaptationStart by viewModel.enableAdaptationStart.collectAsState()
    val showWebChapters by viewModel.showWebChapters.collectAsState()
    
    val savedTabIndex by viewModel.savedTabIndex.collectAsState()
    
    var bookToDelete by remember { mutableStateOf<Book?>(null) }

    // Categories tabs: All=0, Reading=1, Planned=2, Completed=3, Paused=4, Dropped=5
    // Database statuses match: 0=Planned, 1=Reading, 2=Paused, 3=Completed, 4=Dropped
    // Let's create category map to UI status
    val tabNames = listOf("Все", "Читаю", "В планах", "Завершено", "На паузе", "Брошено")
    val currentTab = savedTabIndex

    // Filter books based on tab
    val filteredBooks = remember(books, currentTab) {
        if (currentTab == 0) {
            books
        } else {
            val targetStatus = when (currentTab) {
                1 -> 1 // Reading
                2 -> 0 // Planned
                3 -> 3 // Completed
                4 -> 2 // Paused
                5 -> 4 // Dropped
                else -> 0
            }
            books.filter { it.status == targetStatus }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = AccentOrange,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Добавить",
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Unified header Row for precise spacing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Библиотека",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onNavigateToAnalytics,
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Analytics,
                            contentDescription = "Аналитика",
                            tint = AccentOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    if (showShareButton) {
                        IconButton(
                            onClick = onOpenShareSheet,
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.IosShare,
                                contentDescription = "Поделиться",
                                tint = AccentOrange,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Custom horizontal filters with close spacing and underline indicator
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(tabNames) { index, name ->
                    val isSelected = currentTab == index
                    Column(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) { viewModel.setSavedTabIndex(index) }
                            .padding(bottom = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = name,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.W800 else FontWeight.W500,
                            color = if (isSelected) AccentOrange else Color.Gray,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        // Underline indicator
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .width(if (isSelected) 18.dp else 0.dp)
                                .background(AccentOrange, RoundedCornerShape(1.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (filteredBooks.isEmpty()) {
                // Empty Library state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(AccentOrange.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MenuBook,
                                contentDescription = null,
                                tint = AccentOrange,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Список пуст",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Нажмите + чтобы добавить тайтл",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                // Loaded Books listing
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredBooks, key = { it.id }) { book ->
                        BookRowItem(
                            book = book,
                            showCovers = showCovers,
                            showBookmarks = showBookmarks,
                            bookmarkPosition = bookmarkPosition,
                            enableRating = enableRating,
                            ratingScale = ratingScale,
                            badgeLayoutMode = badgeLayoutMode,
                            enableAdaptationStart = enableAdaptationStart,
                            showWebChapters = showWebChapters,
                            onClick = { onNavigateToEdit(book.id) },
                            onLongClick = { bookToDelete = book }
                        )
                    }
                }
            }
        }
    }

    // Delete confirm dialogue
    bookToDelete?.let { book ->
        AlertDialog(
            onDismissRequest = { bookToDelete = null },
            title = {
                Text(
                    text = "Удалить тайтл?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = "«${book.title}» будет удалён без возможности восстановления.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBook(book.id)
                        bookToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF87171))
                ) {
                    Text("Удалить", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { bookToDelete = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                ) {
                    Text("Отмена", fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookRowItem(
    book: Book,
    showCovers: Boolean,
    showBookmarks: Boolean,
    bookmarkPosition: Int, // 0 = Bottom, 1 = Inline
    enableRating: Boolean,
    ratingScale: Int,
    badgeLayoutMode: Int,
    enableAdaptationStart: Boolean,
    showWebChapters: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val statusColor = getStatusColor(book.status)
    val cardBackground = MaterialTheme.colorScheme.surface
    val shorten = false // Standard formatting inside rows

    if (showCovers) {
        // Detailed row with cover thumbnail
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 2.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(cardBackground)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Cover
            Box(
                modifier = Modifier
                    .size(width = 38.dp, height = 52.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(book.coverColor).copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (!book.coverUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = book.coverUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Image,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // 2. Middle summary area (Expanded)
            Column(
                modifier = Modifier.weight(1.0f)
            ) {
                // Line 1: Title only (takes full available width)
                Text(
                    text = book.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(1.dp))

                // Line 2: Status Indicator only
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = getStatusText(book.status),
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Line 3: Progress Data
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Words count
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.TextFields,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${formatNumber(book.effectiveWords, shorten)} сл.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }

                    // Volume progress
                    if (book.countVolumes && !book.isWeb) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Layers,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = book.volumeLabel(),
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Chapters Progress
                    if (showWebChapters && (book.isWeb || book.isHybridFormat)) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.FormatListNumbered,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = book.chapterLabel(),
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Bookmark - inline mode
                    if (showBookmarks && bookmarkPosition == 1 && !book.currentBookmark.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1.0f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bookmark,
                                contentDescription = null,
                                tint = AccentOrange,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = book.currentBookmark,
                                color = AccentOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Line 4: Bookmark - bottom mode
                if (showBookmarks && bookmarkPosition == 0 && !book.currentBookmark.isNullOrBlank()) {
                     Spacer(modifier = Modifier.height(4.dp))
                     Row(
                         modifier = Modifier.fillMaxWidth(),
                         verticalAlignment = Alignment.CenterVertically
                     ) {
                         Icon(
                             imageVector = Icons.Rounded.Bookmark,
                             contentDescription = null,
                             tint = AccentOrange,
                             modifier = Modifier.size(11.dp)
                         )
                         Spacer(modifier = Modifier.width(3.dp))
                         Text(
                             text = book.currentBookmark,
                             color = AccentOrange,
                             fontSize = 11.sp,
                             fontWeight = FontWeight.SemiBold,
                             maxLines = 1,
                             overflow = TextOverflow.Ellipsis
                         )
                     }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3. Right side: Badges and Chevron
            if (badgeLayoutMode == 1) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (enableRating && book.rating != null) {
                        BookBadge(
                            text = book.getRatingDisplay(ratingScale),
                            color = AccentOrange
                        )
                    }

                    when {
                        book.isHybridFormat -> BookBadge("LN+WN", AccentOrange)
                        book.isWeb -> BookBadge("Веб", Color(0xFFFBBF24))
                        book.isSingle -> BookBadge("Сингл", AccentOrange)
                        book.isSeries -> BookBadge("Серия", Color(0xFFA78BFA))
                    }
                    if (book.isOngoing) {
                        BookBadge("Онг.", Color(0xFF34D399))
                    }

                    if (enableAdaptationStart) {
                        if (book.isSeries && book.startVolume != null) {
                            BookBadge("Старт: т. ${book.startVolume}", Color(0xFF34D399))
                        } else if ((book.isWeb || book.isHybridFormat) && book.startChapter != null) {
                            BookBadge("Старт: гл. ${book.startChapter}", Color(0xFF34D399))
                        }
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (enableRating && book.rating != null) {
                        BookBadge(
                            text = book.getRatingDisplay(ratingScale),
                            color = AccentOrange
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when {
                            book.isHybridFormat -> BookBadge("LN+WN", AccentOrange)
                            book.isWeb -> BookBadge("Веб", Color(0xFFFBBF24))
                            book.isSingle -> BookBadge("Сингл", AccentOrange)
                            book.isSeries -> BookBadge("Серия", Color(0xFFA78BFA))
                        }
                        if (book.isOngoing) {
                            BookBadge("Онг.", Color(0xFF34D399))
                        }
                    }

                    if (enableAdaptationStart) {
                        if (book.isSeries && book.startVolume != null) {
                            BookBadge("Старт: т. ${book.startVolume}", Color(0xFF34D399))
                        } else if ((book.isWeb || book.isHybridFormat) && book.startChapter != null) {
                            BookBadge("Старт: гл. ${book.startChapter}", Color(0xFF34D399))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // 4. Arrow chevron
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    } else {
        // Compact row item (Without cover thumbnails)
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 2.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(cardBackground)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status bar strip on extreme left
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1.0f)
            ) {
                // Line 1: Title only
                Text(
                    text = book.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Line 2: Progress indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Words count
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.TextFields,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${formatNumber(book.effectiveWords, shorten)} сл.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }

                    // Volume progress
                    if (book.countVolumes && !book.isWeb) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Layers,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = book.volumeLabel(),
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Chapters Progress
                    if (showWebChapters && (book.isWeb || book.isHybridFormat)) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.FormatListNumbered,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = book.chapterLabel(),
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Bookmark - inline mode
                    if (showBookmarks && bookmarkPosition == 1 && !book.currentBookmark.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1.0f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bookmark,
                                contentDescription = null,
                                tint = AccentOrange,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = book.currentBookmark,
                                color = AccentOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Line 3: Bookmark - bottom mode
                if (showBookmarks && bookmarkPosition == 0 && !book.currentBookmark.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bookmark,
                            contentDescription = null,
                            tint = AccentOrange,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = book.currentBookmark,
                            color = AccentOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right side: Badges Column and Chevron
            if (badgeLayoutMode == 1) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (enableRating && book.rating != null) {
                        BookBadge(
                            text = book.getRatingDisplay(ratingScale),
                            color = AccentOrange
                        )
                    }

                    when {
                        book.isHybridFormat -> BookBadge("LN+WN", AccentOrange)
                        book.isWeb -> BookBadge("Веб", Color(0xFFFBBF24))
                        book.isSingle -> BookBadge("Сингл", AccentOrange)
                        book.isSeries -> BookBadge("Серия", Color(0xFFA78BFA))
                    }
                    if (book.isOngoing) {
                        BookBadge("Онг.", Color(0xFF34D399))
                    }

                    if (enableAdaptationStart) {
                        if (book.isSeries && book.startVolume != null) {
                            BookBadge("Старт: т. ${book.startVolume}", Color(0xFF34D399))
                        } else if ((book.isWeb || book.isHybridFormat) && book.startChapter != null) {
                            BookBadge("Старт: гл. ${book.startChapter}", Color(0xFF34D399))
                        }
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (enableRating && book.rating != null) {
                        BookBadge(
                            text = book.getRatingDisplay(ratingScale),
                            color = AccentOrange
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when {
                            book.isHybridFormat -> BookBadge("LN+WN", AccentOrange)
                            book.isWeb -> BookBadge("Веб", Color(0xFFFBBF24))
                            book.isSingle -> BookBadge("Сингл", AccentOrange)
                            book.isSeries -> BookBadge("Серия", Color(0xFFA78BFA))
                        }
                        if (book.isOngoing) {
                            BookBadge("Онг.", Color(0xFF34D399))
                        }
                    }

                    if (enableAdaptationStart) {
                        if (book.isSeries && book.startVolume != null) {
                            BookBadge("Старт: т. ${book.startVolume}", Color(0xFF34D399))
                        } else if ((book.isWeb || book.isHybridFormat) && book.startChapter != null) {
                            BookBadge("Старт: гл. ${book.startChapter}", Color(0xFF34D399))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Arrow chevron
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
