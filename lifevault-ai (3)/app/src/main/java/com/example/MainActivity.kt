package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.UniversalSearchDialog
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.LostFoundScreen
import com.example.ui.screens.MemoryVaultScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.PurchaseAnalyzerScreen
import com.example.ui.screens.VaultDropScreen
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoIndigo
import com.example.ui.theme.BentoIndigoContainer
import com.example.ui.theme.BentoIndigoLight
import com.example.ui.theme.LifeVaultTheme
import com.example.viewmodel.LifeVaultViewModel

enum class ScreenRoute(val label: String) {
    HOME("Command"),
    ASSISTANT("Assistant"),
    VAULT("Vault"),
    DROP("Drop"),
    DECISIONS("Decide"),
    DISCOVER("Find"),
    INSIGHTS("Insights"),
    PROFILE("Profile")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LifeVaultViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            LifeVaultTheme(darkTheme = isDarkMode) {
                LifeVaultApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LifeVaultApp(viewModel: LifeVaultViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isOnboardingDone by viewModel.isOnboardingCompleted.collectAsState()
    var currentRoute by remember { mutableStateOf(ScreenRoute.HOME) }
    var showUniversalSearch by remember { mutableStateOf(false) }

    if (!isLoggedIn) {
        LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = {
                // When logged in, screen updates reactively via StateFlow
            },
            modifier = Modifier.fillMaxSize()
        )
    } else if (!isOnboardingDone) {
        OnboardingScreen(
            onComplete = { name ->
                viewModel.completeOnboarding(name)
            },
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BentoBottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { currentRoute = it },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
            ) {
                AnimatedContent(
                    targetState = currentRoute,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_nav"
                ) { route ->
                    when (route) {
                        ScreenRoute.HOME -> HomeScreen(
                            viewModel = viewModel,
                            onNavigateToVault = { currentRoute = ScreenRoute.VAULT },
                            onNavigateToDecisions = { currentRoute = ScreenRoute.DECISIONS },
                            onNavigateToDiscover = { currentRoute = ScreenRoute.DISCOVER },
                            onNavigateToInsights = { currentRoute = ScreenRoute.INSIGHTS },
                            onNavigateToProfile = { currentRoute = ScreenRoute.PROFILE },
                            onOpenSearch = { showUniversalSearch = true },
                            onNavigateToVaultDrop = { currentRoute = ScreenRoute.DROP },
                            onNavigateToAssistant = { currentRoute = ScreenRoute.ASSISTANT }
                        )

                        ScreenRoute.ASSISTANT -> AssistantScreen(
                            viewModel = viewModel,
                            onNavigateToVault = { currentRoute = ScreenRoute.VAULT },
                            onNavigateToDecisions = { currentRoute = ScreenRoute.DECISIONS },
                            onNavigateToDiscover = { currentRoute = ScreenRoute.DISCOVER },
                            onNavigateToVaultDrop = { currentRoute = ScreenRoute.DROP },
                            onBack = { currentRoute = ScreenRoute.HOME }
                        )

                        ScreenRoute.VAULT -> MemoryVaultScreen(
                            viewModel = viewModel
                        )

                        ScreenRoute.DROP -> VaultDropScreen()

                        ScreenRoute.DECISIONS -> PurchaseAnalyzerScreen(
                            viewModel = viewModel
                        )

                        ScreenRoute.DISCOVER -> LostFoundScreen(
                            viewModel = viewModel
                        )

                        ScreenRoute.INSIGHTS -> InsightsScreen(
                            viewModel = viewModel
                        )

                        ScreenRoute.PROFILE -> ProfileScreen(
                            viewModel = viewModel
                        )
                    }
                }

                if (showUniversalSearch) {
                    UniversalSearchDialog(
                        viewModel = viewModel,
                        onDismiss = { showUniversalSearch = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun BentoBottomNavigationBar(
    currentRoute: ScreenRoute,
    onNavigate: (ScreenRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BentoNavItem(
                route = ScreenRoute.HOME,
                selected = currentRoute == ScreenRoute.HOME,
                iconSelected = Icons.Filled.AutoAwesome,
                iconUnselected = Icons.Outlined.AutoAwesome,
                onClick = { onNavigate(ScreenRoute.HOME) },
                activeColor = BentoIndigoLight,
                tag = "nav_home"
            )

            BentoNavItem(
                route = ScreenRoute.VAULT,
                selected = currentRoute == ScreenRoute.VAULT,
                iconSelected = Icons.Filled.Inventory2,
                iconUnselected = Icons.Outlined.Inventory2,
                onClick = { onNavigate(ScreenRoute.VAULT) },
                activeColor = BentoIndigoLight,
                tag = "nav_vault"
            )

            BentoNavItem(
                route = ScreenRoute.DROP,
                selected = currentRoute == ScreenRoute.DROP,
                iconSelected = Icons.Filled.Shield,
                iconUnselected = Icons.Outlined.Shield,
                onClick = { onNavigate(ScreenRoute.DROP) },
                activeColor = Color(0xFF00E5FF),
                tag = "nav_vaultdrop"
            )

            BentoNavItem(
                route = ScreenRoute.DECISIONS,
                selected = currentRoute == ScreenRoute.DECISIONS,
                iconSelected = Icons.Filled.ShoppingBag,
                iconUnselected = Icons.Outlined.ShoppingBag,
                onClick = { onNavigate(ScreenRoute.DECISIONS) },
                activeColor = BentoAmber,
                tag = "nav_decisions"
            )

            BentoNavItem(
                route = ScreenRoute.DISCOVER,
                selected = currentRoute == ScreenRoute.DISCOVER,
                iconSelected = Icons.Filled.LocationOn,
                iconUnselected = Icons.Outlined.LocationOn,
                onClick = { onNavigate(ScreenRoute.DISCOVER) },
                activeColor = BentoEmerald,
                tag = "nav_discover"
            )

            BentoNavItem(
                route = ScreenRoute.INSIGHTS,
                selected = currentRoute == ScreenRoute.INSIGHTS,
                iconSelected = Icons.Filled.Insights,
                iconUnselected = Icons.Outlined.Insights,
                onClick = { onNavigate(ScreenRoute.INSIGHTS) },
                activeColor = BentoIndigoLight,
                tag = "nav_insights"
            )

            BentoNavItem(
                route = ScreenRoute.PROFILE,
                selected = currentRoute == ScreenRoute.PROFILE,
                iconSelected = Icons.Filled.Person,
                iconUnselected = Icons.Outlined.Person,
                onClick = { onNavigate(ScreenRoute.PROFILE) },
                activeColor = BentoIndigoLight,
                tag = "nav_profile"
            )
        }
    }
}

@Composable
private fun BentoNavItem(
    route: ScreenRoute,
    selected: Boolean,
    iconSelected: ImageVector,
    iconUnselected: ImageVector,
    onClick: () -> Unit,
    activeColor: Color,
    tag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) activeColor.copy(alpha = 0.15f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = if (selected) iconSelected else iconUnselected,
                contentDescription = route.label,
                tint = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = route.label,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
