package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

private val colorCache = java.util.concurrent.ConcurrentHashMap<String, Color>()

// Hex Color parsing helper with highly optimized O(1) cache lookup
fun parseHexColor(hex: String, default: Color): Color {
    val key = hex.trim().replace("#", "").uppercase()
    if (key.length != 6 && key.length != 8) return default
    return colorCache.getOrPut(key) {
        try {
            if (key.length == 6) {
                val rgb = key.toLong(16)
                Color(0xFF000000 or rgb)
            } else {
                val argb = key.toLong(16)
                Color(argb)
            }
        } catch (_: Exception) {
            default
        }
    }
}

// Convert Color to Hex string
fun ColorToHex(color: Color): String {
    val r = (color.red * 255).toInt()
    val g = (color.green * 255).toInt()
    val b = (color.blue * 255).toInt()
    return String.format("#%02X%02X%02X", r, g, b)
}

// Formatting Numbers Helper
fun formatNumber(number: Int, shorten: Boolean): String {
    if (!shorten) {
        return "%,d".format(number).replace(",", " ")
    }
    return when {
         number >= 1_000_000 -> "%.1fM".format(number / 1_000_000.0).replace(".0", "")
         number >= 1_000 -> "%.0fK".format(number / 1_000.0)
         else -> number.toString()
    }
}

// Get Color based on Status (with dynamic color overrides)
fun getStatusColor(
    status: Int,
    plannedHex: String = "#60A5FA",
    readingHex: String = "#34D399",
    pausedHex: String = "#FBBF24",
    completedHex: String = "#A78BFA",
    droppedHex: String = "#F87171",
    accentHex: String = "#FF9F0A"
): Color {
    return when (status) {
        0 -> parseHexColor(plannedHex, StatusPlanned)
        1 -> parseHexColor(readingHex, StatusReading)
        2 -> parseHexColor(pausedHex, StatusPaused)
        3 -> parseHexColor(completedHex, StatusCompleted)
        4 -> parseHexColor(droppedHex, StatusDropped)
        else -> parseHexColor(accentHex, AccentOrange)
    }
}

// Get Color based on Publication Format (with dynamic color overrides)
fun getFormatColor(
    isHybrid: Boolean,
    isWeb: Boolean,
    isSingle: Boolean,
    isSeries: Boolean,
    hybridHex: String = "#FF9F0A",
    seriesHex: String = "#A78BFA",
    webHex: String = "#FBBF24",
    singleHex: String = "#FF9F0A",
    accentHex: String = "#FF9F0A"
): Color {
    return when {
         isHybrid -> parseHexColor(hybridHex, AccentOrange)
         isWeb -> parseHexColor(webHex, Color(0xFFFBBF24))
         isSingle -> parseHexColor(singleHex, parseHexColor(accentHex, AccentOrange))
         isSeries -> parseHexColor(seriesHex, Color(0xFFA78BFA))
         else -> parseHexColor(accentHex, AccentOrange)
    }
}

// Get Text based on Status in Russian
fun getStatusText(status: Int): String {
    return when (status) {
        0 -> "В планах"
        1 -> "Читаю"
        2 -> "На паузе"
        3 -> "Завершено"
        4 -> "Брошено"
        else -> "Неизвестно"
    }
}

// Custom Badge component
@Composable
fun BookBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

// Section Header/Category Title in CAPS
@Composable
fun CategoryHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color.Gray,
        letterSpacing = 0.6.sp,
        modifier = modifier.padding(bottom = 8.dp)
    )
}

// Action row used in Share Sheet or general listings
@Composable
fun ShareOptionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1.0f)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun getAdaptiveStatusBarPadding(): Dp {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    return remember(statusBarHeight) {
        // Use the exact system status bar height, fallback to 16.dp if system height is 0.
        // This ensures the content is safely placed below system icons and cutouts on all screens.
        if (statusBarHeight == 0.dp) {
            16.dp
        } else {
            statusBarHeight
        }
    }
}

@Composable
fun CardGroup(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        content()
    }
}

