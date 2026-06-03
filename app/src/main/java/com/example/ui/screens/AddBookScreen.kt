package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Book
import com.example.model.VolumeEntry
import com.example.viewmodel.ReadTrackerViewModel
import com.example.ui.theme.AccentOrange
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookScreen(
    viewModel: ReadTrackerViewModel,
    onNavigateBack: () -> Unit
) {
    val enableHybrid by viewModel.enableHybrid.collectAsState()
    val enableRating by viewModel.enableRating.collectAsState()
    val ratingScale by viewModel.ratingScale.collectAsState()
    val enableAdaptationStart by viewModel.enableAdaptationStart.collectAsState()
    val showBookmarks by viewModel.showBookmarks.collectAsState()
    val showWebChapters by viewModel.showWebChapters.collectAsState()

    var currentStep by remember { mutableStateOf(1) }
    val focusManager = LocalFocusManager.current

    // -- STEP 1 DATA --
    var title by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf<String?>(null) }
    var selectUrlDialogShow by remember { mutableStateOf(false) }
    var selectSourceDialogShow by remember { mutableStateOf(false) }

    // -- STEP 2 DATA --
    var status by remember { mutableStateOf(0) } // Planned
    var formatType by remember { mutableStateOf(1) } // 0=Hybrid, 1=Series, 2=Web, 3=Single
    var countVolumes by remember { mutableStateOf(true) }

    // -- STEP 3 DATA --
    var bookmarkText by remember { mutableStateOf("") }
    var startVolume by remember { mutableStateOf("") }
    var startChapter by remember { mutableStateOf("") }
    var bookRating by remember { mutableStateOf<Int?>(null) } // 1..10
    
    // Web chapters
    var readWebChapters by remember { mutableStateOf("") }
    var totalWebChapters by remember { mutableStateOf("") }

    // Words & calculations
    var useDetailedVolumes by remember { mutableStateOf(false) }
    var singleWords by remember { mutableStateOf("") }
    var singleVolumes by remember { mutableStateOf("") }
    
    // Detailed volumes list
    var volumeEntries = remember { mutableStateListOf<VolumeEntry>() }
    
    // Ongoing switch
    var isOngoing by remember { mutableStateOf(false) }
    var totalVolumesInSeries by remember { mutableStateOf("") }

    // Reset fields if format changed
    LaunchedEffect(formatType) {
        if (formatType == 2) { // Web
            countVolumes = false
        } else if (formatType == 0) { // Hybrid
            countVolumes = true
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                Spacer(modifier = Modifier.height(getAdaptiveStatusBarPadding()))
                TopAppBar(
                    title = { Text("Добавить тайтл", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (currentStep > 1) {
                                currentStep--
                            } else {
                                onNavigateBack()
                            }
                        }) {
                            Icon(
                                imageVector = if (currentStep == 1) Icons.Rounded.Close else Icons.Rounded.ArrowBack,
                                contentDescription = "Назад/Закрыть",
                                tint = AccentOrange
                            )
                        }
                    },
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            // Footer bottom button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding(),
                color = MaterialTheme.colorScheme.background
            ) {
                Button(
                    onClick = {
                        if (currentStep < 3) {
                            if (currentStep == 1 && title.isBlank()) {
                                viewModel.showToast("Введите название тайтла", isSuccess = false)
                                return@Button
                            }
                            currentStep++
                        } else {
                            // Validation & Save Book
                            if (title.isBlank()) {
                                viewModel.showToast("Название тайтла не может быть пустым", isSuccess = false)
                                return@Button
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
                                0 -> 0xFF2D3C4F.toInt() // Muted planned blue-gray
                                1 -> 0xFF1B4D3E.toInt() // Deep reading green
                                2 -> 0xFF4D3F1E.toInt() // Muted paused brown
                                3 -> 0xFF352B4D.toInt() // Deep completed purple
                                4 -> 0xFF4A2525.toInt() // Reddish dropped
                                else -> 0xFF2B2B2D.toInt()
                            }

                            val newBook = Book(
                                id = UUID.randomUUID().toString(),
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

                            viewModel.addBook(newBook)
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentOrange,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentStep < 3) "Далее" else "Добавить",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (currentStep < 3) Icons.Rounded.ArrowForward else Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // STEP INDICATOR ROW
            StepIndicator(activeStep = currentStep) { step ->
                if (step < currentStep) {
                    currentStep = step
                }
            }

            Column(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                when (currentStep) {
                    1 -> Step1Content(
                        title = title,
                        onTitleChange = { title = it },
                        coverUrl = coverUrl,
                        onCoverClick = { selectSourceDialogShow = true }
                    )
                    2 -> Step2Content(
                        status = status,
                        onStatusChange = { status = it },
                        formatType = formatType,
                        onFormatChange = { formatType = it },
                        enableHybrid = enableHybrid,
                        countVolumes = countVolumes,
                        onCountVolumesChange = { countVolumes = it }
                    )
                    3 -> Step3Content(
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
                }
            }
        }
    }

    // Modal Cover Source selector
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
                                // Hardcode cover samples to simulate gallery select easily on emulators
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

    // Modal Web URL Input
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

@Composable
fun StepIndicator(activeStep: Int, onStepClick: (Int) -> Unit) {
    val stepNames = listOf("Обложка", "Статус", "Данные")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        stepNames.forEachIndexed { index, name ->
            val step = index + 1
            val isActive = step == activeStep
            val isPassed = step < activeStep

            val animatedLineColor by animateColorAsState(
                targetValue = if (isActive || isPassed) AccentOrange else Color.Gray.copy(alpha = 0.25f),
                animationSpec = tween(durationMillis = 280)
            )

            val animatedTextColor by animateColorAsState(
                targetValue = when {
                    isActive -> AccentOrange
                    isPassed -> Color.Gray
                    else -> Color.Gray.copy(alpha = 0.5f)
                },
                animationSpec = tween(durationMillis = 280)
            )

            Column(
                modifier = Modifier
                    .weight(1.0f)
                    .clickable(enabled = isPassed) { onStepClick(step) }
            ) {
                // Horizontal divider line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(animatedLineColor)
                )
                Spacer(modifier = Modifier.height(5.dp))
                // Text label below
                Text(
                    text = name,
                    fontSize = 11.sp,
                    color = animatedTextColor,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

// -- STEP 1 VIEW --
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step1Content(
    title: String,
    onTitleChange: (String) -> Unit,
    coverUrl: String?,
    onCoverClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Oblojka box container
        Box(
            modifier = Modifier
                .size(width = 138.dp, height = 200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .drawBehind {
                    if (coverUrl.isNullOrBlank()) {
                        drawRoundRect(
                            color = AccentOrange.copy(alpha = 0.4f),
                            style = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            ),
                            cornerRadius = CornerRadius(16.dp.toPx())
                        )
                    }
                }
                .clickable(onClick = onCoverClick),
            contentAlignment = Alignment.Center
        ) {
            if (!coverUrl.isNullOrBlank()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = "Обложка книги",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradient overlay with change action word
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Изменить", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = null,
                        tint = AccentOrange,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Обложка",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentOrange
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title input section
        Column(modifier = Modifier.fillMaxWidth()) {
            CategoryHeader("Название")
            
            val borderLineColor by animateColorAsState(
                targetValue = if (isFocused) AccentOrange else Color.Transparent
            )

            TextField(
                value = title,
                onValueChange = onTitleChange,
                placeholder = { Text("Введите название...", color = Color.Gray, fontSize = 14.sp) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isFocused) 1.5.dp else 0.dp,
                        color = borderLineColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .focusRequester(focusRequester)
                    .onFocusChanged { isFocused = it.isFocused }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Обложка необязательна", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

// -- STEP 2 VIEW --
@Composable
fun Step2Content(
    status: Int,
    onStatusChange: (Int) -> Unit,
    formatType: Int,
    onFormatChange: (Int) -> Unit,
    enableHybrid: Boolean,
    countVolumes: Boolean,
    onCountVolumesChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CategoryHeader("Статус")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(0, 1, 2, 3, 4).forEach { st ->
                val color = getStatusColor(st)
                val isActive = status == st
                val activeBgLine by animateColorAsState(
                    targetValue = if (isActive) color.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface
                )
                val activeBorderLine by animateColorAsState(
                    targetValue = if (isActive) color else Color.Transparent
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(activeBgLine)
                        .border(
                            width = 1.5.dp,
                            color = if (isActive) activeBorderLine else Color.LightGray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onStatusChange(st) }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = getStatusText(st),
                        fontSize = 15.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        color = if (isActive) color else MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.weight(1.0f))
                    if (isActive) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                        .clickable { onFormatChange(fType) }
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onFormatChange(fType) },
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

        // Switch "Uchityvat' toma"
        if (formatType != 2 && formatType != 0) { // Not Web and Not Hybrid
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
                    onCheckedChange = onCountVolumesChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AccentOrange,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.25f)
                    )
                )
            }
        }
    }
}

// -- STEP 3 VIEW --
@Composable
fun Step3Content(
    formatType: Int,
    countVolumes: Boolean,
    bookmarkText: String,
    onBookmarkChange: (String) -> Unit,
    showBookmarks: Boolean,
    enableAdaptationStart: Boolean,
    startVolume: String,
    onStartVolumeChange: (String) -> Unit,
    startChapter: String,
    onStartChapterChange: (String) -> Unit,
    enableRating: Boolean,
    ratingScale: Int,
    bookRating: Int?,
    onRatingChange: (Int?) -> Unit,
    readWebChapters: String,
    onReadWebChaptersChange: (String) -> Unit,
    totalWebChapters: String,
    onTotalWebChaptersChange: (String) -> Unit,
    useDetailedVolumes: Boolean,
    onUseDetailedVolumesChange: (Boolean) -> Unit,
    singleWords: String,
    onSingleWordsChange: (String) -> Unit,
    singleVolumes: String,
    onSingleVolumesChange: (String) -> Unit,
    volumeEntries: List<VolumeEntry>,
    onAddVolume: () -> Unit,
    onRemoveVolume: (Int) -> Unit,
    onUpdateVolume: (Int, VolumeEntry) -> Unit,
    isOngoing: Boolean,
    onOngoingChange: (Boolean) -> Unit,
    totalVolumesInSeries: String,
    onTotalVolumesInSeriesChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        
        // Block 1: Bookmark (if enabled)
        if (showBookmarks) {
            CategoryHeader("Закладка")
            OutlinedTextField(
                value = bookmarkText,
                onValueChange = onBookmarkChange,
                placeholder = { Text("Впишите главу/том, например: 1.4 глава, 1х3.3", color = Color.Gray, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Rounded.Bookmark, contentDescription = null, tint = AccentOrange) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = AccentOrange,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Block 2: Adaptation Starters (if enabled)
        if (enableAdaptationStart) {
            CategoryHeader("Старт после адаптации")
            if (formatType == 1 || formatType == 3) { // Series or Single, vols
                OutlinedTextField(
                    value = startVolume,
                    onValueChange = onStartVolumeChange,
                    label = { Text("Начальный том (с какого начали)", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = AccentOrange) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = AccentOrange,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            } else { // Web or Hybrid format type, chapters
                OutlinedTextField(
                    value = startChapter,
                    onValueChange = onStartChapterChange,
                    label = { Text("Начальная глава (с какой начали)", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = AccentOrange) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Block 3: Rating (if enabled)
        if (enableRating) {
            CategoryHeader("Оценка тайтла")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 12.dp, horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val starsAmount = if (ratingScale == 5) 5 else 10
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..starsAmount) {
                        val ratingStarValue = if (ratingScale == 5) i * 2 else i
                        val isStarred = bookRating != null && bookRating >= ratingStarValue
                        Icon(
                            imageVector = if (isStarred) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                            contentDescription = "Оценка $ratingStarValue",
                            tint = if (isStarred) AccentOrange else Color.Gray,
                            modifier = Modifier
                                .size(if (ratingScale == 5) 32.dp else 24.dp)
                                .clickable { onRatingChange(ratingStarValue) }
                        )
                    }
                }
                
                if (bookRating != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val chosenDisplay = if (ratingScale == 5) "${(bookRating+1)/2} из 5" else "$bookRating из 10"
                        Text(
                            text = "Выбрано: $chosenDisplay",
                            color = AccentOrange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Сбросить",
                            color = Color(0xFFF87171),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onRatingChange(null) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Block 4: Progressive web/hybrid chapters (only Web=2, Hybrid=0)
        if (formatType == 2) {
            CategoryHeader("Прогресс глав веб-новеллы")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = readWebChapters,
                    onValueChange = onReadWebChaptersChange,
                    label = { Text("Прочитано глав", fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = AccentOrange,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.0f)
                )
                OutlinedTextField(
                    value = totalWebChapters,
                    onValueChange = onTotalWebChaptersChange,
                    label = { Text("Всего глав (необяз.)", fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = AccentOrange,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.0f)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
        } else if (formatType == 0) { // Hybrid WN chapters
            CategoryHeader("Прогресс веб-глав (в гибриде)")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = readWebChapters,
                    onValueChange = onReadWebChaptersChange,
                    label = { Text("Прочитано глав WN", fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = AccentOrange,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.0f)
                )
                OutlinedTextField(
                    value = totalWebChapters,
                    onValueChange = onTotalWebChaptersChange,
                    label = { Text("Всего глав WN (необяз.)", fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = AccentOrange,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.0f)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Block 5: Words totals & volume calculations
        CategoryHeader("Слова и расчёты")
        if (formatType != 2 && countVolumes) { // Not Web, and Volumes are tracked
            // Switch row for "Raschet po tomam"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.0f)) {
                    Text("Расчёт по томам", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (useDetailedVolumes) "Записывать слова каждого тома" else "Ввести суммарно по книге",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = useDetailedVolumes,
                    onCheckedChange = { onUseDetailedVolumesChange(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AccentOrange,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.25f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (useDetailedVolumes) {
                // Dynamic listing of volumes
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    volumeEntries.forEachIndexed { i, entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = if (entry.v == entry.v.toInt().toDouble()) entry.v.toInt().toString() else entry.v.toString(),
                                onValueChange = { input ->
                                    val doubleVal = input.toDoubleOrNull() ?: entry.v
                                    onUpdateVolume(i, entry.copy(v = doubleVal))
                                },
                                label = { Text("Том", fontSize = 12.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = AccentOrange,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.width(76.dp)
                            )

                            OutlinedTextField(
                                value = if (entry.w == 0) "" else entry.w.toString(),
                                onValueChange = { input ->
                                    onUpdateVolume(i, entry.copy(w = input.toIntOrNull() ?: 0))
                                },
                                label = { Text("Слов", fontSize = 12.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = AccentOrange,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.0f)
                            )

                            // Trash delete button
                            IconButton(
                                onClick = { onRemoveVolume(i) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF87171).copy(alpha = 0.10f))
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Remove,
                                    contentDescription = "Удалить том",
                                    tint = Color(0xFFF87171)
                                )
                            }
                        }
                    }

                    // "+ Добавить том"
                    OutlinedButton(
                        onClick = onAddVolume,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentOrange),
                        border = BorderStroke(1.dp, AccentOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null, tint = AccentOrange)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Добавить том", fontWeight = FontWeight.Bold)
                    }

                    if (volumeEntries.isNotEmpty()) {
                        // Total display summary box
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentOrange.copy(alpha = 0.12f))
                                .border(1.dp, AccentOrange.copy(alpha = 0.20f), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Томов: ${volumeEntries.size}",
                                color = AccentOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Слов: ${formatNumber(volumeEntries.sumOf { it.w }, false)}",
                                color = AccentOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                // Manual total inputs СЛОВ + ТОМОВ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = singleWords,
                        onValueChange = onSingleWordsChange,
                        label = { Text("СЛОВ", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Rounded.TextFields, contentDescription = null, tint = Color.Gray) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = AccentOrange,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    )

                    OutlinedTextField(
                        value = singleVolumes,
                        onValueChange = onSingleVolumesChange,
                        label = { Text("ТОМОВ", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Rounded.Layers, contentDescription = null, tint = Color.Gray) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = AccentOrange,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.0f)
                    )
                }
            }
        } else {
            // Web / Single standard - strictly total words input only
            OutlinedTextField(
                value = singleWords,
                onValueChange = onSingleWordsChange,
                label = { Text("Количество прочитанных слов", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Rounded.TextFields, contentDescription = null, tint = Color.Gray) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

        // Block 6: Total volumes/Ongoing stats row (only relevant if countVolumes is true, and format is NOT Web)
        if (countVolumes && formatType != 2) {
            Spacer(modifier = Modifier.height(18.dp))
            CategoryHeader("Всего томов в серии")
            
            // Switch Row Ongoing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.0f)) {
                    Text("Онгоинг", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (isOngoing) "Отображается как N/?" else "Количество томов известно",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = isOngoing,
                    onCheckedChange = onOngoingChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AccentOrange,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.25f)
                    )
                )
            }

            if (!isOngoing) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = totalVolumesInSeries,
                    onValueChange = onTotalVolumesInSeriesChange,
                    placeholder = { Text("Необязательно — напр. 25", color = Color.Gray, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Rounded.BookmarkBorder, contentDescription = null, tint = Color.Gray) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
    }
}
