package com.example.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.viewmodel.AppViewModel

enum class NavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    CHAT("Chat", Icons.Filled.Chat, Icons.Outlined.Chat, "nav_chat"),
    AGENT("Agent", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_agent"),
    PROJECTS("Projects", Icons.Filled.Code, Icons.Outlined.Code, "nav_projects"),
    SKILLS("Skills", Icons.Filled.Extension, Icons.Outlined.Extension, "nav_skills"),
    MODELS("Models", Icons.Filled.Memory, Icons.Outlined.Memory, "nav_models"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationScreen(
    viewModel: AppViewModel,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit
) {
    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }
    var showReverseScreen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (showReverseScreen) "Reverse Engineering" else currentTab.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    if (showReverseScreen) {
                        IconButton(onClick = { showReverseScreen = false }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (!showReverseScreen) {
                        IconButton(onClick = { showReverseScreen = true }) {
                            Icon(Icons.Default.FindInPage, contentDescription = "APK Reverse Engineering")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationTab.values().forEach { tab ->
                    val selected = !showReverseScreen && currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            showReverseScreen = false
                            currentTab = tab
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showReverseScreen) {
                ReverseEngineerScreen(viewModel = viewModel)
            } else {
                when (currentTab) {
                    NavigationTab.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToChat = { currentTab = NavigationTab.CHAT },
                        onNavigateToAgent = { currentTab = NavigationTab.AGENT },
                        onNavigateToProjects = { currentTab = NavigationTab.PROJECTS },
                        onNavigateToSkills = { currentTab = NavigationTab.SKILLS },
                        onNavigateToModels = { currentTab = NavigationTab.MODELS },
                        onNavigateToReverse = { showReverseScreen = true }
                    )
                    NavigationTab.CHAT -> ChatScreen(viewModel = viewModel)
                    NavigationTab.AGENT -> AgentScreen(
                        viewModel = viewModel,
                        onNavigateToProjects = { currentTab = NavigationTab.PROJECTS }
                    )
                    NavigationTab.PROJECTS -> ProjectsScreen(
                        viewModel = viewModel,
                        onNavigateToReverse = { showReverseScreen = true }
                    )
                    NavigationTab.SKILLS -> SkillsScreen(viewModel = viewModel)
                    NavigationTab.MODELS -> ModelsScreen(viewModel = viewModel)
                    NavigationTab.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = onToggleDarkTheme
                    )
                }
            }
        }
    }
}
