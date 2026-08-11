package com.tkno.apex.util

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

data class CleanHistoryEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val packageName: String,
    val appName: String,
    val bytesCleared: Long,
    val timestamp: Long = System.currentTimeMillis()
)

data class StopHistoryEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val packageName: String,
    val appName: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class HistoryTimePeriod {
    TODAY,
    YESTERDAY,
    LAST_7_DAYS,
    LAST_30_DAYS
}

object HistoryManager {
    private const val PREFS_NAME = "apex_history_prefs"
    private const val KEY_CLEAN_HISTORY = "clean_history"
    private const val KEY_STOP_HISTORY = "stop_history"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- Clean History ---

    fun getCleanHistory(context: Context): List<CleanHistoryEntry> {
        val jsonStr = getPrefs(context).getString(KEY_CLEAN_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<CleanHistoryEntry>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    CleanHistoryEntry(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        packageName = obj.optString("packageName", ""),
                        appName = obj.optString("appName", ""),
                        bytesCleared = obj.optLong("bytesCleared", 0L),
                        timestamp = obj.optLong("timestamp", 0L)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.sortedByDescending { it.timestamp }
    }

    fun addCleanRecords(context: Context, entries: List<CleanHistoryEntry>) {
        if (entries.isEmpty()) return
        val currentList = getCleanHistory(context).toMutableList()
        currentList.addAll(0, entries)
        val trimmed = if (currentList.size > 500) currentList.take(500) else currentList
        saveCleanHistory(context, trimmed)
    }

    private fun saveCleanHistory(context: Context, list: List<CleanHistoryEntry>) {
        val jsonArray = JSONArray()
        for (entry in list) {
            val obj = JSONObject()
            obj.put("id", entry.id)
            obj.put("packageName", entry.packageName)
            obj.put("appName", entry.appName)
            obj.put("bytesCleared", entry.bytesCleared)
            obj.put("timestamp", entry.timestamp)
            jsonArray.put(obj)
        }
        getPrefs(context).edit().putString(KEY_CLEAN_HISTORY, jsonArray.toString()).apply()
    }

    fun clearCleanHistory(context: Context) {
        getPrefs(context).edit().remove(KEY_CLEAN_HISTORY).apply()
    }

    // --- Stop History ---

    fun getStopHistory(context: Context): List<StopHistoryEntry> {
        val jsonStr = getPrefs(context).getString(KEY_STOP_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<StopHistoryEntry>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    StopHistoryEntry(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        packageName = obj.optString("packageName", ""),
                        appName = obj.optString("appName", ""),
                        timestamp = obj.optLong("timestamp", 0L)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.sortedByDescending { it.timestamp }
    }

    fun addStopRecords(context: Context, entries: List<StopHistoryEntry>) {
        if (entries.isEmpty()) return
        val currentList = getStopHistory(context).toMutableList()
        currentList.addAll(0, entries)
        val trimmed = if (currentList.size > 500) currentList.take(500) else currentList
        saveStopHistory(context, trimmed)
    }

    private fun saveStopHistory(context: Context, list: List<StopHistoryEntry>) {
        val jsonArray = JSONArray()
        for (entry in list) {
            val obj = JSONObject()
            obj.put("id", entry.id)
            obj.put("packageName", entry.packageName)
            obj.put("appName", entry.appName)
            obj.put("timestamp", entry.timestamp)
            jsonArray.put(obj)
        }
        getPrefs(context).edit().putString(KEY_STOP_HISTORY, jsonArray.toString()).apply()
    }

    fun clearStopHistory(context: Context) {
        getPrefs(context).edit().remove(KEY_STOP_HISTORY).apply()
    }

    // --- Helper Filter Utilities ---

    fun isTimestampInPeriod(timestamp: Long, period: HistoryTimePeriod): Boolean {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val yesterdayStart = todayStart - (24 * 60 * 60 * 1000L)
        val last7DaysStart = todayStart - (6 * 24 * 60 * 60 * 1000L)
        val last30DaysStart = todayStart - (29 * 24 * 60 * 60 * 1000L)

        return when (period) {
            HistoryTimePeriod.TODAY -> timestamp >= todayStart
            HistoryTimePeriod.YESTERDAY -> timestamp in yesterdayStart until todayStart
            HistoryTimePeriod.LAST_7_DAYS -> timestamp >= last7DaysStart
            HistoryTimePeriod.LAST_30_DAYS -> timestamp >= last30DaysStart
        }
    }

    fun filterCleanHistoryByPeriod(list: List<CleanHistoryEntry>, period: HistoryTimePeriod): List<CleanHistoryEntry> {
        return list.filter { isTimestampInPeriod(it.timestamp, period) }
    }

    fun filterStopHistoryByPeriod(list: List<StopHistoryEntry>, period: HistoryTimePeriod): List<StopHistoryEntry> {
        return list.filter { isTimestampInPeriod(it.timestamp, period) }
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 MB"
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb >= 1024) {
            val gb = mb / 1024.0
            String.format("%.1f GB", gb)
        } else if (mb >= 1) {
            String.format("%.1f MB", mb)
        } else {
            val kb = bytes / 1024.0
            String.format("%.0f KB", kb)
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        if (timestamp <= 0) return ""
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}
