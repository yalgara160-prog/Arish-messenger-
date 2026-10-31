package com.example.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ArishLogo
import com.example.ui.components.UserAvatar
import com.example.ui.main.MainViewModel
import com.example.ui.theme.ArishDarkGreen
import com.example.ui.theme.ArishEmeraldGreen

@Composable
fun SettingsScreen(
    mainViewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentProfile by mainViewModel.currentProfileState.collectAsState()
    val themeMode by mainViewModel.themeMode.collectAsState()
    val wallpaper by mainViewModel.chatWallpaper.collectAsState()
    val notificationsEnabled by mainViewModel.notificationsEnabled.collectAsState()
    val readReceiptsEnabled by mainViewModel.readReceiptsEnabled.collectAsState()
    val blockedUsers by mainViewModel.blockedUsersState.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var showBlockedUsersDialog by remember { mutableStateOf(false) }
    var showStorageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

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
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        text = "Settings",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Preview Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToEditProfile() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    name = currentProfile?.displayName ?: "User",
                    photoUrl = currentProfile?.photoUrl,
                    size = 64.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentProfile?.displayName ?: "Arish User",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentProfile?.about ?: "Hey there! I am using Arish Messenger.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            SettingsCategoryTitle("PREFERENCES")

            SettingsItem(
                icon = Icons.Default.DarkMode,
                title = "Theme",
                subtitle = when (themeMode) {
                    "light" -> "Light mode"
                    "dark" -> "Dark mode"
                    else -> "System default"
                },
                onClick = { showThemeDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.Wallpaper,
                title = "Chat Wallpaper",
                subtitle = wallpaper.replaceFirstChar { it.uppercase() },
                onClick = { showWallpaperDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = if (notificationsEnabled) "Tone, vibrations, and banners active" else "Muted",
                trailing = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { mainViewModel.toggleNotifications(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ArishEmeraldGreen)
                    )
                },
                onClick = { mainViewModel.toggleNotifications(!notificationsEnabled) }
            )

            SettingsCategoryTitle("PRIVACY & SECURITY")

            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Read Receipts",
                subtitle = if (readReceiptsEnabled) "Blue double ticks enabled" else "Turned off",
                trailing = {
                    Switch(
                        checked = readReceiptsEnabled,
                        onCheckedChange = { mainViewModel.toggleReadReceipts(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ArishEmeraldGreen)
                    )
                },
                onClick = { mainViewModel.toggleReadReceipts(!readReceiptsEnabled) }
            )

            SettingsItem(
                icon = Icons.Default.Block,
                title = "Blocked Users",
                subtitle = "${blockedUsers.size} contacts blocked",
                onClick = { showBlockedUsersDialog = true }
            )

            SettingsCategoryTitle("DATA & HELP")

            SettingsItem(
                icon = Icons.Default.Storage,
                title = "Storage & Network",
                subtitle = "Network usage and cache data",
                onClick = { showStorageDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.Help,
                title = "Help & Support",
                subtitle = "FAQ, contact us, and privacy policy",
                onClick = {
                    Toast.makeText(context, "Support: contact support@arishmessenger.app", Toast.LENGTH_LONG).show()
                }
            )

            SettingsItem(
                icon = Icons.Default.Info,
                title = "About Arish Messenger",
                subtitle = "Version 1.0 • Stay Connected.",
                onClick = { showAboutDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Logout & Delete
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSignOut() }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Log Out",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDeleteAccountDialog = true }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Delete Account",
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("system" to "System default", "light" to "Light", "dark" to "Dark").forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    mainViewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    mainViewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = ArishEmeraldGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Wallpaper Dialog
    if (showWallpaperDialog) {
        AlertDialog(
            onDismissRequest = { showWallpaperDialog = false },
            title = { Text("Chat Wallpaper", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("classic" to "Default Doodle Pattern", "mint" to "Soft Mint Green", "emerald" to "Emerald Tint", "dark" to "Solid Dark Slate").forEach { (wp, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    mainViewModel.setChatWallpaper(wp)
                                    showWallpaperDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = wallpaper == wp,
                                onClick = {
                                    mainViewModel.setChatWallpaper(wp)
                                    showWallpaperDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = ArishEmeraldGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWallpaperDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Blocked Users Dialog
    if (showBlockedUsersDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedUsersDialog = false },
            title = { Text("Blocked Users", fontWeight = FontWeight.Bold) },
            text = {
                if (blockedUsers.isEmpty()) {
                    Text("No blocked contacts.")
                } else {
                    Column {
                        blockedUsers.forEach { blocked ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "User ID: ${blocked.blockedUserId.take(10)}...")
                                TextButton(onClick = { mainViewModel.unblockUser(blocked.blockedUserId) }) {
                                    Text("Unblock", color = ArishEmeraldGreen)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBlockedUsersDialog = false }) { Text("Done") }
            }
        )
    }

    // Storage Dialog
    if (showStorageDialog) {
        AlertDialog(
            onDismissRequest = { showStorageDialog = false },
            title = { Text("Storage and Data", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Local Cache: 12.4 MB")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Media Messages: 8.2 MB")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Database: Firebase Cloud Firestore (Synchronized)")
                }
            },
            confirmButton = {
                TextButton(onClick = { showStorageDialog = false }) { Text("OK") }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ArishLogo(size = 32.dp, bubbleColor = ArishDarkGreen, letterColor = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("ARISH MESSENGER", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Stay Connected.", fontWeight = FontWeight.SemiBold, color = ArishEmeraldGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("A modern, fast, and simple messaging experience inspired by WhatsApp.\n\n• Real-time cloud sync with Firebase\n• Status stories with 24h expiration\n• Group conversations and direct chats\n• Voice notes and media sharing\n\n© 2026 Arish Messenger Inc.")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ArishEmeraldGreen)
                ) {
                    Text("Got it")
                }
            }
        )
    }

    // Delete Account Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = {
                Text("Deleting your account will remove your profile, chat history, and active status updates. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Forever")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsCategoryTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = ArishEmeraldGreen,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = ArishEmeraldGreen, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (trailing != null) {
            trailing()
        }
    }
}
