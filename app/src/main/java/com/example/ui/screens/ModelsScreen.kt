package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiModel
import com.example.engine.gguf.DeviceMemoryMonitor
import com.example.viewmodel.AppViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelsScreen(viewModel: AppViewModel) {
    val models by viewModel.models.collectAsState()
    val memoryStatus by viewModel.memoryStatus.collectAsState()
    val capabilityTest by viewModel.capabilityTest.collectAsState()
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var testingModel by remember { mutableStateOf<AiModel?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importFilePath by remember { mutableStateOf("/sdcard/Download/custom-model-q4_k_m.gguf") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("models_screen")
    ) {
        // Tab Navigation
        TabRow(selectedTabIndex = selectedTabIndex) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Installed") }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Hugging Face Hub") }
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                text = { Text("Capability Benchmark") }
            )
        }

        // RAM & Device Optimization Banner
        Surface(
            color = if (memoryStatus.isLowMemory) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Default.Memory,
                        contentDescription = null,
                        tint = if (memoryStatus.isLowMemory) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "RAM Available: ${memoryStatus.availableRamMb} MB / ${memoryStatus.totalRamMb} MB",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                TextButton(
                    onClick = { showImportDialog = true }
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import .gguf")
                }
            }
        }

        when (selectedTabIndex) {
            0 -> {
                // Installed Models
                val installed = models.filter { it.isDownloaded }
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Installed GGUF Runtimes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Select a default model to drive chat, autonomous planning, and APK fixing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(installed) { model ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (model.isDefault) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = model.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${model.quantization} • ${model.formattedSize} • ${model.architecture}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    RadioButton(
                                        selected = model.isDefault,
                                        onClick = { viewModel.setDefaultModel(model.id) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = model.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Context: ${model.contextLength} tokens",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(
                                            onClick = {
                                                testingModel = model
                                                viewModel.runModelCapabilityTest(model)
                                                selectedTabIndex = 2
                                            }
                                        ) {
                                            Text("Run Test")
                                        }

                                        IconButton(
                                            onClick = { viewModel.deleteModel(model.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Hugging Face Hub
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Hugging Face Model Store",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Download compact, mobile-optimized GGUF models with resumable download support.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(models) { model ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = model.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${model.quantization} • ${model.formattedSize}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    if (model.isDownloaded) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "Installed",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    } else if (model.isDownloading) {
                                        CircularProgressIndicator(
                                            progress = { model.downloadProgress },
                                            modifier = Modifier.size(32.dp)
                                        )
                                    } else {
                                        Button(
                                            onClick = { viewModel.downloadModel(model) },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Get Model")
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = model.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (model.isDownloading) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { model.downloadProgress },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = "Downloading: ${(model.downloadProgress * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Capability Benchmark
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Model Capability Benchmark",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Evaluates whether the active GGUF model can handle JSON generation, tool calling, multi-step planning, coding, and error correction.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val active = testingModel ?: models.find { it.isDefault } ?: models.first()

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "Target Model: ${active.name}", fontWeight = FontWeight.Bold)
                            Text(text = "Quantization: ${active.quantization} • Arch: ${active.architecture}", style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.runModelCapabilityTest(active) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Execute Capability Suite")
                            }
                        }
                    }

                    if (capabilityTest != null) {
                        val test = capabilityTest!!
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (test.isPassed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (test.isPassed) "PASSED FOR AGENT WORKFLOWS" else "TEST RESULTS",
                                        fontWeight = FontWeight.Bold,
                                        color = if (test.isPassed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                    Icon(
                                        imageVector = if (test.isPassed) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (test.isPassed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                }

                                BenchmarkScoreRow("Kotlin & Compose Coding", test.codingScore)
                                BenchmarkScoreRow("Strict JSON Generation", test.jsonGenerationScore)
                                BenchmarkScoreRow("Agent Tool Calling", test.toolCallingScore)
                                BenchmarkScoreRow("Multi-step Architecture Reasoning", test.reasoningScore)
                                BenchmarkScoreRow("Build Error Diagnosis & Repair", test.errorCorrectionScore)

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = test.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Import GGUF File Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Local GGUF Model") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select or enter the absolute path to a .gguf model file on your phone storage:")
                    OutlinedTextField(
                        value = importFilePath,
                        onValueChange = { importFilePath = it },
                        label = { Text("File Path") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val file = File(importFilePath)
                        viewModel.importGgufFile(file)
                        Toast.makeText(context, "Imported ${file.name}", Toast.LENGTH_SHORT).show()
                        showImportDialog = false
                    }
                ) { Text("Import") }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun BenchmarkScoreRow(label: String, score: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
            Text(text = "$score / 100", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = if (score >= 80) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}
