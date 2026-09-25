package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoAmberContainer
import com.example.ui.theme.BentoBackgroundDark
import com.example.ui.theme.BentoCardDark
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoEmeraldContainer
import com.example.ui.theme.BentoIndigo
import com.example.ui.theme.BentoIndigoContainer
import com.example.ui.theme.BentoIndigoLight

/**
 * Standard Bento Grid Box Container with rounded corners and subtle border
 */
@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    cornerRadius: Dp = 24.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(18.dp)
    ) {
        content()
    }
}

/**
 * Pill-style category or status tag with subtle container tint
 */
@Composable
fun BentoBadge(
    text: String,
    color: Color = BentoIndigoLight,
    containerColor: Color = BentoIndigoContainer,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(containerColor)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

/**
 * Micro Bar visualizer for the Insights bento card
 */
@Composable
fun BentoMicroBars(
    modifier: Modifier = Modifier,
    accentColor: Color = BentoIndigoLight
) {
    val barHeights = listOf(12.dp, 20.dp, 16.dp, 28.dp, 32.dp)
    val opacities = listOf(0.2f, 0.35f, 0.25f, 0.85f, 1.0f)

    Row(
        modifier = modifier.height(36.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        barHeights.forEachIndexed { index, height ->
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(height)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = opacities[index]))
            )
        }
    }
}

/**
 * Intelligent AI Thinking Pulse Indicator
 */
@Composable
fun AIThinkingIndicator(
    modifier: Modifier = Modifier,
    text: String = "LifeVault AI is analyzing..."
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BentoIndigoContainer)
            .border(1.dp, BentoIndigoLight.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = null,
            tint = BentoIndigoLight.copy(alpha = alpha),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            color = BentoIndigoLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Floating Animated "Saved to your memory ✓" banner
 */
@Composable
fun SaveSuccessToast(
    visible: Boolean,
    message: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(100.dp),
            color = BentoEmeraldContainer,
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoEmerald.copy(alpha = 0.6f)),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = BentoEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = message.ifBlank { "Saved to your memory ✓" },
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
