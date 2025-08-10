package com.example.shuttlereg.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ConcurrentHashMap

@Singleton
class CacheManager @Inject constructor(
    private val context: Context,
    val gson: Gson
) {
    val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("shuttlereg_cache", Context.MODE_PRIVATE)
    
    private val memoryCache = ConcurrentHashMap<String, Any>()
    private val cacheExpiryTimes = ConcurrentHashMap<String, Long>()
    
    companion object {
        const val CACHE_EXPIRY_TIME = 5 * 60 * 1000L // 5 minutes
        private const val TOURNAMENTS_CACHE_KEY = "tournaments_cache"
        private const val USER_PROFILE_CACHE_KEY = "user_profile_cache"
        private const val REGISTRATIONS_CACHE_KEY = "registrations_cache"
        private const val MATCHES_CACHE_KEY = "matches_cache"
    }

    /**
     * Cache data in memory with expiry
     */
    fun <T> cacheInMemory(key: String, data: T, expiryTimeMs: Long = CACHE_EXPIRY_TIME) {
        memoryCache[key] = data as Any
        cacheExpiryTimes[key] = System.currentTimeMillis() + expiryTimeMs
    }

    /**
     * Get data from memory cache
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getFromMemoryCache(key: String): T? {
        val expiryTime = cacheExpiryTimes[key] ?: return null
        
        if (System.currentTimeMillis() > expiryTime) {
            // Cache expired, remove it
            memoryCache.remove(key)
            cacheExpiryTimes.remove(key)
            return null
        }
        
        return memoryCache[key] as? T
    }

    /**
     * Cache data in SharedPreferences (persistent)
     */
    fun <T> cacheToDisk(key: String, data: T) {
        val jsonString = gson.toJson(data)
        sharedPreferences.edit {
            putString(key, jsonString)
            putLong("${key}_timestamp", System.currentTimeMillis())
        }
    }

    /**
     * Get data from SharedPreferences cache
     */
    inline fun <reified T> getFromDiskCache(key: String, maxAgeMs: Long = CACHE_EXPIRY_TIME): T? {
        val timestamp = sharedPreferences.getLong("${key}_timestamp", 0)
        
        if (System.currentTimeMillis() - timestamp > maxAgeMs) {
            // Cache expired
            clearDiskCache(key)
            return null
        }
        
        val jsonString = sharedPreferences.getString(key, null) ?: return null
        return try {
            gson.fromJson(jsonString, object : TypeToken<T>() {}.type)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Clear specific cache entry
     */
    fun clearCache(key: String) {
        memoryCache.remove(key)
        cacheExpiryTimes.remove(key)
        clearDiskCache(key)
    }

    fun clearDiskCache(key: String) {
        sharedPreferences.edit {
            remove(key)
            remove("${key}_timestamp")
        }
    }

    /**
     * Clear all cache
     */
    fun clearAllCache() {
        memoryCache.clear()
        cacheExpiryTimes.clear()
        sharedPreferences.edit { clear() }
    }

    /**
     * Get cache size for monitoring
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            memoryCacheSize = memoryCache.size,
            diskCacheSize = sharedPreferences.all.size / 2, // Each entry has data + timestamp
            memoryItems = memoryCache.keys.toList(),
            diskItems = sharedPreferences.all.keys.filter { !it.endsWith("_timestamp") }
        )
    }
}

data class CacheStats(
    val memoryCacheSize: Int,
    val diskCacheSize: Int,
    val memoryItems: List<String>,
    val diskItems: List<String>
)

@Singleton
class ImageCache @Inject constructor(
    private val context: Context
) {
    private val imageMemoryCache = ConcurrentHashMap<String, ByteArray>()
    private val maxCacheSize = 50 * 1024 * 1024 // 50MB max cache
    private var currentCacheSize = 0L

    fun cacheImage(url: String, imageData: ByteArray) {
        if (currentCacheSize + imageData.size > maxCacheSize) {
            // Clear oldest entries to make space
            clearOldestEntries(imageData.size)
        }
        
        imageMemoryCache[url] = imageData
        currentCacheSize += imageData.size
    }

    fun getImage(url: String): ByteArray? {
        return imageMemoryCache[url]
    }

    private fun clearOldestEntries(requiredSpace: Int) {
        val iterator = imageMemoryCache.entries.iterator()
        var freedSpace = 0L
        
        while (iterator.hasNext() && freedSpace < requiredSpace) {
            val entry = iterator.next()
            freedSpace += entry.value.size
            currentCacheSize -= entry.value.size
            iterator.remove()
        }
    }

    fun clearImageCache() {
        imageMemoryCache.clear()
        currentCacheSize = 0L
    }
}

@Singleton
class NetworkRequestOptimizer @Inject constructor() {
    
    private val pendingRequests = ConcurrentHashMap<String, MutableStateFlow<Any?>>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Deduplicate network requests - if same request is already in progress, 
     * return the existing flow instead of making a new request
     */
    fun <T> deduplicateRequest(
        key: String,
        request: suspend () -> T
    ): Flow<T?> {
        val existingFlow = pendingRequests[key]
        
        if (existingFlow != null) {
            @Suppress("UNCHECKED_CAST")
            return existingFlow.asStateFlow() as Flow<T?>
        }

        val newFlow = MutableStateFlow<T?>(null)
        pendingRequests[key] = newFlow as MutableStateFlow<Any?>
        
        // Execute request
        scope.launch {
            try {
                val result = request()
                newFlow.value = result
            } catch (e: Exception) {
                newFlow.value = null
            } finally {
                // Remove from pending requests after completion
                pendingRequests.remove(key)
            }
        }
        
        return newFlow.asStateFlow()
    }

    fun cancelRequest(key: String) {
        pendingRequests.remove(key)
    }
}

@Singleton
class MemoryManager @Inject constructor() {
    
    private val memoryThreshold = 0.8 // 80% memory usage threshold
    
    fun checkMemoryUsage(): MemoryInfo {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        val maxMemory = runtime.maxMemory()
        
        val memoryUsagePercentage = usedMemory.toDouble() / maxMemory.toDouble()
        
        return MemoryInfo(
            totalMemoryMB = totalMemory / (1024 * 1024),
            usedMemoryMB = usedMemory / (1024 * 1024),
            freeMemoryMB = freeMemory / (1024 * 1024),
            maxMemoryMB = maxMemory / (1024 * 1024),
            usagePercentage = memoryUsagePercentage,
            shouldClearCache = memoryUsagePercentage > memoryThreshold
        )
    }
    
    fun triggerGarbageCollection() {
        System.gc()
    }
    
    fun getMemoryPressureLevel(): MemoryPressureLevel {
        val memoryInfo = checkMemoryUsage()
        
        return when {
            memoryInfo.usagePercentage > 0.9 -> MemoryPressureLevel.CRITICAL
            memoryInfo.usagePercentage > 0.75 -> MemoryPressureLevel.HIGH
            memoryInfo.usagePercentage > 0.5 -> MemoryPressureLevel.MEDIUM
            else -> MemoryPressureLevel.LOW
        }
    }
}

data class MemoryInfo(
    val totalMemoryMB: Long,
    val usedMemoryMB: Long,
    val freeMemoryMB: Long,
    val maxMemoryMB: Long,
    val usagePercentage: Double,
    val shouldClearCache: Boolean
)

enum class MemoryPressureLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}

@Singleton
class PerformanceMonitor @Inject constructor(
    private val cacheManager: CacheManager,
    private val imageCache: ImageCache,
    private val memoryManager: MemoryManager
) {
    
    fun performOptimization() {
        val memoryPressure = memoryManager.getMemoryPressureLevel()
        
        when (memoryPressure) {
            MemoryPressureLevel.CRITICAL -> {
                // Aggressive cleanup
                cacheManager.clearAllCache()
                imageCache.clearImageCache()
                memoryManager.triggerGarbageCollection()
            }
            MemoryPressureLevel.HIGH -> {
                // Clear image cache and trigger GC
                imageCache.clearImageCache()
                memoryManager.triggerGarbageCollection()
            }
            MemoryPressureLevel.MEDIUM -> {
                // Clear only image cache
                imageCache.clearImageCache()
            }
            MemoryPressureLevel.LOW -> {
                // No action needed
            }
        }
    }
    
    fun getPerformanceReport(): PerformanceReport {
        val memoryInfo = memoryManager.checkMemoryUsage()
        val cacheStats = cacheManager.getCacheStats()
        
        return PerformanceReport(
            memoryInfo = memoryInfo,
            cacheStats = cacheStats,
            memoryPressureLevel = memoryManager.getMemoryPressureLevel(),
            recommendations = generateRecommendations(memoryInfo, cacheStats)
        )
    }
    
    private fun generateRecommendations(
        memoryInfo: MemoryInfo, 
        cacheStats: CacheStats
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (memoryInfo.usagePercentage > 0.8) {
            recommendations.add("High memory usage detected. Consider clearing cache.")
        }
        
        if (cacheStats.memoryCacheSize > 100) {
            recommendations.add("Large number of items in memory cache. Consider cleanup.")
        }
        
        if (memoryInfo.shouldClearCache) {
            recommendations.add("Memory threshold exceeded. Auto-cleanup recommended.")
        }
        
        return recommendations
    }
}

data class PerformanceReport(
    val memoryInfo: MemoryInfo,
    val cacheStats: CacheStats,
    val memoryPressureLevel: MemoryPressureLevel,
    val recommendations: List<String>
)