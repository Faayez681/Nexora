package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Conversation
import com.example.ui.components.ChatTopBar
import com.example.ui.components.MessageBubble
import com.example.ui.components.MessageComposer
import com.example.ui.components.PinnedMessageBanner
import com.example.ui.components.VoiceCallBanner
import com.example.viewmodel.PulseViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatDetailScreen(
    conversation: Conversation,
    viewModel: PulseViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val typingUsers by viewModel.activeTypingUsers.collectAsStateWithLifecycle()
    val replyingMessage by viewModel.replyingMessage.collectAsStateWithLifecycle()
    val editingMessage by viewModel.editingMessage.collectAsStateWithLifecycle()
    val activeVoiceChannel by viewModel.activeVoiceChannel.collectAsStateWithLifecycle()
    val inChatSearchQuery by viewModel.inChatSearchQuery.collectAsStateWithLifecycle()
    val isInChatSearchActive by viewModel.isInChatSearchActive.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll to latest message when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val pinnedMessage = remember(messages) {
        messages.findLast { it.isPinned }
    }

    val filteredMessages = remember(messages, inChatSearchQuery) {
        if (inChatSearchQuery.isBlank()) messages
        else messages.filter { it.content.contains(inChatSearchQuery, ignoreCase = true) }
    }

    val showScrollToBottom by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex < messages.size - 4 && messages.size > 5
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                ChatTopBar(
                    conversation = conversation,
                    typingUsers = typingUsers,
                    onBackClick = onBack,
                    onVoiceCallClick = { viewModel.toggleVoiceChannel(conversation.id) },
                    isVoiceConnected = activeVoiceChannel == conversation.id,
                    searchQuery = inChatSearchQuery,
                    onSearchQueryChange = { viewModel.inChatSearchQuery.value = it },
                    isSearchActive = isInChatSearchActive,
                    onToggleSearch = { viewModel.isInChatSearchActive.value = it }
                )

                VoiceCallBanner(
                    activeVoiceChannel = activeVoiceChannel,
                    onDisconnect = { viewModel.toggleVoiceChannel(conversation.id) }
                )

                PinnedMessageBanner(
                    pinnedMessage = pinnedMessage,
                    onUnpin = { viewModel.togglePin(it) },
                    onClick = {
                        val index = messages.indexOfFirst { it.id == pinnedMessage?.id }
                        if (index != -1) {
                            scope.launch { listState.animateScrollToItem(index) }
                        }
                    }
                )
            }
        },
        bottomBar = {
            MessageComposer(
                replyingMessage = replyingMessage,
                editingMessage = editingMessage,
                onCancelReply = { viewModel.cancelReply() },
                onCancelEdit = { viewModel.cancelEdit() },
                onSendMessage = { viewModel.sendMessage(it) },
                onSendVoiceNote = { viewModel.sendVoiceNote() },
                onSendAttachment = { name, size, type ->
                    viewModel.sendAttachment(name, size, type)
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Encryption notice badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "End-to-End Encrypted with Signal Protocol",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Messages LazyColumn
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(filteredMessages, key = { it.id }) { msg ->
                        val isMe = msg.senderId == currentUser.id
                        MessageBubble(
                            message = msg,
                            isCurrentUser = isMe,
                            onReply = { viewModel.startReply(it) },
                            onEdit = { viewModel.startEdit(it) },
                            onDelete = { viewModel.deleteMessage(it) },
                            onTogglePin = { viewModel.togglePin(it) },
                            onReaction = { m, emoji -> viewModel.toggleReaction(m, emoji) },
                            searchHighlight = inChatSearchQuery
                        )
                    }

                    // Real-time typing bubble in list
                    if (typingUsers.isNotEmpty()) {
                        item {
                            TypingIndicatorBubble(typingUsers = typingUsers)
                        }
                    }
                }
            }

            // Scroll to bottom FAB
            AnimatedVisibility(
                visible = showScrollToBottom,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    },
                    modifier = Modifier.size(40.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Scroll to bottom",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TypingIndicatorBubble(typingUsers: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "${typingUsers.joinToString(", ")} is typing",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                // 3 pulsating dots
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}
