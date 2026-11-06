package com.example.engine.agent

data class AgentTool(
    val name: String,
    val description: String,
    val requiresConfirmation: Boolean = false
)

object ToolRegistry {
    val availableTools = listOf(
        AgentTool("project_create", "Initializes a new Android Studio project with modern Gradle configuration."),
        AgentTool("file_write", "Creates or updates source code and resource files."),
        AgentTool("file_read", "Inspects file content and syntax."),
        AgentTool("github_skill_search", "Searches GitHub for relevant agent skills and libraries."),
        AgentTool("skill_install", "Downloads and registers a skill into the Skills Manager."),
        AgentTool("build_apk", "Compiles source files, processes resources, and generates signed APK."),
        AgentTool("diagnose_fix_error", "Parses compiler errors and applies surgical patches."),
        AgentTool("apk_analyze", "Inspects APK package structure, permissions, and bytecode.")
    )
}
