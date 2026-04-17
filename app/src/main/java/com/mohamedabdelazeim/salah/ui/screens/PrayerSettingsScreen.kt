package com.mohamedabdelazeim.salah.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedabdelazeim.salah.data.AppPrefs
import com.mohamedabdelazeim.salah.data.PrayerCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerSettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val gold = Color(0xFFFFD700)
    val prayers = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إعدادات الأذان", color = gold, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1B0F))
            )
        },
        containerColor = Color(0xFF0D1B0F)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "اختر صوت الأذان لكل صلاة",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            prayers.forEachIndexed { index, prayerName ->
                PrayerSoundCard(
                    prayerName = prayerName,
                    prayerIndex = index,
                    gold = gold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerSoundCard(prayerName: String, prayerIndex: Int, gold: Color) {
    val ctx = LocalContext.current
    var isSilent by remember { mutableStateOf(AppPrefs.isPrayerSilent(ctx, prayerIndex)) }
    var selectedSound by remember { mutableStateOf(AppPrefs.getPrayerSound(ctx, prayerIndex)) }
    var customUri by remember { mutableStateOf(AppPrefs.getCustomSoundUri(ctx, prayerIndex)) }
    var expanded by remember { mutableStateOf(false) }

    val soundOptions = listOf("azan1" to "أذان 1", "azan2" to "أذان 2")

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            ctx.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            customUri = it.toString()
            AppPrefs.setCustomSoundUri(ctx, prayerIndex, it.toString())
            selectedSound = "custom"
            AppPrefs.setPrayerSound(ctx, prayerIndex, "custom")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2E1C))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(prayerName, color = gold, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("صامت", color = Color.Gray, fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    Switch(
                        checked = isSilent,
                        onCheckedChange = { v ->
                            isSilent = v
                            AppPrefs.setPrayerSilent(ctx, prayerIndex, v)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color.Gray
                        )
                    )
                }
            }

            if (!isSilent) {
                Spacer(Modifier.height(12.dp))

                // Sound selector
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = when (selectedSound) {
                            "azan1" -> "أذان 1"
                            "azan2" -> "أذان 2"
                            "custom" -> "صوت مخصص 🎵"
                            else -> "أذان 1"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("صوت الأذان", color = Color.Gray) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = gold, unfocusedBorderColor = Color.Gray
                        )
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        soundOptions.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedSound = key
                                    AppPrefs.setPrayerSound(ctx, prayerIndex, key)
                                    expanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("اختيار من الهاتف 📁") },
                            onClick = {
                                fileLauncher.launch(arrayOf("audio/*"))
                                expanded = false
                            }
                        )
                    }
                }

                if (customUri != null && selectedSound == "custom") {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "✅ تم اختيار صوت مخصص",
                        color = gold,
                        fontSize = 12.sp
                    )
                }
            } else {
                Spacer(Modifier.height(8.dp))
                Text("🔕 هذه الصلاة على الصامت", color = Color.Gray, fontSize = 13.sp)
            }
        }
    }
}
