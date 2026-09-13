package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: String,
    val timestampMillis: Long,
    val status: String,
    val type: String,
    val mediaUrl: String?,
    val fileName: String?,
    val fileSize: String?,
    val audioDurationSeconds: Int?,
    val replyToId: String?,
    val replyToSender: String?,
    val replyToText: String?,
    val reactionsRaw: String, // format "emoji:count,emoji:count"
    val isEdited: Boolean,
    val isPinned: Boolean
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val type: String,
    val name: String,
    val avatarColor: Long,
    val description: String,
    val lastMessageText: String,
    val lastMessageTime: String,
    val lastMessageSender: String,
    val unreadCount: Int,
    val isOnline: Boolean,
    val isPinned: Boolean,
    val isMuted: Boolean,
    val memberCount: Int,
    val category: String
)
