package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.PulseViewModel

enum class MainNavTab {
    CHATS, COMMUNITIES, CONTACTS, SETTINGS
}

@Composable
fun MainScreen(
    viewModel: PulseViewModel,
    modifier: Modifier = Modifier
) {
    val activeConversation by viewModel.activeConversation.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(MainNavTab.CHATS) }

    val totalUnread = remember(conversations) {
        conversations.sumOf { it.unreadCount }
    }

    // Intercept back button if active conversation is open
    BackHandler(enabled = activeConversation != null) {
        viewModel.closeConversation()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (activeConversation == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = selectedTab == MainNavTab.CHATS,
                        onClick = { selectedTab = MainNavTab.CHATS },
                        icon = {
                            if (totalUnread > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge { Text("$totalUnread") }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selectedTab == MainNavTab.CHATS) Icons.Filled.Chat else Icons.Outlined.Chat,
                                        contentDescription = "Chats"
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (selectedTab == MainNavTab.CHATS) Icons.Filled.Chat else Icons.Outlined.Chat,
                                    contentDescription = "Chats"
                                )
                            }
                        },
                        label = { Text("Chats") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == MainNavTab.COMMUNITIES,
                        onClick = { selectedTab = MainNavTab.COMMUNITIES },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == MainNavTab.COMMUNITIES) Icons.Filled.Forum else Icons.Outlined.Forum,
                                contentDescription = "Communities"
                            )
                        },
                        label = { Text("Servers") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == MainNavTab.CONTACTS,
                        onClick = { selectedTab = MainNavTab.CONTACTS },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == MainNavTab.CONTACTS) Icons.Filled.People else Icons.Outlined.People,
                                contentDescription = "Contacts"
                            )
                        },
                        label = { Text("Contacts") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == MainNavTab.SETTINGS,
                        onClick = { selectedTab = MainNavTab.SETTINGS },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == MainNavTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val conv = activeConversation
            AnimatedContent(
                targetState = conv,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ConversationTransition"
            ) { targetConv ->
                if (targetConv != null) {
                    ChatDetailScreen(
                        conversation = targetConv,
                        viewModel = viewModel,
                        onBack = { viewModel.closeConversation() }
                    )
                } else {
                    when (selectedTab) {
                        MainNavTab.CHATS -> {
                            ChatListScreen(
                                viewModel = viewModel,
                                onOpenChat = { viewModel.selectConversation(it) }
                            )
                        }
                        MainNavTab.COMMUNITIES -> {
                            CommunityScreen(
                                viewModel = viewModel,
                                onOpenChannelChat = { comm, chan ->
                                    viewModel.selectChannel(comm, chan)
                                }
                            )
                        }
                        MainNavTab.CONTACTS -> {
                            ContactsScreen(
                                viewModel = viewModel
                            )
                        }
                        MainNavTab.SETTINGS -> {
                            SettingsScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
