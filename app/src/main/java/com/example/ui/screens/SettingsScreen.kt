package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProviderSettings
import com.example.data.model.ProviderType
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit
) {
    val settings by viewModel.providerSettings.collectAsState()
    val memoryStatus by viewModel.memoryStatus.collectAsState()
    val context = LocalContext.current

    var selectedProvider by remember { mutableStateOf(settings.activeProvider) }
    var openAiUrl by remember { mutableStateOf(settings.openAiBaseUrl) }
    var openAiKey by remember { mutableStateOf(settings.openAiApiKey) }
    var customUrl by remember { mutableStateOf(settings.customApiUrl) }
    var customKey by remember { mutableStateOf(settings.customApiKey) }
    var cpuThreads by remember { mutableIntStateOf(settings.cpuThreads) }
    var contextLength by remember { mutableIntStateOf(settings.contextLength) }
    var gpuAccel by remember { mutableStateOf(settings.gpuAcceleration) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Studio & Runtime Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configure AI inference providers, hardware resources, and display theme.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Appearance
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Appearance", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dark Theme")
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { onToggleDarkTheme(it) }
                        )
                    }
                }
            }
        }

        // Hybrid AI Provider System
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "AI Inference Provider", fontWeight = FontWeight.Bold)
                    Text(
                        text = "Local GGUF runs offline directly on device. Cloud providers require user-configured keys and will never be used without permission.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ProviderType.values().forEach { provider ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedProvider == provider,
                                onClick = { selectedProvider = provider }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (provider) {
                                    ProviderType.LOCAL_GGUF -> "Local GGUF Inference Engine (llama.cpp)"
                                    ProviderType.OPENAI_COMPATIBLE -> "OpenAI-Compatible Endpoint (Ollama / vLLM / Groq)"
                                    ProviderType.GEMINI_CLOUD -> "Gemini API Cloud Engine"
                                    ProviderType.CUSTOM_API -> "Custom Self-Hosted Inference Endpoint"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (selectedProvider == ProviderType.OPENAI_COMPATIBLE) {
                        OutlinedTextField(
                            value = openAiUrl,
                            onValueChange = { openAiUrl = it },
                            label = { Text("Base URL") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = openAiKey,
                            onValueChange = { openAiKey = it },
                            label = { Text("API Key (Stored locally)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (selectedProvider == ProviderType.CUSTOM_API) {
                        OutlinedTextField(
                            value = customUrl,
                            onValueChange = { customUrl = it },
                            label = { Text("Custom API URL") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = customKey,
                            onValueChange = { customKey = it },
                            label = { Text("Access Key") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Hardware & Low-End Optimization (4GB RAM safe)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Hardware & Resource Allocation", fontWeight = FontWeight.Bold)

                    Text("CPU Inference Threads: $cpuThreads")
                    Slider(
                        value = cpuThreads.toFloat(),
                        onValueChange = { cpuThreads = it.toInt() },
                        valueRange = 1f..8f,
                        steps = 6
                    )

                    Text("Context Window Size: $contextLength tokens")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1024, 2048, 4096, 8192).forEach { size ->
                            FilterChip(
                                selected = contextLength == size,
                                onClick = { contextLength = size },
                                label = { Text("$size") }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("GPU / NPU Acceleration")
                            Text("Uses Vulkan or NNAPI where available", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                        Switch(
                            checked = gpuAccel,
                            onCheckedChange = { gpuAccel = it }
                        )
                    }
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    val updated = settings.copy(
                        activeProvider = selectedProvider,
                        openAiBaseUrl = openAiUrl,
                        openAiApiKey = openAiKey,
                        customApiUrl = customUrl,
                        customApiKey = customKey,
                        cpuThreads = cpuThreads,
                        contextLength = contextLength,
                        gpuAcceleration = gpuAccel
                    )
                    viewModel.updateProviderSettings(updated)
                    Toast.makeText(context, "Settings updated successfully!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Settings")
            }
        }
    }
}
