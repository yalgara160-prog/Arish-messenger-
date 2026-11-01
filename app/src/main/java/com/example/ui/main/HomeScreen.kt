package com.example.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chat
import com.example.data.model.Status
import com.example.data.model.UserProfile
import com.example.ui.components.ArishLogo
import com.example.ui.components.UnreadBadge
import com.example.ui.components.UserAvatar
import com.example.ui.theme.ArishBrightGreen
import com.example.ui.theme.ArishDarkGreen
import com.example.ui.theme.ArishEmeraldGreen
import com.example.ui.theme.OnlineGreen
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    mainViewModel: MainViewModel,
    onNavigateToChat: (String) -> Unit,
    onNavigateToNewChat: () -> Unit,
    onNavigateToNewGroup: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToStatusViewer: (Status) -> Unit,
    onOpenAddTextStatus: () -> Unit,
    onOpenAddPhotoStatus: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedTab by mainViewModel.selectedTab.collectAsState()
    val isSearching by mainViewModel.isSearching.collectAsState()
    val searchQuery by mainViewModel.searchQuery.collectAsState()
    val chatsState by mainViewModel.chatsState.collectAsState()
    val statusesState by mainViewModel.statusesState.collectAsState()
    val currentProfile by mainViewModel.currentProfileState.collectAsState()

    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ArishDarkGreen)
            ) {
                // Main Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSearching) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { mainViewModel.setSearchQuery(it) },
                            placeholder = { Text("Search chats or messages...", color = Color.White.copy(alpha = 0.7f)) },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { mainViewModel.toggleSearch(false) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close search", tint = Color.White)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_input_field")
                        )
                    } else {
                        // Original Logo & App Name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            ArishLogo(size = 32.dp, bubbleColor = Color.White, letterColor = ArishDarkGreen)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ARISH MESSENGER",
                                color = Color.White,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Action Icons: Search, New Chat, Overflow Menu
                        IconButton(
                            onClick = { mainViewModel.toggleSearch(true) },
                            modifier = Modifier.testTag("search_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                        }

                        IconButton(
                            onClick = onNavigateToNewChat,
                            modifier = Modifier.testTag("new_chat_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "New Chat", tint = Color.White)
                        }

                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("New group") },
                                    onClick = {
                                        menuExpanded = false
                                        onNavigateToNewGroup()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = {
                                        menuExpanded = false
                                        onNavigateToSettings()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Profile") },
                                    onClick = {
                                        menuExpanded = false
                                        mainViewModel.selectTab(MainTab.PROFILE)
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Log out", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        menuExpanded = false
                                        onSignOut()
                                    }
                                )
                            }
                        }
                    }
                }

                // 3 WhatsApp Style Tabs
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = ArishDarkGreen,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = ArishBrightGreen,
                            height = 3.5.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == MainTab.CHATS,
                        onClick = { mainViewModel.selectTab(MainTab.CHATS) },
                        text = {
                            Text(
                                text = "CHATS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (selectedTab == MainTab.CHATS) Color.White else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == MainTab.STATUS,
                        onClick = { mainViewModel.selectTab(MainTab.STATUS) },
                        text = {
                            Text(
                                text = "STATUS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (selectedTab == MainTab.STATUS) Color.White else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == MainTab.PROFILE,
                        onClick = { mainViewModel.selectTab(MainTab.PROFILE) },
                        text = {
                            Text(
                                text = "PROFILE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (selectedTab == MainTab.PROFILE) Color.White else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                MainTab.CHATS -> {
                    FloatingActionButton(
                        onClick = onNavigateToNewChat,
                        containerColor = ArishEmeraldGreen,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.testTag("fab_new_chat")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "New Chat")
                    }
                }
                MainTab.STATUS -> {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FloatingActionButton(
                            onClick = onOpenAddTextStatus,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("fab_text_status")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Text Status", modifier = Modifier.size(20.dp))
                        }
                        FloatingActionButton(
                            onClick = onOpenAddPhotoStatus,
                            containerColor = ArishEmeraldGreen,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.testTag("fab_photo_status")
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = "Photo Status")
                        }
                    }
                }
                MainTab.PROFILE -> {
                    FloatingActionButton(
                        onClick = onNavigateToEditProfile,
                        containerColor = ArishEmeraldGreen,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.testTag("fab_edit_profile")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                MainTab.CHATS -> {
                    ChatsTabContent(
                        chatsState = chatsState,
                        currentUserId = mainViewModel.currentUserId,
                        onChatClick = onNavigateToChat,
                        onPinChat = { mainViewModel.togglePinChat(it) },
                        onStartNewChat = onNavigateToNewChat
                    )
                }
                MainTab.STATUS -> {
                    StatusTabContent(
                        statusesState = statusesState,
                        currentProfile = currentProfile,
                        currentUserId = mainViewModel.currentUserId,
                        onAddStatus = onOpenAddPhotoStatus,
                        onViewStatus = onNavigateToStatusViewer
                    )
                }
                MainTab.PROFILE -> {
                    ProfileTabContent(
                        profile = currentProfile,
                        onEditProfile = onNavigateToEditProfile,
                        onOpenSettings = onNavigateToSettings,
                        onSignOut = onSignOut
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatsTabContent(
    chatsState: UiState<List<Chat>>,
    currentUserId: String,
    onChatClick: (String) -> Unit,
    onPinChat: (String) -> Unit,
    onStartNewChat: () -> Unit
) {
    when (chatsState) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ArishEmeraldGreen)
            }
        }
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = chatsState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        is UiState.Success -> {
            val chats = chatsState.data
            if (chats.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(ArishEmeraldGreen.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = ArishEmeraldGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No conversations yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the message icon below to start chatting with your contacts on Arish Messenger.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(chats, key = { it.id }) { chat ->
                        ChatItemRow(
                            chat = chat,
                            currentUserId = currentUserId,
                            onClick = { onChatClick(chat.id) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 76.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatItemRow(
    chat: Chat,
    currentUserId: String,
    onClick: () -> Unit
) {
    val isPinned = chat.pinnedByUids.contains(currentUserId)
    val unreadCount = chat.unreadCounts[currentUserId] ?: 0L

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            name = chat.name,
            photoUrl = chat.photoUrl,
            size = 52.dp,
            isGroup = chat.isGroup
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.name.ifBlank { "Direct Chat" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Text(
                    text = formatTimestamp(chat.lastMessageTimestamp ?: chat.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = if (unreadCount > 0) ArishBrightGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (chat.typingUids.any { it != currentUserId }) "typing..." else chat.lastMessageText.ifBlank { "No messages" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (chat.typingUids.any { it != currentUserId }) ArishEmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = if (chat.typingUids.any { it != currentUserId }) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (unreadCount > 0) {
                        UnreadBadge(count = unreadCount)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusTabContent(
    statusesState: UiState<List<Status>>,
    currentProfile: UserProfile?,
    currentUserId: String,
    onAddStatus: () -> Unit,
    onViewStatus: (Status) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // My Status Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddStatus() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    UserAvatar(
                        name = currentProfile?.displayName ?: "Me",
                        photoUrl = currentProfile?.photoUrl,
                        size = 56.dp
                    )
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(ArishBrightGreen)
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add status",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "My Status",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap to add status update",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                thickness = 0.5.dp
            )

            Text(
                text = "RECENT UPDATES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        when (statusesState) {
            is UiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ArishEmeraldGreen, modifier = Modifier.size(28.dp))
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Text(
                        text = statusesState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            is UiState.Success -> {
                val statuses = statusesState.data
                if (statuses.isEmpty()) {
                    item {
                        Text(
                            text = "No recent status updates from your contacts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(statuses, key = { it.id }) { status ->
                        val isViewed = status.viewerUids.contains(currentUserId)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onViewStatus(status) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .border(
                                        2.5.dp,
                                        if (isViewed) Color.LightGray else ArishBrightGreen,
                                        CircleShape
                                    )
                                    .padding(3.dp)
                            ) {
                                UserAvatar(
                                    name = status.userName,
                                    photoUrl = status.userPhotoUrl,
                                    size = 48.dp
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = status.userName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = formatTimestamp(status.createdAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTabContent(
    profile: UserProfile?,
    onEditProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        UserAvatar(
            name = profile?.displayName ?: "User",
            photoUrl = profile?.photoUrl,
            size = 104.dp,
            isOnline = true,
            showOnlineBadge = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = profile?.displayName ?: "Arish User",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "@${profile?.username ?: "username"}",
            style = MaterialTheme.typography.bodyMedium,
            color = ArishEmeraldGreen,
            fontWeight = FontWeight.Medium
        )

        if (!profile?.phoneNumber.isNullOrBlank()) {
            Text(
                text = profile?.phoneNumber.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ABOUT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = profile?.about ?: "Hey there! I am using Arish Messenger.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Action Items
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Column {
                ProfileOptionRow(
                    icon = Icons.Default.Edit,
                    title = "Edit Profile",
                    onClick = onEditProfile
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ProfileOptionRow(
                    icon = Icons.Default.Settings,
                    title = "App Settings & Privacy",
                    onClick = onOpenSettings
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Log out from Arish Messenger",
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clickable { onSignOut() }
                .padding(16.dp)
                .testTag("profile_logout_button")
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = ArishEmeraldGreen)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

private fun formatTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val date = timestamp.toDate()
    val now = Date()
    val diff = now.time - date.time

    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 24 * 60 * 60 * 1000 -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
        diff < 48 * 60 * 60 * 1000 -> "Yesterday"
        else -> SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
    }
}
