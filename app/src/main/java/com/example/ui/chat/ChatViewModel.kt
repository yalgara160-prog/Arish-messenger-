package com.example.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Chat
import com.example.data.model.Message
import com.example.data.repository.ArishRepository
import com.example.ui.main.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ArishRepository,
    val chatId: String,
    val currentUserId: String
) : ViewModel() {

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _replyingTo = MutableStateFlow<Message?>(null)
    val replyingTo: StateFlow<Message?> = _replyingTo.asStateFlow()

    private val _editingMessage = MutableStateFlow<Message?>(null)
    val editingMessage: StateFlow<Message?> = _editingMessage.asStateFlow()

    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()

    private val _recordingDurationSeconds = MutableStateFlow(0)
    val recordingDurationSeconds: StateFlow<Int> = _recordingDurationSeconds.asStateFlow()

    private var recordingTimerJob: Job? = null
    private var typingDebounceJob: Job? = null

    // Messages flow
    val messagesState: StateFlow<UiState<List<Message>>> = repository.observeMessages(chatId)
        .map<List<Message>, UiState<List<Message>>> { messages ->
            // Auto mark received unread messages as read
            viewModelScope.launch {
                messages.filter { it.senderId != currentUserId && it.status != "read" }.forEach {
                    repository.updateMessageStatus(chatId, it.id, "read")
                }
            }
            UiState.Success(messages)
        }
        .catch { emit(UiState.Error(it.message ?: "Failed to load messages")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    fun onInputTextChanged(text: String) {
        _inputText.value = text
        // Typing indicator debounce
        typingDebounceJob?.cancel()
        typingDebounceJob = viewModelScope.launch {
            repository.setTyping(chatId, text.isNotBlank())
            delay(3000L)
            repository.setTyping(chatId, false)
        }
    }

    fun setReplyingTo(message: Message?) {
        _replyingTo.value = message
    }

    fun setEditingMessage(message: Message?) {
        _editingMessage.value = message
        if (message != null) {
            _inputText.value = message.text
        }
    }

    fun cancelEditing() {
        _editingMessage.value = null
        _inputText.value = ""
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        val editing = _editingMessage.value

        if (editing != null) {
            if (text.isNotBlank()) {
                viewModelScope.launch {
                    repository.editMessage(chatId, editing.id, text)
                    _editingMessage.value = null
                    _inputText.value = ""
                }
            }
            return
        }

        if (text.isNotBlank()) {
            val replyMsg = _replyingTo.value
            _inputText.value = ""
            _replyingTo.value = null
            viewModelScope.launch {
                repository.sendMessage(
                    chatId = chatId,
                    text = text,
                    type = "text",
                    replyToMessage = replyMsg
                )
                repository.setTyping(chatId, false)
            }
        }
    }

    fun sendMediaMessage(
        type: String,
        mediaUrl: String,
        mediaName: String = "",
        mediaSize: String = "",
        caption: String = "",
        audioDuration: Long = 0L
    ) {
        val replyMsg = _replyingTo.value
        _replyingTo.value = null
        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                text = caption,
                type = type,
                mediaUrl = mediaUrl,
                mediaName = mediaName,
                mediaSize = mediaSize,
                audioDuration = audioDuration,
                replyToMessage = replyMsg
            )
        }
    }

    fun startVoiceRecording() {
        _isRecordingVoice.value = true
        _recordingDurationSeconds.value = 0
        recordingTimerJob?.cancel()
        recordingTimerJob = viewModelScope.launch {
            while (_isRecordingVoice.value) {
                delay(1000L)
                _recordingDurationSeconds.value += 1
            }
        }
    }

    fun cancelVoiceRecording() {
        _isRecordingVoice.value = false
        recordingTimerJob?.cancel()
        _recordingDurationSeconds.value = 0
    }

    fun stopAndSendVoiceRecording() {
        val duration = _recordingDurationSeconds.value
        _isRecordingVoice.value = false
        recordingTimerJob?.cancel()
        if (duration >= 1) {
            sendMediaMessage(
                type = "audio",
                mediaUrl = "voice_note_${System.currentTimeMillis()}",
                mediaName = "Voice message",
                audioDuration = duration.toLong()
            )
        }
        _recordingDurationSeconds.value = 0
    }

    fun addReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            repository.addReaction(chatId, messageId, emoji)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(chatId, messageId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            repository.setTyping(chatId, false)
        }
    }
}
