package com.example.engine.gguf

import android.app.ActivityManager
import android.content.Context
import java.io.RandomAccessFile

data class MemoryStatus(
    val totalRamMb: Long,
    val availableRamMb: Long,
    val usedRamMb: Long,
    val isLowMemory: Boolean,
    val is4GbOrLower: Boolean,
    val warningMessage: String? = null
)

object DeviceMemoryMonitor {

    fun getMemoryStatus(context: Context): MemoryStatus {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)

        val rawTotal = memoryInfo.totalMem / (1024 * 1024)
        val totalMb = if (rawTotal > 0) rawTotal else (Runtime.getRuntime().maxMemory() / (1024 * 1024)).coerceAtLeast(4096L)
        val rawAvail = memoryInfo.availMem / (1024 * 1024)
        val availMb = if (rawAvail > 0) rawAvail else (totalMb / 2)
        val usedMb = (totalMb - availMb).coerceAtLeast(0L)
        val isLow = memoryInfo.lowMemory || availMb < 600
        val is4GbOrLower = totalMb <= 4200 // 4GB phone has around 3600-4000MB reported

        val warning = when {
            availMb < 500 -> "Critical: Under 500MB RAM free. Large models will cause OOM crashes."
            availMb < 1000 -> "Warning: Less than 1GB free RAM. Recommended to use 0.5B - 1.5B Q4 models."
            is4GbOrLower -> "4GB RAM Device detected. Background models will be unloaded automatically."
            else -> null
        }

        return MemoryStatus(
            totalRamMb = totalMb,
            availableRamMb = availMb,
            usedRamMb = usedMb,
            isLowMemory = isLow,
            is4GbOrLower = is4GbOrLower,
            warningMessage = warning
        )
    }

    fun canSafelyRunModel(context: Context, requiredMb: Int): Pair<Boolean, String> {
        val status = getMemoryStatus(context)
        return if (status.availableRamMb >= requiredMb + 300) {
            Pair(true, "Device has ${status.availableRamMb} MB available RAM (needs ~${requiredMb} MB).")
        } else {
            Pair(
                false,
                "Insufficient RAM! Model needs ~${requiredMb} MB, but device only has ${status.availableRamMb} MB free."
            )
        }
    }
}
