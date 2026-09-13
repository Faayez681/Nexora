package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Conversation
import com.example.model.ConversationType
import com.example.ui.theme.StatusOnline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    conversation: Conversation,
    typingUsers: List<String>,
    onBackClick: () -> Unit,
    onVoiceCallClick: () -> Unit,
    isVoiceConnected: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onToggleSearch: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showOptions by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        AvatarView(
                            name = conversation.name,
                            size = 38.dp,
                            avatarColor = conversation.avatarColor,
                            isOnline = conversation.isOnline
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = conversation.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )

                            // Status or Typing indicator
                            val subtitleText = when {
                                typingUsers.isNotEmpty() -> "${typingUsers.joinToString(", ")} is typing..."
                                conversation.type == ConversationType.GROUP -> "${conversation.memberCount} members • 5 online"
                                conversation.type == ConversationType.CHANNEL -> conversation.description
                                conversation.isOnline -> "Online"
                                else -> "Last seen recently"
                            }

                            val isTyping = typingUsers.isNotEmpty()
                            Text(
                                text = subtitleText,
                                fontSize = 12.sp,
                                color = when {
                                    isTyping -> MaterialTheme.colorScheme.primary
                                    conversation.isOnline -> StatusOnline
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontWeight = if (isTyping) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Search toggle button
                    IconButton(
                        onClick = { onToggleSearch(!isSearchActive) },
                        modifier = Modifier.testTag("chat_search_toggle")
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search in chat"
                        )
                    }

                    // Voice channel / Call button
                    IconButton(
                        onClick = onVoiceCallClick,
                        modifier = Modifier.testTag("chat_voice_call")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Voice Call",
                            tint = if (isVoiceConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // More options menu
                    IconButton(
                        onClick = { showOptions = true },
                        modifier = Modifier.testTag("chat_more_options")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options"
                        )
                    }

                    DropdownMenu(
                        expanded = showOptions,
                        onDismissRequest = { showOptions = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Search messages") },
                            onClick = {
                                onToggleSearch(true)
                                showOptions = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (conversation.isMuted) "Unmute notifications" else "Mute notifications") },
                            onClick = { showOptions = false }
                        )
                        DropdownMenuItem(
                            text = { Text("View encryption keys") },
                            onClick = { showOptions = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear history", color = MaterialTheme.colorScheme.error) },
                            onClick = { showOptions = false }
                        )
                    }
                }
            )

            // In-Chat Search Bar
            AnimatedVisibility(visible = isSearchActive) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search in conversation...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("in_chat_search_field"),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}
