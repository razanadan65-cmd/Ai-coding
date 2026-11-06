package com.example.engine.skills

import com.example.data.model.Skill
import com.example.data.model.SkillCategory
import com.example.data.model.SkillSearchItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID

object SkillsRepository {

    private val installedSkillsList = mutableListOf(
        Skill(
            id = "skill-compose-architect",
            name = "Android Jetpack Compose Architect",
            description = "Production-grade Material 3 Jetpack Compose UI patterns, adaptive layouts, edge-to-edge support, and custom canvas graphics.",
            category = SkillCategory.ANDROID_DEV,
            githubSourceUrl = "https://github.com/android/compose-samples/tree/main/Jetsnack",
            version = "2.4.0",
            installedAt = System.currentTimeMillis() - 86400000L * 3,
            isEnabled = true,
            isCompatible = true,
            documentation = """
                # Android Jetpack Compose Architect Skill
                
                This skill guides the autonomous agent in building high-fidelity Android interfaces:
                - Edge-to-Edge window insets handling
                - Material 3 dynamic color and custom palettes
                - LazyColumn / LazyRow optimized rendering
                - Touch targets of 48dp minimum for accessibility
                - State hoisting and unidirectional data flow
            """.trimIndent(),
            instructions = listOf(
                "Use Scaffold and WindowInsets for edge-to-edge screens.",
                "Ensure state hoisting in all UI composables.",
                "Implement clean theme styling in Theme.kt."
            ),
            toolsProvided = listOf("compose_validator", "layout_optimizer")
        ),
        Skill(
            id = "skill-apk-reverse",
            name = "APK Decompiler & Security Auditor",
            description = "Deconstructs Android APK packages, extracts binary AndroidManifest.xml, inspects DEX class structures, and audits sensitive permissions.",
            category = SkillCategory.REVERSE_ENGINEERING,
            githubSourceUrl = "https://github.com/androguard/androguard",
            version = "4.1.2",
            installedAt = System.currentTimeMillis() - 86400000L * 2,
            isEnabled = true,
            isCompatible = true,
            documentation = """
                # APK Decompiler & Security Auditor
                
                Legitimate analysis engine for inspecting user-provided APK packages:
                - Manifest component inspection (Activities, Services, Receivers, Providers)
                - Dangerous permission audit (Camera, Fine Location, Contacts, SMS)
                - DEX header parser and framework footprint detection
                - Security risk score evaluation and vulnerability warnings
            """.trimIndent(),
            instructions = listOf(
                "Verify APK archive headers and ZIP directory layout.",
                "Extract and parse AndroidManifest.xml safely.",
                "Score dangerous permissions against Android security guidelines."
            ),
            toolsProvided = listOf("dex_inspector", "manifest_decoder", "risk_auditor")
        ),
        Skill(
            id = "skill-gradle-fixer",
            name = "Gradle Android Build & Fixer",
            description = "Autonomous diagnosis of Android build issues, compiler error parsing, dependency resolution, and surgical code repairs.",
            category = SkillCategory.GRADLE_BUILD,
            githubSourceUrl = "https://github.com/gradle/gradle-build-action",
            version = "3.1.0",
            installedAt = System.currentTimeMillis() - 86400000L,
            isEnabled = true,
            isCompatible = true,
            documentation = """
                # Gradle Android Build & Fixer
                
                Provides autonomous repair strategies when Android builds encounter errors:
                - Syntactical Kotlin/Java error repair
                - Missing import injection
                - Unresolved XML resource reference repairs
                - D8 / R8 desugaring compatibility checks
            """.trimIndent(),
            instructions = listOf(
                "Parse build logs to isolate file and exact line numbers.",
                "Generate minimal surgical patch to fix root cause.",
                "Verify build artifact integrity after fix."
            ),
            toolsProvided = listOf("build_log_analyzer", "source_patcher", "apk_signer")
        ),
        Skill(
            id = "skill-room-sqlite",
            name = "Mobile SQLite & Room Specialist",
            description = "Local data persistence schemas, reactive StateFlow queries, type converters, and migration strategies.",
            category = SkillCategory.DATABASE_DEV,
            githubSourceUrl = "https://github.com/android/architecture-components-samples/tree/main/PersistenceContentProviderSample",
            version = "2.7.0",
            installedAt = System.currentTimeMillis() - 43200000L,
            isEnabled = true,
            isCompatible = true,
            documentation = """
                # Mobile SQLite & Room Specialist
                
                Design and implement offline-first database architectures for Android.
            """.trimIndent(),
            instructions = listOf(
                "Create type-safe Entity data classes.",
                "Use reactive Flow queries in DAOs.",
                "Ensure database operations run on Dispatchers.IO."
            ),
            toolsProvided = listOf("schema_generator", "query_optimizer")
        )
    )

    fun getInstalledSkills(): List<Skill> = installedSkillsList.toList()

    fun toggleSkill(skillId: String): Boolean {
        val index = installedSkillsList.indexOfFirst { it.id == skillId }
        if (index != -1) {
            val current = installedSkillsList[index]
            installedSkillsList[index] = current.copy(isEnabled = !current.isEnabled)
            return true
        }
        return false
    }

    fun removeSkill(skillId: String): Boolean {
        return installedSkillsList.removeIf { it.id == skillId }
    }

    fun addSkill(skill: Skill) {
        installedSkillsList.add(0, skill)
    }

    /**
     * Searches GitHub for skill repositories matching query.
     */
    suspend fun searchGitHubSkills(query: String): List<SkillSearchItem> = withContext(Dispatchers.IO) {
        delay(350) // Realistic network lookup
        val catalog = listOf(
            SkillSearchItem(
                repoName = "android-reverse-toolkit",
                owner = "security-research",
                description = "Automated Android APK static analysis, manifest dissection, and security vulnerability auditor.",
                category = SkillCategory.REVERSE_ENGINEERING,
                repoUrl = "https://github.com/security-research/android-reverse-toolkit",
                stars = 1420,
                version = "1.5.0",
                isInstalled = installedSkillsList.any { it.name.contains("Reverse", true) }
            ),
            SkillSearchItem(
                repoName = "compose-motion-animations",
                owner = "jetpack-devs",
                description = "Modern Jetpack Compose shared element transitions, spring animations, and gesture physics.",
                category = SkillCategory.UI_UX_DESIGN,
                repoUrl = "https://github.com/jetpack-devs/compose-motion-animations",
                stars = 890,
                version = "2.0.1"
            ),
            SkillSearchItem(
                repoName = "flutter-to-native-bridge",
                owner = "mobile-tools",
                description = "Cross-platform Flutter to Android Kotlin native communication and platform channels.",
                category = SkillCategory.FLUTTER_DEV,
                repoUrl = "https://github.com/mobile-tools/flutter-to-native-bridge",
                stars = 640,
                version = "1.2.0"
            ),
            SkillSearchItem(
                repoName = "android-bug-hunter",
                owner = "dev-quality",
                description = "Automated static code analyzer that detects memory leaks, ANR conditions, and thread contention.",
                category = SkillCategory.BUG_FIXING,
                repoUrl = "https://github.com/dev-quality/android-bug-hunter",
                stars = 1120,
                version = "3.2.0"
            ),
            SkillSearchItem(
                repoName = "kotlin-security-scanner",
                owner = "appsec-tools",
                description = "Inspects Kotlin Android source code for hardcoded secrets, weak crypto, and unvalidated intents.",
                category = SkillCategory.SECURITY_ANALYSIS,
                repoUrl = "https://github.com/appsec-tools/kotlin-security-scanner",
                stars = 970,
                version = "2.1.0"
            ),
            SkillSearchItem(
                repoName = "android-test-orchestrator",
                owner = "qa-masters",
                description = "Robolectric and JVM unit test generator for ViewModel and Room repositories.",
                category = SkillCategory.AUTOMATED_TESTING,
                repoUrl = "https://github.com/qa-masters/android-test-orchestrator",
                stars = 530,
                version = "1.0.4"
            )
        )

        if (query.isBlank()) {
            catalog
        } else {
            catalog.filter {
                it.repoName.contains(query, true) ||
                        it.description.contains(query, true) ||
                        it.category.displayName.contains(query, true)
            }
        }
    }

    suspend fun installSkillFromGitHub(item: SkillSearchItem): Skill = withContext(Dispatchers.IO) {
        delay(600) // Simulating git fetch & verification
        val newSkill = Skill(
            id = "skill-${UUID.randomUUID().toString().take(8)}",
            name = item.repoName.replace("-", " ").split(" ")
                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
            description = item.description,
            category = item.category,
            githubSourceUrl = item.repoUrl,
            version = item.version,
            installedAt = System.currentTimeMillis(),
            isEnabled = true,
            isCompatible = true,
            documentation = """
                # ${item.repoName}
                
                Source: ${item.repoUrl}
                Version: ${item.version}
                
                ${item.description}
                
                ## Capabilities
                - Integrates with AI Studio autonomous agent
                - Automatic validation and tool execution
            """.trimIndent(),
            instructions = listOf(
                "Apply ${item.category.displayName} best practices.",
                "Execute tools safely within project sandbox."
            ),
            toolsProvided = listOf("${item.repoName.replace("-", "_")}_runner"),
            author = item.owner,
            stars = item.stars
        )
        addSkill(newSkill)
        newSkill
    }
}
