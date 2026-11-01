package com.example.ui.auth

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ArishLogo
import com.example.ui.components.UserAvatar
import com.example.ui.theme.ArishBrightGreen
import com.example.ui.theme.ArishDarkGreen
import com.example.ui.theme.ArishEmeraldGreen
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLoading by authViewModel.isActionLoading.collectAsState()
    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.attemptAutoSignIn(context)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo & Header
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(ArishEmeraldGreen),
                contentAlignment = Alignment.Center
            ) {
                ArishLogo(size = 72.dp, bubbleColor = Color.White, letterColor = ArishEmeraldGreen)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "ARISH MESSENGER",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ArishEmeraldGreen,
                letterSpacing = 1.sp
            )

            Text(
                text = "Stay Connected.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Value proposition card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    FeatureItem(
                        icon = Icons.AutoMirrored.Filled.Chat,
                        title = "Simple & Fast Messaging",
                        subtitle = "Instant messaging with ticks, reactions, and reply support"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FeatureItem(
                        icon = Icons.Default.Security,
                        title = "Secure Cloud Sync",
                        subtitle = "Synced seamlessly with Cloud Firestore and Google Auth"
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Google Sign-In Button
            Button(
                onClick = { authViewModel.signInWithGoogle(context) },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("google_sign_in_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ArishEmeraldGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(26.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Google 'G' icon or Arish bubble
                        ArishLogo(size = 22.dp, bubbleColor = Color.White, letterColor = ArishEmeraldGreen)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Sign in with Google",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            AnimatedVisibility(visible = uiState is AuthUiState.Error) {
                val errorMsg = (uiState as? AuthUiState.Error)?.message ?: ""
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "By signing in, you agree to Arish Messenger's Terms and Privacy Notice.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(ArishEmeraldGreen.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ArishEmeraldGreen,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    authViewModel: AuthViewModel,
    initialDisplayName: String = "",
    initialUsername: String = "",
    initialPhone: String = "",
    initialAbout: String = "Hey there! I am using Arish Messenger.",
    initialPhotoUrl: String = "",
    isEditing: Boolean = false,
    onComplete: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading by authViewModel.isActionLoading.collectAsState()

    var displayName by remember { mutableStateOf(initialDisplayName) }
    var username by remember { mutableStateOf(initialUsername) }
    var phoneNumber by remember { mutableStateOf(initialPhone) }
    var about by remember { mutableStateOf(initialAbout) }
    var selectedPhotoUrl by remember { mutableStateOf(initialPhotoUrl) }

    val countryCodes = listOf(
        "+1 (US/CA)", "+44 (UK)", "+91 (IN)", "+92 (PK)",
        "+971 (UAE)", "+966 (KSA)", "+49 (DE)", "+33 (FR)",
        "+61 (AU)", "+81 (JP)", "+86 (CN)", "+234 (NG)"
    )
    var selectedCountryCode by remember { mutableStateOf("+1 (US/CA)") }
    var countryCodeExpanded by remember { mutableStateOf(false) }

    // Preset avatars for custom selection
    val avatarPresets = listOf(
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
        "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = ArishDarkGreen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ArishLogo(size = 32.dp, bubbleColor = Color.White, letterColor = ArishDarkGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isEditing) "Edit Profile" else "Profile Setup",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isEditing) "Update your details below" else "Please provide your name and optional profile picture",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main avatar preview
            UserAvatar(
                name = displayName.ifBlank { "A" },
                photoUrl = selectedPhotoUrl,
                size = 96.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Choose an avatar:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Option for default monogram
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (selectedPhotoUrl.isBlank()) ArishBrightGreen else Color.LightGray)
                        .clickable { selectedPhotoUrl = "" }
                        .border(
                            2.dp,
                            if (selectedPhotoUrl.isBlank()) ArishDarkGreen else Color.Transparent,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("A", color = Color.White, fontWeight = FontWeight.Bold)
                }

                avatarPresets.forEach { url ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable { selectedPhotoUrl = url }
                            .border(
                                2.dp,
                                if (selectedPhotoUrl == url) ArishBrightGreen else Color.Transparent,
                                CircleShape
                            )
                    ) {
                        UserAvatar(name = "", photoUrl = url, size = 44.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Full Name
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ArishEmeraldGreen) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("full_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ArishEmeraldGreen,
                    focusedLabelColor = ArishEmeraldGreen
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Unique Username (e.g. arish_user)") },
                leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = ArishEmeraldGreen) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("username_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ArishEmeraldGreen,
                    focusedLabelColor = ArishEmeraldGreen
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Country Code & Phone Number
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = countryCodeExpanded,
                    onExpandedChange = { countryCodeExpanded = it },
                    modifier = Modifier.weight(0.42f)
                ) {
                    OutlinedTextField(
                        value = selectedCountryCode.substringBefore(" "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Code") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryCodeExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArishEmeraldGreen,
                            focusedLabelColor = ArishEmeraldGreen
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = countryCodeExpanded,
                        onDismissRequest = { countryCodeExpanded = false }
                    ) {
                        countryCodes.forEach { code ->
                            DropdownMenuItem(
                                text = { Text(code) },
                                onClick = {
                                    selectedCountryCode = code
                                    countryCodeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Mobile Phone") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = ArishEmeraldGreen) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(0.58f)
                        .testTag("phone_number_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArishEmeraldGreen,
                        focusedLabelColor = ArishEmeraldGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About section
            OutlinedTextField(
                value = about,
                onValueChange = { about = it },
                label = { Text("About") },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = ArishEmeraldGreen) },
                singleLine = false,
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ArishEmeraldGreen,
                    focusedLabelColor = ArishEmeraldGreen
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val fullPhone = if (phoneNumber.startsWith("+")) phoneNumber else "${selectedCountryCode.substringBefore(" ")} $phoneNumber".trim()
                    authViewModel.saveProfile(
                        displayName = displayName,
                        username = username,
                        phoneNumber = fullPhone,
                        about = about,
                        photoUrl = selectedPhotoUrl,
                        onSuccess = onComplete,
                        onError = { error ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(error)
                            }
                        }
                    )
                },
                enabled = !isLoading && displayName.isNotBlank() && username.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_profile_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ArishEmeraldGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(26.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (isEditing) "Save Changes" else "Finish Setup",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
