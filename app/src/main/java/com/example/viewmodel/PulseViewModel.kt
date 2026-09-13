package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.PulseRepository
import com.example.model.Channel
import com.example.model.Community
import com.example.model.Conversation
import com.example.model.ConversationType
import com.example.model.Message
import com.example.model.MessageType
import com.example.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FilterTab {
    ALL, DIRECT, GROUPS, CHANNELS, UNREAD
}

enum class AppThemeMode {
    DARK, LIGHT, AMOLED
}

class PulseViewModel(
    private val repository: PulseRepository
) : ViewModel() {

    val currentUser: StateFlow<User> = repository.currentUser
    val sampleUsers = repository.sampleUsers
    val communities: StateFlow<List<Community>> = repository.communities
    val activeVoiceChannel: StateFlow<String?> = repository.activeVoiceChannel

    // UI state
    val searchQuery = MutableStateFlow("")
    val activeFilter = MutableStateFlow(FilterTab.ALL)
    val selectedThemeMode = MutableStateFlow(AppThemeMode.DARK)
    val accentColorIndex = MutableStateFlow(0) // 0: Cyan, 1: Violet, 2: Emerald, 3: Amber
    val compactMode = MutableStateFlow(false)
    val fontSizeScale = MutableStateFlow(1.0f)

    // Active conversation
    private val _activeConversation = MutableStateFlow<Conversation?>(null)
    val activeConversation: StateFlow<Conversation?> = _activeConversation.asStateFlow()

    // Filtered conversations
    val conversations: StateFlow<List<Conversation>> = combine(
        repository.conversations,
        searchQuery,
        activeFilter
    ) { list, query, filter ->
        list.filter { conv ->
            val matchesQuery = query.isBlank() ||
                conv.name.contains(query, ignoreCase = true) ||
                conv.lastMessageText.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                FilterTab.ALL -> true
                FilterTab.DIRECT -> conv.type == ConversationType.DIRECT
                FilterTab.GROUPS -> conv.type == ConversationType.GROUP
                FilterTab.CHANNELS -> conv.type == ConversationType.CHANNEL
                FilterTab.UNREAD -> conv.unreadCount > 0
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active conversation messages
    @OptIn(ExperimentalCoroutinesApi::class)
    val activeMessages: StateFlow<List<Message>> = _activeConversation
        .flatMapLatest { conv ->
            if (conv == null) flowOf(emptyList())
            else repository.getMessages(conv.id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently typing user in active conversation
    val activeTypingUsers: StateFlow<List<String>> = combine(
        repository.typingStatus,
        _activeConversation
    ) { typingMap, conv ->
        if (conv == null) emptyList()
        else typingMap[conv.id] ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reply & Edit state
    val replyingMessage = MutableStateFlow<Message?>(null)
    val editingMessage = MutableStateFlow<Message?>(null)

    // Message search within active conversation
    val inChatSearchQuery = MutableStateFlow("")
    val isInChatSearchActive = MutableStateFlow(false)

    fun selectConversation(conversation: Conversation) {
        _activeConversation.value = conversation
        replyingMessage.value = null
        editingMessage.value = null
        inChatSearchQuery.value = ""
        isInChatSearchActive.value = false
        viewModelScope.launch {
            repository.markAsRead(conversation.id)
        }
    }

    fun closeConversation() {
        _activeConversation.value = null
        replyingMessage.value = null
        editingMessage.value = null
        isInChatSearchActive.value = false
    }

    fun selectChannel(community: Community, channel: Channel) {
        val conv = Conversation(
            id = "chan_" + channel.id,
            type = ConversationType.CHANNEL,
            name = "# " + channel.name,
            avatarColor = community.iconColor,
            description = channel.topic,
            lastMessageText = "Channel stream",
            lastMessageTime = "Now",
            lastMessageSender = "System",
            unreadCount = 0,
            isOnline = true,
            isPinned = false,
            isMuted = false,
            memberCount = community.memberCount,
            category = community.name
        )
        selectConversation(conv)
    }

    fun sendMessage(text: String) {
        val conv = _activeConversation.value ?: return
        if (text.isBlank()) return

        val replyTo = replyingMessage.value
        val editing = editingMessage.value

        viewModelScope.launch {
            if (editing != null) {
                repository.editMessage(editing.id, text.trim())
                editingMessage.value = null
            } else {
                repository.sendMessage(
                    conversationId = conv.id,
                    content = text.trim(),
                    type = MessageType.TEXT,
                    replyTo = replyTo
                )
                replyingMessage.value = null
            }
        }
    }

    fun sendVoiceNote() {
        val conv = _activeConversation.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = conv.id,
                content = "Voice note (0:14)",
                type = MessageType.AUDIO,
                audioDurationSeconds = 14,
                replyTo = replyingMessage.value
            )
            replyingMessage.value = null
        }
    }

    fun sendAttachment(name: String, size: String, type: MessageType) {
        val conv = _activeConversation.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = conv.id,
                content = "Attached: $name",
                type = type,
                fileName = name,
                fileSize = size,
                replyTo = replyingMessage.value
            )
            replyingMessage.value = null
        }
    }

    fun startReply(message: Message) {
        editingMessage.value = null
        replyingMessage.value = message
    }

    fun cancelReply() {
        replyingMessage.value = null
    }

    fun startEdit(message: Message) {
        replyingMessage.value = null
        editingMessage.value = message
    }

    fun cancelEdit() {
        editingMessage.value = null
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun togglePin(message: Message) {
        viewModelScope.launch {
            repository.togglePinMessage(message.id, message.isPinned)
        }
    }

    fun toggleReaction(message: Message, emoji: String) {
        viewModelScope.launch {
            repository.toggleReaction(message, emoji)
        }
    }

    fun switchUser(user: User) {
        repository.switchUser(user)
    }

    fun toggleVoiceChannel(channelId: String) {
        repository.toggleVoiceChannel(channelId)
    }

    fun createConversation(name: String, type: ConversationType, description: String = "") {
        viewModelScope.launch {
            val id = repository.createNewConversation(name, type, description)
            val newConv = Conversation(
                id = id,
                type = type,
                name = name,
                description = description,
                lastMessageText = "Chat started",
                lastMessageTime = "Just now",
                lastMessageSender = currentUser.value.displayName,
                unreadCount = 0,
                isOnline = true
            )
            selectConversation(newConv)
        }
    }
}

class PulseViewModelFactory(private val repository: PulseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PulseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PulseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
