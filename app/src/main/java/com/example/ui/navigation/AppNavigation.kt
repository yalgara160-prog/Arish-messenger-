package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Status
import com.example.data.repository.ArishRepository
import com.example.ui.auth.AuthUiState
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.ProfileSetupScreen
import com.example.ui.auth.SignInScreen
import com.example.ui.chat.ChatScreen
import com.example.ui.chat.ChatViewModel
import com.example.ui.components.ArishLogo
import com.example.ui.contacts.CreateGroupScreen
import com.example.ui.contacts.NewChatScreen
import com.example.ui.main.HomeScreen
import com.example.ui.main.MainViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.status.AddPhotoStatusDialog
import com.example.ui.status.AddTextStatusDialog
import com.example.ui.status.StatusViewerScreen
import com.example.ui.theme.ArishBrightGreen
import com.example.ui.theme.ArishDarkGreen
import com.example.ui.theme.ArishEmeraldGreen
import kotlinx.coroutines.delay

sealed interface Screen {
    data object Splash : Screen
    data object SignIn : Screen
    data object ProfileSetup : Screen
    data object Home : Screen
    data class Chat(val chatId: String, val title: String, val photoUrl: String = "", val isGroup: Boolean = false) : Screen
    data object NewChat : Screen
    data object CreateGroup : Screen
    data class StatusViewer(val status: Status) : Screen
    data object Settings : Screen
    data object EditProfile : Screen
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    repository: ArishRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authState by authViewModel.uiState.collectAsState()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
    var mainViewModel by remember { mutableStateOf<MainViewModel?>(null) }
    var chatViewModel by remember { mutableStateOf<ChatViewModel?>(null) }

    // Status dialog states
    var showAddTextStatusDialog by remember { mutableStateOf(false) }
    var showAddPhotoStatusDialog by remember { mutableStateOf(false) }

    // Handle splash timeout and auth routing
    LaunchedEffect(authState) {
        if (currentScreen is Screen.Splash) {
            delay(1200L) // smooth splash branding display
        }
        when (val state = authState) {
            is AuthUiState.Unauthenticated, is AuthUiState.Error -> {
                currentScreen = Screen.SignIn
                mainViewModel = null
                chatViewModel = null
            }
            is AuthUiState.ProfileSetupRequired -> {
                currentScreen = Screen.ProfileSetup
            }
            is AuthUiState.Authenticated -> {
                if (mainViewModel == null || mainViewModel?.currentUserId != state.user.uid) {
                    mainViewModel = MainViewModel(repository, state.user.uid)
                }
                if (currentScreen is Screen.Splash || currentScreen is Screen.SignIn || currentScreen is Screen.ProfileSetup) {
                    currentScreen = Screen.Home
                }
            }
            is AuthUiState.Loading -> {
                // Keep current state
            }
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            is Screen.Splash -> {
                SplashScreen()
            }

            is Screen.SignIn -> {
                SignInScreen(authViewModel = authViewModel)
            }

            is Screen.ProfileSetup -> {
                ProfileSetupScreen(
                    authViewModel = authViewModel,
                    onComplete = {
                        currentScreen = Screen.Home
                    }
                )
            }

            is Screen.Home -> {
                mainViewModel?.let { vm ->
                    val wallpaper by vm.chatWallpaper.collectAsState()
                    HomeScreen(
                        mainViewModel = vm,
                        onNavigateToChat = { chatId ->
                            // Look up chat title from list or default
                            val chatsList = (vm.chatsState.value as? com.example.ui.main.UiState.Success)?.data
                            val foundChat = chatsList?.find { it.id == chatId }
                            val title = foundChat?.name?.ifBlank { "Chat" } ?: "Chat"
                            val isGroup = foundChat?.isGroup ?: false
                            val photo = foundChat?.photoUrl ?: ""

                            chatViewModel = ChatViewModel(repository, chatId, vm.currentUserId)
                            currentScreen = Screen.Chat(chatId, title, photo, isGroup)
                        },
                        onNavigateToNewChat = { currentScreen = Screen.NewChat },
                        onNavigateToNewGroup = { currentScreen = Screen.CreateGroup },
                        onNavigateToSettings = { currentScreen = Screen.Settings },
                        onNavigateToEditProfile = { currentScreen = Screen.EditProfile },
                        onNavigateToStatusViewer = { status ->
                            vm.markStatusViewed(status.id)
                            currentScreen = Screen.StatusViewer(status)
                        },
                        onOpenAddTextStatus = { showAddTextStatusDialog = true },
                        onOpenAddPhotoStatus = { showAddPhotoStatusDialog = true },
                        onSignOut = {
                            authViewModel.signOut(context)
                        }
                    )
                } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ArishEmeraldGreen)
                }
            }

            is Screen.Chat -> {
                BackHandler { currentScreen = Screen.Home }
                mainViewModel?.let { mvm ->
                    val wallpaper by mvm.chatWallpaper.collectAsState()
                    chatViewModel?.let { cvm ->
                        ChatScreen(
                            chatViewModel = cvm,
                            chatTitle = screen.title,
                            chatPhotoUrl = screen.photoUrl,
                            isGroup = screen.isGroup,
                            wallpaperType = wallpaper,
                            onNavigateBack = { currentScreen = Screen.Home }
                        )
                    }
                }
            }

            is Screen.NewChat -> {
                BackHandler { currentScreen = Screen.Home }
                mainViewModel?.let { vm ->
                    NewChatScreen(
                        mainViewModel = vm,
                        onNavigateBack = { currentScreen = Screen.Home },
                        onNavigateToNewGroup = { currentScreen = Screen.CreateGroup },
                        onChatReady = { chatId, otherUserName ->
                            chatViewModel = ChatViewModel(repository, chatId, vm.currentUserId)
                            currentScreen = Screen.Chat(chatId, otherUserName, "", false)
                        }
                    )
                }
            }

            is Screen.CreateGroup -> {
                BackHandler { currentScreen = Screen.Home }
                mainViewModel?.let { vm ->
                    CreateGroupScreen(
                        mainViewModel = vm,
                        onNavigateBack = { currentScreen = Screen.Home },
                        onGroupCreated = { chatId, groupName ->
                            chatViewModel = ChatViewModel(repository, chatId, vm.currentUserId)
                            currentScreen = Screen.Chat(chatId, groupName, "", true)
                        }
                    )
                }
            }

            is Screen.StatusViewer -> {
                BackHandler { currentScreen = Screen.Home }
                mainViewModel?.let { vm ->
                    StatusViewerScreen(
                        status = screen.status,
                        currentUserId = vm.currentUserId,
                        onClose = { currentScreen = Screen.Home },
                        onReply = { replyMsg ->
                            vm.startDirectChat(
                                com.example.data.model.UserProfile(
                                    userId = screen.status.userId,
                                    displayName = screen.status.userName,
                                    photoUrl = screen.status.userPhotoUrl
                                )
                            ) { chatId ->
                                chatViewModel = ChatViewModel(repository, chatId, vm.currentUserId)
                                chatViewModel?.sendMessage()
                                currentScreen = Screen.Chat(chatId, screen.status.userName, screen.status.userPhotoUrl, false)
                            }
                        },
                        onDelete = { statusId ->
                            vm.deleteStatus(statusId)
                        }
                    )
                }
            }

            is Screen.Settings -> {
                BackHandler { currentScreen = Screen.Home }
                mainViewModel?.let { vm ->
                    SettingsScreen(
                        mainViewModel = vm,
                        onNavigateBack = { currentScreen = Screen.Home },
                        onNavigateToEditProfile = { currentScreen = Screen.EditProfile },
                        onSignOut = { authViewModel.signOut(context) }
                    )
                }
            }

            is Screen.EditProfile -> {
                BackHandler { currentScreen = Screen.Home }
                mainViewModel?.let { vm ->
                    val profile = vm.currentProfileState.collectAsState().value
                    ProfileSetupScreen(
                        authViewModel = authViewModel,
                        initialDisplayName = profile?.displayName ?: "",
                        initialUsername = profile?.username ?: "",
                        initialPhone = profile?.phoneNumber ?: "",
                        initialAbout = profile?.about ?: "Hey there! I am using Arish Messenger.",
                        initialPhotoUrl = profile?.photoUrl ?: "",
                        isEditing = true,
                        onComplete = { currentScreen = Screen.Home }
                    )
                }
            }
        }
    }

    // Status Dialogs
    if (showAddTextStatusDialog) {
        AddTextStatusDialog(
            onDismiss = { showAddTextStatusDialog = false },
            onPost = { text, colorHex ->
                mainViewModel?.postTextStatus(text, colorHex) {}
            }
        )
    }

    if (showAddPhotoStatusDialog) {
        AddPhotoStatusDialog(
            onDismiss = { showAddPhotoStatusDialog = false },
            onPost = { photoUrl, caption ->
                mainViewModel?.postPhotoStatus(photoUrl, caption) {}
            }
        )
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArishDarkGreen),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                ArishLogo(size = 64.dp, bubbleColor = Color.White, letterColor = ArishDarkGreen)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ARISH MESSENGER",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Text(
                text = "Stay Connected.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = ArishBrightGreen,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
