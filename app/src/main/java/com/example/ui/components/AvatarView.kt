package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserStatus
import com.example.ui.theme.StatusAway
import com.example.ui.theme.StatusOffline
import com.example.ui.theme.StatusOnline

@Composable
fun AvatarView(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    avatarColor: Long = 0xFF3B82F6,
    isOnline: Boolean? = null,
    status: UserStatus? = null,
    showBorder: Boolean = false
) {
    val initials = name.trim().split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
        .ifEmpty { "?" }

    val baseColor = Color(avatarColor)
    val gradientBrush = Brush.linearGradient(
        colors = listOf(baseColor, baseColor.copy(alpha = 0.75f))
    )

    Box(
        modifier = modifier
            .size(size)
            .testTag("avatar_$name"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(gradientBrush)
                .then(
                    if (showBorder) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontSize = (size.value * 0.38f).sp,
                fontWeight = FontWeight.Bold
            )
        }

        val showStatus = isOnline != null || status != null
        if (showStatus) {
            val statusColor = when {
                status == UserStatus.ONLINE || isOnline == true -> StatusOnline
                status == UserStatus.AWAY -> StatusAway
                else -> StatusOffline
            }

            val indicatorSize = (size.value * 0.28f).coerceAtLeast(10f).dp
            val borderSize = (size.value * 0.05f).coerceAtLeast(1.5f).dp

            Box(
                modifier = Modifier
                    .size(indicatorSize)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(statusColor)
                    .border(borderSize, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}
