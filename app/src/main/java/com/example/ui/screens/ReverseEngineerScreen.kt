package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SecurityRiskScore
import com.example.viewmodel.AppViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReverseEngineerScreen(viewModel: AppViewModel) {
    val analysis by viewModel.apkAnalysis.collectAsState()
    val context = LocalContext.current
    var importPath by remember { mutableStateOf("/sdcard/Download/sample-app.apk") }
    var showImportDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reverse_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header & Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.FindInPage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "APK Reverse Engineering & Security Audit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Inspect APK packages, verify manifest declarations, examine DEX bytecode structures, and audit security risks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.analyzeCurrentProjectApk() },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Inspect Project APK", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showImportDialog = true },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import External APK", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (analysis == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No APK analyzed yet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap 'Inspect Project APK' or import an APK from device storage.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            val report = analysis!!

            // Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = report.appName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = report.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Surface(
                                color = when (report.securityRiskScore) {
                                    SecurityRiskScore.LOW -> MaterialTheme.colorScheme.primaryContainer
                                    SecurityRiskScore.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                                    else -> MaterialTheme.colorScheme.errorContainer
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = report.securityRiskScore.label,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (report.securityRiskScore) {
                                        SecurityRiskScore.LOW -> MaterialTheme.colorScheme.primary
                                        SecurityRiskScore.MEDIUM -> MaterialTheme.colorScheme.onSecondaryContainer
                                        else -> MaterialTheme.colorScheme.error
                                    }
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        DetailRow("Version", "${report.versionName} (Build ${report.versionCode})")
                        DetailRow("SDK Targets", "Min: API ${report.minSdk} • Target: API ${report.targetSdk}")
                        DetailRow("Archive Size", report.fileSizeFormatted)
                        DetailRow("DEX Bytecode Files", "${report.dexCount} (.dex units)")
                        DetailRow("Architectures", if (report.nativeAbis.isEmpty()) "Any (Pure DEX)" else report.nativeAbis.joinToString(", "))
                        DetailRow("Identified Frameworks", report.detectedFrameworks.joinToString(", "))
                    }
                }
            }

            // Permissions Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Declared Permissions (${report.permissions.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        report.permissions.forEach { perm ->
                            val isDangerous = report.dangerousPermissions.contains(perm)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDangerous) Icons.Default.Warning else Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (isDangerous) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = perm.substringAfterLast("."),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isDangerous) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // AI Architecture Audit Report
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "AI Security & Architecture Audit",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = report.aiArchitectureReport,
                            color = Color(0xFFE2E8F0),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import APK for Analysis") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter path to APK file on phone storage:")
                    OutlinedTextField(
                        value = importPath,
                        onValueChange = { importPath = it },
                        label = { Text("APK Path") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val file = File(importPath)
                        viewModel.analyzeApkFile(file)
                        Toast.makeText(context, "Analyzing ${file.name}...", Toast.LENGTH_SHORT).show()
                        showImportDialog = false
                    }
                ) { Text("Analyze") }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
