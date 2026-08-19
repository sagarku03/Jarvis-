package com.example

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.AppScreen
import com.example.ui.JarvisViewModel
import com.example.ui.components.BottomNav
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.RoutinesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.JarvisTheme

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            JarvisTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()

                // Permission Launchers
                val micLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    viewModel.refreshMetrics()
                    if (isGranted) {
                        viewModel.startListening()
                    }
                }

                val contactsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) {
                    viewModel.refreshMetrics()
                }

                val phoneLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) {
                    viewModel.refreshMetrics()
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        BottomNav(
                            currentScreen = currentScreen,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            AppScreen.CORE -> HomeScreen(
                                viewModel = viewModel,
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                            AppScreen.ROUTINES -> RoutinesScreen(
                                viewModel = viewModel
                            )
                            AppScreen.HISTORY -> HistoryScreen(
                                viewModel = viewModel
                            )
                            AppScreen.MEMORY -> MemoryScreen(
                                viewModel = viewModel
                            )
                            AppScreen.PERMISSIONS -> PermissionsScreen(
                                viewModel = viewModel,
                                onRequestMicrophone = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                                onRequestContacts = { contactsLauncher.launch(Manifest.permission.READ_CONTACTS) },
                                onRequestPhone = { phoneLauncher.launch(Manifest.permission.CALL_PHONE) }
                            )
                            AppScreen.SETTINGS -> SettingsScreen(
                                viewModel = viewModel
                            )
                            AppScreen.ONBOARDING -> HomeScreen(
                                viewModel = viewModel,
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("TRIGGER_VOICE_LISTENING", false) == true) {
            viewModel.navigateTo(AppScreen.CORE)
            viewModel.startListening()
        }
    }
}
