package com.example.restaura20

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Data class
data class AppUsageInfo(val appName: String, val usageTimeMillis: Long, val color: Color)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val showPermission = remember { mutableStateOf(false) }
            val showHome = remember { mutableStateOf(false) }
            val appUsageStats = remember { mutableStateListOf<AppUsageInfo>() }
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0f172a)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = { Text("REST AURA", color = Color.White) },
                        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF1E293B))
                    )
                    when {
                        showPermission.value -> {
                            PermissionScreen(
                                onGranted = {
                                    scope.launch {
                                        // small delay to let permission settings settle
                                        delay(500)
                                        val data = fetchUsageStats(context)
                                        appUsageStats.clear()
                                        appUsageStats.addAll(data)
                                        showHome.value = true
                                    }
                                }
                            )
                        }
                        showHome.value -> {
                            HomeScreen(appUsageData = appUsageStats)
                        }
                        else -> {
                            RegisterScreen(
                                onRegister = {
                                    showPermission.value = true
                                },
                                onGoogle = {
                                    showPermission.value = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Checking usage stats permission
    private fun isUsageAccessGranted(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Binder.getCallingUid(),
                packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Binder.getCallingUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun hasUsageStatsPermission(): Boolean = isUsageAccessGranted()
}

// Registration UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onRegister: () -> Unit, onGoogle: () -> Unit) {
    var phone by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to RestAura",
            fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)

        Spacer(Modifier.height(24.dp))

        Text("Phone no:", color = Color.White)
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            placeholder = { Text("Enter phone", color = Color.LightGray) },
            textStyle = TextStyle(Color.White),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Text("Email:", color = Color.White)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Enter email", color = Color.LightGray) },
            textStyle = TextStyle(Color.White),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
            Text("Register", color = Color.White)
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onGoogle, modifier = Modifier.fillMaxWidth()) {
            Text("Sign in with Google", color = Color.Black)
        }
    }
}

// Permission UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
// Permission UI
fun PermissionScreen(onGranted: () -> Unit) {
    val context = LocalContext.current
    val usageGranted by remember {
        mutableStateOf(
            Settings.canDrawOverlays(context) && AppOpsManager.MODE_ALLOWED == (context
                .getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager)
                .let { appOpsManager ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        appOpsManager.unsafeCheckOpNoThrow(
                            AppOpsManager.OPSTR_GET_USAGE_STATS,
                            android.os.Process.myUid(),
                            context.packageName
                        )
                    } else {
                        appOpsManager.checkOpNoThrow(
                            AppOpsManager.OPSTR_GET_USAGE_STATS,
                            android.os.Process.myUid(),
                            context.packageName
                        )
                    }
                }
        )
    }
    val overlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Grant Permissions", color = Color.White, fontSize = 24.sp)
        Spacer(Modifier.height(16.dp))
        Text("Usage Access: " + if (usageGranted) "Granted" else "Denied",
            color = if (usageGranted) Color.Green else Color.Red)
        Spacer(Modifier.height(8.dp))
        Text("Overlay: " + if (overlayGranted) "Granted" else "Denied",
            color = if (overlayGranted) Color.Green else Color.Red)
        Spacer(Modifier.height(24.dp))
        if (!usageGranted) {
            Button(onClick = {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }) { Text("Grant Usage Access", color = Color.White) }
        }
        if (!overlayGranted) {
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                context.startActivity(Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                ))
            }) { Text("Grant Overlay", color = Color.White) }
        }
        Spacer(Modifier.height(24.dp))
        if (usageGranted && overlayGranted) {
            Button(onClick = onGranted, modifier = Modifier.fillMaxWidth()) {
                Text("Continue", color = Color.White)
            }
        }
    }
}

// Home UI
@Composable
fun HomeScreen(appUsageData: List<AppUsageInfo>) {
    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Your Screen Time Today", color = Color.White, fontSize = 24.sp)
        Spacer(Modifier.height(16.dp))

        val total = appUsageData.sumOf { it.usageTimeMillis }
        Text("Today: ${(total / 60000)} mins", color = Color.White)

        Spacer(Modifier.height(16.dp))
        if (appUsageData.firstOrNull()?.appName == "No data") {
            Text("No usage data available", color = Color.LightGray)
        } else {
            PieChart(data = appUsageData)
            Spacer(Modifier.height(16.dp))
            Text("Top 5 Most Used Apps", color = Color.White)
            appUsageData.forEach { u ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(16.dp).background(u.color))
                    Spacer(Modifier.width(8.dp))
                    Text(u.appName, color = Color.White, modifier = Modifier.weight(1f))
                    Text(formatMillisToTime(u.usageTimeMillis), color = Color.LightGray)
                }
            }
        }
    }
}

// Fetch UsageStats
fun fetchUsageStats(context: Context): List<AppUsageInfo> {
    val mgr = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val end = System.currentTimeMillis()
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val stats = mgr.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, cal.timeInMillis, end)
    if (stats.isNullOrEmpty()) return listOf(AppUsageInfo("No data", 0, Color.Gray))

    val filtered = stats.mapNotNull {
        try {
            val ai = context.packageManager.getApplicationInfo(it.packageName, 0)
            val lbl = context.packageManager.getApplicationLabel(ai).toString()
            val sys = ai.flags and ApplicationInfo.FLAG_SYSTEM != 0
            val self = it.packageName == context.packageName
            if (!sys && !self && it.totalTimeInForeground > 0)
                AppUsageInfo(lbl, it.totalTimeInForeground, Color.Transparent)
            else null
        } catch (e: Exception) {
            Log.e("UsageStats", e.localizedMessage ?: "err")
            null
        }
    }
    if (filtered.isEmpty()) return listOf(AppUsageInfo("No data", 0, Color.Gray))
    return filtered.distinctBy { it.appName }
        .sortedByDescending { it.usageTimeMillis }
        .take(5)
        .mapIndexed { i, u ->
            u.copy(color = listOf(
                Color(0xFFE57373), Color(0xFF64B5F6),
                Color(0xFF81C784), Color(0xFFFFD54F),
                Color(0xFFBA68C8)
            )[i % 5])
        }
}

// Pie Chart composable
@Composable
fun PieChart(data: List<AppUsageInfo>) {
    val total = data.sumOf { it.usageTimeMillis }.coerceAtLeast(1L)
    Canvas(Modifier.size(200.dp)) {
        var start = -90f
        data.forEach {
            val sweep = (it.usageTimeMillis.toFloat() / total) * 360f
            drawArc(it.color, startAngle = start, sweepAngle = sweep, useCenter = false, style = Stroke(40f))
            start += sweep
        }
        drawIntoCanvas { c ->
            val paint = android.graphics.Paint().apply {
                color = Color.White.toArgb()
                textSize = 40f; textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true; isFakeBoldText = true
            }
            c.nativeCanvas.drawText("Today", center.x, center.y - 20, paint)
            c.nativeCanvas.drawText("${total / 60000} mins", center.x, center.y + 20, paint)
        }
    }
}

private fun formatMillisToTime(m: Long): String {
    val min = TimeUnit.MILLISECONDS.toMinutes(m)
    val hr = TimeUnit.MINUTES.toHours(min)
    val rem = min % 60
    return if (hr > 0) "${hr}h ${rem}m" else "${rem}m"
}