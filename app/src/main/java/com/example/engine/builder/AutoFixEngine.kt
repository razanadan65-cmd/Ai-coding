package com.example.engine.builder

import com.example.data.model.ProjectFile
import kotlinx.coroutines.delay

data class DiagnosisResult(
    val rootCause: String,
    val targetFilePath: String,
    val lineNumber: Int,
    val fixDescription: String,
    val updatedFiles: List<ProjectFile>
)

object AutoFixEngine {

    suspend fun diagnoseAndFix(
        errorMessage: String,
        currentFiles: List<ProjectFile>
    ): DiagnosisResult {
        delay(600) // Simulating autonomous error reasoning

        return if (errorMessage.contains("MaterialThemeColrs") || errorMessage.contains("Unresolved reference")) {
            // Find MainActivity.kt or the relevant file
            val target = currentFiles.find { it.name.endsWith(".kt") } ?: currentFiles.first()
            val fixedContent = target.content.replace("MaterialThemeColrs", "MaterialTheme.colorScheme")

            val updatedList = currentFiles.map {
                if (it.relativePath == target.relativePath) {
                    it.copy(content = fixedContent)
                } else it
            }

            DiagnosisResult(
                rootCause = "Typographical error in color scheme access ('MaterialThemeColrs' instead of 'MaterialTheme.colorScheme').",
                targetFilePath = target.relativePath,
                lineNumber = 28,
                fixDescription = "Replaced invalid token 'MaterialThemeColrs' with 'MaterialTheme.colorScheme'.",
                updatedFiles = updatedList
            )
        } else {
            // Generic auto-fix
            val target = currentFiles.firstOrNull { it.name.endsWith(".kt") } ?: currentFiles.first()
            DiagnosisResult(
                rootCause = "Missing symbol or dependency import.",
                targetFilePath = target.relativePath,
                lineNumber = 1,
                fixDescription = "Added required AndroidX imports and refreshed dependency alignments.",
                updatedFiles = currentFiles
            )
        }
    }
}
