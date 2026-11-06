package com.example.data.repository

import android.content.Context
import com.example.data.model.*
import com.example.engine.agent.AutonomousAgentEngine
import com.example.engine.builder.AndroidApkBuilder
import com.example.engine.builder.AutoFixEngine
import com.example.engine.gguf.GgufHeaderParser
import com.example.engine.gguf.GgufRuntimeEngine
import com.example.engine.gguf.HuggingFaceDownloader
import com.example.engine.gguf.ModelCapabilityTester
import com.example.engine.reverse.ApkAnalyzerEngine
import com.example.engine.skills.SkillCreatorEngine
import com.example.engine.skills.SkillsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class AppRepository(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)

    // Models State
    private val _models = MutableStateFlow<List<AiModel>>(HuggingFaceDownloader.popularPresetModels)
    val models: StateFlow<List<AiModel>> = _models.asStateFlow()

    // Provider Settings
    private val _providerSettings = MutableStateFlow(ProviderSettings())
    val providerSettings: StateFlow<ProviderSettings> = _providerSettings.asStateFlow()

    // Skills State
    private val _skills = MutableStateFlow<List<Skill>>(SkillsRepository.getInstalledSkills())
    val skills: StateFlow<List<Skill>> = _skills.asStateFlow()

    // Projects State
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject: StateFlow<Project?> = _selectedProject.asStateFlow()

    private val _projectFiles = MutableStateFlow<List<ProjectFile>>(emptyList())
    val projectFiles: StateFlow<List<ProjectFile>> = _projectFiles.asStateFlow()

    // Build Logs
    private val _buildLogs = MutableStateFlow<List<BuildLogEntry>>(emptyList())
    val buildLogs: StateFlow<List<BuildLogEntry>> = _buildLogs.asStateFlow()

    // Agent Session
    private val _agentSession = MutableStateFlow<AgentSession?>(null)
    val agentSession: StateFlow<AgentSession?> = _agentSession.asStateFlow()

    // Chat
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "welcome",
                role = ChatRole.ASSISTANT,
                content = "Welcome to AI Studio! I am your on-device AI engineering assistant running on local GGUF models. Ask me code questions, explore Android architectures, or switch to Agent Mode to autonomously generate, fix, and compile APKs without a PC."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatGenerating = MutableStateFlow(false)
    val isChatGenerating: StateFlow<Boolean> = _isChatGenerating.asStateFlow()

    // Reverse Engineering / APK Analysis
    private val _apkAnalysis = MutableStateFlow<ApkAnalysisResult?>(null)
    val apkAnalysis: StateFlow<ApkAnalysisResult?> = _apkAnalysis.asStateFlow()

    // Capability Test Results
    private val _capabilityTest = MutableStateFlow<ModelCapabilityTestResult?>(null)
    val capabilityTest: StateFlow<ModelCapabilityTestResult?> = _capabilityTest.asStateFlow()

    init {
        // Initialize sample demo project: Car Sticker Studio
        createInitialDemoProject()
    }

    private fun createInitialDemoProject() {
        val demoProject = Project(
            id = "proj-car-sticker",
            name = "Car Sticker Studio",
            description = "Modern vehicle decal and custom car livery design application with real-time vector canvas.",
            packageName = "com.aistudio.carsticker",
            versionName = "1.0.0",
            versionCode = 1,
            lastBuildStatus = BuildStatus.SUCCESS,
            apkGenerated = true
        )
        val demoApk = AndroidApkBuilder.generatePhysicalApkFile(context, demoProject, false)
        val finalizedProject = demoProject.copy(
            apkPath = demoApk.absolutePath,
            apkSizeBytes = demoApk.length()
        )

        _projects.value = listOf(finalizedProject)
        _selectedProject.value = finalizedProject
        _projectFiles.value = listOf(
            ProjectFile("app/src/main/AndroidManifest.xml", "AndroidManifest.xml", """
                <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.aistudio.carsticker">
                    <uses-permission android:name="android.permission.INTERNET" />
                    <application android:label="Car Sticker Studio" android:theme="@style/Theme.Material3.DayNight">
                        <activity android:name=".MainActivity" android:exported="true">
                            <intent-filter>
                                <action android:name="android.intent.action.MAIN" />
                                <category android:name="android.intent.category.LAUNCHER" />
                            </intent-filter>
                        </activity>
                    </application>
                </manifest>
            """.trimIndent()),
            ProjectFile("app/build.gradle.kts", "build.gradle.kts", """
                plugins { alias(libs.plugins.android.application); alias(libs.plugins.kotlin.compose) }
                android { namespace = "com.aistudio.carsticker"; compileSdk = 36 }
            """.trimIndent()),
            ProjectFile("app/src/main/java/com/aistudio/carsticker/MainActivity.kt", "MainActivity.kt", """
                package com.aistudio.carsticker
                import android.os.Bundle
                import androidx.activity.ComponentActivity
                import androidx.activity.compose.setContent
                import androidx.compose.foundation.layout.*
                import androidx.compose.material3.*
                import androidx.compose.runtime.*
                import androidx.compose.ui.Modifier

                class MainActivity : ComponentActivity() {
                    override fun onCreate(savedInstanceState: Bundle?) {
                        super.onCreate(savedInstanceState)
                        setContent {
                            MaterialTheme {
                                Surface(modifier = Modifier.fillMaxSize()) {
                                    Text("Car Sticker Studio Ready!")
                                }
                            }
                        }
                    }
                }
            """.trimIndent())
        )
    }

    // Agent Execution
    fun runAgentPrompt(prompt: String) {
        scope.launch {
            AutonomousAgentEngine.runAutonomousBuild(
                context = context,
                prompt = prompt,
                onProjectCreated = { project, files ->
                    _projects.value = listOf(project) + _projects.value
                    _selectedProject.value = project
                    _projectFiles.value = files
                },
                onApkReady = { project, apkPath ->
                    _projects.value = _projects.value.map {
                        if (it.id == project.id) project else it
                    }
                    _selectedProject.value = project
                }
            ).collect { session ->
                _agentSession.value = session
            }
        }
    }

    // Chat Execution
    fun sendChatMessage(userText: String, attachedFileName: String? = null) {
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = ChatRole.USER,
            content = userText,
            attachedFileName = attachedFileName
        )
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatGenerating.value = true

        scope.launch {
            val activeModel = getActiveModel()
            val assistantId = UUID.randomUUID().toString()
            val placeholder = ChatMessage(
                id = assistantId,
                role = ChatRole.ASSISTANT,
                content = "",
                modelName = activeModel.name
            )
            _chatMessages.value = _chatMessages.value + placeholder

            GgufRuntimeEngine.streamChatResponse(
                messages = _chatMessages.value,
                activeModel = activeModel,
                providerSettings = _providerSettings.value
            ).collect { chunk ->
                _chatMessages.value = _chatMessages.value.map { msg ->
                    if (msg.id == assistantId) msg.copy(content = chunk) else msg
                }
            }
            _isChatGenerating.value = false
        }
    }

    fun stopChatGeneration() {
        _isChatGenerating.value = false
    }

    fun clearChatHistory() {
        _chatMessages.value = listOf(
            ChatMessage(
                id = "welcome",
                role = ChatRole.ASSISTANT,
                content = "Chat cleared. Ask me any Android development question or request code explanations."
            )
        )
    }

    // Models & Downloads
    fun downloadModel(model: AiModel) {
        scope.launch {
            _models.value = _models.value.map {
                if (it.id == model.id) it.copy(isDownloading = true, downloadProgress = 0f) else it
            }

            HuggingFaceDownloader.downloadModel(context, model).collect { progress ->
                _models.value = _models.value.map {
                    if (it.id == model.id) {
                        it.copy(
                            downloadProgress = progress,
                            isDownloaded = progress >= 1f,
                            isDownloading = progress < 1f
                        )
                    } else it
                }
            }
        }
    }

    fun deleteModel(modelId: String) {
        _models.value = _models.value.map {
            if (it.id == modelId) it.copy(isDownloaded = false, downloadProgress = 0f) else it
        }
    }

    fun setDefaultModel(modelId: String) {
        _models.value = _models.value.map {
            it.copy(isDefault = it.id == modelId)
        }
        _providerSettings.value = _providerSettings.value.copy(selectedModelId = modelId)
    }

    fun runModelCapabilityTest(model: AiModel) {
        scope.launch {
            _capabilityTest.value = null
            val result = ModelCapabilityTester.runBenchmark(model)
            _capabilityTest.value = result
        }
    }

    fun importLocalGgufFile(file: File) {
        val meta = GgufHeaderParser.parse(file)
        val imported = AiModel(
            id = "imported-${UUID.randomUUID().toString().take(6)}",
            name = meta.modelName,
            fileName = file.name,
            fileSizeBytes = file.length(),
            quantization = meta.quantization,
            contextLength = meta.contextLength,
            architecture = meta.architecture,
            isDownloaded = true,
            isDefault = false,
            providerType = ProviderType.LOCAL_GGUF,
            description = "Imported GGUF file from phone storage (${meta.architecture}, ${meta.quantization})."
        )
        _models.value = listOf(imported) + _models.value
    }

    fun updateProviderSettings(settings: ProviderSettings) {
        _providerSettings.value = settings
    }

    // Skills
    fun toggleSkill(skillId: String) {
        SkillsRepository.toggleSkill(skillId)
        _skills.value = SkillsRepository.getInstalledSkills()
    }

    fun removeSkill(skillId: String) {
        SkillsRepository.removeSkill(skillId)
        _skills.value = SkillsRepository.getInstalledSkills()
    }

    fun installSkill(item: SkillSearchItem) {
        scope.launch {
            SkillsRepository.installSkillFromGitHub(item)
            _skills.value = SkillsRepository.getInstalledSkills()
        }
    }

    fun createSkillWithAi(prompt: String, name: String) {
        scope.launch {
            SkillCreatorEngine.generateSkillFromPrompt(prompt, name)
            _skills.value = SkillsRepository.getInstalledSkills()
        }
    }

    // Project & Build
    fun selectProject(project: Project) {
        _selectedProject.value = project
    }

    fun saveFileContent(relativePath: String, newContent: String) {
        _projectFiles.value = _projectFiles.value.map {
            if (it.relativePath == relativePath) it.copy(content = newContent) else it
        }
    }

    fun addProjectFile(relativePath: String, content: String = "") {
        val file = ProjectFile(
            relativePath = relativePath,
            name = relativePath.substringAfterLast("/"),
            content = content
        )
        _projectFiles.value = _projectFiles.value + file
    }

    fun deleteProjectFile(relativePath: String) {
        _projectFiles.value = _projectFiles.value.filter { it.relativePath != relativePath }
    }

    fun buildCurrentProject(isRelease: Boolean = false, simulateError: Boolean = false) {
        val project = _selectedProject.value ?: return
        scope.launch {
            _buildLogs.value = emptyList()
            _selectedProject.value = project.copy(
                lastBuildStatus = BuildStatus.BUILDING,
                hasBuildError = simulateError
            )

            AndroidApkBuilder.buildApk(context, project, isRelease, simulateError).collect { log ->
                _buildLogs.value = _buildLogs.value + log
            }

            if (!simulateError) {
                val apk = AndroidApkBuilder.generatePhysicalApkFile(context, project, isRelease)
                val updated = project.copy(
                    lastBuildStatus = BuildStatus.SUCCESS,
                    apkGenerated = true,
                    apkPath = apk.absolutePath,
                    apkSizeBytes = apk.length(),
                    hasBuildError = false
                )
                _selectedProject.value = updated
                _projects.value = _projects.value.map { if (it.id == updated.id) updated else it }
            } else {
                _selectedProject.value = project.copy(
                    lastBuildStatus = BuildStatus.FAILED,
                    hasBuildError = true
                )
            }
        }
    }

    fun autoFixBuildErrors() {
        val project = _selectedProject.value ?: return
        val lastError = _buildLogs.value.lastOrNull { it.type == LogType.ERROR }?.message ?: "Unresolved reference"

        scope.launch {
            _buildLogs.value = _buildLogs.value + BuildLogEntry(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                "[AGENT] Diagnosing build error autonomously with active skill 'Gradle Android Build & Fixer'...",
                LogType.INFO
            )

            val diagnosis = AutoFixEngine.diagnoseAndFix(lastError, _projectFiles.value)
            _projectFiles.value = diagnosis.updatedFiles

            _buildLogs.value = _buildLogs.value + BuildLogEntry(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                "[AUTO-FIX] ${diagnosis.rootCause} Applied fix: ${diagnosis.fixDescription}",
                LogType.SUCCESS
            )

            // Rebuild
            buildCurrentProject(isRelease = false, simulateError = false)
        }
    }

    fun installProjectApk() {
        val project = _selectedProject.value ?: return
        if (project.apkPath.isNotBlank()) {
            val apk = File(project.apkPath)
            if (apk.exists()) {
                AndroidApkBuilder.installApk(context, apk)
            }
        }
    }

    fun exportProjectApk() {
        val project = _selectedProject.value ?: return
        if (project.apkPath.isNotBlank()) {
            val apk = File(project.apkPath)
            if (apk.exists()) {
                AndroidApkBuilder.shareApk(context, apk)
            }
        }
    }

    fun exportProjectSourceZip() {
        val project = _selectedProject.value ?: return
        scope.launch {
            val zip = AndroidApkBuilder.exportProjectZip(context, project, _projectFiles.value)
            AndroidApkBuilder.shareApk(context, zip)
        }
    }

    // Reverse Engineering
    fun analyzeApkFile(file: File) {
        scope.launch {
            val report = ApkAnalyzerEngine.analyzeApk(context, file)
            _apkAnalysis.value = report
        }
    }

    fun analyzeCurrentProjectApk() {
        val project = _selectedProject.value ?: return
        if (project.apkPath.isNotBlank()) {
            val file = File(project.apkPath)
            analyzeApkFile(file)
        }
    }

    private fun getActiveModel(): AiModel {
        val id = _providerSettings.value.selectedModelId
        return _models.value.find { it.id == id } ?: _models.value.first()
    }
}
