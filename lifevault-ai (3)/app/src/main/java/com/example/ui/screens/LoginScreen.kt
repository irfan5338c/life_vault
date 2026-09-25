package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.viewmodel.LifeVaultViewModel

// Premium AI Vault Design Tokens
private val VaultCyan = Color(0xFF00E5FF)
private val VaultBlue = Color(0xFF2563EB)
private val VaultDeepNavy = Color(0xFF030712)
private val VaultSurfaceNavy = Color(0xFF0B1326)
private val VaultInputBg = Color(0xFF0D1830)
private val VaultBorderMuted = Color(0xFF1E293B)
private val VaultBorderActive = Color(0xFF00E5FF)
private val VaultTextMuted = Color(0xFF94A3B8)
private val VaultTextLight = Color(0xFFF1F5F9)

@Composable
fun LoginScreen(
    viewModel: LifeVaultViewModel,
    onLoginSuccess: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authErrorMessage by viewModel.authErrorMessage.collectAsState()
    val rememberMePref by viewModel.rememberMe.collectAsState()
    val savedEmail by viewModel.userEmail.collectAsState()

    var isSignUpMode by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    // Pre-fill with saved email if remember-me was enabled, or start empty for pristine feel
    var emailInput by remember { mutableStateOf(if (rememberMePref) savedEmail else "") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(rememberMePref) }

    // Password reset dialog state
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmailInput by remember { mutableStateOf("") }
    var resetNewPasswordInput by remember { mutableStateOf("") }
    var resetMessage by remember { mutableStateOf<String?>(null) }
    var resetSuccess by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    // Subtle entrance animation trigger
    var entranceVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        entranceVisible = true
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        VaultDeepNavy,
                        Color(0xFF050B1B),
                        Color(0xFF09142A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        val isWideScreen = maxWidth >= 760.dp

        // Ambient background particles & soft glowing gradient shapes (subtle, non-distracting)
        AmbientVaultBackground(modifier = Modifier.fillMaxSize())

        if (isWideScreen) {
            // Widescreen / Tablet Landscape: Left subtle dashboard preview, Right centered login card
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary dashboard preview on large screens (blurred, visually secondary)
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    ProductDashboardPreview(modifier = Modifier.fillMaxWidth())
                }

                // Primary Auth Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 460.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = entranceVisible,
                        enter = fadeIn(tween(600)) + slideInVertically(
                            initialOffsetY = { 40 },
                            animationSpec = tween(600, easing = FastOutSlowInEasing)
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            VaultBrandHeader()

                            MainAuthCard(
                                isSignUpMode = isSignUpMode,
                                nameInput = nameInput,
                                onNameChange = { nameInput = it; viewModel.clearAuthError() },
                                emailInput = emailInput,
                                onEmailChange = { emailInput = it; viewModel.clearAuthError() },
                                passwordInput = passwordInput,
                                onPasswordChange = { passwordInput = it; viewModel.clearAuthError() },
                                confirmPasswordInput = confirmPasswordInput,
                                onConfirmPasswordChange = { confirmPasswordInput = it; viewModel.clearAuthError() },
                                passwordVisible = passwordVisible,
                                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                                rememberMe = rememberMe,
                                onRememberMeChange = {
                                    rememberMe = it
                                    viewModel.setRememberMe(it)
                                },
                                isLoading = isAuthLoading,
                                errorMessage = authErrorMessage,
                                onSignInClick = {
                                    focusManager.clearFocus()
                                    viewModel.signIn(emailInput, passwordInput, rememberMe, onLoginSuccess)
                                },
                                onSignUpClick = {
                                    focusManager.clearFocus()
                                    if (passwordInput != confirmPasswordInput) {
                                        viewModel.setAuthError("Passwords do not match")
                                        return@MainAuthCard
                                    }
                                    viewModel.signUp(nameInput, emailInput, passwordInput, onLoginSuccess)
                                },
                                onGoogleSignInClick = {
                                    viewModel.signInWithGoogle(onSuccess = onLoginSuccess)
                                },
                                onToggleModeClick = {
                                    isSignUpMode = !isSignUpMode
                                    viewModel.clearAuthError()
                                },
                                onForgotPasswordClick = {
                                    resetEmailInput = emailInput
                                    resetNewPasswordInput = ""
                                    resetMessage = null
                                    resetSuccess = false
                                    showForgotPasswordDialog = true
                                }
                            )

                            // Privacy Message & Trust Indicators
                            PrivacyAndTrustSection(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        } else {
            // Mobile Portrait: Clean centered column with smooth scrolling
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = entranceVisible,
                    enter = fadeIn(tween(600)) + slideInVertically(
                        initialOffsetY = { 30 },
                        animationSpec = tween(600, easing = FastOutSlowInEasing)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 440.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Branding: Elegant Icon + "LifeVault AI" + Tagline
                        VaultBrandHeader()

                        // Main Login Card
                        MainAuthCard(
                            modifier = Modifier.fillMaxWidth(),
                            isSignUpMode = isSignUpMode,
                            nameInput = nameInput,
                            onNameChange = { nameInput = it; viewModel.clearAuthError() },
                            emailInput = emailInput,
                            onEmailChange = { emailInput = it; viewModel.clearAuthError() },
                            passwordInput = passwordInput,
                            onPasswordChange = { passwordInput = it; viewModel.clearAuthError() },
                            confirmPasswordInput = confirmPasswordInput,
                            onConfirmPasswordChange = { confirmPasswordInput = it; viewModel.clearAuthError() },
                            passwordVisible = passwordVisible,
                            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                            rememberMe = rememberMe,
                            onRememberMeChange = {
                                rememberMe = it
                                viewModel.setRememberMe(it)
                            },
                            isLoading = isAuthLoading,
                            errorMessage = authErrorMessage,
                            onSignInClick = {
                                focusManager.clearFocus()
                                viewModel.signIn(emailInput, passwordInput, rememberMe, onLoginSuccess)
                            },
                            onSignUpClick = {
                                focusManager.clearFocus()
                                if (passwordInput != confirmPasswordInput) {
                                    viewModel.setAuthError("Passwords do not match")
                                    return@MainAuthCard
                                }
                                viewModel.signUp(nameInput, emailInput, passwordInput, onLoginSuccess)
                            },
                            onGoogleSignInClick = {
                                viewModel.signInWithGoogle(onSuccess = onLoginSuccess)
                            },
                            onToggleModeClick = {
                                isSignUpMode = !isSignUpMode
                                viewModel.clearAuthError()
                            },
                            onForgotPasswordClick = {
                                resetEmailInput = emailInput
                                resetNewPasswordInput = ""
                                resetMessage = null
                                resetSuccess = false
                                showForgotPasswordDialog = true
                            }
                        )

                        // Privacy Message & Trust Indicators
                        PrivacyAndTrustSection(modifier = Modifier.fillMaxWidth())

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    // Password Reset Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            containerColor = VaultSurfaceNavy,
            shape = RoundedCornerShape(22.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(VaultCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = VaultCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Reset Vault Password",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (resetMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (resetSuccess) Color(0xFF065F46).copy(alpha = 0.35f)
                                    else Color(0xFFEF4444).copy(alpha = 0.2f)
                                )
                                .border(
                                    1.dp,
                                    if (resetSuccess) Color(0xFF10B981).copy(alpha = 0.5f)
                                    else Color(0xFFEF4444).copy(alpha = 0.4f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = resetMessage ?: "",
                                fontSize = 13.sp,
                                color = if (resetSuccess) Color(0xFF6EE7B7) else Color(0xFFFCA5A5)
                            )
                        }
                    } else {
                        Text(
                            text = "Enter your vault email address and choose a new password (min. 6 characters).",
                            fontSize = 13.sp,
                            color = VaultTextMuted
                        )
                    }

                    if (!resetSuccess) {
                        OutlinedTextField(
                            value = resetEmailInput,
                            onValueChange = { resetEmailInput = it },
                            placeholder = { Text("Email address", color = VaultTextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = VaultInputBg,
                                unfocusedContainerColor = VaultInputBg,
                                focusedBorderColor = VaultCyan,
                                unfocusedBorderColor = VaultBorderMuted,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = resetNewPasswordInput,
                            onValueChange = { resetNewPasswordInput = it },
                            placeholder = { Text("New password", color = VaultTextMuted) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = VaultInputBg,
                                unfocusedContainerColor = VaultInputBg,
                                focusedBorderColor = VaultCyan,
                                unfocusedBorderColor = VaultBorderMuted,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!resetSuccess) {
                            viewModel.resetPassword(resetEmailInput, resetNewPasswordInput) { success, msg ->
                                resetSuccess = success
                                resetMessage = msg
                            }
                        } else {
                            showForgotPasswordDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VaultCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (resetSuccess) "Done" else "Update Password",
                        color = Color(0xFF030712),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                if (!resetSuccess) {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("Cancel", color = VaultTextMuted)
                    }
                }
            }
        )
    }
}

/**
 * Ambient Vault Background:
 * Subtle deep gradient with soft animated orbs and gentle particles.
 */
@Composable
private fun AmbientVaultBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "vault_ambient")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Soft cyan orb in upper-left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    VaultCyan.copy(alpha = pulseAlpha),
                    Color.Transparent
                ),
                center = Offset(w * 0.25f, h * 0.2f),
                radius = w * 0.55f
            ),
            center = Offset(w * 0.25f, h * 0.2f),
            radius = w * 0.55f
        )

        // Soft royal blue orb in bottom-right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    VaultBlue.copy(alpha = pulseAlpha * 0.7f),
                    Color.Transparent
                ),
                center = Offset(w * 0.8f, h * 0.8f),
                radius = w * 0.6f
            ),
            center = Offset(w * 0.8f, h * 0.8f),
            radius = w * 0.6f
        )

        // Subtle decorative vault grid lines / accent particles
        val particles = listOf(
            Offset(w * 0.15f, h * 0.35f),
            Offset(w * 0.85f, h * 0.25f),
            Offset(w * 0.2f, h * 0.75f),
            Offset(w * 0.8f, h * 0.65f),
            Offset(w * 0.5f, h * 0.15f),
            Offset(w * 0.65f, h * 0.9f)
        )
        particles.forEachIndexed { index, pos ->
            val pAlpha = (pulseAlpha * (0.8f + (index % 3) * 0.4f)).coerceIn(0.04f, 0.25f)
            drawCircle(
                color = VaultCyan.copy(alpha = pAlpha),
                radius = 2.2f,
                center = pos
            )
        }
    }
}

/**
 * BRANDING:
 * Display: LifeVault AI
 * Tagline: “Your life. One intelligent, private vault.”
 * Small elegant AI/vault icon above the logo.
 */
@Composable
private fun VaultBrandHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Small elegant AI/vault icon above the logo
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            VaultCyan.copy(alpha = 0.2f),
                            Color(0xFF0F2040)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            VaultCyan.copy(alpha = 0.6f),
                            Color(0xFF1E293B)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = VaultCyan),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = "LifeVault Secure AI Shield",
                tint = VaultCyan,
                modifier = Modifier.size(24.dp)
            )
        }

        // Title: "LifeVault AI"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "LifeVault ",
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 0.4.sp
            )
            Text(
                text = "AI",
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                color = VaultCyan,
                letterSpacing = 0.4.sp
            )
        }

        // Tagline: “Your life. One intelligent, private vault.”
        Text(
            text = "Your life. One intelligent, private vault.",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = VaultTextMuted,
            textAlign = TextAlign.Center,
            letterSpacing = 0.2.sp
        )
    }
}

/**
 * MAIN LOGIN CARD:
 * - Centered
 * - Rounded corners
 * - Glassmorphism effect
 * - Subtle border
 * - Soft shadow/glow
 * - Responsive on desktop and mobile
 */
@Composable
private fun MainAuthCard(
    isSignUpMode: Boolean,
    nameInput: String,
    onNameChange: (String) -> Unit,
    emailInput: String,
    onEmailChange: (String) -> Unit,
    passwordInput: String,
    onPasswordChange: (String) -> Unit,
    confirmPasswordInput: String,
    onConfirmPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onToggleModeClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0x3300E5FF),
                ambientColor = Color(0x22000000)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(VaultSurfaceNavy.copy(alpha = 0.92f))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        VaultCyan.copy(alpha = 0.35f),
                        VaultBorderMuted.copy(alpha = 0.7f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Heading & Subtitle
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = if (isSignUpMode) "Create your vault 🔐" else "Welcome back 👋",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (isSignUpMode)
                        "Start your private, intelligent vault today."
                    else
                        "Sign in to access your personal intelligence.",
                    fontSize = 12.5.sp,
                    color = VaultTextMuted
                )
            }

            // Error Message Banner with clear feedback
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.16f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFFCA5A5),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Full Name Input (Sign Up Mode only)
            if (isSignUpMode) {
                VaultInputField(
                    value = nameInput,
                    onValueChange = onNameChange,
                    placeholder = "Full name",
                    leadingIcon = Icons.Outlined.Person,
                    enabled = !isLoading,
                    testTag = "input_fullname"
                )
            }

            // Input 1: Email Address
            VaultInputField(
                value = emailInput,
                onValueChange = onEmailChange,
                placeholder = "Email address",
                leadingIcon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                enabled = !isLoading,
                testTag = "input_email"
            )

            // Input 2: Password
            VaultInputField(
                value = passwordInput,
                onValueChange = onPasswordChange,
                placeholder = "Password",
                leadingIcon = Icons.Outlined.Lock,
                keyboardType = KeyboardType.Password,
                imeAction = if (isSignUpMode) ImeAction.Next else ImeAction.Done,
                onDone = {
                    if (!isSignUpMode && !isLoading) onSignInClick()
                },
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = onTogglePasswordVisibility,
                enabled = !isLoading,
                testTag = "input_password"
            )

            // Confirm Password (Sign Up Mode only)
            if (isSignUpMode) {
                VaultInputField(
                    value = confirmPasswordInput,
                    onValueChange = onConfirmPasswordChange,
                    placeholder = "Confirm password",
                    leadingIcon = Icons.Outlined.Lock,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    onDone = {
                        if (!isLoading) onSignUpClick()
                    },
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onTogglePasswordVisibility = onTogglePasswordVisibility,
                    enabled = !isLoading,
                    testTag = "input_confirm_password"
                )
            }

            // Remember Me & Forgot Password Row (Sign In mode only)
            if (!isSignUpMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Modern Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(enabled = !isLoading) { onRememberMeChange(!rememberMe) }
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (rememberMe) VaultCyan else VaultInputBg)
                                .border(
                                    width = 1.dp,
                                    color = if (rememberMe) VaultCyan else VaultBorderMuted,
                                    shape = RoundedCornerShape(5.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (rememberMe) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF030712),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Remember me",
                            fontSize = 12.5.sp,
                            color = VaultTextLight
                        )
                    }

                    // Forgot Password Link
                    Text(
                        text = "Forgot password?",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = VaultCyan,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(enabled = !isLoading) { onForgotPasswordClick() }
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                    )
                }
            }

            // Primary Button: "Sign in →" or "Create your vault →"
            Surface(
                onClick = {
                    if (isSignUpMode) onSignUpClick() else onSignInClick()
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_signin_primary")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = if (isLoading) {
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF0284C7).copy(alpha = 0.6f),
                                        Color(0xFF2563EB).copy(alpha = 0.6f)
                                    )
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        VaultCyan,
                                        Color(0xFF0284C7),
                                        VaultBlue
                                    )
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isSignUpMode) "Create your vault" else "Sign in",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }

            // OR Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(VaultBorderMuted)
                )
                Text(
                    text = "OR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(horizontal = 12.dp),
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(VaultBorderMuted)
                )
            }

            // Secondary Authentication: "Continue with Google"
            Surface(
                onClick = onGoogleSignInClick,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0B1426),
                border = BorderStroke(1.dp, Color(0xFF2A3A54)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("btn_google_signin")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Continue with Google",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            // Signup Toggle Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSignUpMode) "Already have a vault? " else "New to LifeVault? ",
                    fontSize = 12.5.sp,
                    color = VaultTextMuted
                )
                Text(
                    text = if (isSignUpMode) "Sign in" else "Create your vault",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = VaultCyan,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(enabled = !isLoading) { onToggleModeClick() }
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                )
            }
        }
    }
}

/**
 * High-quality input field with subtle focus glow and smooth keyboard navigation.
 */
@Composable
private fun VaultInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onDone: () -> Unit = {},
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: () -> Unit = {},
    enabled: Boolean = true,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        placeholder = { Text(placeholder, color = VaultTextMuted, fontSize = 13.5.sp) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = placeholder,
                tint = if (isFocused) VaultCyan else VaultTextMuted,
                modifier = Modifier.size(19.dp)
            )
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(
                    onClick = onTogglePasswordVisibility,
                    enabled = enabled,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = if (passwordVisible) VaultCyan else VaultTextMuted,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone() }
        ),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = VaultInputBg,
            unfocusedContainerColor = VaultInputBg,
            disabledContainerColor = VaultInputBg.copy(alpha = 0.5f),
            focusedBorderColor = VaultCyan,
            unfocusedBorderColor = VaultBorderMuted,
            disabledBorderColor = VaultBorderMuted.copy(alpha = 0.5f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = Color.White.copy(alpha = 0.5f)
        ),
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}

/**
 * PRIVACY MESSAGE & TRUST INDICATORS:
 * Below the authentication controls show:
 * 🔐 Your personal data stays private and under your control.
 *
 * Add three compact trust indicators:
 * Private by Design
 * AI Powered
 * Secure Sharing
 */
@Composable
private fun PrivacyAndTrustSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Privacy Notice Line
        Text(
            text = "🔐 Your personal data stays private and under your control.",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFCBD5E1),
            textAlign = TextAlign.Center
        )

        // Three Compact Trust Indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrustIndicatorPill(
                icon = Icons.Outlined.Shield,
                label = "Private by Design"
            )
            Spacer(modifier = Modifier.width(8.dp))
            TrustIndicatorPill(
                icon = Icons.Outlined.AutoAwesome,
                label = "AI Powered"
            )
            Spacer(modifier = Modifier.width(8.dp))
            TrustIndicatorPill(
                icon = Icons.Outlined.Fingerprint,
                label = "Secure Sharing"
            )
        }
    }
}

@Composable
private fun TrustIndicatorPill(
    icon: ImageVector,
    label: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0B1428))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = VaultCyan,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

/**
 * OPTIONAL PRODUCT PREVIEW (FOR WIDESCREEN / TABLETS):
 * Shows blurred, dimmed examples of the LifeVault dashboard:
 * Memories, Purchases, Belongings, Lost & Found, Before You Buy, Secure Files.
 * Kept visually secondary so the login card remains the focus.
 */
@Composable
private fun ProductDashboardPreview(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .alpha(0.65f)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "YOUR PERSONAL INTELLIGENCE VAULT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = VaultCyan,
            letterSpacing = 1.5.sp
        )

        // Grid of 6 dashboard previews
        val previewItems = listOf(
            Triple(Icons.Outlined.Image, "Memories", "Grand Canyon Family Trip 2025"),
            Triple(Icons.Outlined.ShoppingBag, "Purchases", "Sony WH-1000XM5 • ₹24,990"),
            Triple(Icons.Outlined.Search, "Belongings", "Spare Car Key • Desk Drawer #2"),
            Triple(Icons.Outlined.Fingerprint, "Lost & Found", "Gym Water Bottle • Locker 42"),
            Triple(Icons.Outlined.AutoAwesome, "Before You Buy", "S25 vs Pixel 9 Pro Comparison"),
            Triple(Icons.Outlined.Description, "Secure Files", "Passport & Health Insurance Card")
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (i in previewItems.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val item1 = previewItems[i]
                    DashboardPreviewCard(
                        icon = item1.first,
                        category = item1.second,
                        title = item1.third,
                        modifier = Modifier.weight(1f)
                    )
                    if (i + 1 < previewItems.size) {
                        val item2 = previewItems[i + 1]
                        DashboardPreviewCard(
                            icon = item2.first,
                            category = item2.second,
                            title = item2.third,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardPreviewCard(
    icon: ImageVector,
    category: String,
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF091224).copy(alpha = 0.7f))
            .border(1.dp, Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(VaultCyan.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = VaultCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = category,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VaultCyan
                )
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = VaultTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
