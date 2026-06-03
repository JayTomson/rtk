package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Book
import com.example.viewmodel.ReadTrackerViewModel
import com.example.ui.theme.AccentOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: ReadTrackerViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val books by viewModel.books.collectAsState()
    val shortenNumbers by viewModel.shortenNumbers.collectAsState()
    val stackedStats by viewModel.stackedStats.collectAsState()
    val analyticsShowMode by viewModel.analyticsShowMode.collectAsState()

    // Calculating Metrics with optimized remember block
    val completedSeriesCount = remember(books) { books.count { it.status == 3 && it.isSeries } }
    val completedSinglesCount = remember(books) { books.count { it.status == 3 && it.isSingle } }
    val completedHybridsCount = remember(books) { books.count { it.status == 3 && it.isHybridFormat } }
    val completedWebCount = remember(books) { books.count { it.status == 3 && it.isWeb } }

    val totalVolumesRead = remember(books) { books.sumOf { if (it.countVolumes && !it.isWeb) it.effectiveVolumes else 0 } }
    val hasBooksWithVolumes = remember(books) { books.any { it.countVolumes && !it.isWeb } }
    val totalWordsRead = remember(books) { books.sumOf { it.effectiveWords } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Аналитика", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // METRICS CARD SECTION
            val statsList = remember(completedSeriesCount, completedSinglesCount, completedHybridsCount, completedWebCount, analyticsShowMode, hasBooksWithVolumes, totalVolumesRead) {
                val showSingles = analyticsShowMode == 0 || analyticsShowMode == 1
                val showWeb = analyticsShowMode == 0 || analyticsShowMode == 2
                mutableListOf<@Composable () -> Unit>().apply {
                    add {
                        StatCard(
                            count = completedSeriesCount.toString(),
                            label = "Завершено серий",
                            icon = Icons.Rounded.EmojiEvents,
                            color = Color(0xFF34D399) // Green
                        )
                    }

                    if (showSingles && completedSinglesCount > 0) {
                        add {
                            StatCard(
                                count = completedSinglesCount.toString(),
                                label = "Завершено синглов",
                                icon = Icons.Rounded.ContentCopy,
                                color = Color(0xFF06B6D4) // Cyan
                            )
                        }
                    }

                    if (completedHybridsCount > 0) {
                        add {
                            StatCard(
                                count = completedHybridsCount.toString(),
                                label = "Завершено LN+WN",
                                icon = Icons.Rounded.AutoStories,
                                color = Color(0xFFFBBF24) // Yellow
                            )
                        }
                    }

                    if (showWeb) {
                        add {
                            StatCard(
                                count = completedWebCount.toString(),
                                label = "Завершено веб",
                                icon = Icons.Rounded.Language,
                                color = Color(0xFFA78BFA) // Violet
                            )
                        }
                    }

                    if (hasBooksWithVolumes) {
                        add {
                            StatCard(
                                count = totalVolumesRead.toString(),
                                label = "Прочитано томов",
                                icon = Icons.Rounded.Layers,
                                color = Color(0xFF60A5FA) // Blue
                            )
                        }
                    }
                }
            }

            if (stackedStats) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    statsList.forEach { card -> card() }
                }
            } else {
                // Inline mode: Dynamic, responsive grid layout that adapts based on the total number of metric cards
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    when (statsList.size) {
                        1 -> {
                            statsList[0]()
                        }
                        2 -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) { statsList[0]() }
                                Box(modifier = Modifier.weight(1f)) { statsList[1]() }
                            }
                        }
                        3 -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) { statsList[0]() }
                                Box(modifier = Modifier.weight(1f)) { statsList[1]() }
                                Box(modifier = Modifier.weight(1f)) { statsList[2]() }
                            }
                        }
                        4 -> {
                            // Two equal rows of 2 for balanced structure
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) { statsList[0]() }
                                Box(modifier = Modifier.weight(1f)) { statsList[1]() }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) { statsList[2]() }
                                Box(modifier = Modifier.weight(1f)) { statsList[3]() }
                            }
                        }
                        else -> {
                            // 5 or more: Row of 3 on top, and remaining in the second row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) { statsList[0]() }
                                Box(modifier = Modifier.weight(1f)) { statsList[1]() }
                                Box(modifier = Modifier.weight(1f)) { statsList[2]() }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (j in 3 until statsList.size) {
                                    Box(modifier = Modifier.weight(1f)) { statsList[j]() }
                                }
                                val remaining = 3 - (statsList.size - 3)
                                if (remaining > 0) {
                                    repeat(remaining) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Total Words - always full width row
            StatCard(
                count = formatNumber(totalWordsRead, shortenNumbers),
                label = "Прочитано слов за всё время",
                icon = Icons.Rounded.TextFields,
                color = AccentOrange,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // STATUS PROGRESS BARS BLOCK
            CategoryHeader("По статусам")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 4.dp)
            ) {
                // List states: Planned=0, Reading=1, Paused=2, Completed=3, Dropped=4
                val statuses = listOf(1, 0, 3, 2, 4) // Order of displaying: reading, planned, completed, paused, dropped
                val totalBooks = books.size.coerceAtLeast(1)

                statuses.forEachIndexed { index, st ->
                    val statusCount = books.count { it.status == st }
                    val ratio = statusCount.toFloat() / totalBooks.toFloat()
                    val statusColor = getStatusColor(st)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = getStatusText(st),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            // Badge with count
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(statusColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = statusCount.toString(),
                                    color = statusColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Progress line indicator
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = statusColor,
                            trackColor = statusColor.copy(alpha = 0.10f)
                        )
                    }

                    if (index < statuses.size - 1) {
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Settings Tile Row Shortcut links to SettingsScreen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(onClick = onNavigateToSettings)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentOrange.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        tint = AccentOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = "Настройки",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Управление функциями, тема, экспорт",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun StatCard(
    count: String,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Big numeric value text
        Text(
            text = count,
            fontSize = 28.sp,
            fontWeight = FontWeight.W800,
            lineHeight = 31.sp,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Description label
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            lineHeight = 15.sp
        )
    }
}
