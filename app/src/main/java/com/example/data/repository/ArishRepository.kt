package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.BlockedUser
import com.example.data.model.Chat
import com.example.data.model.Message
import com.example.data.model.Status
import com.example.data.model.UserProfile
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class ArishRepository(val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // ==========================================
    // USER PROFILES & CONTACTS
    // ==========================================

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot -> snapshot.toObject(UserProfile::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    fun observeAllUsers(): Flow<List<UserProfile>> = flow {
        val path = "users"
        emitAll(
            db.collection("users")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(UserProfile::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val payload = hashMapOf<String, Any>(
                "userId" to uid,
                "displayName" to profile.displayName,
                "username" to profile.username.trim().lowercase(),
                "phoneNumber" to profile.phoneNumber,
                "about" to profile.about,
                "photoUrl" to profile.photoUrl,
                "isOnline" to profile.isOnline,
                "lastSeen" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (profile.createdAt == null) {
                payload["createdAt"] = FieldValue.serverTimestamp()
            }
            docRef.set(payload, com.google.firebase.firestore.SetOptions.merge()).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updatePresence(isOnline: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val docRef = db.collection("users").document(uid)
        try {
            docRef.update(
                mapOf(
                    "isOnline" to isOnline,
                    "lastSeen" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
        }
    }

    // ==========================================
    // CHATS & CONVERSATIONS
    // ==========================================

    fun observeUserChats(): Flow<List<Chat>> = flow {
        val uid = requireUserId()
        val path = "chats"
        emitAll(
            db.collection("chats")
                .whereArrayContains("participantUids", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Chat::class.java).sortedWith(
                        compareByDescending<Chat> { it.pinnedByUids.contains(uid) }
                            .thenByDescending { it.lastMessageTimestamp?.seconds ?: it.updatedAt?.seconds ?: 0L }
                    )
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun getOrCreateDirectChat(otherUser: UserProfile): String {
        val uid = requireUserId()
        val otherUid = otherUser.userId
        val existing = db.collection("chats")
            .whereEqualTo("isGroup", false)
            .whereArrayContains("participantUids", uid)
            .get()
            .await()

        for (doc in existing.documents) {
            val chat = doc.toObject(Chat::class.java)
            if (chat != null && chat.participantUids.contains(otherUid)) {
                return chat.id
            }
        }

        // Create new direct chat
        val newChatId = UUID.randomUUID().toString()
        val chatRef = db.collection("chats").document(newChatId)
        val payload = hashMapOf<String, Any>(
            "id" to newChatId,
            "isGroup" to false,
            "name" to otherUser.displayName,
            "description" to "",
            "photoUrl" to otherUser.photoUrl,
            "adminUids" to emptyList<String>(),
            "participantUids" to listOf(uid, otherUid),
            "pinnedByUids" to emptyList<String>(),
            "lastMessageText" to "Chat started",
            "lastMessageSenderId" to uid,
            "lastMessageTimestamp" to FieldValue.serverTimestamp(),
            "unreadCounts" to mapOf(otherUid to 0L, uid to 0L),
            "typingUids" to emptyList<String>(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            chatRef.set(payload).await()
            return newChatId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, chatRef.path)
            throw e
        }
    }

    suspend fun createGroupChat(name: String, description: String, memberUids: List<String>, photoUrl: String = ""): String {
        val uid = requireUserId()
        val allMembers = (memberUids + uid).distinct()
        val newChatId = UUID.randomUUID().toString()
        val chatRef = db.collection("chats").document(newChatId)
        val payload = hashMapOf<String, Any>(
            "id" to newChatId,
            "isGroup" to true,
            "name" to name,
            "description" to description,
            "photoUrl" to photoUrl,
            "adminUids" to listOf(uid),
            "participantUids" to allMembers,
            "pinnedByUids" to emptyList<String>(),
            "lastMessageText" to "Group created",
            "lastMessageSenderId" to uid,
            "lastMessageTimestamp" to FieldValue.serverTimestamp(),
            "unreadCounts" to allMembers.associateWith { 0L },
            "typingUids" to emptyList<String>(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            chatRef.set(payload).await()
            return newChatId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, chatRef.path)
            throw e
        }
    }

    fun observeMessages(chatId: String): Flow<List<Message>> = flow {
        requireUserId()
        val path = "chats/$chatId/messages"
        emitAll(
            db.collection("chats").document(chatId).collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Message::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun sendMessage(
        chatId: String,
        text: String,
        type: String = "text",
        mediaUrl: String = "",
        mediaName: String = "",
        mediaSize: String = "",
        audioDuration: Long = 0L,
        replyToMessage: Message? = null
    ) {
        val uid = requireUserId()
        val chatDoc = db.collection("chats").document(chatId).get().await()
        val chat = chatDoc.toObject(Chat::class.java) ?: return

        val msgId = UUID.randomUUID().toString()
        val msgRef = db.collection("chats").document(chatId).collection("messages").document(msgId)

        val senderProfileDoc = db.collection("users").document(uid).get().await()
        val senderName = senderProfileDoc.getString("displayName") ?: "User"

        val payload = hashMapOf<String, Any>(
            "id" to msgId,
            "chatId" to chatId,
            "senderId" to uid,
            "senderName" to senderName,
            "recipientId" to (chat.participantUids.firstOrNull { it != uid } ?: ""),
            "participantUids" to chat.participantUids,
            "text" to text,
            "type" to type,
            "mediaUrl" to mediaUrl,
            "mediaName" to mediaName,
            "mediaSize" to mediaSize,
            "audioDuration" to audioDuration,
            "status" to "sent",
            "replyToMessageId" to (replyToMessage?.id ?: ""),
            "replyToText" to (replyToMessage?.text ?: ""),
            "replyToSender" to (replyToMessage?.senderName ?: ""),
            "reactions" to emptyMap<String, String>(),
            "isEdited" to false,
            "isDeleted" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            msgRef.set(payload).await()

            // Update chat last message
            val snippet = when (type) {
                "image" -> "📷 Photo"
                "audio" -> "🎤 Voice message (${audioDuration}s)"
                "document" -> "📄 $mediaName"
                else -> text
            }
            val chatUpdates = hashMapOf<String, Any>(
                "lastMessageText" to snippet,
                "lastMessageSenderId" to uid,
                "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("chats").document(chatId).update(chatUpdates).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            throw e
        }
    }

    suspend fun updateMessageStatus(chatId: String, messageId: String, status: String) {
        requireUserId()
        val msgRef = db.collection("chats").document(chatId).collection("messages").document(messageId)
        try {
            msgRef.update(
                mapOf(
                    "status" to status,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, msgRef.path)
        }
    }

    suspend fun addReaction(chatId: String, messageId: String, emoji: String) {
        val uid = requireUserId()
        val msgRef = db.collection("chats").document(chatId).collection("messages").document(messageId)
        try {
            msgRef.update(
                mapOf(
                    "reactions.$uid" to emoji,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, msgRef.path)
        }
    }

    suspend fun editMessage(chatId: String, messageId: String, newText: String) {
        requireUserId()
        val msgRef = db.collection("chats").document(chatId).collection("messages").document(messageId)
        try {
            msgRef.update(
                mapOf(
                    "text" to newText,
                    "isEdited" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, msgRef.path)
            throw e
        }
    }

    suspend fun deleteMessage(chatId: String, messageId: String) {
        requireUserId()
        val msgRef = db.collection("chats").document(chatId).collection("messages").document(messageId)
        try {
            msgRef.update(
                mapOf(
                    "isDeleted" to true,
                    "text" to "This message was deleted",
                    "mediaUrl" to "",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, msgRef.path)
            throw e
        }
    }

    suspend fun setTyping(chatId: String, isTyping: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val chatRef = db.collection("chats").document(chatId)
        try {
            if (isTyping) {
                chatRef.update("typingUids", FieldValue.arrayUnion(uid)).await()
            } else {
                chatRef.update("typingUids", FieldValue.arrayRemove(uid)).await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, chatRef.path)
        }
    }

    suspend fun togglePinChat(chatId: String) {
        val uid = requireUserId()
        val chatRef = db.collection("chats").document(chatId)
        try {
            val doc = chatRef.get().await()
            val chat = doc.toObject(Chat::class.java) ?: return
            if (chat.pinnedByUids.contains(uid)) {
                chatRef.update("pinnedByUids", FieldValue.arrayRemove(uid)).await()
            } else {
                chatRef.update("pinnedByUids", FieldValue.arrayUnion(uid)).await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, chatRef.path)
        }
    }

    // ==========================================
    // STATUS (STORIES)
    // ==========================================

    fun observeRecentStatuses(): Flow<List<Status>> = flow {
        requireUserId()
        val path = "statuses"
        emitAll(
            db.collection("statuses")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Status::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun postStatus(
        type: String,
        content: String,
        caption: String = "",
        backgroundColor: String = "#128C7E"
    ) {
        val uid = requireUserId()
        val profileDoc = db.collection("users").document(uid).get().await()
        val profile = profileDoc.toObject(UserProfile::class.java)

        val statusId = UUID.randomUUID().toString()
        val statusRef = db.collection("statuses").document(statusId)
        val expireTime = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)

        val payload = hashMapOf<String, Any>(
            "id" to statusId,
            "userId" to uid,
            "userName" to (profile?.displayName ?: "User"),
            "userPhotoUrl" to (profile?.photoUrl ?: ""),
            "type" to type,
            "content" to content,
            "caption" to caption,
            "backgroundColor" to backgroundColor,
            "viewerUids" to emptyList<String>(),
            "createdAt" to FieldValue.serverTimestamp(),
            "expiresAt" to Timestamp(expireTime)
        )
        try {
            statusRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, statusRef.path)
            throw e
        }
    }

    suspend fun markStatusViewed(statusId: String) {
        val uid = requireUserId()
        val statusRef = db.collection("statuses").document(statusId)
        try {
            statusRef.update("viewerUids", FieldValue.arrayUnion(uid)).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, statusRef.path)
        }
    }

    suspend fun deleteStatus(statusId: String) {
        val uid = requireUserId()
        val statusRef = db.collection("statuses").document(statusId)
        try {
            statusRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, statusRef.path)
            throw e
        }
    }

    // ==========================================
    // BLOCKED USERS
    // ==========================================

    fun observeBlockedUsers(): Flow<List<BlockedUser>> = flow {
        val uid = requireUserId()
        val path = "blockedUsers"
        emitAll(
            db.collection("blockedUsers")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(BlockedUser::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun blockUser(blockedUserId: String) {
        val uid = requireUserId()
        val blockId = "${uid}_$blockedUserId"
        val docRef = db.collection("blockedUsers").document(blockId)
        val payload = hashMapOf<String, Any>(
            "id" to blockId,
            "userId" to uid,
            "blockedUserId" to blockedUserId,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun unblockUser(blockedUserId: String) {
        val uid = requireUserId()
        val blockId = "${uid}_$blockedUserId"
        val docRef = db.collection("blockedUsers").document(blockId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }
}
