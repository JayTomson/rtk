package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.ReadTrackerTheme
import com.example.ui.theme.AccentOrange
import com.example.viewmodel.ReadTrackerViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge for premium transparent navigation bar at the bottom
        enableEdgeToEdge()

        setContent {
            val viewModel: ReadTrackerViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val colorAccentHex by viewModel.colorAccent.collectAsState()
            val customPrimary = remember(colorAccentHex) { parseHexColor(colorAccentHex, AccentOrange) }
            val toastMessage by viewModel.toastMessage.collectAsState()
            val disableAnimations by viewModel.disableAnimations.collectAsState()

            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            // Real-time custom designed Snackbars / Toast notifications
            LaunchedEffect(toastMessage) {
                toastMessage?.let { (msg, isSuccess) ->
                    // Dismiss previous active snackbar first
                    snackbarHostState.currentSnackbarData?.dismiss()
                    // Clear state in viewModel immediately to avoid any repeated triggers
                    viewModel.clearToast()
                    // Display current snackbar with Short duration
                    snackbarHostState.showSnackbar(
                        message = msg,
                        actionLabel = if (isSuccess) "success" else "error",
                        duration = SnackbarDuration.Short
                    )
                }
            }

            ReadTrackerTheme(themeMode = themeMode, primaryColor = customPrimary) {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route ?: "library"

                // State representing the "Поделиться" BottomSheet
                var showShareBottomSheet by remember { mutableStateOf(false) }

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            val isSuccess = data.visuals.actionLabel == "success"
                            val bgAccentColor = if (isSuccess) customPrimary else Color(0xFFF87171)
                            Snackbar(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 20.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                containerColor = bgAccentColor,
                                contentColor = Color.White
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSuccess) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = data.visuals.message,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "library",
                        modifier = Modifier.fillMaxSize(),
                        enterTransition = { if (disableAnimations) EnterTransition.None else fadeIn(animationSpec = tween(220)) },
                        exitTransition = { if (disableAnimations) ExitTransition.None else fadeOut(animationSpec = tween(220)) },
                        popEnterTransition = { if (disableAnimations) EnterTransition.None else fadeIn(animationSpec = tween(220)) },
                        popExitTransition = { if (disableAnimations) ExitTransition.None else fadeOut(animationSpec = tween(220)) }
                    ) {
                        composable("library") {
                            LibraryScreen(
                                viewModel = viewModel,
                                onNavigateToAdd = { navController.navigate("add_book") },
                                onNavigateToEdit = { bookId -> navController.navigate("edit_book/$bookId") },
                                onNavigateToAnalytics = { navController.navigate("analytics") },
                                onOpenShareSheet = { showShareBottomSheet = true }
                            )
                        }

                        composable("analytics") {
                            AnalyticsScreen(
                                viewModel = viewModel,
                                onNavigateToSettings = { navController.navigate("settings") },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateToColorSettings = { navController.navigate("color_settings") },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("color_settings") {
                            ColorSettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("add_book") {
                            AddBookScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "edit_book/{bookId}",
                            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                            EditBookScreen(
                                bookId = bookId,
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("share_analytics") {
                            ShareAnalyticsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("share_list") {
                            ShareListScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }

                    // BottomSheet Share panel dialog sheet
                    if (showShareBottomSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showShareBottomSheet = false },
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                            containerColor = MaterialTheme.colorScheme.surface,
                            dragHandle = {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 12.dp)
                                        .size(36.dp, 4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color.Gray.copy(alpha = 0.3f))
                                )
                            }
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(start = 16.dp, end = 16.dp, bottom = 44.dp)
                            ) {
                                Text(
                                    text = "Поделиться",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                ShareOptionTile(
                                    title = "Аналитика",
                                    subtitle = "Карточка со статистикой",
                                    icon = Icons.Rounded.Analytics,
                                    color = customPrimary,
                                    onClick = {
                                        showShareBottomSheet = false
                                        navController.navigate("share_analytics")
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                ShareOptionTile(
                                    title = "Список тайтлов",
                                    subtitle = "Все тайтлы в одной карточке",
                                    icon = Icons.Rounded.FormatListBulleted,
                                    color = Color(0xFF34D399),
                                    onClick = {
                                        showShareBottomSheet = false
                                        navController.navigate("share_list")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
