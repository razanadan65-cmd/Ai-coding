package com.example.data.model

data class Skill(
    val id: String,
    val name: String,
    val description: String,
    val category: SkillCategory,
    val githubSourceUrl: String,
    val version: String,
    val installedAt: Long,
    val isEnabled: Boolean = true,
    val isCompatible: Boolean = true,
    val documentation: String,
    val instructions: List<String> = emptyList(),
    val toolsProvided: List<String> = emptyList(),
    val isCustomCreated: Boolean = false,
    val author: String = "AI Studio Community",
    val stars: Int = 0
)

enum class SkillCategory(val displayName: String) {
    ANDROID_DEV("Android Development"),
    KOTLIN_DEV("Kotlin Development"),
    JAVA_DEV("Java Development"),
    FLUTTER_DEV("Flutter Development"),
    UI_UX_DESIGN("UI/UX Design"),
    REVERSE_ENGINEERING("Reverse Engineering"),
    APK_ANALYSIS("APK Analysis"),
    BUG_FIXING("Bug Fixing"),
    DEBUGGING("Debugging"),
    SECURITY_ANALYSIS("Security Analysis"),
    GRADLE_BUILD("Gradle Build"),
    AUTOMATED_TESTING("Automated Testing"),
    DATABASE_DEV("Database Development")
}

data class SkillSearchItem(
    val repoName: String,
    val owner: String,
    val description: String,
    val category: SkillCategory,
    val repoUrl: String,
    val stars: Int,
    val version: String,
    val isInstalled: Boolean = false
)
