package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val username: String = "",
    val phoneNumber: String = "",
    val about: String = "Hey there! I am using Arish Messenger.",
    val photoUrl: String = "",
    val isOnline: Boolean = false,
    @ServerTimestamp val lastSeen: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Chat(
    @DocumentId val id: String = "",
    val isGroup: Boolean = false,
    val name: String = "",
    val description: String = "",
    val photoUrl: String = "",
    val adminUids: List<String> = emptyList(),
    val participantUids: List<String> = emptyList(),
    val pinnedByUids: List<String> = emptyList(),
    val lastMessageText: String = "",
    val lastMessageSenderId: String = "",
    @ServerTimestamp val lastMessageTimestamp: Timestamp? = null,
    val unreadCounts: Map<String, Long> = emptyMap(),
    val typingUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Message(
    @DocumentId val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val recipientId: String = "",
    val participantUids: List<String> = emptyList(),
    val text: String = "",
    val type: String = "text", // "text", "image", "audio", "document"
    val mediaUrl: String = "",
    val mediaName: String = "",
    val mediaSize: String = "",
    val audioDuration: Long = 0L,
    val status: String = "sent", // "sent", "delivered", "read"
    val replyToMessageId: String = "",
    val replyToText: String = "",
    val replyToSender: String = "",
    val reactions: Map<String, String> = emptyMap(), // uid -> emoji
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Status(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val type: String = "text", // "text", "image"
    val content: String = "",
    val caption: String = "",
    val backgroundColor: String = "#128C7E",
    val viewerUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    val expiresAt: Timestamp? = null
)

@IgnoreExtraProperties
data class BlockedUser(
    @DocumentId val id: String = "",
    val userId: String = "",
    val blockedUserId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
