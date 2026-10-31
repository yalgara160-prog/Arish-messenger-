package com.example.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Chat
import com.example.data.model.Message
import com.example.ui.components.MessageStatusTick
import com.example.ui.components.UserAvatar
import com.example.ui.main.UiState
import com.example.ui.theme.ArishBrightGreen
import com.example.ui.theme.ArishDarkGreen
import com.example.ui.theme.ArishEmeraldGreen
import com.example.ui.theme.DarkChatBubbleIncoming
import com.example.ui.theme.DarkChatBubbleOutgoing
import com.example.ui.theme.LightChatBubbleIncoming
import com.example.ui.theme.LightChatBubbleOutgoing
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    chatTitle: String,
    chatPhotoUrl: String = "",
    isGroup: Boolean = false,
    wallpaperType: String = "classic",
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUserId = chatViewModel.currentUserId
    val messagesState by chatViewModel.messagesState.collectAsState()
    val inputText by chatViewModel.inputText.collectAsState()
    val replyingTo by chatViewModel.replyingTo.collectAsState()
    val editingMessage by chatViewModel.editingMessage.collectAsState()
    val isRecordingVoice by chatViewModel.isRecordingVoice.collectAsState()
    val recordingDuration by chatViewModel.recordingDurationSeconds.collectAsState()

    var selectedMessageForOptions by remember { mutableStateOf<Message?>(null) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var isSearchingMessages by remember { mutableStateOf(false) }
    var messageSearchQuery by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new messages
    LaunchedEffect(messagesState) {
        if (messagesState is UiState.Success) {
            val list = (messagesState as UiState.Success<List<Message>>).data
            if (list.isNotEmpty()) {
                listState.animateScrollToItem(list.size - 1)
            }
        }
    }

    // Wallpaper background color
    val wallpaperBgColor = when (wallpaperType) {
        "mint" -> Color(0xFFE8F5E9)
        "emerald" -> Color(0xFFE0F2F1)
        "dark" -> Color(0xFF0B141A)
        else -> MaterialTheme.colorScheme.background
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = ArishDarkGreen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    UserAvatar(
                        name = chatTitle,
                        photoUrl = chatPhotoUrl,
                        size = 40.dp,
                        isGroup = isGroup
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    if (isSearchingMessages) {
                        OutlinedTextField(
                            value = messageSearchQuery,
                            onValueChange = { messageSearchQuery = it },
                            placeholder = { Text("Search in chat...", color = Color.White.copy(alpha = 0.7f)) },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { isSearchingMessages = false; messageSearchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = chatTitle,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isGroup) "tap for group info" else "online",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 12.sp
                            )
                        }

                        IconButton(onClick = { isSearchingMessages = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search messages", tint = Color.White)
                        }

                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Search messages") },
                                    onClick = {
                                        menuExpanded = false
                                        isSearchingMessages = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Media & docs") },
                                    onClick = {
                                        menuExpanded = false
                                        showAttachmentSheet = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear chat") },
                                    onClick = {
                                        menuExpanded = false
                                        Toast.makeText(context, "Chat cleared", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(wallpaperBgColor)
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Messages List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (messagesState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = ArishEmeraldGreen)
                        }
                    }
                    is UiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = (messagesState as UiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    is UiState.Success -> {
                        val allMessages = (messagesState as UiState.Success<List<Message>>).data
                        val filteredMessages = if (messageSearchQuery.isBlank()) {
                            allMessages
                        } else {
                            allMessages.filter { it.text.contains(messageSearchQuery, ignoreCase = true) }
                        }

                        if (filteredMessages.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    shadowElevation = 1.dp,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Text(
                                        text = "Messages are synchronized in real-time with Firebase.\nSend a message to start the conversation!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(filteredMessages, key = { it.id }) { msg ->
                                    val isFromMe = msg.senderId == currentUserId
                                    MessageBubbleItem(
                                        message = msg,
                                        isFromMe = isFromMe,
                                        isGroup = isGroup,
                                        onLongClick = { selectedMessageForOptions = msg },
                                        onReplyClick = { chatViewModel.setReplyingTo(msg) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quoted reply banner
            AnimatedVisibility(visible = replyingTo != null) {
                replyingTo?.let { reply ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(36.dp)
                                    .background(ArishEmeraldGreen, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = reply.senderName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ArishEmeraldGreen
                                )
                                Text(
                                    text = reply.text.ifBlank { reply.type },
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = { chatViewModel.setReplyingTo(null) }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Editing message banner
            AnimatedVisibility(visible = editingMessage != null) {
                Surface(
                    color = ArishBrightGreen.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = ArishDarkGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editing message", style = MaterialTheme.typography.bodySmall, color = ArishDarkGreen, modifier = Modifier.weight(1f))
                        IconButton(onClick = { chatViewModel.cancelEditing() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel edit", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Input Bar or Voice Recording Bar
            if (isRecordingVoice) {
                VoiceRecordingBar(
                    durationSeconds = recordingDuration,
                    onCancel = { chatViewModel.cancelVoiceRecording() },
                    onSend = { chatViewModel.stopAndSendVoiceRecording() }
                )
            } else {
                MessageInputBar(
                    text = inputText,
                    onTextChanged = { chatViewModel.onInputTextChanged(it) },
                    onSend = { chatViewModel.sendMessage() },
                    onOpenAttachments = { showAttachmentSheet = true },
                    onStartVoice = { chatViewModel.startVoiceRecording() }
                )
            }
        }
    }

    // Message options modal (Reply, Copy, Edit, Delete, Reactions)
    selectedMessageForOptions?.let { message ->
        val isSender = message.senderId == currentUserId
        MessageOptionsSheet(
            message = message,
            isSender = isSender,
            onDismiss = { selectedMessageForOptions = null },
            onReact = { emoji ->
                chatViewModel.addReaction(message.id, emoji)
                selectedMessageForOptions = null
            },
            onReply = {
                chatViewModel.setReplyingTo(message)
                selectedMessageForOptions = null
            },
            onCopy = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("message", message.text))
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                selectedMessageForOptions = null
            },
            onEdit = {
                chatViewModel.setEditingMessage(message)
                selectedMessageForOptions = null
            },
            onDelete = {
                chatViewModel.deleteMessage(message.id)
                selectedMessageForOptions = null
            }
        )
    }

    // Attachment picker sheet
    if (showAttachmentSheet) {
        AttachmentPickerSheet(
            onDismiss = { showAttachmentSheet = false },
            onSendPhoto = { url, caption ->
                chatViewModel.sendMediaMessage(
                    type = "image",
                    mediaUrl = url,
                    caption = caption
                )
                showAttachmentSheet = false
            },
            onSendDocument = { name, size ->
                chatViewModel.sendMediaMessage(
                    type = "document",
                    mediaUrl = "sample_doc_url",
                    mediaName = name,
                    mediaSize = size
                )
                showAttachmentSheet = false
            },
            onSendAudio = { name, duration ->
                chatViewModel.sendMediaMessage(
                    type = "audio",
                    mediaUrl = "audio_clip",
                    mediaName = name,
                    audioDuration = duration
                )
                showAttachmentSheet = false
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubbleItem(
    message: Message,
    isFromMe: Boolean,
    isGroup: Boolean,
    onLongClick: () -> Unit,
    onReplyClick: () -> Unit
) {
    val bubbleColor = if (isFromMe) {
        if (androidx.compose.foundation.isSystemInDarkTheme()) DarkChatBubbleOutgoing else LightChatBubbleOutgoing
    } else {
        if (androidx.compose.foundation.isSystemInDarkTheme()) DarkChatBubbleIncoming else LightChatBubbleIncoming
    }

    val alignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleShape = if (isFromMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = alignment
    ) {
        Surface(
            shape = bubbleShape,
            color = bubbleColor,
            shadowElevation = 1.dp,
            modifier = Modifier
                .widthIn(max = 310.dp)
                .clip(bubbleShape)
                .combinedClickable(
                    onClick = onReplyClick,
                    onLongClick = onLongClick
                )
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                // Group sender name
                if (isGroup && !isFromMe && message.senderName.isNotBlank()) {
                    Text(
                        text = message.senderName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = ArishDarkGreen,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // Quoted reply in bubble
                if (message.replyToText.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Row(modifier = Modifier.padding(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(28.dp)
                                    .background(ArishEmeraldGreen, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = message.replyToSender,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = ArishEmeraldGreen
                                )
                                Text(
                                    text = message.replyToText,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Media Content (Photo, Audio, Doc)
                when (message.type) {
                    "image" -> {
                        if (message.mediaUrl.isNotBlank()) {
                            AsyncImage(
                                model = message.mediaUrl,
                                contentDescription = "Shared image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                    "audio" -> {
                        VoiceMessagePlayer(audioDuration = message.audioDuration, isFromMe = isFromMe)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    "document" -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Document",
                                tint = ArishEmeraldGreen,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = message.mediaName.ifBlank { "Document.pdf" },
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = message.mediaSize.ifBlank { "PDF document" },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                // Text Content
                if (message.text.isNotBlank()) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 15.sp,
                        fontStyle = if (message.isDeleted) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                        color = if (message.isDeleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Bottom row: Edited tag, Timestamp, Ticks
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.isEdited) {
                        Text(
                            text = "edited",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = formatMessageTime(message.createdAt),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    if (isFromMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        MessageStatusTick(status = message.status, size = 15.dp)
                    }
                }

                // Reactions Bar overlay
                if (message.reactions.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        message.reactions.values.distinct().take(4).forEach { emoji ->
                            Text(text = emoji, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 2.dp))
                        }
                        if (message.reactions.size > 1) {
                            Text(
                                text = message.reactions.size.toString(),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceMessagePlayer(audioDuration: Long, isFromMe: Boolean) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(ArishEmeraldGreen)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Slider(
                value = progress,
                onValueChange = { progress = it },
                colors = SliderDefaults.colors(
                    thumbColor = ArishEmeraldGreen,
                    activeTrackColor = ArishEmeraldGreen
                ),
                modifier = Modifier.height(20.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${audioDuration}s",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = ArishEmeraldGreen,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun MessageInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    onOpenAttachments: () -> Unit,
    onStartVoice: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Emoji picker */ }) {
                    Icon(
                        imageVector = Icons.Default.SentimentSatisfiedAlt,
                        contentDescription = "Emoji",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    placeholder = { Text("Message", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("message_input_field")
                )

                IconButton(onClick = onOpenAttachments, modifier = Modifier.testTag("attachment_button")) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach media",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Mic or Send FAB
        FloatingActionButton(
            onClick = {
                if (text.isNotBlank()) onSend() else onStartVoice()
            },
            shape = CircleShape,
            containerColor = ArishEmeraldGreen,
            contentColor = Color.White,
            modifier = Modifier
                .size(48.dp)
                .testTag("send_or_mic_button")
        ) {
            if (text.isNotBlank()) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Default.Mic, contentDescription = "Record voice message", modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun VoiceRecordingBar(
    durationSeconds: Int,
    onCancel: () -> Unit,
    onSend: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                )
                Spacer(modifier = Modifier.width(10.dp))
                val minutes = durationSeconds / 60
                val seconds = durationSeconds % 60
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Red
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Slide or tap trash to cancel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onCancel, modifier = Modifier.testTag("cancel_voice_record")) {
                    Icon(Icons.Default.Delete, contentDescription = "Cancel", tint = Color.Red)
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onSend,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ArishEmeraldGreen)
                        .testTag("send_voice_record")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send audio", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageOptionsSheet(
    message: Message,
    isSender: Boolean,
    onDismiss: () -> Unit,
    onReact: (String) -> Unit,
    onReply: () -> Unit,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(modifier = Modifier.padding(bottom = 32.dp, start = 16.dp, end = 16.dp)) {
            // Reactions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("👍", "❤️", "😂", "😮", "😢", "🙏").forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onReact(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 22.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OptionRowItem(icon = Icons.AutoMirrored.Filled.Reply, label = "Reply", onClick = onReply)
            OptionRowItem(icon = Icons.Default.ContentCopy, label = "Copy text", onClick = onCopy)

            if (isSender) {
                OptionRowItem(icon = Icons.Default.Edit, label = "Edit message", onClick = onEdit)
                OptionRowItem(icon = Icons.Default.Delete, label = "Delete message", color = MaterialTheme.colorScheme.error, onClick = onDelete)
            }
        }
    }
}

@Composable
private fun OptionRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, color = color, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttachmentPickerSheet(
    onDismiss: () -> Unit,
    onSendPhoto: (url: String, caption: String) -> Unit,
    onSendDocument: (name: String, size: String) -> Unit,
    onSendAudio: (name: String, duration: Long) -> Unit
) {
    val samplePhotos = listOf(
        "https://images.unsplash.com/photo-1579202673506-ca3ce28943ef?w=400",
        "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?w=400",
        "https://images.unsplash.com/photo-1543610892-0b1f7e6d8ac1?w=400"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Share Content",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ArishDarkGreen
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentOptionIcon(
                    icon = Icons.Default.Photo,
                    label = "Photo",
                    color = Color(0xFFE91E63),
                    onClick = {
                        onSendPhoto(samplePhotos.random(), "Shared via Arish Messenger")
                    }
                )
                AttachmentOptionIcon(
                    icon = Icons.Default.Description,
                    label = "Document",
                    color = Color(0xFF536DFE),
                    onClick = {
                        onSendDocument("Project_Brief_2026.pdf", "1.4 MB")
                    }
                )
                AttachmentOptionIcon(
                    icon = Icons.Default.Mic,
                    label = "Audio",
                    color = Color(0xFFFF9800),
                    onClick = {
                        onSendAudio("Audio_Note.m4a", 15L)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AttachmentOptionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

private fun formatMessageTime(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(timestamp.toDate())
}
