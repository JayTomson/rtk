@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Book
import com.example.model.VolumeEntry
import com.example.viewmodel.ReadTrackerViewModel
import com.example.ui.theme.AccentOrange

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun EditBookScreen(
    bookId: String,
    viewModel: ReadTrackerViewModel,
    onNavigateBack: () -> Unit
) {
    val books by viewModel.books.collectAsState()
    val book = books.find { it.id == bookId }

    if (book == null) {
        LaunchedEffect(Unit) {
            viewModel.showToast("Тайтл не найден", isSuccess = false)
            onNavigateBack()
        }
        return
    }

    val enableHybrid by viewModel.enableHybrid.collectAsState()
    val enableRating by viewModel.enableRating.collectAsState()
    val ratingScale by viewModel.ratingScale.collectAsState()
    val enableAdaptationStart by viewModel.enableAdaptationStart.collectAsState()
    val showBookmarks by viewModel.showBookmarks.collectAsState()
    val showWebChapters by viewModel.showWebChapters.collectAsState()

    // -- LOADED STATES --
    var title by remember { mutableStateOf(book.title) }
    var coverUrl by remember { mutableStateOf(book.coverUrl) }
    var status by remember { mutableStateOf(book.status) }
    
    // Format: 0=Hybrid, 1=Series, 2=Web, 3=Single
    var formatType by remember {
        mutableStateOf(
            when {
                book.isHybridFormat -> 0
                book.isWeb -> 2
                book.isSingle -> 3
                else -> 1
            }
        )
    }

    var countVolumes by remember { mutableStateOf(book.countVolumes) }
    var bookmarkText by remember { mutableStateOf(book.currentBookmark ?: "") }
    var startVolume by remember { mutableStateOf(book.startVolume?.toString() ?: "") }
    var startChapter by remember { mutableStateOf(book.startChapter?.toString() ?: "") }
    var bookRating by remember { mutableStateOf(book.rating) }
    
    // Web chapters
    var readWebChapters by remember {
        mutableStateOf(
            if (book.isHybridFormat) (book.hybridWebChapters?.toString() ?: "")
            else (book.webChapters?.toString() ?: "")
        )
    }
    var totalWebChapters by remember {
        mutableStateOf(
            if (book.isHybridFormat) (book.hybridTotalWebChapters?.toString() ?: "")
            else (book.totalWebChapters?.toString() ?: "")
        )
    }

    // Words & volumes
    var useDetailedVolumes by remember { mutableStateOf(book.useDetailedVolumes) }
    var singleWords by remember { mutableStateOf(book.words.toString()) }
    var singleVolumes by remember { mutableStateOf(book.volumes.toString()) }
    
    // Detailed list
    val volumeEntries = remember { mutableStateListOf<VolumeEntry>().apply { addAll(book.volumeEntries) } }
    
    // Ongoing status
    var isOngoing by remember { mutableStateOf(book.isOngoing) }
    var totalVolumesInSeries by remember { mutableStateOf(book.totalVolumesInSeries?.toString() ?: "") }

    // Dialog state
    var selectUrlDialogShow by remember { mutableStateOf(false) }
    var selectSourceDialogShow by remember { mutableStateOf(false) }

    // Conflicting resets on format change
    val initialFormat = remember { formatType }
    LaunchedEffect(formatType) {
        if (formatType != initialFormat) {
            if (formatType == 2) {
                countVolumes = false
            } else if (formatType == 0) {
                countVolumes = true
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                Spacer(modifier = Modifier.height(getAdaptiveStatusBarPadding()))
                TopAppBar(
                    title = { Text("Редактировать", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Назад", tint = AccentOrange)
                        }
                    },
                    actions = {
                    IconButton(onClick = {
                        if (title.isBlank()) {
                            viewModel.showToast("Название тайтла не может быть пустым", isSuccess = false)
                            return@IconButton
                        }

                        val wordsValue = if (formatType != 2 && countVolumes && useDetailedVolumes) {
                            volumeEntries.sumOf { it.w }
                        } else {
                            singleWords.toIntOrNull() ?: 0
                        }

                        val volsValue = if (formatType != 2 && countVolumes && useDetailedVolumes) {
                            volumeEntries.size
                        } else {
                            singleVolumes.toIntOrNull() ?: 0
                        }

                        val calculatedCoverColor = when (status) {
                            0 -> 0xFF2D3C4F.toInt()
                            1 -> 0xFF1B4D3E.toInt()
                            2 -> 0xFF4D3F1E.toInt()
                            3 -> 0xFF352B4D.toInt()
                            4 -> 0xFF4A2525.toInt()
                            else -> 0xFF2B2B2D.toInt()
                        }

                        val updatedBook = book.copy(
                            title = title.trim(),
                            status = status,
                            isSeries = formatType == 1,
                            isWeb = formatType == 2,
                            isSingle = formatType == 3,
                            isHybridFormat = formatType == 0,
                            countVolumes = countVolumes,
                            words = wordsValue,
                            volumes = volsValue,
                            useDetailedVolumes = useDetailedVolumes,
                            volumeEntries = volumeEntries.toList(),
                            coverUrl = coverUrl,
                            coverColor = calculatedCoverColor,
                            currentBookmark = bookmarkText.trim().ifEmpty { null },
                            startVolume = startVolume.toIntOrNull(),
                            startChapter = startChapter.toIntOrNull(),
                            rating = bookRating,
                            isOngoing = isOngoing,
                            totalVolumesInSeries = if (isOngoing) null else totalVolumesInSeries.toIntOrNull(),
                            webChapters = if (formatType == 2) (readWebChapters.toIntOrNull() ?: 0) else null,
                            totalWebChapters = if (formatType == 2) totalWebChapters.toIntOrNull() else null,
                            hybridWebChapters = if (formatType == 0) (readWebChapters.toIntOrNull() ?: 0) else null,
                            hybridTotalWebChapters = if (formatType == 0) totalWebChapters.toIntOrNull() else null
                        )

                        viewModel.updateBook(updatedBook)
                        onNavigateBack()
                    }) {
                        Icon(imageVector = Icons.Rounded.Check, contentDescription = "Сохранить", tint = AccentOrange)
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Upper row: cover + title textfield
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cover box
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 110.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .drawBehind {
                            if (coverUrl.isNullOrBlank()) {
                                drawRoundRect(
                                    color = AccentOrange.copy(alpha = 0.4f),
                                    style = Stroke(
                                        width = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f), 0f)
                                    ),
                                    cornerRadius = CornerRadius(8.dp.toPx())
                                )
                            }
                        }
                        .clickable { selectSourceDialogShow = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (!coverUrl.isNullOrBlank()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = coverUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Change tag bottom overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Изменить", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.CameraAlt,
                                contentDescription = null,
                                tint = AccentOrange,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Обложка", fontSize = 9.sp, color = AccentOrange, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Title input field
                Column(modifier = Modifier.weight(1.0f)) {
                    CategoryHeader("Название")
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Введите название...", color = Color.Gray, fontSize = 14.sp) },
                        singleLine = false,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = AccentOrange,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // STAtUS LIST (wrap chips arrangement)
            CategoryHeader("Статус")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                listOf(0, 1, 2, 3, 4).forEach { st ->
                    val color = getStatusColor(st)
                    val isActive = status == st
                    val chipBg by animateColorAsState(
                        targetValue = if (isActive) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(chipBg)
                            .border(
                                width = 1.5.dp,
                                color = if (isActive) color else Color.Gray.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { status = st }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = getStatusText(st),
                            color = if (isActive) color else MaterialTheme.colorScheme.onBackground,
                            fontSize = 13.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // FORMAT OPTIONS
            CategoryHeader("Формат издания")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp)
            ) {
                val formats = mutableListOf<Triple<Int, String, String>>()
                if (enableHybrid) {
                    formats.add(Triple(0, "LN+WN Гибрид", "Комплексный формат (LN тома + WN онгоинг главы)"))
                }
                formats.add(Triple(1, "Серия томов", "Серийное издание печатных томов (LN / Книги)"))
                formats.add(Triple(2, "Веб-новелла", "Азиатские веб-романы, разбитые строго по главам (WN)"))
                formats.add(Triple(3, "Сингл (Одиночное)", "Одиночный роман (Оношот / Том-сингл)"))

                formats.forEachIndexed { index, format ->
                    val fType = format.first
                    val isSelected = formatType == fType

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { formatType = fType }
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { formatType = fType },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentOrange)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1.0f)) {
                            Text(
                                text = format.second,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = format.third,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    if (index < formats.size - 1) {
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f))
                    }
                }
            }

            // Switch volumes
            if (formatType != 2 && formatType != 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.0f)) {
                        Text("Учитывать тома", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Отключите для изданий без томов", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = countVolumes,
                        onCheckedChange = { countVolumes = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentOrange,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray.copy(alpha = 0.25f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sub widgets of parameters (same as step 3)
            Step3Content(
                formatType = formatType,
                countVolumes = countVolumes,
                bookmarkText = bookmarkText,
                onBookmarkChange = { bookmarkText = it },
                showBookmarks = showBookmarks,
                enableAdaptationStart = enableAdaptationStart,
                startVolume = startVolume,
                onStartVolumeChange = { startVolume = it },
                startChapter = startChapter,
                onStartChapterChange = { startChapter = it },
                enableRating = enableRating,
                ratingScale = ratingScale,
                bookRating = bookRating,
                onRatingChange = { bookRating = it },
                readWebChapters = readWebChapters,
                onReadWebChaptersChange = { readWebChapters = it },
                totalWebChapters = totalWebChapters,
                onTotalWebChaptersChange = { totalWebChapters = it },
                useDetailedVolumes = useDetailedVolumes,
                onUseDetailedVolumesChange = { useDetailedVolumes = it },
                singleWords = singleWords,
                onSingleWordsChange = { singleWords = it },
                singleVolumes = singleVolumes,
                onSingleVolumesChange = { singleVolumes = it },
                volumeEntries = volumeEntries,
                onAddVolume = { volumeEntries.add(VolumeEntry(volumeEntries.size + 1.0, 0)) },
                onRemoveVolume = { index -> if (index in volumeEntries.indices) volumeEntries.removeAt(index) },
                onUpdateVolume = { index, item -> if (index in volumeEntries.indices) volumeEntries[index] = item },
                isOngoing = isOngoing,
                onOngoingChange = { isOngoing = it },
                totalVolumesInSeries = totalVolumesInSeries,
                onTotalVolumesInSeriesChange = { totalVolumesInSeries = it }
            )

            Spacer(modifier = Modifier.height(60.dp)) // Safe scrolling margin
        }
    }

    // Dialogue prompts for photo URL
    if (selectSourceDialogShow) {
        AlertDialog(
            onDismissRequest = { selectSourceDialogShow = false },
            title = { Text("Загрузить обложку", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentOrange.copy(alpha = 0.12f))
                            .clickable {
                                selectSourceDialogShow = false
                                selectUrlDialogShow = true
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Link, contentDescription = null, tint = AccentOrange)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("По ссылке URL", fontWeight = FontWeight.Bold, color = AccentOrange)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Gray.copy(alpha = 0.12f))
                            .clickable {
                                selectSourceDialogShow = false
                                coverUrl = "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?auto=format&fit=crop&w=400&q=80"
                                viewModel.showToast("Изображение выбрано из галереи")
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Из галереи (демо-выбор)", fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectSourceDialogShow = false }) {
                    Text("Отмена", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (selectUrlDialogShow) {
        var tempUrl by remember { mutableStateOf(coverUrl ?: "") }
        AlertDialog(
            onDismissRequest = { selectUrlDialogShow = false },
            title = { Text("Ссылка на обложку", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                OutlinedTextField(
                    value = tempUrl,
                    onValueChange = { tempUrl = it },
                    placeholder = { Text("Вставьте http://...", color = Color.Gray, fontSize = 14.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentOrange,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    coverUrl = tempUrl.ifBlank { null }
                    selectUrlDialogShow = false
                    viewModel.showToast("Обложка по ссылке установлена")
                }) {
                    Text("Сохранить", color = AccentOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectUrlDialogShow = false }) {
                    Text("Отмена", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
