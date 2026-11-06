package com.example.data.model

data class AiModel(
    val id: String,
    val name: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val quantization: String, // e.g. "Q4_K_M", "Q8_0", "F16"
    val contextLength: Int, // e.g. 2048, 4096, 8192
    val architecture: String = "llama",
    val isDownloaded: Boolean = false,
    val downloadProgress: Float = 0f,
    val isDownloading: Boolean = false,
    val isDefault: Boolean = false,
    val providerType: ProviderType = ProviderType.LOCAL_GGUF,
    val hfRepo: String = "",
    val minRamMb: Int = 1500,
    val description: String = ""
) {
    val formattedSize: String
        get() {
            val mb = fileSizeBytes / (1024 * 1024)
            return if (mb >= 1024) String.format("%.2f GB", mb / 1024.0) else "$mb MB"
        }
}

enum class ProviderType {
    LOCAL_GGUF,
    OPENAI_COMPATIBLE,
    GEMINI_CLOUD,
    CUSTOM_API
}

data class ProviderSettings(
    val activeProvider: ProviderType = ProviderType.LOCAL_GGUF,
    val selectedModelId: String = "qwen-2.5-coder-1.5b",
    val openAiBaseUrl: String = "https://api.openai.com/v1",
    val openAiApiKey: String = "",
    val customApiUrl: String = "",
    val customApiKey: String = "",
    val cpuThreads: Int = 4,
    val contextLength: Int = 4096,
    val gpuAcceleration: Boolean = true
)

data class ModelCapabilityTestResult(
    val modelId: String,
    val codingScore: Int = 0, // 0-100
    val jsonGenerationScore: Int = 0,
    val toolCallingScore: Int = 0,
    val reasoningScore: Int = 0,
    val errorCorrectionScore: Int = 0,
    val testedAt: Long = 0L,
    val isPassed: Boolean = false,
    val notes: String = ""
)
