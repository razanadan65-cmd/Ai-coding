package com.example.engine.skills

import com.example.data.model.Skill
import com.example.data.model.SkillCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID

object SkillCreatorEngine {

    suspend fun generateSkillFromPrompt(
        prompt: String,
        suggestedName: String = ""
    ): Skill = withContext(Dispatchers.IO) {
        delay(650) // AI synthesis delay

        val title = if (suggestedName.isNotBlank()) {
            suggestedName
        } else {
            deriveSkillTitle(prompt)
        }

        val category = when {
            prompt.contains("reverse", true) || prompt.contains("apk", true) || prompt.contains("decompile", true) ->
                SkillCategory.REVERSE_ENGINEERING
            prompt.contains("security", true) || prompt.contains("vulnerability", true) ->
                SkillCategory.SECURITY_ANALYSIS
            prompt.contains("ui", true) || prompt.contains("design", true) || prompt.contains("compose", true) ->
                SkillCategory.UI_UX_DESIGN
            prompt.contains("bug", true) || prompt.contains("fix", true) || prompt.contains("error", true) ->
                SkillCategory.BUG_FIXING
            prompt.contains("flutter", true) ->
                SkillCategory.FLUTTER_DEV
            prompt.contains("database", true) || prompt.contains("room", true) || prompt.contains("sql", true) ->
                SkillCategory.DATABASE_DEV
            else ->
                SkillCategory.ANDROID_DEV
        }

        val documentation = """
            # $title
            
            ## Overview
            $prompt
            
            ## Execution Instructions
            1. Verify the target project environment and dependencies.
            2. Apply domain-specific rules for ${category.displayName}.
            3. Ensure high cohesion, clean architecture, and mobile accessibility.
            4. Validate output before finalizing changes.
            
            ## Safety & Guardrails
            - Never execute destructive operations without user confirmation.
            - Restrict execution to the active project workspace.
            - Ensure generated code adheres to modern Android security policies.
        """.trimIndent()

        val skill = Skill(
            id = "custom-skill-${UUID.randomUUID().toString().take(8)}",
            name = title,
            description = prompt,
            category = category,
            githubSourceUrl = "https://aistudio.local/skills/${title.lowercase().replace(" ", "-")}",
            version = "1.0.0",
            installedAt = System.currentTimeMillis(),
            isEnabled = true,
            isCompatible = true,
            documentation = documentation,
            instructions = listOf(
                "Initialize context for $title.",
                "Execute domain tasks adhering to ${category.displayName}.",
                "Validate changes and log operations."
            ),
            toolsProvided = listOf("${title.lowercase().replace(" ", "_")}_tool"),
            isCustomCreated = true,
            author = "AI Studio Generator"
        )

        SkillsRepository.addSkill(skill)
        skill
    }

    private fun deriveSkillTitle(prompt: String): String {
        val cleaned = prompt.replace(Regex("(?i)^(create a skill that|create a skill to|build a skill for|create a skill|make a skill)"), "").trim()
        val words = cleaned.split(" ").filter { it.isNotBlank() }.take(5)
        return if (words.isNotEmpty()) {
            words.joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } } + " Specialist"
        } else {
            "Custom Autonomous Skill"
        }
    }
}
