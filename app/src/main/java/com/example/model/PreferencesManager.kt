package com.example.model

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ReadTrackerPrefs", Context.MODE_PRIVATE)

    // Flow backing for real-time reactive updates
    private val _themeMode = MutableStateFlow(prefs.getInt("themeMode", 1))
    val themeMode: StateFlow<Int> = _themeMode

    private val _shortenNumbers = MutableStateFlow(prefs.getBoolean("shortenNumbers", false))
    val shortenNumbers: StateFlow<Boolean> = _shortenNumbers

    private val _showShareButton = MutableStateFlow(prefs.getBoolean("showShareButton", false))
    val showShareButton: StateFlow<Boolean> = _showShareButton

    private val _stackedStats = MutableStateFlow(prefs.getBoolean("stackedStats", false))
    val stackedStats: StateFlow<Boolean> = _stackedStats

    private val _showCovers = MutableStateFlow(prefs.getBoolean("showCovers", false))
    val showCovers: StateFlow<Boolean> = _showCovers

    private val _showWebChapters = MutableStateFlow(prefs.getBoolean("showWebChapters", true))
    val showWebChapters: StateFlow<Boolean> = _showWebChapters

    private val _showBookmarks = MutableStateFlow(prefs.getBoolean("showBookmarks", true))
    val showBookmarks: StateFlow<Boolean> = _showBookmarks

    private val _bookmarkPosition = MutableStateFlow(prefs.getInt("bookmarkPosition", 0)) // 0 = Bottom, 1 = Inline (In row)
    val bookmarkPosition: StateFlow<Int> = _bookmarkPosition

    private val _enableAdaptationStart = MutableStateFlow(prefs.getBoolean("enableAdaptationStart", false))
    val enableAdaptationStart: StateFlow<Boolean> = _enableAdaptationStart

    private val _enableHybrid = MutableStateFlow(prefs.getBoolean("enableHybrid", true))
    val enableHybrid: StateFlow<Boolean> = _enableHybrid

    private val _enableRating = MutableStateFlow(prefs.getBoolean("enableRating", true))
    val enableRating: StateFlow<Boolean> = _enableRating

    private val _ratingScale = MutableStateFlow(prefs.getInt("ratingScale", 10)) // 5 or 10
    val ratingScale: StateFlow<Int> = _ratingScale

    private val _badgeLayoutMode = MutableStateFlow(prefs.getInt("badgeLayoutMode", 0)) // 0 = Column, 1 = Row
    val badgeLayoutMode: StateFlow<Int> = _badgeLayoutMode

    private val _analyticsShowMode = MutableStateFlow(prefs.getInt("analyticsShowMode", 0)) // 0 = Both, 1 = Only Singles, 2 = Only Web, 3 = None
    val analyticsShowMode: StateFlow<Int> = _analyticsShowMode

    private val _showWebInStats = MutableStateFlow(prefs.getBoolean("showWebInStats", true))
    val showWebInStats: StateFlow<Boolean> = _showWebInStats

    private val _savedTabIndex = MutableStateFlow(prefs.getInt("savedTabIndex", 0))
    val savedTabIndex: StateFlow<Int> = _savedTabIndex

    private val _disableAnimations = MutableStateFlow(prefs.getBoolean("disableAnimations", false))
    val disableAnimations: StateFlow<Boolean> = _disableAnimations

    private val _cardSpacing = MutableStateFlow(prefs.getFloat("cardSpacing", 2.0f))
    val cardSpacing: StateFlow<Float> = _cardSpacing

    private val _titleFontSize = MutableStateFlow(prefs.getFloat("titleFontSize", 14.0f))
    val titleFontSize: StateFlow<Float> = _titleFontSize

    private val _filterSpacing = MutableStateFlow(prefs.getFloat("filterSpacing", 0.0f))
    val filterSpacing: StateFlow<Float> = _filterSpacing

    // Custom colors flows
    private val _colorAccent = MutableStateFlow(prefs.getString("colorAccent", "#FF9F0A") ?: "#FF9F0A")
    val colorAccent: StateFlow<String> = _colorAccent

    private val _colorFormatHybrid = MutableStateFlow(prefs.getString("colorFormatHybrid", "#FF9F0A") ?: "#FF9F0A")
    val colorFormatHybrid: StateFlow<String> = _colorFormatHybrid

    private val _colorFormatSeries = MutableStateFlow(prefs.getString("colorFormatSeries", "#A78BFA") ?: "#A78BFA")
    val colorFormatSeries: StateFlow<String> = _colorFormatSeries

    private val _colorFormatWeb = MutableStateFlow(prefs.getString("colorFormatWeb", "#FBBF24") ?: "#FBBF24")
    val colorFormatWeb: StateFlow<String> = _colorFormatWeb

    private val _colorFormatSingle = MutableStateFlow(prefs.getString("colorFormatSingle", "#FF9F0A") ?: "#FF9F0A")
    val colorFormatSingle: StateFlow<String> = _colorFormatSingle

    private val _colorStatusPlanned = MutableStateFlow(prefs.getString("colorStatusPlanned", "#60A5FA") ?: "#60A5FA")
    val colorStatusPlanned: StateFlow<String> = _colorStatusPlanned

    private val _colorStatusReading = MutableStateFlow(prefs.getString("colorStatusReading", "#34D399") ?: "#34D399")
    val colorStatusReading: StateFlow<String> = _colorStatusReading

    private val _colorStatusPaused = MutableStateFlow(prefs.getString("colorStatusPaused", "#FBBF24") ?: "#FBBF24")
    val colorStatusPaused: StateFlow<String> = _colorStatusPaused

    private val _colorStatusCompleted = MutableStateFlow(prefs.getString("colorStatusCompleted", "#A78BFA") ?: "#A78BFA")
    val colorStatusCompleted: StateFlow<String> = _colorStatusCompleted

    private val _colorStatusDropped = MutableStateFlow(prefs.getString("colorStatusDropped", "#F87171") ?: "#F87171")
    val colorStatusDropped: StateFlow<String> = _colorStatusDropped

    private val _books = MutableStateFlow(JsonParser.jsonToBooks(prefs.getString("books", "")))
    val books: StateFlow<List<Book>> = _books

    fun setThemeMode(value: Int) {
        prefs.edit().putInt("themeMode", value).apply()
        _themeMode.value = value
    }

    fun setShortenNumbers(value: Boolean) {
        prefs.edit().putBoolean("shortenNumbers", value).apply()
        _shortenNumbers.value = value
    }

    fun setShowShareButton(value: Boolean) {
        prefs.edit().putBoolean("showShareButton", value).apply()
        _showShareButton.value = value
    }

    fun setStackedStats(value: Boolean) {
        prefs.edit().putBoolean("stackedStats", value).apply()
        _stackedStats.value = value
    }

    fun setShowCovers(value: Boolean) {
        prefs.edit().putBoolean("showCovers", value).apply()
        _showCovers.value = value
    }

    fun setShowWebChapters(value: Boolean) {
        prefs.edit().putBoolean("showWebChapters", value).apply()
        _showWebChapters.value = value
    }

    fun setShowBookmarks(value: Boolean) {
        prefs.edit().putBoolean("showBookmarks", value).apply()
        _showBookmarks.value = value
    }

    fun setBookmarkPosition(value: Int) {
        prefs.edit().putInt("bookmarkPosition", value).apply()
        _bookmarkPosition.value = value
    }

    fun setEnableAdaptationStart(value: Boolean) {
        prefs.edit().putBoolean("enableAdaptationStart", value).apply()
        _enableAdaptationStart.value = value
    }

    fun setEnableHybrid(value: Boolean) {
        prefs.edit().putBoolean("enableHybrid", value).apply()
        _enableHybrid.value = value
    }

    fun setEnableRating(value: Boolean) {
        prefs.edit().putBoolean("enableRating", value).apply()
        _enableRating.value = value
    }

    fun setRatingScale(value: Int) {
        prefs.edit().putInt("ratingScale", value).apply()
        _ratingScale.value = value
    }

    fun setBadgeLayoutMode(value: Int) {
        prefs.edit().putInt("badgeLayoutMode", value).apply()
        _badgeLayoutMode.value = value
    }

    fun setAnalyticsShowMode(value: Int) {
        prefs.edit().putInt("analyticsShowMode", value).apply()
        _analyticsShowMode.value = value
    }

    fun setShowWebInStats(value: Boolean) {
        prefs.edit().putBoolean("showWebInStats", value).apply()
        _showWebInStats.value = value
    }

    fun setSavedTabIndex(value: Int) {
        prefs.edit().putInt("savedTabIndex", value).apply()
        _savedTabIndex.value = value
    }

    fun setDisableAnimations(value: Boolean) {
        prefs.edit().putBoolean("disableAnimations", value).apply()
        _disableAnimations.value = value
    }

    fun setCardSpacing(value: Float) {
        prefs.edit().putFloat("cardSpacing", value).apply()
        _cardSpacing.value = value
    }

    fun setTitleFontSize(value: Float) {
        prefs.edit().putFloat("titleFontSize", value).apply()
        _titleFontSize.value = value
    }

    fun setFilterSpacing(value: Float) {
        prefs.edit().putFloat("filterSpacing", value).apply()
        _filterSpacing.value = value
    }

    fun setColorAccent(value: String) {
        prefs.edit().putString("colorAccent", value).apply()
        _colorAccent.value = value
    }

    fun setColorFormatHybrid(value: String) {
        prefs.edit().putString("colorFormatHybrid", value).apply()
        _colorFormatHybrid.value = value
    }

    fun setColorFormatSeries(value: String) {
        prefs.edit().putString("colorFormatSeries", value).apply()
        _colorFormatSeries.value = value
    }

    fun setColorFormatWeb(value: String) {
        prefs.edit().putString("colorFormatWeb", value).apply()
        _colorFormatWeb.value = value
    }

    fun setColorFormatSingle(value: String) {
        prefs.edit().putString("colorFormatSingle", value).apply()
        _colorFormatSingle.value = value
    }

    fun setColorStatusPlanned(value: String) {
        prefs.edit().putString("colorStatusPlanned", value).apply()
        _colorStatusPlanned.value = value
    }

    fun setColorStatusReading(value: String) {
        prefs.edit().putString("colorStatusReading", value).apply()
        _colorStatusReading.value = value
    }

    fun setColorStatusPaused(value: String) {
        prefs.edit().putString("colorStatusPaused", value).apply()
        _colorStatusPaused.value = value
    }

    fun setColorStatusCompleted(value: String) {
        prefs.edit().putString("colorStatusCompleted", value).apply()
        _colorStatusCompleted.value = value
    }

    fun setColorStatusDropped(value: String) {
        prefs.edit().putString("colorStatusDropped", value).apply()
        _colorStatusDropped.value = value
    }

    fun resetColorsToDefault() {
        prefs.edit()
            .putString("colorAccent", "#FF9F0A")
            .putString("colorFormatHybrid", "#FF9F0A")
            .putString("colorFormatSeries", "#A78BFA")
            .putString("colorFormatWeb", "#FBBF24")
            .putString("colorFormatSingle", "#FF9F0A")
            .putString("colorStatusPlanned", "#60A5FA")
            .putString("colorStatusReading", "#34D399")
            .putString("colorStatusPaused", "#FBBF24")
            .putString("colorStatusCompleted", "#A78BFA")
            .putString("colorStatusDropped", "#F87171")
            .apply()
        _colorAccent.value = "#FF9F0A"
        _colorFormatHybrid.value = "#FF9F0A"
        _colorFormatSeries.value = "#A78BFA"
        _colorFormatWeb.value = "#FBBF24"
        _colorFormatSingle.value = "#FF9F0A"
        _colorStatusPlanned.value = "#60A5FA"
        _colorStatusReading.value = "#34D399"
        _colorStatusPaused.value = "#FBBF24"
        _colorStatusCompleted.value = "#A78BFA"
        _colorStatusDropped.value = "#F87171"
    }

    fun saveBooks(value: List<Book>) {
        val json = JsonParser.booksToJson(value)
        prefs.edit().putString("books", json).apply()
        _books.value = value
    }
}
