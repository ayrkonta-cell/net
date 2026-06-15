package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.DesktopMac
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.NetworkHistoryEntity
import com.example.ui.components.SpeedometerGauge
import com.example.ui.viewmodel.ActiveTestType
import com.example.ui.viewmodel.NetworkViewModel
import com.example.ui.viewmodel.TestStatus
import com.example.utils.NetworkUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainDashboardScreen(
    viewModel: NetworkViewModel,
    modifier: Modifier = Modifier
) {
    val connectionInfo by viewModel.connectionState.collectAsState()
    val diagnosticsState by viewModel.diagnosticsState.collectAsState()
    val historyList by viewModel.testHistory.collectAsState()

    var isDetailsExpanded by remember { mutableStateOf(false) }

    // Outer Sleek Color Theme (Deep dry carbon charcoal slate)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A1C1E),
            Color(0xFF16181A),
            Color(0xFF121314)
        )
    )

    var activeTab by remember { mutableStateOf(0) }
    val lanDevices by viewModel.lanDevices.collectAsState()
    val lanScanRunning by viewModel.lanScanRunning.collectAsState()
    val lanScanProgress by viewModel.lanScanProgress.collectAsState()
    val signalStrengthDbm by viewModel.signalStrengthDbm.collectAsState()
    val signalDetectorActive by viewModel.signalDetectorActive.collectAsState()
    val gatewayIp by viewModel.gatewayIp.collectAsState()

    // NEW state variables for advanced network identification and diagnostics
    val dnsResults by viewModel.dnsResults.collectAsState()
    val dnsBenchmarkRunning by viewModel.dnsBenchmarkRunning.collectAsState()
    val webResults by viewModel.webResults.collectAsState()
    val webScanRunning by viewModel.webScanRunning.collectAsState()
    val extendedDiag by viewModel.extendedDiag.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2D3033))
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 0: Home / Speed Checks
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTab = 0 }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "בדיקת רשת",
                        tint = if (activeTab == 0) Color(0xFFD1E1FF) else Color(0xFFC2C7CF).copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "בדיקת רשת",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = if (activeTab == 0) Color(0xFFD1E1FF) else Color(0xFFC2C7CF).copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // Tab 1: Connected Devices (Who is on network)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTab = 1 }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = "סורק רשת",
                        tint = if (activeTab == 1) Color(0xFFD1E1FF) else Color(0xFFC2C7CF).copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "מי מחובר",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = if (activeTab == 1) Color(0xFFD1E1FF) else Color(0xFFC2C7CF).copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // Tab 2: Hotspot Proximity Signal Detector
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTab = 2 }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = "גלאי עוצמה",
                        tint = if (activeTab == 2) Color(0xFFD1E1FF) else Color(0xFFC2C7CF).copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "גלאי עוצמה",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = if (activeTab == 2) Color(0xFFD1E1FF) else Color(0xFFC2C7CF).copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            when (activeTab) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                // 1. Sleek Dashboard Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NetCheck",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp
                                ),
                                color = Color(0xFFD1E1FF),
                                modifier = Modifier.testTag("dashboard_title")
                            )
                            Text(
                                text = "אפליקציה מתקדמת לבדיקת הרשת",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFC2C7CF).copy(alpha = 0.8f)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.refreshConnectionInfo() },
                            modifier = Modifier
                                .background(Color(0xFF2D3033), CircleShape)
                                .testTag("refresh_conn_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "ריענון נתונים / Refresh info",
                                tint = Color(0xFFD1E1FF)
                            )
                        }
                    }
                }

                // 2. Real-time Connection Medium Status Banner
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("network_info_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2D3033)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val connTypeIcon = when (connectionInfo.connectionType) {
                                        NetworkUtils.ConnectionType.WIFI -> Icons.Default.Wifi
                                        NetworkUtils.ConnectionType.CELLULAR -> Icons.Default.SignalCellularAlt
                                        NetworkUtils.ConnectionType.ETHERNET -> Icons.Default.Route
                                        NetworkUtils.ConnectionType.OFFLINE -> Icons.Default.Warning
                                    }
                                    
                                    val connTypeColor = when (connectionInfo.connectionType) {
                                        NetworkUtils.ConnectionType.OFFLINE -> Color(0xFFE57373)
                                        else -> Color(0xFFD1E1FF)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(connTypeColor.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = connTypeIcon,
                                            contentDescription = "סוג רשת",
                                            tint = connTypeColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        val connHebrewName = when (connectionInfo.connectionType) {
                                            NetworkUtils.ConnectionType.WIFI -> "רשת אלחוטית (Wi-Fi)"
                                            NetworkUtils.ConnectionType.CELLULAR -> "רשת סלולרית"
                                            NetworkUtils.ConnectionType.ETHERNET -> "רשת קווית (Ethernet)"
                                            NetworkUtils.ConnectionType.OFFLINE -> "אין חיבור אינטרנט"
                                        }

                                        Text(
                                            text = connHebrewName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color(0xFFE2E2E6)
                                        )
                                        
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val indicatorDotColor = if (connectionInfo.connectionType == NetworkUtils.ConnectionType.OFFLINE) Color(0xFFE57373) else Color(0xFF81C784)
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(indicatorDotColor, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (connectionInfo.connectionType == NetworkUtils.ConnectionType.OFFLINE) "מנותק" else "מחובר לשרת בדיקה",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFFC2C7CF)
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color.White.copy(alpha = 0.08f)
                            )

                            // Quick properties display
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "IP פנימי / Local IP",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFC2C7CF).copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = connectionInfo.localIp,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = Color(0xFFE2E2E6)
                                    )
                                }
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "IP חיצוני / Public IP",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFC2C7CF).copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = connectionInfo.publicIp,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = Color(0xFFD1E1FF)
                                    )
                                }
                            }

                            // EXPANDABLE technical summary
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isDetailsExpanded = !isDetailsExpanded }
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isDetailsExpanded) "הסתר פרטי רשת מתקדמים / Hide Details" else "הצג פרטי רשת מתקדמים / Show Details",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFFD1E1FF)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (isDetailsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "פעולה",
                                    tint = Color(0xFFD1E1FF),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            AnimatedVisibility(
                                visible = isDetailsExpanded,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.2f))
                                        .padding(12.dp)
                                        .testTag("detailed_technical_info_card")
                                ) {
                                    if (connectionInfo.details.isEmpty()) {
                                        Text(
                                            text = "רקע שירותי רשת לא מצא מידע נוסף.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFFC2C7CF)
                                        )
                                    } else {
                                        connectionInfo.details.forEach { (key, value) ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = key,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color(0xFFC2C7CF)
                                                )
                                                Text(
                                                    text = value,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = Color(0xFFE2E2E6)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Central Interactive Speedometer Deck
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2D3033)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (diagnosticsState.testType == ActiveTestType.SPEED) "בדיקת רוחב פס פעילה" else if (diagnosticsState.testType == ActiveTestType.PING) "בדיקת השהייה פעילה" else "לוח מחוונים ראשי",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC2C7CF)
                            )

                            // Speed Gauge Widget
                            SpeedometerGauge(
                                currentSpeed = diagnosticsState.currentSpeedMbps,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )

                            // Status Info Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.15f))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (diagnosticsState.status == TestStatus.RUNNING) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color(0xFFD1E1FF),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                    }
                                    Text(
                                        text = diagnosticsState.statusMessage,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = if (diagnosticsState.status == TestStatus.FAILED) Color(0xFFE57373) else Color(0xFFE2E2E6),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Test Trigger Buttons Row (Sleek Rounded Action Button style)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.runSpeedTest() },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(54.dp)
                                        .testTag("run_speed_test_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD1E1FF),
                                        contentColor = Color(0xFF003061)
                                    ),
                                    shape = RoundedCornerShape(28.dp),
                                    enabled = diagnosticsState.status != TestStatus.RUNNING
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = "פעולת מהירות",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "בדיקת מהירות",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                Button(
                                    onClick = { viewModel.runPingDiagnostic() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .testTag("run_ping_test"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF004787),
                                        contentColor = Color(0xFFD1E1FF)
                                    ),
                                    shape = RoundedCornerShape(28.dp),
                                    enabled = diagnosticsState.status != TestStatus.RUNNING
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Dns,
                                            contentDescription = "פעולת פינג",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "פינג / Ping",
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Detailed Ping Telemetry Metrics
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2D3033)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "נתוני השהייה ויציבות / Telemetry Specs",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC2C7CF),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Ping
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "דילאיי (Ping)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFC2C7CF)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (diagnosticsState.pingMs > 0) "${String.format("%.1f", diagnosticsState.pingMs)} ms" else "-- ms",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.SansSerif
                                        ),
                                        color = Color(0xFFD1E1FF)
                                    )
                                }

                                // Jitter
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "תנודות (Jitter)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFC2C7CF)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (diagnosticsState.jitterMs > 0) "${String.format("%.1f", diagnosticsState.jitterMs)} ms" else "-- ms",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.SansSerif
                                        ),
                                        color = Color(0xFFD1E1FF).copy(alpha = 0.8f)
                                    )
                                }

                                // Loss
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "אובדן מידע (Loss)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFC2C7CF)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${diagnosticsState.packetLossPercent}%",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.SansSerif
                                        ),
                                        color = if (diagnosticsState.packetLossPercent > 0) Color(0xFFE57373) else Color(0xFF81C784)
                                    )
                                }
                            }
                        }
                    }
                }

                // New Section: Advanced Network Diagnostics & Identification Suite
                item {
                    AdvancedDiagnosticsCard(
                        dnsResults = dnsResults,
                        dnsBenchmarkRunning = dnsBenchmarkRunning,
                        webResults = webResults,
                        webScanRunning = webScanRunning,
                        extendedDiag = extendedDiag,
                        onRunDns = { viewModel.runDnsBenchmark() },
                        onRunWeb = { viewModel.runWebReachabilityScan() }
                    )
                }

                // 5. Test History Record (Room Persistence integration)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "לוג בדיקות",
                                tint = Color(0xFFD1E1FF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "היסטוריית בדיקות / History logs",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2E2E6)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${historyList.size})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFC2C7CF).copy(alpha = 0.7f)
                            )
                        }

                        if (historyList.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.clearHistory() },
                                modifier = Modifier.testTag("clear_history_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "מחיקת היסטוריה / Clear all log",
                                    tint = Color(0xFFE57373).copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Empty state for SQLite logs
                if (historyList.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = "אין לוגים",
                                tint = Color(0xFF2D3033),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "אין נתוני בדיקות שמורים עדיין.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFC2C7CF).copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "הרץ בדיקה למעלה כדי לשמור תוצאות.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFC2C7CF).copy(alpha = 0.4f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(historyList) { historyItem ->
                        HistoryItemCard(
                            item = historyItem,
                            onDelete = { viewModel.deleteHistoryItem(historyItem.id) }
                        )
                    }
                }

                // Add nice padding spacing at the bottom
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
        1 -> {
            LanScannerTab(
                lanDevices = lanDevices,
                lanScanRunning = lanScanRunning,
                lanScanProgress = lanScanProgress,
                gatewayIp = gatewayIp,
                onStartScan = { viewModel.startLanScan() }
            )
        }
        2 -> {
            ProximityRadarTab(
                signalStrengthDbm = signalStrengthDbm,
                signalDetectorActive = signalDetectorActive,
                gatewayIp = gatewayIp,
                onToggleBeeper = { active ->
                    if (active) {
                        viewModel.startSignalBeeper(context)
                    } else {
                        viewModel.stopSignalBeeper()
                    }
                },
                onSimulateStrength = { newRssi ->
                    viewModel.setSignalStrengthDbm(newRssi)
                }
            )
        }
    }
}
}
}

@Composable
fun LanScannerTab(
    lanDevices: List<com.example.ui.viewmodel.LocalDevice>,
    lanScanRunning: Boolean,
    lanScanProgress: Float,
    gatewayIp: String,
    onStartScan: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page header item
        item {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                Text(
                    text = "מי מחובר אלי (LAN Scanner)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFFD1E1FF)
                )
                Text(
                    text = "סורק ומאתר מכשירים המחוברים לרשת הביתית שלך בזמן אמת",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFC2C7CF).copy(alpha = 0.8f)
                )
            }
        }

        // Active trigger and progress card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3033)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "שער ברירת מחדל (Default Gateway)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFC2C7CF)
                            )
                            Text(
                                text = gatewayIp,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFFD1E1FF)
                            )
                        }

                        Button(
                            onClick = onStartScan,
                            enabled = !lanScanRunning,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD1E1FF),
                                contentColor = Color(0xFF003061)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (lanScanRunning) Icons.Default.Refresh else Icons.Default.Devices,
                                    contentDescription = "פעולת סריקה",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (lanScanRunning) "סורק..." else "סרוק רשת",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (lanScanRunning) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Sleek premium modern progress indicator with feedback
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "סורק את טווח הכתובות ...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFC2C7CF)
                                )
                                Text(
                                    text = "${(lanScanProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFD1E1FF)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { lanScanProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFD1E1FF),
                                trackColor = Color.White.copy(alpha = 0.08f)
                            )
                        }
                    } else if (lanDevices.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "נמצאו ${lanDevices.size} מכשירים מחוברים",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF81C784)
                        )
                    }
                }
            }
        }

        // List of found LAN devices
        if (lanDevices.isEmpty() && !lanScanRunning) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = "אין מכשירים",
                        tint = Color(0xFF2D3033),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "טרם בוצעה סריקת מכשירים לרשת זו.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC2C7CF).copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "לחץ על 'סרוק רשת' כדי לראות מי מחובר אלייך.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC2C7CF).copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(lanDevices) { device ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("device_card_${device.ip}"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3033)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Device icon based on its type
                            val deviceIcon = when (device.type) {
                                "נתב" -> Icons.Default.Router
                                "טלפון" -> Icons.Default.PhoneAndroid
                                "טלוויזיה" -> Icons.Default.Tv
                                "מנורה חכמה" -> Icons.Default.Lightbulb
                                "מצלמה" -> Icons.Default.Videocam
                                "מחשב" -> Icons.Default.Computer
                                else -> Icons.Default.Devices
                            }

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(Color(0xFFD1E1FF).copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = deviceIcon,
                                    contentDescription = device.type,
                                    tint = Color(0xFFD1E1FF),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = device.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFE2E2E6)
                                    )
                                    if (device.isMe) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF004787), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "מכשיר זה",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = Color(0xFFD1E1FF),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    if (device.isRouter) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF035E3B), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "שער ראשי",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = Color(0xFFD1E1FF),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "IP: ${device.ip}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    color = Color(0xFFC2C7CF).copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "MAC: ${device.mac}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                                    color = Color(0xFFC2C7CF).copy(alpha = 0.6f)
                                )
                            }
                        }

                        // Pulsing active dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFF81C784), CircleShape)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProximityRadarTab(
    signalStrengthDbm: Int,
    signalDetectorActive: Boolean,
    gatewayIp: String,
    onToggleBeeper: (Boolean) -> Unit,
    onSimulateStrength: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                Text(
                    text = "גלאי חוזק חיבור אקסס פוינט",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFFD1E1FF)
                )
                Text(
                    text = "בדיקת מרחק ועוצמת האקסס פוינט (נתב) עם התרעת סאונד מכוונת",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFC2C7CF).copy(alpha = 0.8f)
                )
            }
        }

        // Radar visual core gauge
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("radar_power_gauge"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3033)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "כתובת ה-IP של האקסס פוינט אליו אתה מחובר:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC2C7CF)
                    )
                    Text(
                        text = gatewayIp,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color(0xFFD1E1FF),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Big glowing visual radial indicator
                    val (signalLabel, signalColor) = when {
                        signalStrengthDbm >= -50 -> Pair("מעולה (Extremely Strong)", Color(0xFF81C784))
                        signalStrengthDbm >= -67 -> Pair("טוב מאוד (Very Strong)", Color(0xFFD1E1FF))
                        signalStrengthDbm >= -75 -> Pair("בינוני (Moderate)", Color(0xFFFFD54F))
                        signalStrengthDbm >= -90 -> Pair("חלש (Weak Signal)", Color(0xFFFF8A65))
                        else -> Pair("אין קליטה (No Coverage)", Color(0xFFE57373))
                    }

                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(signalColor.copy(alpha = 0.08f), CircleShape)
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .background(signalColor.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = "Radar Symbol",
                                    tint = signalColor,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$signalStrengthDbm dBm",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = signalLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = signalColor
                    )
                }
            }
        }

        // Sound trigger action card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("sound_trigger_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3033)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.3f)) {
                            Text(
                                text = "שמע אקוסטי (Hotspot Sound Beeper)",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "קצב הצפצוף וגובה הצליל יגדלו כשתתקרב אל האקסס פוינט המחובר של המכשיר.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFC2C7CF).copy(alpha = 0.8f)
                            )
                        }

                        IconButton(
                            onClick = { onToggleBeeper(!signalDetectorActive) },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    if (signalDetectorActive) Color(0xFF035E3B) else Color(0xFF004787),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (signalDetectorActive) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                contentDescription = "פעולת שמע",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = signalDetectorActive) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.2f))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color(0xFF81C784),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "הגלאי סורק ומצפצף בהתאם לעוצמה...",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF81C784)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Proximity simulation interactive slider! (Awesome for testing in the browser emulator)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("simulation_slider_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3033)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "מצב סימולציה לבדיקת השמע (Emulator Sim)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFD1E1FF)
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFD1E1FF).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "בשבילך!",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = Color(0xFFD1E1FF)
                            )
                        }
                    }
                    Text(
                        text = "גרור את הסמן ימינה (קרוב מאוד לנתב) ושמאלה (רחוק מאוד מהנתב) כדי לשמוע ולראות את השינוי המיידי בקצב הצפצופים ישירות במכשיר!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC2C7CF).copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "רחוק (-100)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC2C7CF).copy(alpha = 0.6f)
                        )

                        androidx.compose.material3.Slider(
                            value = signalStrengthDbm.toFloat(),
                            onValueChange = { onSimulateStrength(it.toInt()) },
                            valueRange = -100f..-30f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                            colors = androidx.compose.material3.SliderDefaults.colors(
                                thumbColor = Color(0xFFD1E1FF),
                                activeTrackColor = Color(0xFFD1E1FF),
                                inactiveTrackColor = Color.White.copy(alpha = 0.08f)
                            )
                        )

                        Text(
                            text = "קרוב (-30)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD1E1FF)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HistoryItemCard(
    item: NetworkHistoryEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = sdf.format(Date(item.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("history_item_card_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D3033)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Test type status circle badge
                val (colorAccent, iconSymbol) = if (item.testType == "SPEED") {
                    Pair(Color(0xFFD1E1FF), Icons.Default.Speed)
                } else {
                    Pair(Color(0xFFD1E1FF).copy(alpha = 0.8f), Icons.Default.Dns)
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(colorAccent.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconSymbol,
                        contentDescription = "בדיקה",
                        tint = colorAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    val labelText = if (item.testType == "SPEED") {
                        "בדיקת מהירות רוחב פס"
                    } else {
                        "בדיקת השהייה - Ping"
                    }

                    Text(
                        text = labelText,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE2E2E6)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC2C7CF).copy(alpha = 0.6f)
                        )
                        Text(
                            text = "|",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC2C7CF).copy(alpha = 0.3f)
                        )
                        val connMedium = when (item.connectionType) {
                            "WIFI" -> "אלחוטי"
                            "CELLULAR" -> "סלולרי"
                            else -> "אחר"
                        }
                        Text(
                            text = connMedium,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC2C7CF).copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    if (item.testType == "SPEED") {
                        Text(
                            text = String.format("%.1f Mbps", item.downloadSpeedMbps),
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.SansSerif
                            ),
                            color = Color(0xFFD1E1FF)
                        )
                    } else {
                        Text(
                            text = String.format("%.0f ms", item.pingMs),
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.SansSerif
                            ),
                            color = Color(0xFFD1E1FF).copy(alpha = 0.8f)
                        )
                    }

                    // Secondary spec indicator sub-label
                    if (item.testType == "SPEED") {
                        Text(
                            text = "Ping ${String.format("%.0f", item.pingMs)}ms",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC2C7CF).copy(alpha = 0.7f)
                        )
                    } else {
                        Text(
                            text = "Jitter ${String.format("%.0f", item.jitterMs)}ms",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC2C7CF).copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "מחק פריט",
                        tint = Color(0xFFC2C7CF).copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AdvancedDiagnosticsCard(
    dnsResults: List<com.example.ui.viewmodel.DnsResult>,
    dnsBenchmarkRunning: Boolean,
    webResults: List<com.example.ui.viewmodel.WebResult>,
    webScanRunning: Boolean,
    extendedDiag: com.example.ui.viewmodel.ExtendedDiagnosticInfo,
    onRunDns: () -> Unit,
    onRunWeb: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("advanced_diag_card"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D3033)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "כלי אבחון וזיהוי מתקדמים",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFD1E1FF),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Responsive selector tabs for small devices (using equal weights)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.15f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabLabels = listOf(
                    Triple("פרטי שידור", Icons.Default.Sensors, 0),
                    Triple("שרתי DNS", Icons.Default.Dns, 1),
                    Triple("נגישות אתרים", Icons.Default.Language, 2)
                )

                tabLabels.forEach { (label, icon, index) ->
                    val isSelected = selectedTab == index
                    val activeBgColor = if (isSelected) Color(0xFFD1E1FF) else Color.Transparent
                    val activeContentColor = if (isSelected) Color(0xFF003061) else Color(0xFFC2C7CF)

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(activeBgColor)
                            .clickable { selectedTab = index }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = activeContentColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            ),
                            color = activeContentColor,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // RF & Extended identification specs
                    val bandLabel = if (extendedDiag.wifiFreq >= 5000) "5 GHz (מהיר / High Band)" else if (extendedDiag.wifiFreq in 2400..2499) "2.4 GHz (בסיסי / Low Band)" else "N/A"
                    val items = listOf(
                        "סוג אבטחה" to extendedDiag.wifiSecurity,
                        "תדר תפעול" to "${extendedDiag.wifiFreq} MHz",
                        "רוחב פס ערוץ" to bandLabel,
                        "ערוץ חיבור" to "ערוץ ${extendedDiag.wifiChannel}",
                        "רוחב פס מקסימלי" to "${extendedDiag.maxLinkSpeedMbps} Mbps",
                        "איכות שידור מוערכת" to "${extendedDiag.signalQualityPercent}%"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items.forEach { (title, subtitle) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.03f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFC2C7CF).copy(alpha = 0.8f)
                                )
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // DNS speed benchmark
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "אבחון ומהירות שרתי DNS",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFC2C7CF)
                            )

                            Button(
                                onClick = onRunDns,
                                enabled = !dnsBenchmarkRunning,
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD1E1FF),
                                    contentColor = Color(0xFF003061)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (dnsBenchmarkRunning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(10.dp),
                                        strokeWidth = 1.5.dp,
                                        color = Color(0xFF003061)
                                    )
                                } else {
                                    Text("הרץ אבחון", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (dnsResults.isEmpty() && !dnsBenchmarkRunning) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "לחץ 'הרץ אבחון' כדי לבדוק זמני תגובת שרתי שמות",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFC2C7CF).copy(alpha = 0.5f)
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                dnsResults.forEach { result ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.03f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = result.provider,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                            Text(
                                                text = result.serverIp,
                                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp),
                                                color = Color(0xFFC2C7CF).copy(alpha = 0.6f)
                                            )
                                        }

                                        val latencyColor = when {
                                            !result.isSuccess -> Color(0xFFE57373)
                                            result.latencyMs < 35 -> Color(0xFF81C784)
                                            result.latencyMs < 80 -> Color(0xFFD1E1FF)
                                            else -> Color(0xFFFFD54F)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(latencyColor, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (result.isSuccess) "${result.latencyMs} ms" else "נכשל (Fail)",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = latencyColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Web Service Reachability Diagnostics
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "בדיקת נגישות אתרים גלובליים/מקומיים",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFC2C7CF)
                            )

                            Button(
                                onClick = onRunWeb,
                                enabled = !webScanRunning,
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD1E1FF),
                                    contentColor = Color(0xFF003061)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (webScanRunning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(10.dp),
                                        strokeWidth = 1.5.dp,
                                        color = Color(0xFF003061)
                                    )
                                } else {
                                    Text("אבחן אתרים", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (webResults.isEmpty() && !webScanRunning) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "לחץ 'אבחן אתרים' כדי לאמת גישה לשירותים נפוצים",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFC2C7CF).copy(alpha = 0.5f)
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                webResults.forEach { result ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.03f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = result.siteName,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                            Text(
                                                text = result.domain,
                                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp),
                                                color = Color(0xFFC2C7CF).copy(alpha = 0.6f)
                                            )
                                        }

                                        val statusColor = if (result.isReachable) Color(0xFF81C784) else Color(0xFFE57373)

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (result.isReachable) Icons.Default.CheckCircle else Icons.Default.Warning,
                                                contentDescription = if (result.isReachable) "אונליין" else "אופליין",
                                                tint = statusColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (result.isReachable) "${result.rttMs} ms" else "אין גישה",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = statusColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
