package com.tkno.blueiris.model

import android.graphics.drawable.Drawable

data class AppCacheInfo(
    val packageName: String,
    val name: String,
    val cacheBytes: Long,
    val cacheSizeString: String,
    val appSizeBytes: Long = 0L,
    val appSizeString: String = "0 KB",
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false,
    val isStopped: Boolean = false,
    val isStoppable: Boolean = true,
    val isCleanable: Boolean = true,
    val installTime: Long = 0L,
    val isEnabled: Boolean = true,
    val lastUpdateTime: Long = 0L,
    val lastUsedTime: Long = 0L
)
