package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ReadTrackerViewModel
import com.example.ui.theme.AccentOrange
import androidx.compose.ui.text.font.FontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ReadTrackerViewModel,
    onNavigateToColorSettings: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val pendingImportBooks by viewModel.pendingImportBooks.collectAsState()

    // File Picker Activity contract launcher for importing Json
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.handleImportUri(context, uri)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                Spacer(modifier = Modifier.height(getAdaptiveStatusBarPadding()))
                TopAppBar(
                    title = { Text("Настройки", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = "Назад",
                                tint = MaterialTheme.colorScheme.onBackground
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
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            
            // THEME GROUP
            ThemeSettingsGroup(viewModel)
            Spacer(modifier = Modifier.height(20.dp))

            // ADVANCED FUNCTIONS GROUP
            AdvancedSettingsGroup(viewModel)
            Spacer(modifier = Modifier.height(20.dp))

            // APPEARANCE GROUPS
            AppearanceSettingsGroup(viewModel)
            Spacer(modifier = Modifier.height(20.dp))

            // COLOR SETTINGS
            CategoryHeader("Настройки цвета")
            CardGroup {
                ActionTile(
                    title = "Кастомизация цветов",
                    subtitle = "Настроить цвета интерфейса, типов и статусов",
                    icon = Icons.Rounded.Palette,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToColorSettings
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            // FILE MANAGEMENT DATA ACTIONS GROUP
            DataSettingsGroup(viewModel, context, importFileLauncher)
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Confirmation Import dialogues
    pendingImportBooks?.let { booksToImport ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelImport() },
            title = { Text("Импорт библиотеки", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Text(
                    text = "Будет загружено ${booksToImport.size} тайтлов. Текущая библиотека будет полностью заменена.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmImport() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Заменить", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.cancelImport() },
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

@Composable
fun ThemeSettingsGroup(viewModel: ReadTrackerViewModel) {
    val themeMode by viewModel.themeMode.collectAsState()

    CategoryHeader("Тема")
    CardGroup {
        listOf(
            Triple(0, "AMOLED", Icons.Rounded.DarkMode),
            Triple(1, "Тёмная", Icons.Rounded.Brightness2),
            Triple(2, "Светлая", Icons.Rounded.WbSunny)
        ).forEachIndexed { i, theme ->
            val optionMode = theme.first
            val isActive = themeMode == optionMode

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setThemeMode(optionMode) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = theme.third,
                    contentDescription = null,
                    tint = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = theme.second,
                    fontSize = 15.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.weight(1.0f)
                )
                Icon(
                    imageVector = if (isActive) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (i < 2) {
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f))
            }
        }
    }
}

@Composable
fun AdvancedSettingsGroup(viewModel: ReadTrackerViewModel) {
    val showBookmarks by viewModel.showBookmarks.collectAsState()
    val bookmarkPosition by viewModel.bookmarkPosition.collectAsState()
    val enableAdaptationStart by viewModel.enableAdaptationStart.collectAsState()
    val enableHybrid by viewModel.enableHybrid.collectAsState()
    val enableRating by viewModel.enableRating.collectAsState()
    val ratingScale by viewModel.ratingScale.collectAsState()
    val badgeLayoutMode by viewModel.badgeLayoutMode.collectAsState()

    CategoryHeader("Дополнительный функционал")
    CardGroup {
        // Bookmarks
        SwitchRow(
            title = "Закладки",
            subtitle = if (showBookmarks) "Поле введения текущей главы без влияния на статистику" else "Поле заметок отключено",
            checked = showBookmarks,
            onCheckedChange = { viewModel.setShowBookmarks(it) }
        )

        if (showBookmarks) {
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f))
            // Bookmark placement select row
            DropdownRow(
                title = "Расположение закладки",
                subtitle = "Положение текущей закладки в списке книг",
                options = listOf("Снизу" to 0, "В ряд" to 1),
                selectedValue = bookmarkPosition,
                onValueChange = { viewModel.setBookmarkPosition(it) }
            )
        }

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f))

        // Adaptations
        SwitchRow(
            title = "Читать после адаптации",
            subtitle = if (enableAdaptationStart) "Возможность указать том/главу, с которых вы начали" else "Функция \"Старт после адаптации\" отключена",
            checked = enableAdaptationStart,
            onCheckedChange = { viewModel.setEnableAdaptationStart(it) }
        )

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f))

        // Hybrid mode
        SwitchRow(
            title = "Гибридный формат LN+WN",
            subtitle = if (enableHybrid) "Позволяет объединить LN и WN в одной карточке" else "Раздельные карточки томов и глав",
            checked = enableHybrid,
            onCheckedChange = { viewModel.setEnableHybrid(it) }
        )

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f))

        // Ratings toggle
        SwitchRow(
            title = "Оценка тайтлов",
            subtitle = if (enableRating) "Возможность оценивать тайтлы" else "Функция выставления оценки отключена",
            checked = enableRating,
            onCheckedChange = { viewModel.setEnableRating(it) }
        )

        if (enableRating) {
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f))
            DropdownRow(
                title = "Шкала оценки",
                subtitle = "Инструмент шкалы градации оценок",
                options = listOf("5 звёзд" to 5, "10 звёзд" to 10),
                selectedValue = ratingScale,
                onValueChange = { viewModel.setRatingScale(it) }
            )
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f))
            DropdownRow(
                title = "Расположение оценки и типа",
                subtitle = "Выбрать, как располагать оценку и тип на карточке",
                options = listOf("В столбик" to 0, "В ряд" to 1),
                selectedValue = badgeLayoutMode,
                onValueChange = { viewModel.setBadgeLayoutMode(it) }
            )
        }
    }
}

@Composable
fun AppearanceSettingsGroup(viewModel: ReadTrackerViewModel) {
    val analyticsShowMode by viewModel.analyticsShowMode.collectAsState()
    val showCovers by viewModel.showCovers.collectAsState()
    val shortenNumbers by viewModel.shortenNumbers.collectAsState()
    val stackedStats by viewModel.stackedStats.collectAsState()
    val showShareButton by viewModel.showShareButton.collectAsState()
    val showWebChapters by viewModel.showWebChapters.collectAsState()
    val disableAnimations by viewModel.disableAnimations.collectAsState()
    val filterSpacing by viewModel.filterSpacing.collectAsState()
    val cardSpacing by viewModel.cardSpacing.collectAsState()
    val titleFontSize by viewModel.titleFontSize.collectAsState()

    CategoryHeader("Отображение")
    CardGroup {
        DropdownRow(
            title = "Показ в аналитике",
            subtitle = "Выбрать, какие типы отображать в отчётах",
            options = listOf(
                "Синглы и Веб" to 0,
                "Только синглы" to 1,
                "Только Веб" to 2,
                "Скрыто" to 3
            ),
            selectedValue = analyticsShowMode,
            onValueChange = { viewModel.setAnalyticsShowMode(it) }
        )
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f))
        SwitchRow(
            title = "Обложки тайтлов",
            subtitle = if (showCovers) "Показывать обложки в списке" else "Компактный вид без обложек",
            checked = showCovers,
            onCheckedChange = { viewModel.setShowCovers(it) }
        )
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f))
        SwitchRow(
            title = "Сокращать числа",
            subtitle = "Например: 150K вместо 150 000",
            checked = shortenNumbers,
            onCheckedChange = { viewModel.setShortenNumbers(it) }
        )
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f))
        SwitchRow(
            title = "Широкие карточки статистики",
            subtitle = "Располагать метрики аналитики друг под другом",
            checked = stackedStats,
            onCheckedChange = { viewModel.setStackedStats(it) }
        )
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f))
        SwitchRow(
            title = "Кнопка «Поделиться»",
            subtitle = "Отображать шторку экспорта в шапке библиотеки",
            checked = showShareButton,
            onCheckedChange = { viewModel.setShowShareButton(it) }
        )
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f))
        SwitchRow(
            title = "Главы для Веб-романов",
            subtitle = if (showWebChapters) "Показывать X/Y гл. на карточках" else "Прогресс глав скрыт в списке",
            checked = showWebChapters,
            onCheckedChange = { viewModel.setShowWebChapters(it) }
        )
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f))
        SwitchRow(
            title = "Отключение анимаций",
            subtitle = if (disableAnimations) "Анимации переходов выключены" else "Плавные переходы между страницами",
            checked = disableAnimations,
            onCheckedChange = { viewModel.setDisableAnimations(it) }
        )
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f))
        SliderRow(
            title = "Расстояние от шапки до фильтров",
            subtitle = "Отступ между надписью 'Библиотека' и панелью вкладок",
            value = filterSpacing,
            valueRange = 0.0f..30.0f,
            onValueChange = { viewModel.setFilterSpacing(it) }
        )
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f))
        SliderRow(
            title = "Расстояние в карточках",
            subtitle = "Настройка высоты и интервала строк внутри карточек",
            value = cardSpacing,
            valueRange = 0.0f..10.0f,
            onValueChange = { viewModel.setCardSpacing(it) }
        )
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f))
        SliderRow(
            title = "Размер названия тайтла",
            subtitle = "Настройка размера шрифта для заголовка на карточке",
            value = titleFontSize,
            valueRange = 10.0f..22.0f,
            valueSuffix = " sp",
            onValueChange = { viewModel.setTitleFontSize(it) }
        )
    }
}

@Composable
fun DataSettingsGroup(
    viewModel: ReadTrackerViewModel,
    context: Context,
    importFileLauncher: ManagedActivityResultLauncher<String, Uri?>
) {
    CategoryHeader("Данные")
    CardGroup {
        ActionTile(
            title = "Экспорт библиотеки",
            subtitle = "Сохранить в JSON-файл",
            icon = Icons.Rounded.UploadFile,
            color = Color(0xFF34D399),
            onClick = { viewModel.exportLibrary(context) }
        )
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f))
        ActionTile(
            title = "Импорт библиотеки",
            subtitle = "Загрузить из JSON-файла",
            icon = Icons.Rounded.DownloadForOffline,
            color = Color(0xFF60A5FA),
            onClick = { importFileLauncher.launch("application/json") }
        )
    }
}

@Composable
fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.0f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            Text(subtitle, fontSize = 11.sp, color = Color.Gray, lineHeight = 14.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.Gray.copy(alpha = 0.25f)
            )
        )
    }
}

@Composable
fun DropdownRow(
    title: String,
    subtitle: String,
    options: List<Pair<String, Int>>,
    selectedValue: Int,
    onValueChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentSelectedName = options.find { it.second == selectedValue }?.first ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.0f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            Text(subtitle, fontSize = 11.sp, color = Color.Gray)
        }
        
        Box {
            Text(
                text = currentSelectedName,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(vertical = 4.dp, horizontal = 8.dp)
            )
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.first, fontWeight = FontWeight.Medium) },
                        onClick = {
                            onValueChange(option.second)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
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
        Column(modifier = Modifier.weight(1.0f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SliderRow(
    title: String,
    subtitle: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueSuffix: String = "",
    onValueChange: (Float) -> Unit
) {
    var sliderValue by remember(value) { androidx.compose.runtime.mutableFloatStateOf(value) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.0f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
            Text(
                text = "${sliderValue.toInt()}$valueSuffix",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onValueChange(sliderValue) },
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = Color.Gray.copy(alpha = 0.24f)
            )
        )
    }
}

