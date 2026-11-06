package com.example.data.model

data class Project(
    val id: String,
    val name: String,
    val description: String,
    val technology: String = "Kotlin / Jetpack Compose",
    val packageName: String,
    val versionName: String = "1.0.0",
    val versionCode: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val apkGenerated: Boolean = false,
    val apkPath: String = "",
    val apkSizeBytes: Long = 0L,
    val lastBuildStatus: BuildStatus = BuildStatus.IDLE,
    val hasBuildError: Boolean = false,
    val rootDirPath: String = ""
)

enum class BuildStatus(val label: String) {
    IDLE("Ready"),
    BUILDING("Building APK..."),
    SUCCESS("Build Succeeded"),
    FAILED("Build Failed"),
    REBUILDING("Auto-Fixing & Rebuilding...")
}

data class ProjectFile(
    val relativePath: String,
    val name: String,
    val content: String,
    val isDirectory: Boolean = false,
    val extension: String = "",
    val sizeBytes: Long = 0L
)

data class BuildLogEntry(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val type: LogType = LogType.INFO
)

enum class LogType {
    INFO,
    WARNING,
    ERROR,
    SUCCESS,
    COMMAND
}
