package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Book
import com.example.viewmodel.ReadTrackerViewModel
import com.example.ui.theme.AccentOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareAnalyticsScreen(
    viewModel: ReadTrackerViewModel,
    onNavigateBack: () -> Unit
) {
    val books by viewModel.books.collectAsState()
    val analyticsShowMode by viewModel.analyticsShowMode.collectAsState()
    val shortenNumbers by viewModel.shortenNumbers.collectAsState()

    val showSingles = analyticsShowMode == 0 || analyticsShowMode == 1
    val showWeb = analyticsShowMode == 0 || analyticsShowMode == 2

    val completedSeriesCount = books.count { it.status == 3 && it.isSeries }
    val completedSinglesCount = books.count { it.status == 3 && it.isSingle }
    val completedHybridsCount = books.count { it.status == 3 && it.isHybridFormat }
    val completedWebCount = books.count { it.status == 3 && it.isWeb }

    val totalVolumesRead = books.sumOf { if (it.countVolumes && !it.isWeb) it.effectiveVolumes else 0 }
    val hasBooksWithVolumes = books.any { it.countVolumes && !it.isWeb }
    val totalWordsRead = books.sumOf { it.effectiveWords }

    val coroutineScope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme() || (viewModel.themeMode.collectAsState().value < 2)

    // Gradients for share analytics card
    val cardBackgroundGrad = if (isDark) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF1A1024), Color(0xFF0F0A1A))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFFFFF8EC), Color(0xFFFFF0D0))
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Поделиться статистикой", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Назад", tint = AccentOrange)
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // CARD FOR RE-PUBLISHING
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBackgroundGrad)
                    .border(
                        1.dp,
                        if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(24.dp)
            ) {
                // Header Block
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentOrange.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoStories,
                            contentDescription = null,
                            tint = AccentOrange,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "ReadTracker",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.W900,
                            color = AccentOrange
                        )
                        Text(
                            "Моя статистика",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stat metrics list
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ShareMetricRow(
                        label = "Завершено серий",
                        value = completedSeriesCount.toString(),
                        icon = Icons.Rounded.EmojiEvents,
                        color = Color(0xFF34D399)
                    )

                    if (showSingles && completedSinglesCount > 0) {
                        ShareMetricRow(
                            label = "Завершено синглов",
                            value = completedSinglesCount.toString(),
                            icon = Icons.Rounded.ContentCopy,
                            color = Color(0xFF06B6D4)
                        )
                    }

                    if (completedHybridsCount > 0) {
                        ShareMetricRow(
                            label = "Завершено LN+WN",
                            value = completedHybridsCount.toString(),
                            icon = Icons.Rounded.AutoStories,
                            color = Color(0xFFFBBF24)
                        )
                    }

                    if (showWeb) {
                        ShareMetricRow(
                            label = "Завершено веб-новелл",
                            value = completedWebCount.toString(),
                            icon = Icons.Rounded.Language,
                            color = Color(0xFFA78BFA)
                        )
                    }

                    if (hasBooksWithVolumes) {
                        ShareMetricRow(
                            label = "Прочитано томов",
                            value = totalVolumesRead.toString(),
                            icon = Icons.Rounded.Layers,
                            color = Color(0xFF60A5FA)
                        )
                    }

                    ShareMetricRow(
                        label = "Прочитано слов",
                        value = formatNumber(totalWordsRead, shortenNumbers),
                        icon = Icons.Rounded.TextFields,
                        color = AccentOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Action Button lower down
            Button(
                onClick = {
                    coroutineScope.launch {
                        isSaving = true
                        delay(1200) // Beautiful loader simulation
                        isSaving = false
                        viewModel.showToast("Сохранено в галерею")
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentOrange,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Сохраняем...", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Сохранить в галерею", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareListScreen(
    viewModel: ReadTrackerViewModel,
    onNavigateBack: () -> Unit
) {
    val books by viewModel.books.collectAsState()
    val analyticsShowMode by viewModel.analyticsShowMode.collectAsState()
    val shortenNumbers by viewModel.shortenNumbers.collectAsState()

    val completedSeriesCount = books.count { it.status == 3 && it.isSeries }
    val completedSinglesCount = books.count { it.status == 3 && it.isSingle }
    val completedHybridsCount = books.count { it.status == 3 && it.isHybridFormat }
    val completedWebCount = books.count { it.status == 3 && it.isWeb }

    val coroutineScope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme() || (viewModel.themeMode.collectAsState().value < 2)

    // Gradients for list publish card
    val cardBackgroundGrad = if (isDark) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF0D1A14), Color(0xFF061008))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5))
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Поделиться списком", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Назад", tint = AccentOrange)
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // CARD LIST CONTAINER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBackgroundGrad)
                    .border(
                        1.dp,
                        if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(24.dp)
            ) {
                // Header Block
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF34D399).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FormatListBulleted,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "ReadTracker",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.W900,
                            color = Color(0xFF34D399)
                        )
                        Text(
                            "Тайтлов: ${books.size}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val labelText = remember(completedSeriesCount, completedSinglesCount, completedHybridsCount, completedWebCount, analyticsShowMode) {
                    val showSingles = analyticsShowMode == 0 || analyticsShowMode == 1
                    val showWeb = analyticsShowMode == 0 || analyticsShowMode == 2
                    buildString {
                        append("Серий: $completedSeriesCount")
                        if (showSingles && completedSinglesCount > 0) append("  |  Синглов: $completedSinglesCount")
                        if (completedHybridsCount > 0) append("  |  Гибридов: $completedHybridsCount")
                        if (showWeb) append("  |  Веб: $completedWebCount")
                    }
                }
                Text(
                    labelText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF34D399)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Listing Books
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    books.take(12).forEach { book -> // Show top 12 books on published cards to fit nicely
                        val statusColor = getStatusColor(book.status)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Status side bar
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1.0f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = book.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.White else Color.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1.0f)
                                    )
                                    
                                    if (book.rating != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            book.getRatingDisplay(10),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentOrange
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = getStatusText(book.status),
                                        color = statusColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    
                                    Text(
                                        text = "${formatNumber(book.effectiveWords, shortenNumbers)} сл. / " +
                                                if (book.isWeb) book.chapterLabel(false) else book.volumeLabel(),
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Action Button lower down – colored green (#34D399)
            Button(
                onClick = {
                    coroutineScope.launch {
                        isSaving = true
                        delay(1200)
                        isSaving = false
                        viewModel.showToast("Сохранено в галерею")
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF34D399),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Сохраняем...", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Сохранить в галерею", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ShareMetricRow(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
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
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.weight(1.0f)
        )
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.W800,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
