package com.mohamedabdelazeim.salah.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.mohamedabdelazeim.salah.R
import com.mohamedabdelazeim.salah.data.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigateToAdhkar: () -> Unit, onNavigateToSettings: () -> Unit) {
    val ctx = LocalContext.current
    val gold = Color(0xFFFFD700)
    val scope = rememberCoroutineScope()

    var prayerTimes by remember { mutableStateOf<com.mohamedabdelazeim.salah.data.PrayerTimes?>(null) }
    var isLoadingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            scope.launch { getLocation(ctx) { lat, lng ->
                AppPrefs.saveLocation(ctx, lat, lng)
                prayerTimes = PrayerCalculator.calculate(lat, lng)
                PrayerScheduler.scheduleAll(ctx, lat, lng)
                isLoadingLocation = false
            }}
        } else {
            locationError = "يرجى منح إذن الموقع"
            isLoadingLocation = false
        }
    }

    // Load on first open
    LaunchedEffect(Unit) {
        if (AppPrefs.isLocationSaved(ctx)) {
            // Load from saved location instantly
            val lat = AppPrefs.getLatitude(ctx)
            val lng = AppPrefs.getLongitude(ctx)
            prayerTimes = PrayerCalculator.calculate(lat, lng)
            // Silent background refresh
            scope.launch {
                getLocation(ctx) { newLat, newLng ->
                    if (Math.abs(newLat - lat) > 0.01 || Math.abs(newLng - lng) > 0.01) {
                        AppPrefs.saveLocation(ctx, newLat, newLng)
                        prayerTimes = PrayerCalculator.calculate(newLat, newLng)
                        PrayerScheduler.scheduleAll(ctx, newLat, newLng)
                    }
                }
            }
        } else {
            // First time - request location
            isLoadingLocation = true
            val hasPermission = ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                getLocation(ctx) { lat, lng ->
                    AppPrefs.saveLocation(ctx, lat, lng)
                    prayerTimes = PrayerCalculator.calculate(lat, lng)
                    PrayerScheduler.scheduleAll(ctx, lat, lng)
                    isLoadingLocation = false
                }
            } else {
                locationLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_father),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0xDD000000), Color(0xEE000000)))
            )
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // Title
            Text("محمد عبد العظيم", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = gold)
            Text("مواقيت الصلاة والأذكار", fontSize = 16.sp, color = Color.White)
            Text("🤲", fontSize = 28.sp, modifier = Modifier.padding(vertical = 4.dp))

            Spacer(Modifier.height(16.dp))

            when {
                isLoadingLocation -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xCC1B2E1C))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = gold)
                            Spacer(Modifier.height(8.dp))
                            Text("جاري تحديد موقعك...", color = Color.White)
                        }
                    }
                }
                locationError != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xCC2E1B1B))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚠️ $locationError", color = Color.White, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = {
                                locationError = null
                                isLoadingLocation = true
                                locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                            }, colors = ButtonDefaults.buttonColors(containerColor = gold)) {
                                Text("إعادة المحاولة", color = Color.Black)
                            }
                        }
                    }
                }
                prayerTimes != null -> {
                    PrayerTimesCard(prayerTimes = prayerTimes!!, gold = gold, ctx = ctx)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Zekr Section
            ZekrCard(gold = gold, ctx = ctx)

            Spacer(Modifier.height(16.dp))

            // Navigation Buttons
            Button(
                onClick = onNavigateToAdhkar,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
            ) {
                Icon(Icons.Default.MenuBook, null, tint = gold)
                Spacer(Modifier.width(8.dp))
                Text("أذكار الصباح والمساء", color = gold, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onNavigateToSettings,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2E1C))
            ) {
                Icon(Icons.Default.Settings, null, tint = gold)
                Spacer(Modifier.width(8.dp))
                Text("إعدادات الأذان", color = gold, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun PrayerTimesCard(prayerTimes: com.mohamedabdelazeim.salah.data.PrayerTimes, gold: Color, ctx: Context) {
    val prayers = PrayerCalculator.getPrayerList(prayerTimes)
    val nextPrayer = PrayerCalculator.getNextPrayer(prayerTimes)
    val fmt = SimpleDateFormat("hh:mm a", Locale("ar"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC0D1B0F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🕌 مواقيت الصلاة", color = gold, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp))

            prayers.forEach { prayer ->
                val isNext = prayer.index == nextPrayer?.index
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isNext) Color(0x33FFD700) else Color.Transparent)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isNext) Text("▶ ", color = gold, fontSize = 12.sp)
                        Text(
                            prayer.nameAr,
                            color = if (isNext) gold else Color.White,
                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        fmt.format(Date(prayer.timeMillis)),
                        color = if (isNext) gold else Color.LightGray,
                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZekrCard(gold: Color, ctx: Context) {
    var enabled by remember { mutableStateOf(AppPrefs.isZekrEnabled(ctx)) }
    var selectedInterval by remember { mutableStateOf(AppPrefs.getZekrIntervalMinutes(ctx)) }
    var expanded by remember { mutableStateOf(false) }
    val intervals = listOf(1, 5, 10, 15, 30, 60, 120)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC1B2E1C))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📿 إعدادات الأذكار", color = gold, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp))

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = "كل $selectedInterval دقيقة",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = gold, unfocusedBorderColor = Color.Gray
                    )
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    intervals.forEach { min ->
                        DropdownMenuItem(
                            text = { Text("كل $min دقيقة") },
                            onClick = {
                                selectedInterval = min
                                AppPrefs.setZekrIntervalMinutes(ctx, min)
                                if (enabled) ZekrScheduler.schedule(ctx, min.toLong())
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (enabled) "✅ الأذكار مفعّلة" else "⏸ الأذكار متوقفة",
                    color = if (enabled) gold else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = { v ->
                        enabled = v
                        AppPrefs.setZekrEnabled(ctx, v)
                        if (v) ZekrScheduler.schedule(ctx, selectedInterval.toLong())
                        else ZekrScheduler.cancel(ctx)
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = gold)
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun getLocation(ctx: Context, onResult: (Double, Double) -> Unit) {
    try {
        val client = LocationServices.getFusedLocationProviderClient(ctx)
        client.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onResult(location.latitude, location.longitude)
            }
        }
    } catch (e: Exception) {
        // fallback to Cairo
        onResult(30.0444, 31.2357)
    }
}
