package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BuildStatus
import com.example.data.model.LogType
import com.example.data.model.ProjectFile
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    viewModel: AppViewModel,
    onNavigateToReverse: () -> Unit
) {
    val selectedProject by viewModel.selectedProject.collectAsState()
    val projectFiles by viewModel.projectFiles.collectAsState()
    val buildLogs by viewModel.buildLogs.collectAsState()
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var currentFile by remember { mutableStateOf<ProjectFile?>(null) }
    var editedContent by remember { mutableStateOf("") }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFilePathInput by remember { mutableStateOf("") }

    LaunchedEffect(projectFiles) {
        if (currentFile == null && projectFiles.isNotEmpty()) {
            val file = projectFiles.first()
            currentFile = file
            editedContent = file.content
        } else if (currentFile != null) {
            val updated = projectFiles.find { it.relativePath == currentFile?.relativePath }
            if (updated != null) {
                currentFile = updated
                editedContent = updated.content
            }
        }
    }

    if (selectedProject == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No project selected. Run Agent Mode to create one!")
        }
        return
    }

    val project = selectedProject!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("projects_screen")
    ) {
        // Project Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${project.packageName} (v${project.versionName})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Build Status Pill
                    Surface(
                        color = when (project.lastBuildStatus) {
                            BuildStatus.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
                            BuildStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                            BuildStatus.BUILDING -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = project.lastBuildStatus.label,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (project.lastBuildStatus) {
                                BuildStatus.SUCCESS -> MaterialTheme.colorScheme.primary
                                BuildStatus.FAILED -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Build Action Chips Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.buildCurrentProject(isRelease = false) },
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Build Debug APK", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.buildCurrentProject(isRelease = true) },
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Build Release APK", fontSize = 11.sp)
                    }

                    // Deliberate Error + Auto-Fix button
                    OutlinedButton(
                        onClick = { viewModel.buildCurrentProject(isRelease = false, simulateError = true) },
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Trigger Error", fontSize = 11.sp)
                    }

                    if (project.hasBuildError) {
                        Button(
                            onClick = { viewModel.autoFixBuildErrors() },
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Auto-Fix & Rebuild", fontSize = 11.sp)
                        }
                    }

                    if (project.apkGenerated) {
                        Button(
                            onClick = { viewModel.installProjectApk() },
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Install APK", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.exportProjectApk() },
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export APK", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.analyzeCurrentProjectApk()
                                onNavigateToReverse()
                            },
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.FindInPage, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reverse Engineer APK", fontSize = 11.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.exportProjectSourceZip() },
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export Source ZIP", fontSize = 11.sp)
                    }
                }
            }
        }

        // Tabs: File Explorer, Code Editor, Build Logs
        TabRow(selectedTabIndex = selectedTabIndex) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Files (${projectFiles.size})") }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Code Editor") }
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                text = { Text("Build Logs") }
            )
        }

        // Tab Content
        when (selectedTabIndex) {
            0 -> {
                // File Explorer
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Project Files",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showNewFileDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "New File")
                        }
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(projectFiles) { file ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentFile = file
                                        editedContent = file.content
                                        selectedTabIndex = 1
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (currentFile?.relativePath == file.relativePath)
                                        MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                file.name.endsWith(".kt") -> Icons.Default.Code
                                                file.name.endsWith(".xml") -> Icons.Default.Description
                                                file.name.endsWith(".kts") -> Icons.Default.Settings
                                                else -> Icons.Default.InsertDriveFile
                                            },
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Column {
                                            Text(text = file.name, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                text = file.relativePath,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteProjectFile(file.relativePath) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Code Editor
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentFile?.name ?: "No file open",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                if (currentFile != null) {
                                    viewModel.saveFileContent(currentFile!!.relativePath, editedContent)
                                    Toast.makeText(context, "Saved ${currentFile!!.name}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = currentFile != null
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxSize().weight(1f)
                    ) {
                        OutlinedTextField(
                            value = editedContent,
                            onValueChange = { editedContent = it },
                            modifier = Modifier.fillMaxSize(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFFE2E8F0),
                                unfocusedTextColor = Color(0xFFCBD5E1),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
            2 -> {
                // Build Logs Console
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0B0F19))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Android Gradle Build Logs",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${buildLogs.size} lines",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (buildLogs.isEmpty()) {
                            item {
                                Text(
                                    text = "Ready to build. Tap 'Build Debug APK' above to execute.",
                                    color = Color(0xFF64748B),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        items(buildLogs) { log ->
                            Text(
                                text = log.message,
                                color = when (log.type) {
                                    LogType.SUCCESS -> Color(0xFF10B981)
                                    LogType.ERROR -> Color(0xFFF43F5E)
                                    LogType.WARNING -> Color(0xFFF59E0B)
                                    else -> Color(0xFFCBD5E1)
                                },
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("New File") },
            text = {
                OutlinedTextField(
                    value = newFilePathInput,
                    onValueChange = { newFilePathInput = it },
                    label = { Text("File Path (e.g. app/src/main/res/values/strings.xml)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFilePathInput.isNotBlank()) {
                            viewModel.addProjectFile(newFilePathInput, "// New file created")
                            newFilePathInput = ""
                            showNewFileDialog = false
                        }
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) { Text("Cancel") }
            }
        )
    }
}
