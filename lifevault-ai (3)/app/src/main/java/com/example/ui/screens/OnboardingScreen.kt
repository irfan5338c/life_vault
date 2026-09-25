package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BentoBadge
import com.example.ui.components.BentoCard
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoAmberContainer
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoEmeraldContainer
import com.example.ui.theme.BentoIndigo
import com.example.ui.theme.BentoIndigoContainer
import com.example.ui.theme.BentoIndigoLight
import com.example.ui.theme.BentoPurple
import com.example.ui.theme.BentoPurpleContainer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onComplete: (name: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) }
    var enteredName by remember { mutableStateOf("Alex") }
    val selectedGoals = remember {
        mutableStateListOf(
            "Remembering things",
            "Managing purchases",
            "Personal organization"
        )
    }

    val totalSteps = 5

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Step Progress Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..totalSteps) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(4.dp)
                        .width(if (i == step) 28.dp else 12.dp)
                        .clip(CircleShape)
                        .background(
                            if (i <= step) BentoIndigoLight else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        // Main Step Content with animated transitions
        AnimatedContent(
            targetState = step,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            label = "onboarding_step"
        ) { currentStep ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (currentStep) {
                    1 -> {
                        // Screen 1: "Your life, remembered."
                        BentoBadge(text = "LifeVault AI", color = BentoIndigoLight)
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Your life,\nremembered.",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 38.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "LifeVault helps you remember what matters and make smarter everyday decisions.",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        BentoCard(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = BentoIndigoLight,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "“Remember. Decide. Find. Understand.”",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }

                    2 -> {
                        // Screen 2: 4 Simple Concepts
                        Text(
                            text = "Four Pillars of Intelligence",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Everything connects seamlessly.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ConceptRow(emoji = "🧠", title = "Remember", subtitle = "Save belongings, dates, ideas naturally", color = BentoIndigoLight, container = BentoIndigoContainer)
                            ConceptRow(emoji = "🛒", title = "Decide", subtitle = "Smart 'Before You Buy' analysis (Buy/Wait/Skip)", color = BentoAmber, container = BentoAmberContainer)
                            ConceptRow(emoji = "📍", title = "Find", subtitle = "Community Lost & Found with AI similarity match", color = BentoEmerald, container = BentoEmeraldContainer)
                            ConceptRow(emoji = "📊", title = "Understand", subtitle = "Meaningful life & spending change insights", color = BentoPurple, container = BentoPurpleContainer)
                        }
                    }

                    3 -> {
                        // Screen 3: "What would you like LifeVault to help you with?"
                        Text(
                            text = "What would you like LifeVault to help you with?",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Select all that apply to personalize your vault.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        val options = listOf(
                            "Remembering things",
                            "Managing purchases",
                            "Finding lost items",
                            "Understanding spending/life patterns",
                            "Studying",
                            "Personal organization"
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            options.forEach { option ->
                                val isSelected = selectedGoals.contains(option)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) BentoIndigoContainer else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(
                                            1.dp,
                                            if (isSelected) BentoIndigoLight else MaterialTheme.colorScheme.outlineVariant,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            if (isSelected) selectedGoals.remove(option)
                                            else selectedGoals.add(option)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = BentoIndigoLight,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = option,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) BentoIndigoLight else MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }
                    }

                    4 -> {
                        // Screen 4: Privacy guarantee
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(BentoEmeraldContainer)
                                .border(1.dp, BentoEmerald.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = BentoEmerald,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "You control what LifeVault remembers.",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Never secretly record conversations or collect sensitive information without explicit permission. Private-by-default, offline-ready local storage.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        BentoCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                PrivacyBullet("No background telemetry or microphone eavesdropping")
                                PrivacyBullet("Full export and 1-tap data deletion controls")
                                PrivacyBullet("Authorized queries only access authorized vault data")
                            }
                        }
                    }

                    5 -> {
                        // Screen 5: Create Account / Profile
                        Text(
                            text = "Create Your Vault",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "How would you like LifeVault to address you?",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = enteredName,
                            onValueChange = { enteredName = it },
                            label = { Text("Your Name") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoIndigoLight,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_input")
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Preloaded with realistic demo data so you can test all features immediately. You can clear demo data anytime from Profile.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Bottom Navigation Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 1) {
                Text(
                    text = "Back",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { step-- }
                        .padding(8.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }

            Button(
                onClick = {
                    if (step < totalSteps) {
                        step++
                    } else {
                        onComplete(enteredName)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BentoIndigo,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("onboarding_next_button")
            ) {
                Text(
                    text = if (step == totalSteps) "Open LifeVault" else "Continue",
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ConceptRow(
    emoji: String,
    title: String,
    subtitle: String,
    color: Color,
    container: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(container),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 20.sp)
        }
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PrivacyBullet(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = BentoEmerald,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
