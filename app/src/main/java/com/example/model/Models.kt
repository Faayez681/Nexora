package com.example.model

enum class UserStatus {
    ONLINE, OFFLINE, AWAY, DND
}

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarColor: Long = 0xFF4F46E5,
    val bio: String = "",
    val status: UserStatus = UserStatus.ONLINE,
    val lastSeen: String = "Just now",
    val isCurrentUser: Boolean = false
)

enum class ConversationType {
    DIRECT, GROUP, CHANNEL
}

data class Conversation(
    val id: String,
    val type: ConversationType,
    val name: String,
    val avatarColor: Long = 0xFF3B82F6,
    val description: String = "",
    val lastMessageText: String = "",
    val lastMessageTime: String = "",
    val lastMessageSender: String = "",
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val memberCount: Int = 2,
    val memberIds: List<String> = emptyList(),
    val category: String = "General"
)

enum class MessageType {
    TEXT, IMAGE, AUDIO, FILE, CODE
}

enum class MessageDeliveryStatus {
    SENDING, SENT, DELIVERED, READ
}

data class Reaction(
    val emoji: String,
    val count: Int,
    val userIds: List<String> = emptyList()
)

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val status: MessageDeliveryStatus = MessageDeliveryStatus.READ,
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String? = null,
    val fileName: String? = null,
    val fileSize: String? = null,
    val audioDurationSeconds: Int? = null,
    val replyToId: String? = null,
    val replyToSender: String? = null,
    val replyToText: String? = null,
    val reactions: List<Reaction> = emptyList(),
    val isEdited: Boolean = false,
    val isPinned: Boolean = false
)

data class Channel(
    val id: String,
    val communityId: String,
    val name: String,
    val topic: String,
    val isVoice: Boolean = false,
    val unreadCount: Int = 0,
    val connectedVoiceUsers: List<String> = emptyList()
)

data class Community(
    val id: String,
    val name: String,
    val iconColor: Long,
    val description: String,
    val memberCount: Int,
    val channels: List<Channel> = emptyList()
)
