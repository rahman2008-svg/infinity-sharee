package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()

            MyApplicationTheme(darkTheme = viewModel.isDarkMode.value) {
                var currentScreen by remember { mutableStateOf("splash") }

                // State to pass category selections directly
                var initialTabForSend by remember { mutableStateOf("Photos") }

                // Handle the splash, onboarding, and permissions progression
                LaunchedEffect(viewModel.splashCompleted.value, viewModel.onboardingCompleted.value, viewModel.permissionsGranted.value) {
                    if (viewModel.splashCompleted.value) {
                        if (!viewModel.onboardingCompleted.value) {
                            currentScreen = "onboarding"
                        } else if (!viewModel.permissionsGranted.value) {
                            currentScreen = "permission"
                        } else {
                            if (currentScreen == "splash" || currentScreen == "onboarding" || currentScreen == "permission") {
                                currentScreen = "home"
                            }
                        }
                    }
                }

                // Global system Back Button Handling
                BackHandler(enabled = currentScreen != "home" && currentScreen != "splash") {
                    when (currentScreen) {
                        "send_pair", "scan_qr" -> currentScreen = "send_select"
                        "onboarding", "permission" -> { /* Stay on screen */ }
                        // Don't silently drop an in-progress transfer on back press.
                        "transferring" -> { /* Require explicit cancel from the screen itself */ }
                        else -> currentScreen = "home"
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        when (currentScreen) {
                            "splash" -> SplashScreen(viewModel)
                            "onboarding" -> OnboardingScreen(viewModel)
                            "permission" -> PermissionScreen(viewModel)
                            "home" -> HomeDashboardScreen(viewModel, onNavigate = { route ->
                                when (route) {
                                    "music" -> {
                                        initialTabForSend = "Music"
                                        currentScreen = "send_select"
                                    }
                                    "video" -> {
                                        initialTabForSend = "Videos"
                                        currentScreen = "send_select"
                                    }
                                    "photo" -> {
                                        initialTabForSend = "Photos"
                                        currentScreen = "send_select"
                                    }
                                    "document" -> {
                                        initialTabForSend = "Docs"
                                        currentScreen = "send_select"
                                    }
                                    "apps" -> {
                                        initialTabForSend = "Apps"
                                        currentScreen = "send_select"
                                    }
                                    else -> currentScreen = route
                                }
                            })
                            "send_select" -> {
                                // FIX: pass the selected category tab through to the screen.
                                SendFileSelectorScreen(
                                    viewModel,
                                    initialTab = initialTabForSend,
                                    onNavigate = { route ->
                                        currentScreen = route
                                    }
                                )
                            }
                            "send_pair" -> SendPairScreen(viewModel, onNavigate = { route ->
                                currentScreen = route
                            })
                            "scan_qr" -> ScanQrScreen(viewModel, onNavigate = { route ->
                                currentScreen = route
                            })
                            "receive_wait" -> ReceiveWaitScreen(viewModel, onNavigate = { route ->
                                currentScreen = route
                            })
                            "transferring" -> ActiveTransferScreen(viewModel, onNavigate = { route ->
                                currentScreen = route
                            })
                            "phone_clone" -> PhoneCloneScreen(viewModel, onNavigate = { route ->
                                currentScreen = route
                            })
                            "file_manager" -> FileManagerScreen(viewModel, onNavigate = { route ->
                                currentScreen = route
                            })
                            "media_player" -> MediaPlayerScreen(viewModel, onNavigate = { route ->
                                currentScreen = route
                            })
                            "history" -> TransferHistoryScreen(viewModel, onNavigate = { route ->
                                currentScreen = route
                            })
                            "settings" -> SettingsScreen(viewModel, onNavigate = { route ->
                                currentScreen = route
                            })
                            "web_share" -> WebShareScreen(viewModel, onNavigate = { route ->
                                currentScreen = route
                            })
                            "dup_finder" -> DuplicateFinderScreen(viewModel, onNavigate = { route ->
                                currentScreen = route
                            })
                        }
                    }
                }
            }
        }
    }
}
