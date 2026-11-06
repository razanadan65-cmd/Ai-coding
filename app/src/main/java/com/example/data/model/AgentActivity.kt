package com.example.data.model

data class AgentStep(
    val id: String,
    val title: String,
    val description: String,
    val status: StepStatus = StepStatus.PENDING,
    val toolName: String? = null,
    val toolInput: String? = null,
    val toolOutput: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class StepStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    SKIPPED
}

data class AgentSession(
    val id: String,
    val prompt: String,
    val projectId: String? = null,
    val projectName: String = "",
    val steps: List<AgentStep> = emptyList(),
    val isRunning: Boolean = false,
    val isCompleted: Boolean = false,
    val generatedApkPath: String? = null,
    val errorDetails: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachedFileName: String? = null,
    val codeLanguage: String? = null,
    val codeContent: String? = null,
    val modelName: String? = null
)

enum class ChatRole {
    USER,
    ASSISTANT,
    SYSTEM
}

data class ChatSession(
    val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val messages: List<ChatMessage> = emptyList(),
    val modelId: String = "qwen-2.5-coder-1.5b"
)
