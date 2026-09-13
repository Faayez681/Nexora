package com.example.data.repository

import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.local.PulseDao
import com.example.model.Channel
import com.example.model.Community
import com.example.model.Conversation
import com.example.model.ConversationType
import com.example.model.Message
import com.example.model.MessageDeliveryStatus
import com.example.model.MessageType
import com.example.model.Reaction
import com.example.model.User
import com.example.model.UserStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class PulseRepository(
    private val pulseDao: PulseDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    // Current logged-in user state
    private val _currentUser = MutableStateFlow(
        User(
            id = "user_me",
            username = "alex_dev",
            displayName = "Alex Vance",
            avatarColor = 0xFF6366F1,
            bio = "Building high-performance distributed real-time systems 🚀",
            status = UserStatus.ONLINE,
            isCurrentUser = true
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // Available users to switch between or chat with
    val sampleUsers = listOf(
        User("user_me", "alex_dev", "Alex Vance", 0xFF6366F1, "Distributed systems engineer", UserStatus.ONLINE, isCurrentUser = true),
        User("user_alice", "alice_k", "Alice Kim", 0xFFEC4899, "Design lead @ Pulse • Creating sleek UI/UX", UserStatus.ONLINE),
        User("user_bob", "bob_ross", "Bob Miller", 0xFF10B981, "Backend Architect | WebSockets & Rust", UserStatus.ONLINE),
        User("user_elena", "elena_v", "Elena Rostova", 0xFFF59E0B, "Security researcher & cryptography fan", UserStatus.AWAY),
        User("user_marcus", "marcus_w", "Marcus Wright", 0xFF06B6D4, "DevOps / SRE | K8s & Cloud", UserStatus.OFFLINE, "2 hours ago")
    )

    // Real-time typing indicators map: conversationId -> set of typing user names
    private val _typingStatus = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val typingStatus: StateFlow<Map<String, List<String>>> = _typingStatus.asStateFlow()

    // Communities & Channels
    private val _communities = MutableStateFlow<List<Community>>(emptyList())
    val communities: StateFlow<List<Community>> = _communities.asStateFlow()

    // Active voice channel state
    private val _activeVoiceChannel = MutableStateFlow<String?>(null)
    val activeVoiceChannel: StateFlow<String?> = _activeVoiceChannel.asStateFlow()

    init {
        scope.launch {
            initDefaultData()
            initCommunities()
        }
    }

    private fun initCommunities() {
        _communities.value = listOf(
            Community(
                id = "comm_engineers",
                name = "Engineering Core",
                iconColor = 0xFF3B82F6,
                description = "Architecture, distributed databases, high throughput sockets & protocols.",
                memberCount = 1420,
                channels = listOf(
                    Channel("chan_eng_general", "comm_engineers", "general-arch", "Discussing system topologies"),
                    Channel("chan_eng_perf", "comm_engineers", "benchmarks", "Performance metrics & load tests"),
                    Channel("chan_eng_voice", "comm_engineers", "Lounge Voice", "Open audio collaboration", isVoice = true, connectedVoiceUsers = listOf("Bob Miller", "Elena"))
                )
            ),
            Community(
                id = "comm_design",
                name = "Pulse Design Collective",
                iconColor = 0xFFEC4899,
                description = "Modern UI patterns, micro-interactions, dark themes & accessibility.",
                memberCount = 840,
                channels = listOf(
                    Channel("chan_design_system", "comm_design", "design-tokens", "Typography, palette & specs"),
                    Channel("chan_critique", "comm_design", "showcase", "Design feedback & mockups"),
                    Channel("chan_design_voice", "comm_design", "Studio Audio", "Design sync", isVoice = true)
                )
            )
        )
    }

    private suspend fun initDefaultData() {
        if (pulseDao.getConversationCount() > 0) return

        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val initialConversations = listOf(
            ConversationEntity(
                id = "conv_alice",
                type = "DIRECT",
                name = "Alice Kim",
                avatarColor = 0xFFEC4899,
                description = "Senior Product Designer",
                lastMessageText = "The new glassmorphism micro-interaction feels so fluid! Check the preview.",
                lastMessageTime = "10:42",
                lastMessageSender = "Alice",
                unreadCount = 2,
                isOnline = true,
                isPinned = true,
                isMuted = false,
                memberCount = 2,
                category = "Direct"
            ),
            ConversationEntity(
                id = "conv_bob",
                type = "DIRECT",
                name = "Bob Miller",
                avatarColor = 0xFF10B981,
                description = "Backend Architect",
                lastMessageText = "Benchmarked the WebSocket gateway with 50,000 concurrent sockets. P99 is 4ms.",
                lastMessageTime = "09:15",
                lastMessageSender = "Bob",
                unreadCount = 0,
                isOnline = true,
                isPinned = false,
                isMuted = false,
                memberCount = 2,
                category = "Direct"
            ),
            ConversationEntity(
                id = "conv_guild",
                type = "GROUP",
                name = "Pulse Protocol Core",
                avatarColor = 0xFF8B5CF6,
                description = "Real-time messaging architecture & protocol discussions",
                lastMessageText = "Elena: Added end-to-end ratchet encryption keys rotation spec.",
                lastMessageTime = "Yesterday",
                lastMessageSender = "Elena",
                unreadCount = 5,
                isOnline = true,
                isPinned = true,
                isMuted = false,
                memberCount = 18,
                category = "Groups"
            ),
            ConversationEntity(
                id = "conv_elena",
                type = "DIRECT",
                name = "Elena Rostova",
                avatarColor = 0xFFF59E0B,
                description = "Security & Crypto Engineer",
                lastMessageText = "Sent you the cryptographic audit report. All hash checks verified.",
                lastMessageTime = "Yesterday",
                lastMessageSender = "Elena",
                unreadCount = 0,
                isOnline = false,
                isPinned = false,
                isMuted = true,
                memberCount = 2,
                category = "Direct"
            )
        )
        pulseDao.insertConversations(initialConversations)

        // Initial messages for Alice
        val initialMessagesAlice = listOf(
            MessageEntity(
                id = "msg_1",
                conversationId = "conv_alice",
                senderId = "user_alice",
                senderName = "Alice Kim",
                content = "Hey Alex! Did you get a chance to review the real-time interaction tokens?",
                timestamp = "10:30",
                timestampMillis = System.currentTimeMillis() - 600000,
                status = "READ",
                type = "TEXT",
                mediaUrl = null,
                fileName = null,
                fileSize = null,
                audioDurationSeconds = null,
                replyToId = null,
                replyToSender = null,
                replyToText = null,
                reactionsRaw = "👍:2,🔥:1",
                isEdited = false,
                isPinned = false
            ),
            MessageEntity(
                id = "msg_2",
                conversationId = "conv_alice",
                senderId = "user_me",
                senderName = "Alex Vance",
                content = "Yes! The spring physics and staggered list reveal look phenomenal. The active pill indicator aligns seamlessly with M3.",
                timestamp = "10:35",
                timestampMillis = System.currentTimeMillis() - 300000,
                status = "READ",
                type = "TEXT",
                mediaUrl = null,
                fileName = null,
                fileSize = null,
                audioDurationSeconds = null,
                replyToId = "msg_1",
                replyToSender = "Alice Kim",
                replyToText = "Hey Alex! Did you get a chance to review the real-time interaction tokens?",
                reactionsRaw = "❤️:1",
                isEdited = false,
                isPinned = true
            ),
            MessageEntity(
                id = "msg_3",
                conversationId = "conv_alice",
                senderId = "user_alice",
                senderName = "Alice Kim",
                content = "The new glassmorphism micro-interaction feels so fluid! Check the preview.",
                timestamp = "10:42",
                timestampMillis = System.currentTimeMillis() - 60000,
                status = "READ",
                type = "TEXT",
                mediaUrl = null,
                fileName = null,
                fileSize = null,
                audioDurationSeconds = null,
                replyToId = null,
                replyToSender = null,
                replyToText = null,
                reactionsRaw = "🚀:3",
                isEdited = false,
                isPinned = false
            )
        )
        pulseDao.insertMessages(initialMessagesAlice)

        // Initial messages for Pulse Protocol Core Group
        val initialMessagesGuild = listOf(
            MessageEntity(
                id = "msg_g1",
                conversationId = "conv_guild",
                senderId = "user_bob",
                senderName = "Bob Miller",
                content = "Welcome everyone to Pulse Protocol Core! We are standardizing WebSocket event payloads for zero packet overhead.",
                timestamp = "Yesterday",
                timestampMillis = System.currentTimeMillis() - 86400000,
                status = "READ",
                type = "TEXT",
                mediaUrl = null,
                fileName = null,
                fileSize = null,
                audioDurationSeconds = null,
                replyToId = null,
                replyToSender = null,
                replyToText = null,
                reactionsRaw = "🎉:5,👏:3",
                isEdited = false,
                isPinned = true
            ),
            MessageEntity(
                id = "msg_g2",
                conversationId = "conv_guild",
                senderId = "user_elena",
                senderName = "Elena Rostova",
                content = "Elena: Added end-to-end ratchet encryption keys rotation spec.",
                timestamp = "Yesterday",
                timestampMillis = System.currentTimeMillis() - 43200000,
                status = "READ",
                type = "TEXT",
                mediaUrl = null,
                fileName = null,
                fileSize = null,
                audioDurationSeconds = null,
                replyToId = null,
                replyToSender = null,
                replyToText = null,
                reactionsRaw = "🔒:4",
                isEdited = false,
                isPinned = false
            )
        )
        pulseDao.insertMessages(initialMessagesGuild)
    }

    // Conversations Flow
    val conversations: Flow<List<Conversation>> = pulseDao.getAllConversations().map { list ->
        list.map { it.toDomain() }
    }

    // Messages Flow for conversation
    fun getMessages(conversationId: String): Flow<List<Message>> {
        return pulseDao.getMessagesForConversation(conversationId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun sendMessage(
        conversationId: String,
        content: String,
        type: MessageType = MessageType.TEXT,
        replyTo: Message? = null,
        fileName: String? = null,
        fileSize: String? = null,
        audioDurationSeconds: Int? = null
    ) {
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val msgId = "msg_" + UUID.randomUUID().toString().take(8)
        val user = _currentUser.value

        val entity = MessageEntity(
            id = msgId,
            conversationId = conversationId,
            senderId = user.id,
            senderName = user.displayName,
            content = content,
            timestamp = now,
            timestampMillis = System.currentTimeMillis(),
            status = MessageDeliveryStatus.SENT.name,
            type = type.name,
            mediaUrl = null,
            fileName = fileName,
            fileSize = fileSize,
            audioDurationSeconds = audioDurationSeconds,
            replyToId = replyTo?.id,
            replyToSender = replyTo?.senderName,
            replyToText = replyTo?.content,
            reactionsRaw = "",
            isEdited = false,
            isPinned = false
        )

        pulseDao.insertMessage(entity)

        // Optimistic UI: update delivery status to DELIVERED then READ
        scope.launch {
            delay(500)
            pulseDao.updateMessage(entity.copy(status = MessageDeliveryStatus.DELIVERED.name))
            delay(800)
            pulseDao.updateMessage(entity.copy(status = MessageDeliveryStatus.READ.name))

            // Trigger realistic interactive response simulation
            simulateIncomingResponse(conversationId, content)
        }
    }

    private suspend fun simulateIncomingResponse(conversationId: String, userMessage: String) {
        // If it's a direct conversation with Alice or Bob, trigger simulated real-time response
        val sender = when (conversationId) {
            "conv_alice" -> sampleUsers[1] // Alice
            "conv_bob" -> sampleUsers[2] // Bob
            "conv_elena" -> sampleUsers[3] // Elena
            "conv_guild" -> sampleUsers[1] // Alice in group
            else -> null
        } ?: return

        // 1. Start typing indicator
        setTyping(conversationId, sender.displayName, isTyping = true)
        delay(1800)
        setTyping(conversationId, sender.displayName, isTyping = false)

        val replies = when (conversationId) {
            "conv_alice" -> listOf(
                "That matches our design specifications perfectly! The contrast ratio and motion spring curves are locked in.",
                "I love this direction. Let's make sure the dark mode tokens keep that sharp neon cyan accent.",
                "Super clean! The typing indicator and delivery checkmarks feel so tactile and responsive."
            )
            "conv_bob" -> listOf(
                "Confirmed! The message broker handled the batch without any latency spike. Ready for production.",
                "WebSocket heartbeat latency remains strictly under 8ms across all active channels.",
                "Great note. I've tuned the Redis presence pub/sub channel for instant reconnection."
            )
            "conv_guild" -> listOf(
                "Elena: Double ratchet protocol key exchange verified across all group nodes.",
                "Bob: Node clustering is now automated with health checks enabled."
            )
            else -> listOf("Got it! Syncing with the real-time stream.")
        }

        val replyText = replies.random()
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val incomingMsg = MessageEntity(
            id = "msg_" + UUID.randomUUID().toString().take(8),
            conversationId = conversationId,
            senderId = sender.id,
            senderName = sender.displayName,
            content = replyText,
            timestamp = now,
            timestampMillis = System.currentTimeMillis(),
            status = MessageDeliveryStatus.READ.name,
            type = MessageType.TEXT.name,
            mediaUrl = null,
            fileName = null,
            fileSize = null,
            audioDurationSeconds = null,
            replyToId = null,
            replyToSender = null,
            replyToText = null,
            reactionsRaw = "",
            isEdited = false,
            isPinned = false
        )
        pulseDao.insertMessage(incomingMsg)
    }

    fun setTyping(conversationId: String, userName: String, isTyping: Boolean) {
        val current = _typingStatus.value.toMutableMap()
        val list = current[conversationId]?.toMutableList() ?: mutableListOf()
        if (isTyping) {
            if (!list.contains(userName)) list.add(userName)
        } else {
            list.remove(userName)
        }
        current[conversationId] = list
        _typingStatus.value = current
    }

    suspend fun editMessage(messageId: String, newContent: String) {
        pulseDao.editMessage(messageId, newContent)
    }

    suspend fun deleteMessage(messageId: String) {
        pulseDao.deleteMessageById(messageId)
    }

    suspend fun togglePinMessage(messageId: String, currentPinned: Boolean) {
        pulseDao.setPinned(messageId, !currentPinned)
    }

    suspend fun toggleReaction(message: Message, emoji: String) {
        val userId = _currentUser.value.id
        val updatedReactions = message.reactions.toMutableList()
        val existingIndex = updatedReactions.indexOfFirst { it.emoji == emoji }

        if (existingIndex != -1) {
            val existing = updatedReactions[existingIndex]
            if (existing.userIds.contains(userId)) {
                val newUsers = existing.userIds - userId
                if (newUsers.isEmpty()) {
                    updatedReactions.removeAt(existingIndex)
                } else {
                    updatedReactions[existingIndex] = existing.copy(count = newUsers.size, userIds = newUsers)
                }
            } else {
                val newUsers = existing.userIds + userId
                updatedReactions[existingIndex] = existing.copy(count = newUsers.size, userIds = newUsers)
            }
        } else {
            updatedReactions.add(Reaction(emoji = emoji, count = 1, userIds = listOf(userId)))
        }

        val raw = serializeReactions(updatedReactions)
        pulseDao.updateReactions(message.id, raw)
    }

    suspend fun createNewConversation(
        name: String,
        type: ConversationType,
        description: String = "",
        category: String = "Direct"
    ): String {
        val id = "conv_" + UUID.randomUUID().toString().take(8)
        val colors = listOf(0xFF3B82F6, 0xFF10B981, 0xFF8B5CF6, 0xFFF59E0B, 0xFF06B6D4)
        val entity = ConversationEntity(
            id = id,
            type = type.name,
            name = name,
            avatarColor = colors.random(),
            description = description,
            lastMessageText = "Conversation created",
            lastMessageTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            lastMessageSender = _currentUser.value.displayName,
            unreadCount = 0,
            isOnline = true,
            isPinned = false,
            isMuted = false,
            memberCount = if (type == ConversationType.GROUP) 5 else 2,
            category = category
        )
        pulseDao.insertConversation(entity)
        return id
    }

    suspend fun markAsRead(conversationId: String) {
        pulseDao.markConversationAsRead(conversationId)
    }

    fun switchUser(user: User) {
        _currentUser.value = user.copy(isCurrentUser = true)
    }

    fun toggleVoiceChannel(channelId: String) {
        if (_activeVoiceChannel.value == channelId) {
            _activeVoiceChannel.value = null
        } else {
            _activeVoiceChannel.value = channelId
        }
    }

    // Helper converters
    private fun ConversationEntity.toDomain() = Conversation(
        id = id,
        type = ConversationType.valueOf(type),
        name = name,
        avatarColor = avatarColor,
        description = description,
        lastMessageText = lastMessageText,
        lastMessageTime = lastMessageTime,
        lastMessageSender = lastMessageSender,
        unreadCount = unreadCount,
        isOnline = isOnline,
        isPinned = isPinned,
        isMuted = isMuted,
        memberCount = memberCount,
        category = category
    )

    private fun MessageEntity.toDomain() = Message(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        senderName = senderName,
        content = content,
        timestamp = timestamp,
        timestampMillis = timestampMillis,
        status = MessageDeliveryStatus.valueOf(status),
        type = MessageType.valueOf(type),
        mediaUrl = mediaUrl,
        fileName = fileName,
        fileSize = fileSize,
        audioDurationSeconds = audioDurationSeconds,
        replyToId = replyToId,
        replyToSender = replyToSender,
        replyToText = replyToText,
        reactions = parseReactions(reactionsRaw),
        isEdited = isEdited,
        isPinned = isPinned
    )

    private fun parseReactions(raw: String): List<Reaction> {
        if (raw.isBlank()) return emptyList()
        return raw.split(",").mapNotNull { item ->
            val parts = item.split(":")
            if (parts.size >= 2) {
                val emoji = parts[0]
                val count = parts[1].toIntOrNull() ?: 1
                Reaction(emoji = emoji, count = count)
            } else null
        }
    }

    private fun serializeReactions(reactions: List<Reaction>): String {
        return reactions.joinToString(",") { "${it.emoji}:${it.count}" }
    }
}
