package com.mohamedabdelazeim.salah.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedabdelazeim.salah.service.AzanService
import com.mohamedabdelazeim.salah.ui.theme.SalahTheme
import kotlinx.coroutines.delay

class AzanActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prayerName = intent.getStringExtra("prayer_name") ?: ""

        setContent {
            SalahTheme {
                AzanScreen(
                    prayerName = prayerName,
                    onStop = { stopAndClose() }
                )
            }
        }
    }

    private fun stopAndClose() {
        val stopIntent = Intent(this, AzanService::class.java).apply {
            action = AzanService.ACTION_STOP
        }
        startService(stopIntent)
        finish()
    }

    override fun onBackPressed() {
        stopAndClose()
    }
}

@Composable
fun AzanScreen(prayerName: String, onStop: () -> Unit) {
    val gold = Color(0xFFFFD700)
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D1B0F), Color(0xFF1B5E20), Color(0xFF0D1B0F))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + scaleIn()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Text("🕌", fontSize = 80.sp)

                Text(
                    "حان وقت",
                    color = Color.White,
                    fontSize = 24.sp
                )

                Text(
                    prayerName,
                    color = gold,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    "اللهم صلِّ وسلِّم على نبينا محمد",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onStop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = gold),
                    shape = CircleShape
                ) {
                    Text(
                        "إيقاف الأذان",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
