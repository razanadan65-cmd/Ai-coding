package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BuildStatus
import com.example.data.model.Project
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToAgent: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToSkills: () -> Unit,
    onNavigateToModels: () -> Unit,
    onNavigateToReverse: () -> Unit
) {
    val memoryStatus by viewModel.memoryStatus.collectAsState()
    val models by viewModel.models.collectAsState()
    val skills by viewModel.skills.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val installedModels = models.filter { it.isDownloaded }
    val defaultModel = models.find { it.isDefault } ?: models.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI Studio",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Autonomous Mobile AI Development Platform",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = defaultModel?.quantization ?: "GGUF",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Two Primary Actions: New Chat & New Agent Project
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onNavigateToAgent,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_new_agent"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Agent Mode", fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToChat,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_new_chat"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Chat", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Memory & Low-RAM Monitor Banner (4GB devices optimization)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (memoryStatus.isLowMemory) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (memoryStatus.isLowMemory) Icons.Default.Warning else Icons.Default.Memory,
                            contentDescription = "RAM Monitor",
                            tint = if (memoryStatus.isLowMemory) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Device RAM: ${memoryStatus.availableRamMb} MB free / ${memoryStatus.totalRamMb} MB total",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (memoryStatus.is4GbOrLower) "Optimized for budget phones (≤4GB RAM)" else "GGUF runtime active",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.refreshMemoryStatus() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Memory", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Active Engine & Model Card
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Local GGUF Models",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToModels) {
                    Text("Manage (${installedModels.size})")
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(installedModels) { model ->
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .clickable { viewModel.setDefaultModel(model.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (model.isDefault) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = model.quantization,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                if (model.isDefault) {
                                    Text(
                                        text = "Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = model.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Size: ${model.formattedSize} • Context: ${model.contextLength}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Skills Quick Overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Installed Skills",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToSkills) {
                    Text("Browse Skills (${skills.size})")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                skills.take(3).forEach { skill ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = skill.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = skill.category.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Switch(
                                checked = skill.isEnabled,
                                onCheckedChange = { viewModel.toggleSkill(skill.id) }
                            )
                        }
                    }
                }
            }
        }

        // Recent Projects & Builds
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Projects & Builds",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToProjects) {
                    Text("View Workspace")
                }
            }

            if (projects.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No projects yet. Use Agent Mode to create your first app!")
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    projects.forEach { proj ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectProject(proj)
                                    onNavigateToProjects()
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = proj.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (proj.apkGenerated) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (proj.apkGenerated) "APK Ready" else proj.lastBuildStatus.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (proj.apkGenerated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = proj.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (proj.apkGenerated) {
                                        Button(
                                            onClick = {
                                                viewModel.selectProject(proj)
                                                viewModel.installProjectApk()
                                            },
                                            modifier = Modifier.height(36.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Install APK", fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                viewModel.selectProject(proj)
                                                viewModel.exportProjectApk()
                                            },
                                            modifier = Modifier.height(36.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Export APK", fontSize = 12.sp)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.selectProject(proj)
                                            viewModel.analyzeCurrentProjectApk()
                                            onNavigateToReverse()
                                        },
                                        modifier = Modifier.height(36.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.FindInPage, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Audit APK", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
