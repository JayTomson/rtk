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

    private val _showShareButton = MutableStateFlow(prefs.getBoolean("showShareButton", true))
    val showShareButton: StateFlow<Boolean> = _showShareButton

    private val _stackedStats = MutableStateFlow(prefs.getBoolean("stackedStats", false))
    val stackedStats: StateFlow<Boolean> = _stackedStats

    private val _showCovers = MutableStateFlow(prefs.getBoolean("showCovers", true))
    val showCovers: StateFlow<Boolean> = _showCovers

    private val _showWebChapters = MutableStateFlow(prefs.getBoolean("showWebChapters", false))
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

    fun saveBooks(value: List<Book>) {
        val json = JsonParser.booksToJson(value)
        prefs.edit().putString("books", json).apply()
        _books.value = value
    }
}
