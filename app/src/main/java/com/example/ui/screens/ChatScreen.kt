package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.ChatRole
import com.example.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: AppViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val isGenerating by viewModel.isChatGenerating.collectAsState()
    val models by viewModel.models.collectAsState()
    val activeModel = models.find { it.isDefault } ?: models.firstOrNull()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var attachedFile by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(messages.size, messages.lastOrNull()?.content) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("chat_screen")
    ) {
        // Model Indicator & Actions Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isGenerating) MaterialTheme.colorScheme.primary else Color(0xFF10B981))
                    )
                    Text(
                        text = activeModel?.name ?: "Local GGUF",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "(${activeModel?.quantization ?: "Q4_K_M"})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    if (isGenerating) {
                        TextButton(
                            onClick = { viewModel.stopChatGeneration() },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop")
                        }
                    }
                    IconButton(onClick = { viewModel.clearChatHistory() }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Clear Chat", tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages) { msg ->
                ChatMessageBubble(
                    message = msg,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(msg.content))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Attached File Chip if present
        if (attachedFile != null) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = attachedFile ?: "", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(
                        onClick = { attachedFile = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // Input Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        attachedFile = "CarStickerActivity.kt (Source)"
                        Toast.makeText(context, "Simulated file attached", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Attach file", tint = MaterialTheme.colorScheme.outline)
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask anything or paste code...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendChatMessage(inputText, attachedFile)
                            inputText = ""
                            attachedFile = null
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("btn_send_chat"),
                    enabled = inputText.isNotBlank() && !isGenerating
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    onCopy: () -> Unit
) {
    val isUser = message.role == ChatRole.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (isUser) "You" else (message.modelName ?: "AI Assistant"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (message.attachedFileName != null) {
                    Surface(
                        color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.InsertDriveFile, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = message.attachedFileName, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Render content
                if (message.content.contains("```")) {
                    FormattedCodeContent(message.content, isUser)
                } else {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Copy button for assistant responses
                if (!isUser && message.content.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormattedCodeContent(content: String, isUser: Boolean) {
    val parts = content.split("```")
    Column {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                // Code block
                val lines = part.trim().lines()
                val code = if (lines.size > 1) lines.drop(1).joinToString("\n") else part
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = code,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            } else {
                if (part.isNotBlank()) {
                    Text(
                        text = part.trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
