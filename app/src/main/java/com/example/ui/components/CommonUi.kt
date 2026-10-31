package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.ArishDarkGreen
import com.example.ui.theme.ArishEmeraldGreen
import com.example.ui.theme.MessageTickBlue
import com.example.ui.theme.MessageTickGray
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.UnreadBadgeGreen

@Composable
fun ArishLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    bubbleColor: Color = Color.White,
    letterColor: Color = ArishEmeraldGreen
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Bubble Path
        val bubblePath = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            cubicTo(w * 0.25f, h * 0.15f, w * 0.1f, h * 0.32f, w * 0.1f, h * 0.52f)
            cubicTo(w * 0.1f, h * 0.65f, w * 0.18f, h * 0.76f, w * 0.3f, h * 0.83f)
            lineTo(w * 0.22f, h * 0.95f)
            lineTo(w * 0.42f, h * 0.89f)
            cubicTo(w * 0.45f, h * 0.89f, w * 0.47f, h * 0.9f, w * 0.5f, h * 0.9f)
            cubicTo(w * 0.75f, h * 0.9f, w * 0.9f, h * 0.73f, w * 0.9f, h * 0.52f)
            cubicTo(w * 0.9f, h * 0.32f, w * 0.75f, h * 0.15f, w * 0.5f, h * 0.15f)
            close()
        }
        drawPath(bubblePath, color = bubbleColor)

        // Stylized Letter "A"
        val letterPath = Path().apply {
            moveTo(w * 0.5f, h * 0.30f)
            lineTo(w * 0.35f, h * 0.70f)
            lineTo(w * 0.42f, h * 0.70f)
            lineTo(w * 0.46f, h * 0.60f)
            lineTo(w * 0.54f, h * 0.60f)
            lineTo(w * 0.58f, h * 0.70f)
            lineTo(w * 0.65f, h * 0.70f)
            close()

            // Inner triangle cutout
            moveTo(w * 0.5f, h * 0.40f)
            lineTo(w * 0.53f, h * 0.54f)
            lineTo(w * 0.47f, h * 0.54f)
            close()
        }
        drawPath(letterPath, color = letterColor)
    }
}

@Composable
fun UserAvatar(
    name: String,
    photoUrl: String? = null,
    size: Dp = 48.dp,
    isOnline: Boolean = false,
    showOnlineBadge: Boolean = false,
    isGroup: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clickableModifier = if (onClick != null) modifier.clickable { onClick() } else modifier

    Box(modifier = clickableModifier.size(size)) {
        if (!photoUrl.isNullOrBlank() && photoUrl.startsWith("http")) {
            AsyncImage(
                model = photoUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
            )
        } else {
            val bg = if (isGroup) ArishDarkGreen else ArishEmeraldGreen
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                if (isGroup) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Group",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.55f)
                    )
                } else if (name.isNotBlank()) {
                    Text(
                        text = name.first().uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.45f).sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.55f)
                    )
                }
            }
        }

        if (showOnlineBadge && isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(OnlineGreen)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

@Composable
fun MessageStatusTick(
    status: String,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp
) {
    when (status) {
        "read" -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Read",
                tint = MessageTickBlue,
                modifier = modifier.size(size)
            )
        }
        "delivered" -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Delivered",
                tint = MessageTickGray,
                modifier = modifier.size(size)
            )
        }
        else -> { // "sent"
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Sent",
                tint = MessageTickGray,
                modifier = modifier.size(size)
            )
        }
    }
}

@Composable
fun UnreadBadge(
    count: Long,
    modifier: Modifier = Modifier
) {
    if (count > 0) {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(UnreadBadgeGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("unread_count_badge")
            )
        }
    }
}
