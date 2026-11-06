package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AppRepository
import com.example.engine.gguf.DeviceMemoryMonitor
import com.example.engine.gguf.MemoryStatus
import com.example.engine.skills.SkillsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class AppViewModel(application: Application) : AndroidViewModel(application) {

    val repository = AppRepository(application.applicationContext)

    val models = repository.models
    val providerSettings = repository.providerSettings
    val skills = repository.skills
    val projects = repository.projects
    val selectedProject = repository.selectedProject
    val projectFiles = repository.projectFiles
    val buildLogs = repository.buildLogs
    val agentSession = repository.agentSession
    val chatMessages = repository.chatMessages
    val isChatGenerating = repository.isChatGenerating
    val apkAnalysis = repository.apkAnalysis
    val capabilityTest = repository.capabilityTest

    // GitHub Skill Search State
    private val _skillSearchResults = MutableStateFlow<List<SkillSearchItem>>(emptyList())
    val skillSearchResults: StateFlow<List<SkillSearchItem>> = _skillSearchResults.asStateFlow()

    private val _isSearchingSkills = MutableStateFlow(false)
    val isSearchingSkills: StateFlow<Boolean> = _isSearchingSkills.asStateFlow()

    // Device RAM Status
    private val _memoryStatus = MutableStateFlow(DeviceMemoryMonitor.getMemoryStatus(application.applicationContext))
    val memoryStatus: StateFlow<MemoryStatus> = _memoryStatus.asStateFlow()

    init {
        searchGitHubSkills("")
    }

    fun refreshMemoryStatus() {
        _memoryStatus.value = DeviceMemoryMonitor.getMemoryStatus(getApplication<Application>().applicationContext)
    }

    fun searchGitHubSkills(query: String) {
        viewModelScope.launch {
            _isSearchingSkills.value = true
            _skillSearchResults.value = SkillsRepository.searchGitHubSkills(query)
            _isSearchingSkills.value = false
        }
    }

    fun installSkill(item: SkillSearchItem) {
        repository.installSkill(item)
    }

    fun createSkillWithAi(prompt: String, name: String) {
        repository.createSkillWithAi(prompt, name)
    }

    fun toggleSkill(skillId: String) {
        repository.toggleSkill(skillId)
    }

    fun removeSkill(skillId: String) {
        repository.removeSkill(skillId)
    }

    fun runAgent(prompt: String) {
        repository.runAgentPrompt(prompt)
    }

    fun sendChatMessage(text: String, attachedFile: String? = null) {
        repository.sendChatMessage(text, attachedFile)
    }

    fun stopChatGeneration() {
        repository.stopChatGeneration()
    }

    fun clearChatHistory() {
        repository.clearChatHistory()
    }

    fun downloadModel(model: AiModel) {
        repository.downloadModel(model)
    }

    fun deleteModel(modelId: String) {
        repository.deleteModel(modelId)
    }

    fun setDefaultModel(modelId: String) {
        repository.setDefaultModel(modelId)
    }

    fun runModelCapabilityTest(model: AiModel) {
        repository.runModelCapabilityTest(model)
    }

    fun importGgufFile(file: File) {
        repository.importLocalGgufFile(file)
    }

    fun updateProviderSettings(settings: ProviderSettings) {
        repository.updateProviderSettings(settings)
    }

    fun selectProject(project: Project) {
        repository.selectProject(project)
    }

    fun saveFileContent(relativePath: String, newContent: String) {
        repository.saveFileContent(relativePath, newContent)
    }

    fun addProjectFile(relativePath: String, content: String = "") {
        repository.addProjectFile(relativePath, content)
    }

    fun deleteProjectFile(relativePath: String) {
        repository.deleteProjectFile(relativePath)
    }

    fun buildCurrentProject(isRelease: Boolean = false, simulateError: Boolean = false) {
        repository.buildCurrentProject(isRelease, simulateError)
    }

    fun autoFixBuildErrors() {
        repository.autoFixBuildErrors()
    }

    fun installProjectApk() {
        repository.installProjectApk()
    }

    fun exportProjectApk() {
        repository.exportProjectApk()
    }

    fun exportProjectSourceZip() {
        repository.exportProjectSourceZip()
    }

    fun analyzeCurrentProjectApk() {
        repository.analyzeCurrentProjectApk()
    }

    fun analyzeApkFile(file: File) {
        repository.analyzeApkFile(file)
    }
}
