package com.example.data.model

data class ApkAnalysisResult(
    val fileName: String,
    val filePath: String,
    val fileSizeFormatted: String,
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val minSdk: Int,
    val targetSdk: Int,
    val permissions: List<String>,
    val dangerousPermissions: List<String>,
    val activities: List<String>,
    val services: List<String>,
    val receivers: List<String>,
    val providers: List<String>,
    val dexCount: Int,
    val nativeAbis: List<String>,
    val detectedFrameworks: List<String>,
    val securityRiskScore: SecurityRiskScore,
    val securityFindings: List<SecurityFinding>,
    val aiArchitectureReport: String
)

enum class SecurityRiskScore(val label: String, val level: Int) {
    LOW("Low Risk", 1),
    MEDIUM("Moderate Risk", 2),
    HIGH("High Risk", 3),
    CRITICAL("Critical Risk", 4)
}

data class SecurityFinding(
    val title: String,
    val description: String,
    val severity: SecurityRiskScore
)
