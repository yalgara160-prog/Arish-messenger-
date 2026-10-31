package com.example.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Chat
import com.example.data.model.Status
import com.example.data.model.UserProfile
import com.example.data.repository.ArishRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class MainTab {
    CHATS, STATUS, PROFILE
}

class MainViewModel(
    val repository: ArishRepository,
    val currentUserId: String
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(MainTab.CHATS)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Chats state
    val chatsState: StateFlow<UiState<List<Chat>>> = combine(
        repository.observeUserChats(),
        _searchQuery
    ) { chats, query ->
        if (query.isBlank()) {
            chats
        } else {
            chats.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.lastMessageText.contains(query, ignoreCase = true)
            }
        }
    }
        .map<List<Chat>, UiState<List<Chat>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load chats")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    // All Users / Contacts
    val usersState: StateFlow<UiState<List<UserProfile>>> = combine(
        repository.observeAllUsers(),
        _searchQuery
    ) { users, query ->
        val filtered = users.filter { it.userId != currentUserId }
        if (query.isBlank()) {
            filtered
        } else {
            filtered.filter {
                it.displayName.contains(query, ignoreCase = true) ||
                it.username.contains(query, ignoreCase = true) ||
                it.phoneNumber.contains(query, ignoreCase = true)
            }
        }
    }
        .map<List<UserProfile>, UiState<List<UserProfile>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load users")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    // Current User Profile
    val currentProfileState: StateFlow<UserProfile?> = repository.observeUserProfile(currentUserId)
        .catch { emit(null) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    // Statuses
    val statusesState: StateFlow<UiState<List<Status>>> = repository.observeRecentStatuses()
        .map<List<Status>, UiState<List<Status>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load statuses")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    // Blocked users
    val blockedUsersState = repository.observeBlockedUsers()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // App Preferences
    private val _themeMode = MutableStateFlow("system") // "system", "light", "dark"
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _chatWallpaper = MutableStateFlow("classic") // "classic", "dark", "mint", "emerald"
    val chatWallpaper: StateFlow<String> = _chatWallpaper.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _readReceiptsEnabled = MutableStateFlow(true)
    val readReceiptsEnabled: StateFlow<Boolean> = _readReceiptsEnabled.asStateFlow()

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch(active: Boolean) {
        _isSearching.value = active
        if (!active) _searchQuery.value = ""
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
    }

    fun setChatWallpaper(wallpaper: String) {
        _chatWallpaper.value = wallpaper
    }

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    fun toggleReadReceipts(enabled: Boolean) {
        _readReceiptsEnabled.value = enabled
    }

    fun togglePinChat(chatId: String) {
        viewModelScope.launch {
            repository.togglePinChat(chatId)
        }
    }

    fun startDirectChat(otherUser: UserProfile, onChatReady: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val chatId = repository.getOrCreateDirectChat(otherUser)
                onChatReady(chatId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun createGroupChat(
        name: String,
        description: String,
        selectedUserIds: List<String>,
        photoUrl: String = "",
        onGroupCreated: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val chatId = repository.createGroupChat(
                    name = name,
                    description = description,
                    memberUids = selectedUserIds,
                    photoUrl = photoUrl
                )
                onGroupCreated(chatId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun postTextStatus(text: String, backgroundColor: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.postStatus(
                    type = "text",
                    content = text,
                    caption = "",
                    backgroundColor = backgroundColor
                )
                onSuccess()
            } catch (e: Exception) {
                // Error
            }
        }
    }

    fun postPhotoStatus(photoUrl: String, caption: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.postStatus(
                    type = "image",
                    content = photoUrl,
                    caption = caption,
                    backgroundColor = "#128C7E"
                )
                onSuccess()
            } catch (e: Exception) {
                // Error
            }
        }
    }

    fun markStatusViewed(statusId: String) {
        viewModelScope.launch {
            repository.markStatusViewed(statusId)
        }
    }

    fun deleteStatus(statusId: String) {
        viewModelScope.launch {
            repository.deleteStatus(statusId)
        }
    }

    fun blockUser(userId: String) {
        viewModelScope.launch {
            repository.blockUser(userId)
        }
    }

    fun unblockUser(userId: String) {
        viewModelScope.launch {
            repository.unblockUser(userId)
        }
    }
}
