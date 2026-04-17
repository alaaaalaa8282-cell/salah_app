package com.mohamedabdelazeim.salah

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mohamedabdelazeim.salah.ui.screens.AdhkarScreen
import com.mohamedabdelazeim.salah.ui.screens.HomeScreen
import com.mohamedabdelazeim.salah.ui.screens.PrayerSettingsScreen
import com.mohamedabdelazeim.salah.ui.theme.SalahTheme

class MainActivity : ComponentActivity() {

    private val notifLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            SalahTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val nav = rememberNavController()
                    NavHost(nav, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                onNavigateToAdhkar = { nav.navigate("adhkar") },
                                onNavigateToSettings = { nav.navigate("settings") }
                            )
                        }
                        composable("adhkar") { AdhkarScreen(onBack = { nav.popBackStack() }) }
                        composable("settings") { PrayerSettingsScreen(onBack = { nav.popBackStack() }) }
                    }
                }
            }
        }
    }
}
