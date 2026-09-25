package com.example.ui.components

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoIndigo
import com.example.ui.theme.BentoIndigoContainer
import com.example.ui.theme.BentoIndigoLight
import com.example.ui.theme.BentoRose

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceInputDialog(
    onDismiss: () -> Unit,
    onSpeechResult: (String) -> Unit
) {
    val context = LocalContext.current
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf(context.getString(R.string.voice_speak_prompt)) }
    var soundLevel by remember { mutableFloatStateOf(0f) }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    // Launcher for system speech intent (fallback or direct option)
    val systemSpeechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                recognizedText = spoken
                statusMessage = "Voice recognized!"
                onSpeechResult(spoken)
                onDismiss()
            }
        }
    }

    fun launchSystemSpeechIntent() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, context.getString(R.string.voice_speak_prompt))
        }
        try {
            systemSpeechLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(R.string.voice_speech_not_available),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (!isGranted) {
            statusMessage = context.getString(R.string.voice_permission_required)
        }
    }

    // Function to start speech recognition listener
    fun startListening() {
        if (!hasAudioPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            // SpeechRecognizer service not available; try system activity intent
            launchSystemSpeechIntent()
            return
        }

        try {
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                    statusMessage = context.getString(R.string.voice_listening)
                }

                override fun onBeginningOfSpeech() {
                    statusMessage = "Listening to your speech…"
                }

                override fun onRmsChanged(rmsdB: Float) {
                    soundLevel = (rmsdB.coerceIn(0f, 10f) / 10f)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    isListening = false
                    statusMessage = "Processing your voice command…"
                }

                override fun onError(error: Int) {
                    isListening = false
                    val errorMsg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Tap to try again."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timed out. Speak closer to the mic."
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Check microphone."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network connection issue for speech."
                        else -> "Could not capture voice (code $error). You can tap below or retry."
                    }
                    statusMessage = errorMsg
                }

                override fun onResults(results: Bundle?) {
                    isListening = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()
                    if (!text.isNullOrBlank()) {
                        recognizedText = text
                        statusMessage = "Command captured!"
                        onSpeechResult(text)
                        onDismiss()
                    } else {
                        statusMessage = "No speech detected. Please try again."
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!partial.isNullOrBlank()) {
                        recognizedText = partial
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            recognizer.startListening(intent)
            isListening = true
            statusMessage = context.getString(R.string.voice_listening)
        } catch (e: Exception) {
            isListening = false
            statusMessage = "Microphone error: ${e.localizedMessage ?: "Unknown error"}"
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        isListening = false
    }

    // Auto-request or start when dialog opens
    LaunchedEffect(hasAudioPermission) {
        if (hasAudioPermission) {
            startListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {}
        }
    }

    // Animated pulse for mic
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Dialog(
        onDismissRequest = {
            stopListening()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .testTag("voice_input_dialog"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                BentoIndigoLight.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BentoIndigoContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = BentoIndigoLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = stringResource(R.string.voice_input_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = {
                            stopListening()
                            onDismiss()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Microphone visualizer button
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer glow ring
                    if (isListening) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            BentoIndigoLight.copy(alpha = 0.35f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    // Main interactive mic circle
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                if (isListening) {
                                    Brush.linearGradient(listOf(BentoIndigoLight, BentoIndigo))
                                } else {
                                    Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                                }
                            )
                            .border(
                                2.dp,
                                if (isListening) BentoEmerald else MaterialTheme.colorScheme.outlineVariant,
                                CircleShape
                            )
                            .clickable {
                                if (isListening) {
                                    stopListening()
                                } else {
                                    startListening()
                                }
                            }
                            .testTag("voice_dialog_mic_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Filled.Mic else Icons.Filled.MicOff,
                            contentDescription = if (isListening) "Mute / Stop" else "Start Mic",
                            tint = if (isListening) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Waveform bars
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.height(28.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(7) { index ->
                        val barHeight = if (isListening) {
                            val wave = ((index % 3 + 1) * 8f) + (soundLevel * 14f)
                            wave.coerceIn(6f, 26f).dp
                        } else {
                            5.dp
                        }
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (isListening) BentoEmerald else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status message
                Text(
                    text = statusMessage,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isListening) BentoIndigoLight else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Live recognized text preview if any
                if (recognizedText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .border(1.dp, BentoEmerald.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Transcribed:",
                                fontSize = 11.sp,
                                color = BentoEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "\"$recognizedText\"",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

                // Permission grant button if not granted
                if (!hasAudioPermission) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoIndigoLight),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("grant_permission_btn")
                    ) {
                        Text(
                            text = stringResource(R.string.voice_grant_permission),
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }

                // Sample Voice Prompts (Useful on emulators or quick test)
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Or tap a sample voice command to test:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                val sampleCommands = listOf(
                    "Where is my passport?",
                    "I kept keys in blue backpack",
                    "Should I buy headphones for $80?",
                    "What changed this month?"
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sampleCommands.forEach { sample ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, BentoIndigoLight.copy(alpha = 0.35f), RoundedCornerShape(100.dp))
                                .clickable {
                                    stopListening()
                                    recognizedText = sample
                                    onSpeechResult(sample)
                                    onDismiss()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🎙 $sample",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            stopListening()
                            launchSystemSpeechIntent()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RecordVoiceOver,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = BentoIndigoLight
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "System Mic",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (recognizedText.isNotBlank()) {
                        Button(
                            onClick = {
                                stopListening()
                                onSpeechResult(recognizedText)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoEmerald)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Send",
                                fontSize = 12.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
