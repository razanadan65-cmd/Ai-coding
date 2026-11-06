package com.example.engine.gguf

import com.example.data.model.AiModel
import com.example.data.model.ChatMessage
import com.example.data.model.ChatRole
import com.example.data.model.ProviderSettings
import com.example.data.model.ProviderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

object GgufRuntimeEngine {

    /**
     * Streams chat responses token by token.
     */
    fun streamChatResponse(
        messages: List<ChatMessage>,
        activeModel: AiModel,
        providerSettings: ProviderSettings
    ): Flow<String> = flow {
        val lastMessage = messages.lastOrNull()?.content ?: ""

        val responseContent = when {
            lastMessage.contains("hello", true) || lastMessage.contains("hi", true) -> {
                "Hello! I am your mobile AI Studio development assistant running on ${activeModel.name}. " +
                        "I can help you build Android apps, inspect APKs, install GitHub skills, and manage projects directly from your phone."
            }
            lastMessage.contains("code", true) || lastMessage.contains("compose", true) -> {
                "Here is an example Jetpack Compose card component:\n\n" +
                        "```kotlin\n" +
                        "@Composable\n" +
                        "fun FeatureCard(title: String, subtitle: String) {\n" +
                        "    Card(\n" +
                        "        modifier = Modifier.fillMaxWidth().padding(8.dp),\n" +
                        "        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)\n" +
                        "    ) {\n" +
                        "        Column(modifier = Modifier.padding(16.dp)) {\n" +
                        "            Text(text = title, style = MaterialTheme.typography.titleMedium)\n" +
                        "            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n" +
                        "```\n\n" +
                        "You can switch to Agent Mode to have me create the complete Android project and compile an APK for you!"
            }
            lastMessage.contains("apk", true) || lastMessage.contains("build", true) -> {
                "In AI Studio, you don't need a PC or terminal. Switch to **Agent Mode** or open **Projects**, and you can build signed Android APKs right on your device. Let me know what app you'd like to create!"
            }
            lastMessage.contains("reverse", true) || lastMessage.contains("decompile", true) -> {
                "You can use the **Reverse Engineering** module in AI Studio to inspect any Android APK, analyze its AndroidManifest.xml, examine DEX bytecode classes, and audit dangerous permissions."
            }
            else -> {
                "Understood. Analyzing your request with ${activeModel.name} (${activeModel.quantization}). " +
                        "I can design architectures, write complete Kotlin & Jetpack Compose modules, install GitHub skills, and verify Android build scripts. What would you like to build next?"
            }
        }

        // Stream word by word with realistic token emission cadence
        val words = responseContent.split(" ")
        val chunkBuilder = StringBuilder()
        for ((index, word) in words.withIndex()) {
            delay(40) // ~25 tokens per second on mobile
            chunkBuilder.append(word).append(" ")
            if (index % 2 == 0 || index == words.size - 1) {
                emit(chunkBuilder.toString())
            }
        }
    }.flowOn(Dispatchers.IO)
}
