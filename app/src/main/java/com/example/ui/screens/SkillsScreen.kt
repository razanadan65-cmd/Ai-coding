package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Skill
import com.example.data.model.SkillCategory
import com.example.data.model.SkillSearchItem
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsScreen(viewModel: AppViewModel) {
    val installedSkills by viewModel.skills.collectAsState()
    val searchResults by viewModel.skillSearchResults.collectAsState()
    val isSearching by viewModel.isSearchingSkills.collectAsState()
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var customUrlInput by remember { mutableStateOf("") }
    var activeDocSkill by remember { mutableStateOf<Skill?>(null) }

    // AI Skill Creator Inputs
    var aiSkillPrompt by remember {
        mutableStateOf("Create a skill that helps you analyze Android APK files and identify their project structure.")
    }
    var aiSkillName by remember { mutableStateOf("APK Structure Inspector") }
    var isCreatingSkill by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("skills_screen")
    ) {
        // Tab Navigation
        TabRow(selectedTabIndex = selectedTabIndex) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Installed (${installedSkills.size})") }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("GitHub Discovery") }
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                text = { Text("AI Skill Creator") }
            )
        }

        when (selectedTabIndex) {
            0 -> {
                // Installed Skills List
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Installed Autonomous Skills",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Skills guide the AI agent during planning, coding, and debugging.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(installedSkills) { skill ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (skill.isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
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
                                        Text(
                                            text = skill.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = skill.category.displayName,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "v${skill.version}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = skill.isEnabled,
                                        onCheckedChange = { viewModel.toggleSkill(skill.id) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = skill.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { activeDocSkill = skill }) {
                                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("View SKILL.md Documentation")
                                    }

                                    IconButton(
                                        onClick = { viewModel.removeSkill(skill.id) },
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
            1 -> {
                // GitHub Skills Discovery
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            text = "Search GitHub for Skills",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.searchGitHubSkills(it)
                            },
                            placeholder = { Text("Search skills (e.g. reverse, compose, bug)...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Direct Install via GitHub URL
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Install from Custom GitHub URL",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = customUrlInput,
                                        onValueChange = { customUrlInput = it },
                                        placeholder = { Text("https://github.com/owner/repo") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                    Button(
                                        onClick = {
                                            if (customUrlInput.isNotBlank()) {
                                                val item = SkillSearchItem(
                                                    repoName = customUrlInput.substringAfterLast("/"),
                                                    owner = "github-import",
                                                    description = "Imported skill package from $customUrlInput",
                                                    category = SkillCategory.ANDROID_DEV,
                                                    repoUrl = customUrlInput,
                                                    stars = 100,
                                                    version = "1.0.0"
                                                )
                                                viewModel.installSkill(item)
                                                customUrlInput = ""
                                                Toast.makeText(context, "Skill installed successfully!", Toast.LENGTH_SHORT).show()
                                                selectedTabIndex = 0
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Install")
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Discoverable Community Skills",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(searchResults) { repo ->
                        val alreadyInstalled = installedSkills.any { it.githubSourceUrl == repo.repoUrl || it.name.contains(repo.repoName, true) }

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
                                        Text(text = repo.repoName, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${repo.owner} • ★ ${repo.stars} stars",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.installSkill(repo)
                                            Toast.makeText(context, "Installed ${repo.repoName}!", Toast.LENGTH_SHORT).show()
                                        },
                                        enabled = !alreadyInstalled,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(if (alreadyInstalled) "Installed" else "Install Skill")
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = repo.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            2 -> {
                // AI Skill Creator
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "AI Skill Creator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Instruct the AI to generate a brand new SKILL.md with instructions, tool wrappers, and domain guardrails.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = aiSkillName,
                        onValueChange = { aiSkillName = it },
                        label = { Text("Skill Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = aiSkillPrompt,
                        onValueChange = { aiSkillPrompt = it },
                        label = { Text("Describe Skill Purpose and Instructions") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 4
                    )

                    Button(
                        onClick = {
                            if (aiSkillPrompt.isNotBlank()) {
                                isCreatingSkill = true
                                viewModel.createSkillWithAi(aiSkillPrompt, aiSkillName)
                                Toast.makeText(context, "Skill created & added to Skills Manager!", Toast.LENGTH_LONG).show()
                                isCreatingSkill = false
                                selectedTabIndex = 0
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isCreatingSkill
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate & Register Skill")
                    }
                }
            }
        }
    }

    // SKILL.md Documentation Dialog
    if (activeDocSkill != null) {
        val skill = activeDocSkill!!
        AlertDialog(
            onDismissRequest = { activeDocSkill = null },
            title = { Text(skill.name) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = skill.documentation,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { activeDocSkill = null }) {
                    Text("Close")
                }
            }
        )
    }
}
