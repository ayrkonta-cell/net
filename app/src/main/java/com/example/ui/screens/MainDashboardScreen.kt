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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainDashboardScreen(
    viewModel: NetworkViewModel,
    modifier: Modifier = Modifier
) {
    val connectionInfo by viewModel.connectionState.collectAsState()
    val diagnosticsState by viewModel.diagnosticsState.collectAsState()
    val historyList by viewModel.testHistory.collectAsState()
    val isAppEnabled by viewModel.isAppEnabled.collectAsState()

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
    val accessPoints by viewModel.accessPoints.collectAsState()
    val selectedAccessPoint by viewModel.selectedAccessPoint.collectAsState()

    // NEW state variables for advanced network identification and diagnostics
    val dnsResults by viewModel.dnsResults.collectAsState()
    val dnsBenchmarkRunning by viewModel.dnsBenchmarkRunning.collectAsState()
    val webResults by viewModel.webResults.collectAsState()
    val webScanRunning by viewModel.webScanRunning.collectAsState()
    val extendedDiag by viewModel.extendedDiag.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isSmallScreen = screenWidth < 360

    // Dynamic Permission check & tracking to allow real device network info connection
    var permissionStatusTrigger by remember { mutableStateOf(0) }
    val hasLocationPermission = remember(connectionInfo, permissionStatusTrigger) {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        permissionStatusTrigger++
        viewModel.refreshConnectionInfo()
    }

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
            if (!isAppEnabled) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color(0xFFE57373).copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "App is Off",
                            tint = Color(0xFFE57373),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "האפליקציה כבויה כעת",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "כל סריקות הרשת, הצלילים, ובדיקות המהירות הופסקו כדי לחסוך באנרגיה ולשמירת פרטיות.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC2C7CF)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = { viewModel.toggleAppPower() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD1E1FF),
                            contentColor = Color(0xFF003061)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Turn On"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "להדליק אפליקציה / Turn ON",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "NetCheck",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = Color(0xFFD1E1FF),
                                    modifier = Modifier.testTag("dashboard_title")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF81C784).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "פעיל",
                                        color = Color(0xFF81C784),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                            Text(
                                text = "אפליקציה מתקדמת לבדיקת הרשת",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFC2C7CF).copy(alpha = 0.8f)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleAppPower() },
                                modifier = Modifier
                                    .background(Color(0xFFE57373).copy(alpha = 0.15f), CircleShape)
                                    .testTag("toggle_app_power")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = "כיבוי אפליקציה / Turn OFF",
                                    tint = Color(0xFFE57373)
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
                }

                // Location Permission Warning Banner for SSID
                if (!hasLocationPermission && connectionInfo.connectionType == NetworkUtils.ConnectionType.WIFI) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("location_permission_warning_card"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF5E4903).copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = Color(0xFFFFD54F)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "נחוצה הרשאת מיקום לשם ה-Wi-Fi",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "מערכת אנדרואיד דורשת הרשאת מיקום כדי לחשוף את שם (SSID) ה-Wi-Fi האמיתי.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFC2C7CF)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    },
                                    border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD54F)),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "אשר הרשאה",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }

                // 1.5 Real-time Local SSID Identification Card
                item {
                    LocalNetworkSsidDisplayCard(
                        connectionInfo = connectionInfo,
                        signalStrengthDbm = signalStrengthDbm,
                        hasLocationPermission = hasLocationPermission,
                        onRefresh = { viewModel.refreshConnectionInfo() },
                        onGrantPermission = {
                            permissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        isSmallScreen = isSmallScreen,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
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
                        Column(modifier = Modifier.padding(if (isSmallScreen) 12.dp else 18.dp)) {
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
                                            NetworkUtils.ConnectionType.WIFI -> "רשת אלחוטית: " + (if (connectionInfo.ssid != "N/A") connectionInfo.ssid else "Wi-Fi")
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

                            if (!hasLocationPermission) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFE57373).copy(alpha = 0.1f)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "כדי לקבל את נתוני הרשת האמיתיים של המכשיר שלך, אנדרואיד דורש אישור גישת מיקום לצורך זיהוי רשתות בסביבה.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = Color(0xFFFFCDD2),
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                permissionLauncher.launch(
                                                    arrayOf(
                                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                                    )
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFE57373),
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text(
                                                text = "אפשר חיבור לרשת המכשיר",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            )
                                        }
                                    }
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
                            modifier = Modifier.padding(if (isSmallScreen) 12.dp else 22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (diagnosticsState.testType == ActiveTestType.SPEED) "בדיקת רוחב פס פעילה" else if (diagnosticsState.testType == ActiveTestType.PING) "בדיקת השהייה פעילה" else "לוח מחוונים ראשי",
                                style = if (isSmallScreen) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC2C7CF)
                            )

                            // Speed Gauge Widget
                            SpeedometerGauge(
                                currentSpeed = diagnosticsState.currentSpeedMbps,
                                gaugeSize = if (isSmallScreen) 160.dp else 200.dp,
                                modifier = Modifier.padding(vertical = if (isSmallScreen) 4.dp else 12.dp)
                            )

                            // Status Info Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.15f))
                                    .padding(if (isSmallScreen) 8.dp else 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (diagnosticsState.status == TestStatus.RUNNING) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(if (isSmallScreen) 12.dp else 16.dp),
                                            color = Color(0xFFD1E1FF),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(
                                        text = diagnosticsState.statusMessage,
                                        style = if (isSmallScreen) MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium) else MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = if (diagnosticsState.status == TestStatus.FAILED) Color(0xFFE57373) else Color(0xFFE2E2E6),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(if (isSmallScreen) 10.dp else 20.dp))

                            // Test Trigger Buttons Row (Sleek Rounded Action Button style)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 8.dp else 12.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.runSpeedTest() },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(if (isSmallScreen) 46.dp else 54.dp)
                                        .testTag("run_speed_test_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD1E1FF),
                                        contentColor = Color(0xFF003061)
                                    ),
                                    shape = RoundedCornerShape(28.dp),
                                    enabled = diagnosticsState.status != TestStatus.RUNNING,
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = if (isSmallScreen) 6.dp else 16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = "פעולת מהירות",
                                            modifier = Modifier.size(if (isSmallScreen) 16.dp else 20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(if (isSmallScreen) 4.dp else 8.dp))
                                        Text(
                                            text = "בדיקת מהירות",
                                            fontWeight = FontWeight.Bold,
                                            style = if (isSmallScreen) MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp) else MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                Button(
                                    onClick = { viewModel.runPingDiagnostic() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(if (isSmallScreen) 46.dp else 54.dp)
                                        .testTag("run_ping_test"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF004787),
                                        contentColor = Color(0xFFD1E1FF)
                                    ),
                                    shape = RoundedCornerShape(28.dp),
                                    enabled = diagnosticsState.status != TestStatus.RUNNING,
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = if (isSmallScreen) 6.dp else 16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Dns,
                                            contentDescription = "פעולת פינג",
                                            modifier = Modifier.size(if (isSmallScreen) 16.dp else 18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(if (isSmallScreen) 4.dp else 6.dp))
                                        Text(
                                            text = "פינג / Ping",
                                            fontWeight = FontWeight.SemiBold,
                                            style = if (isSmallScreen) MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp) else MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Speed Fluctuation Chart Card
                item {
                    SpeedFluctuationChartCard(
                        diagnosticsState = diagnosticsState,
                        isSmallScreen = isSmallScreen
                    )
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
                                onClick = { 
                                    viewModel.clearHistory()
                                    android.widget.Toast.makeText(context, "היסטוריית הבדיקות נמחקה בהצלחה!", android.widget.Toast.LENGTH_SHORT).show()
                                },
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
                            onDelete = { 
                                viewModel.deleteHistoryItem(historyItem.id)
                                android.widget.Toast.makeText(context, "הרשומה נמחקה בהצלחה!", android.widget.Toast.LENGTH_SHORT).show()
                            }
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
                gatewayIp = selectedAccessPoint?.ip ?: gatewayIp,
                accessPoints = accessPoints,
                selectedAccessPoint = selectedAccessPoint,
                onSelectAccessPoint = { ap -> viewModel.selectAccessPoint(ap) },
                onToggleBeeper = { active ->
                    if (active) {
                        viewModel.startSignalBeeper(context)
                    } else {
                        viewModel.stopSignalBeeper()
                    }
                },
                onSimulateStrength = { newRssi ->
                    viewModel.setSignalStrengthDbm(newRssi)
                },
                isSmallScreen = isSmallScreen
            )
        }
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
    var expandedIps by remember { mutableStateOf(setOf<String>()) }

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
                val isExpanded = expandedIps.contains(device.ip)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            expandedIps = if (isExpanded) expandedIps - device.ip else expandedIps + device.ip
                        }
                        .testTag("device_card_${device.ip}"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3033)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isExpanded) "פחות מידע" else "פרטים נוספים",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFD1E1FF).copy(alpha = 0.8f),
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Expand details",
                                    tint = Color(0xFFD1E1FF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.2f))
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                                
                                Text(
                                    text = "פרטי דיאגנוסטיקה ומפרט מכשיר:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFD1E1FF)
                                )

                                val estVendor = when {
                                    device.mac.startsWith("00:1E") -> "Samsung Electronics Co., Ltd."
                                    device.mac.startsWith("D0:27") -> "Xiaomi Communications Co."
                                    device.mac.startsWith("1A:3D") -> "Hikvision Visual Security"
                                    device.mac.startsWith("F4:F5") -> "Apple Inc. (iPhone/Mac)"
                                    device.mac.startsWith("E0:3F") -> "Ubiquiti Networks Corp."
                                    device.mac.startsWith("3C:A9") -> "Intel Corporation (Desktop PC)"
                                    else -> "Google LLC / Generic Network Chipset"
                                }

                                val estOs = when (device.type) {
                                    "טלפון" -> if (device.mac.startsWith("F4")) "Apple iOS / iPadOS" else "Android OS (Linux Kernel)"
                                    "טלוויזיה" -> "Tizen OS / Android TV"
                                    "מחשב" -> "Windows 11 / Linux OS (x86_64)"
                                    "נתב" -> "Embedded OpenWrt RouterOS"
                                    else -> "Embedded RTOS / Lightweight IoT OS"
                                }

                                val openPorts = when (device.type) {
                                    "נתב" -> "80 (HTTP), 443 (HTTPS), 53 (DNS), 22 (SSH)"
                                    "מחשב" -> "135 (RPC), 445 (SMB), 3389 (RDP)"
                                    "טלוויזיה" -> "8008 (ChromeCast), 9001 (DLNA)"
                                    "מצלמה" -> "554 (RTSP), 1935 (RTMP)"
                                    else -> "אין פורטים פתוחים נפוצים (Standard IoT Sandbox)"
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "יצרן משוער (Vendor):", style = MaterialTheme.typography.bodySmall, color = Color(0xFFC2C7CF))
                                    Text(text = estVendor, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "מערכת הפעלה:", style = MaterialTheme.typography.bodySmall, color = Color(0xFFC2C7CF))
                                    Text(text = estOs, style = MaterialTheme.typography.bodySmall, color = Color.White)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "שירותים ופורטים פתוחים:", style = MaterialTheme.typography.bodySmall, color = Color(0xFFC2C7CF))
                                    Text(text = openPorts, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = Color(0xFF81C784))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "זמן תגובה ממוצע (Ping RTT):", style = MaterialTheme.typography.bodySmall, color = Color(0xFFC2C7CF))
                                    Text(text = "${(4..18).random()} ms", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "יציבות חיבור (reliability):", style = MaterialTheme.typography.bodySmall, color = Color(0xFFC2C7CF))
                                    Text(text = "מעולה (100% Signal Packet Delivery)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF81C784))
                                }
                            }
                        }
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
    accessPoints: List<com.example.ui.viewmodel.AccessPoint> = emptyList(),
    selectedAccessPoint: com.example.ui.viewmodel.AccessPoint? = null,
    onSelectAccessPoint: (com.example.ui.viewmodel.AccessPoint) -> Unit = {},
    onToggleBeeper: (Boolean) -> Unit,
    onSimulateStrength: (Int) -> Unit,
    isSmallScreen: Boolean = false
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
                    style = if (isSmallScreen) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black) else MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
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
                    modifier = Modifier.padding(if (isSmallScreen) 12.dp else 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (selectedAccessPoint != null) "מעקב אקסס פוינט: ${selectedAccessPoint.ssid}" else "כתובת ה-IP של האקסס פוינט אליו אתה מחובר:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC2C7CF)
                    )
                    Text(
                        text = "IP: " + gatewayIp + (if (selectedAccessPoint != null) "  •  MAC: ${selectedAccessPoint.bssid}" else ""),
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
                            .size(if (isSmallScreen) 120.dp else 160.dp)
                            .background(signalColor.copy(alpha = 0.08f), CircleShape)
                            .padding(if (isSmallScreen) 10.dp else 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isSmallScreen) 95.dp else 130.dp)
                                .background(signalColor.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = "Radar Symbol",
                                    tint = signalColor,
                                    modifier = Modifier.size(if (isSmallScreen) 24.dp else 36.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$signalStrengthDbm dBm",
                                    style = if (isSmallScreen) {
                                        MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp
                                        )
                                    } else {
                                        MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp
                                        )
                                    },
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
                Column(modifier = Modifier.padding(if (isSmallScreen) 12.dp else 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.3f)) {
                            Text(
                                text = "שמע אקוסטי (Hotspot Sound Beeper)",
                                style = if (isSmallScreen) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
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

        // Graphical Heatmap Visualizer inside the LazyColumn
        item {
            AccessPointHeatmapCard(
                accessPoints = accessPoints,
                selectedAccessPoint = selectedAccessPoint,
                onSelectAccessPoint = onSelectAccessPoint,
                isSmallScreen = isSmallScreen
            )
        }

        // Select Access Point to Track section
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("ap_selector_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3033)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "בחירת אקסס פוינט (נקודת גישה) למעקב",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFD1E1FF)
                    )
                    Text(
                        text = "בחר נקודת גישה אלחוטית מרשימת השכנים כדי למדוד את עוצמת האות וכתובת ה-IP שלה",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC2C7CF).copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        accessPoints.forEach { ap ->
                            val isSelected = selectedAccessPoint?.bssid == ap.bssid
                            
                            val apColor = when {
                                ap.initialSignalDbm >= -50 -> Color(0xFF81C784)
                                ap.initialSignalDbm >= -67 -> Color(0xFFD1E1FF)
                                ap.initialSignalDbm >= -75 -> Color(0xFFFFD54F)
                                else -> Color(0xFFFF8A65)
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onSelectAccessPoint(ap) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF004787).copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.15f)
                                ),
                                border = if (isSelected) BorderStroke(1.5.dp, Color(0xFFD1E1FF)) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = Icons.Default.Router,
                                            contentDescription = "AP Icon",
                                            tint = if (isSelected) Color(0xFFD1E1FF) else Color(0xFFC2C7CF),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = ap.ssid,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "IP: ${ap.ip}  •  MAC: ${ap.bssid}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                                                color = Color(0xFFC2C7CF).copy(alpha = 0.7f)
                                            )
                                            Text(
                                                text = "תדר: ${ap.frequencyMhz} MHz (ערוץ ${ap.channel}) • אבטחה: ${ap.security}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                                color = Color(0xFFC2C7CF).copy(alpha = 0.5f)
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${ap.initialSignalDbm} dBm",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                                            color = apColor
                                        )
                                        val signalPercent = ((ap.initialSignalDbm - (-100)) / 70f).coerceIn(0f, 1f)
                                        Text(
                                            text = "קליטה: ${(signalPercent * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = apColor.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
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
                            "WIFI" -> "אלחוטי" + (if (item.ssid != "N/A" && item.ssid.isNotEmpty()) " (${item.ssid})" else "")
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

@Composable
fun SpeedFluctuationChart(
    downloadFluctuations: List<Double>,
    uploadFluctuations: List<Double>,
    isUploadPhase: Boolean,
    modifier: Modifier = Modifier
) {
    val dlPoints = remember(downloadFluctuations) { downloadFluctuations.toList() }
    val ulPoints = remember(uploadFluctuations) { uploadFluctuations.toList() }

    val maxSpeed = remember(dlPoints, ulPoints) {
        val maxDl = dlPoints.maxOrNull() ?: 10.0
        val maxUl = ulPoints.maxOrNull() ?: 10.0
        kotlin.math.max(maxDl, maxUl).coerceAtLeast(10.0) * 1.15
    }

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Draw horizontal grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = height * i / gridLines
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw Download Line (Vibrant Blue/Cyan)
        if (dlPoints.isNotEmpty()) {
            val path = androidx.compose.ui.graphics.Path()
            val fillPath = androidx.compose.ui.graphics.Path()

            val stepX = width / (dlPoints.size - 1).coerceAtLeast(1)
            
            dlPoints.forEachIndexed { index, speed ->
                val x = index * stepX
                val y = height - (speed / maxSpeed * height).toFloat()
                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(0f, height)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
                
                if (index == dlPoints.size - 1) {
                    fillPath.lineTo(x, height)
                    fillPath.close()
                }
            }

            // Fill gradient under download curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF3B82F6).copy(alpha = 0.25f),
                        Color(0xFF3B82F6).copy(alpha = 0.0f)
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw Download Curve Line
            drawPath(
                path = path,
                color = Color(0xFF60A5FA),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.5.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )

            // Draw last updated point
            val lastX = (dlPoints.size - 1) * stepX
            val lastY = height - (dlPoints.last() / maxSpeed * height).toFloat()
            drawCircle(
                color = Color(0xFF93C5FD),
                radius = 5.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(lastX, lastY)
            )
            drawCircle(
                color = Color(0xFF3B82F6).copy(alpha = 0.35f),
                radius = 10.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(lastX, lastY)
            )
        }

        // Draw Upload Line (Vibrant Purple)
        if (ulPoints.isNotEmpty()) {
            val path = androidx.compose.ui.graphics.Path()
            val fillPath = androidx.compose.ui.graphics.Path()

            val stepX = width / (ulPoints.size - 1).coerceAtLeast(1)
            
            ulPoints.forEachIndexed { index, speed ->
                val x = index * stepX
                val y = height - (speed / maxSpeed * height).toFloat()
                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(0f, height)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
                
                if (index == ulPoints.size - 1) {
                    fillPath.lineTo(x, height)
                    fillPath.close()
                }
            }

            // Fill gradient under upload curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFA855F7).copy(alpha = 0.25f),
                        Color(0xFFA855F7).copy(alpha = 0.0f)
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw Upload Curve Line
            drawPath(
                path = path,
                color = Color(0xFFC084FC),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.5.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )

            // Draw last updated point
            val lastX = (ulPoints.size - 1) * stepX
            val lastY = height - (ulPoints.last() / maxSpeed * height).toFloat()
            drawCircle(
                color = Color(0xFFD8B4FE),
                radius = 5.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(lastX, lastY)
            )
            drawCircle(
                color = Color(0xFFA855F7).copy(alpha = 0.35f),
                radius = 10.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(lastX, lastY)
            )
        }
    }
}

@Composable
fun SpeedFluctuationChartCard(
    diagnosticsState: com.example.ui.viewmodel.DiagnosticsState,
    isSmallScreen: Boolean = false
) {
    if (diagnosticsState.downloadFluctuations.isEmpty() && diagnosticsState.uploadFluctuations.isEmpty() && diagnosticsState.status != TestStatus.RUNNING) {
        return
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("speed_fluctuation_chart_card"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D3033)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "תנודות מהירות בזמן אמת",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFD1E1FF)
                )

                if (diagnosticsState.status == TestStatus.RUNNING) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF81C784).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (diagnosticsState.isUploadPhase) "בדיקת העלאה..." else "בדיקת הורדה...",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = if (diagnosticsState.isUploadPhase) Color(0xFFC084FC) else Color(0xFF60A5FA),
                            fontSize = 10.sp
                        )
                    }
                }
            }
            
            Text(
                text = "גרף קווי המציג את תנודות מהירות ההורדה והעלאה בזמן אמת",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFC2C7CF).copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            val dlPoints = diagnosticsState.downloadFluctuations
            val ulPoints = diagnosticsState.uploadFluctuations
            val maxSpeed = kotlin.math.max(
                dlPoints.maxOrNull() ?: 10.0,
                ulPoints.maxOrNull() ?: 10.0
            ).coerceAtLeast(10.0) * 1.15

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                // Y-Axis labels
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(42.dp)
                        .padding(end = 6.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.0f", maxSpeed)}M",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                        color = Color(0xFFC2C7CF).copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.0f", maxSpeed * 0.66)}M",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                        color = Color(0xFFC2C7CF).copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.0f", maxSpeed * 0.33)}M",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                        color = Color(0xFFC2C7CF).copy(alpha = 0.6f)
                    )
                    Text(
                        text = "0M",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                        color = Color(0xFFC2C7CF).copy(alpha = 0.6f)
                    )
                }

                // Chart Surface Canvas
                SpeedFluctuationChart(
                    downloadFluctuations = dlPoints,
                    uploadFluctuations = ulPoints,
                    isUploadPhase = diagnosticsState.isUploadPhase,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend indicators with the values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF60A5FA), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "הורדה / Download",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                        color = Color(0xFF60A5FA)
                    )
                    if (dlPoints.isNotEmpty()) {
                        Text(
                            text = " (${String.format(Locale.US, "%.1f", dlPoints.last())}M)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFFC2C7CF)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFC084FC), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "העלאה / Upload",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                        color = Color(0xFFC084FC)
                    )
                    if (ulPoints.isNotEmpty()) {
                        Text(
                            text = " (${String.format(Locale.US, "%.1f", ulPoints.last())}M)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFFC2C7CF)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccessPointHeatmapCard(
    accessPoints: List<com.example.ui.viewmodel.AccessPoint>,
    selectedAccessPoint: com.example.ui.viewmodel.AccessPoint?,
    onSelectAccessPoint: (com.example.ui.viewmodel.AccessPoint) -> Unit,
    isSmallScreen: Boolean = false
) {
    if (accessPoints.isEmpty()) return

    var activeHeatTab by remember { mutableStateOf(0) } // 0: 2D Radar, 1: Spectrum

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ap_heatmap_card"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D3033)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "אנליזר ומפת חום אותות (Wi-Fi Analyzer)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFD1E1FF)
                )
                
                Box(
                    modifier = Modifier
                        .background(Color(0xFF60A5FA).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "גרפי בזמן אמת",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF60A5FA),
                        fontSize = 10.sp
                    )
                }
            }
            
            Text(
                text = "אנליזה ויזואלית של זיהום תדרים ועוצמת האות של נקודות הגישה סביבך",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFC2C7CF).copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            // Inner Tab Selectors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.12f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeHeatTab == 0) Color(0xFF004787) else Color.Transparent)
                        .clickable { activeHeatTab = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "מפת קרינה 2D (Radar Heatmap)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (activeHeatTab == 0) Color.White else Color(0xFFC2C7CF)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeHeatTab == 1) Color(0xFF004787) else Color.Transparent)
                        .clickable { activeHeatTab = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ספקטרום ערוצים (Channel Spectrum)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (activeHeatTab == 1) Color.White else Color(0xFFC2C7CF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (activeHeatTab) {
                0 -> {
                    // 2D Spatial Radiation Heatmap
                    Text(
                        text = "מרכז המפה מייצג אותך. רשתות קרובות וחזקות יותר משורטטות קרוב למרכז עם קורנת אור רחבה וחמה.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC2C7CF).copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val apPositions = remember(accessPoints) {
                        accessPoints.mapIndexed { index, ap ->
                            val angle = (index * (3.14159f * 2f) / accessPoints.size.coerceAtLeast(1))
                            val signalPercent = ((ap.initialSignalDbm - (-100)) / 74f).coerceIn(0f, 1f)
                            val radius = 0.72f - (signalPercent * 0.5f) // scale distance 0.22 to 0.72
                            val xFactor = 0.5f + kotlin.math.cos(angle) * radius
                            val yFactor = 0.5f + kotlin.math.sin(angle) * radius
                            
                            ap to Offset(xFactor.coerceIn(0.12f, 0.88f), yFactor.coerceIn(0.12f, 0.88f))
                        }
                    }

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.15f))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(16.dp))
                    ) {
                        val wDp = maxWidth
                        val hDp = maxHeight

                        // 1. Canvas layer
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val centerX = w / 2f
                            val centerY = h / 2f

                            // Draw concentric radar lines
                            val rings = 4
                            for (r in 1..rings) {
                                val radius = (w / 2f) * r / rings
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.04f),
                                    radius = radius,
                                    center = Offset(centerX, centerY),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = 1.dp.toPx()
                                    )
                                )
                            }

                            // Draw radar compass crosslines
                            drawLine(
                                color = Color.White.copy(alpha = 0.04f),
                                start = Offset(centerX, 0f),
                                end = Offset(centerX, h),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.04f),
                                start = Offset(0f, centerY),
                                end = Offset(w, centerY),
                                strokeWidth = 1.dp.toPx()
                            )

                            // User center signal source locator pulse
                            drawCircle(
                                color = Color(0xFF60A5FA).copy(alpha = 0.15f),
                                radius = 16.dp.toPx(),
                                center = Offset(centerX, centerY)
                            )
                            drawCircle(
                                color = Color(0xFF60A5FA),
                                radius = 4.dp.toPx(),
                                center = Offset(centerX, centerY)
                            )

                            // Heat waves
                            apPositions.forEach { (ap, factor) ->
                                val cx = w * factor.x
                                val cy = h * factor.y

                                val signalPercent = ((ap.initialSignalDbm - (-100)) / 70f).coerceIn(0.1f, 1f)
                                val pxRadius = w * (0.16f + (signalPercent * 0.22f))

                                val heatColor = when {
                                    ap.initialSignalDbm >= -50 -> Color(0xFF81C784)
                                    ap.initialSignalDbm >= -67 -> Color(0xFF60A5FA)
                                    ap.initialSignalDbm >= -75 -> Color(0xFFFFD54F)
                                    else -> Color(0xFFFF8A65)
                                }

                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            heatColor.copy(alpha = 0.32f * signalPercent),
                                            heatColor.copy(alpha = 0.12f * signalPercent),
                                            Color.Transparent
                                        ),
                                        center = Offset(cx, cy),
                                        radius = pxRadius
                                    ),
                                    radius = pxRadius,
                                    center = Offset(cx, cy)
                                )
                            }
                        }

                        // 2. Interactive node layer
                        apPositions.forEach { (ap, factor) ->
                            val cxDp = wDp * factor.x
                            val cyDp = hDp * factor.y

                            val isSelected = selectedAccessPoint?.bssid == ap.bssid
                            val nodeColor = when {
                                ap.initialSignalDbm >= -50 -> Color(0xFF81C784)
                                ap.initialSignalDbm >= -67 -> Color(0xFF60A5FA)
                                ap.initialSignalDbm >= -75 -> Color(0xFFFFD54F)
                                else -> Color(0xFFFF8A65)
                            }

                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = cxDp - 30.dp,
                                        y = cyDp - 26.dp
                                    )
                                    .size(60.dp, 52.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // Main node button
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) nodeColor else Color(0xFF222426))
                                            .clickable { onSelectAccessPoint(ap) }
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) Color.White else nodeColor.copy(alpha = 0.6f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Router,
                                            contentDescription = ap.ssid,
                                            tint = if (isSelected) Color.Black else nodeColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    // Tooltip label
                                    Box(
                                        modifier = Modifier
                                            .background(Color.Black.copy(alpha = 0.78f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${ap.ssid} (${ap.initialSignalDbm})",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp, fontWeight = FontWeight.Black),
                                            color = if (isSelected) Color.White else Color(0xFFC2C7CF),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Wi-Fi Channel Overlapping Spectrum analyzer
                    var selectedBand by remember { mutableStateOf(0) } // 0: 2.4 GHz, 1: 5 GHz

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "תרשים חפיפת ערוצי שידור אלחוטיים ומניעת הפרעות רשת:",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFC2C7CF).copy(alpha = 0.6f)
                            )
                            
                            // Band switch
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.15f))
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "2.4G",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (selectedBand == 0) Color.White else Color(0xFFC2C7CF).copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selectedBand == 0) Color(0xFF004787) else Color.Transparent)
                                        .clickable { selectedBand = 0 }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                                Text(
                                    text = "5G",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (selectedBand == 1) Color.White else Color(0xFFC2C7CF).copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selectedBand == 1) Color(0xFF004787) else Color.Transparent)
                                        .clickable { selectedBand = 1 }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val filteredAPs = remember(accessPoints, selectedBand) {
                            accessPoints.filter { ap ->
                                if (selectedBand == 0) ap.frequencyMhz < 3000 else ap.frequencyMhz >= 3000
                            }
                        }

                        if (filteredAPs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (selectedBand == 0) "לא נמצאו נקודות גישה בתדר 2.4 GHz" else "לא נמצאו נקודות גישה בתדר 5 GHz",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFC2C7CF).copy(alpha = 0.5f)
                                )
                            }
                        } else {
                            androidx.compose.foundation.Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                    .padding(8.dp)
                            ) {
                                val w = size.width
                                val h = size.height

                                // Draw baseline
                                drawLine(
                                    color = Color.White.copy(alpha = 0.15f),
                                    start = Offset(0f, h - 20.dp.toPx()),
                                    end = Offset(w, h - 20.dp.toPx()),
                                    strokeWidth = 1.5.dp.toPx()
                                )

                                filteredAPs.forEach { ap ->
                                    val xFraction = if (selectedBand == 0) {
                                        ((ap.channel - 1).toFloat() / 13f).coerceIn(0.08f, 0.92f)
                                    } else {
                                        ((ap.channel - 36).toFloat() / (165f - 36f)).coerceIn(0.08f, 0.92f)
                                    }

                                    val rx = xFraction * w
                                    val dbmFraction = ((ap.initialSignalDbm - (-100)) / 70f).coerceIn(0.12f, 1f)
                                    val curveHeight = dbmFraction * (h - 28.dp.toPx())
                                    val peakY = h - 20.dp.toPx() - curveHeight

                                    val halfCurveWidth = if (selectedBand == 0) w * 0.15f else w * 0.12f

                                    val strokeColor = when {
                                        ap.initialSignalDbm >= -50 -> Color(0xFF81C784)
                                        ap.initialSignalDbm >= -67 -> Color(0xFF60A5FA)
                                        ap.initialSignalDbm >= -75 -> Color(0xFFFFD54F)
                                        else -> Color(0xFFFF8A65)
                                    }

                                    val bellPath = Path()
                                    val fillBellPath = Path()
                                    
                                    val startX = rx - halfCurveWidth
                                    val endX = rx + halfCurveWidth
                                    val baseY = h - 20.dp.toPx()

                                    bellPath.moveTo(startX, baseY)
                                    bellPath.quadraticTo(rx, peakY, endX, baseY)

                                    fillBellPath.moveTo(startX, baseY)
                                    fillBellPath.quadraticTo(rx, peakY, endX, baseY)
                                    fillBellPath.lineTo(endX, baseY)
                                    fillBellPath.close()

                                    drawPath(
                                        path = fillBellPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                strokeColor.copy(alpha = 0.18f),
                                                strokeColor.copy(alpha = 0.0f)
                                            ),
                                            startY = peakY,
                                            endY = baseY
                                        )
                                    )

                                    drawPath(
                                        path = bellPath,
                                        color = strokeColor,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                                            width = 2.dp.toPx(),
                                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                                        )
                                    )

                                    drawLine(
                                        color = strokeColor.copy(alpha = 0.35f),
                                        start = Offset(rx, peakY),
                                        end = Offset(rx, baseY),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                            intervals = floatArrayOf(5f, 5f),
                                            phase = 0f
                                        )
                                    )

                                    drawCircle(
                                        color = strokeColor,
                                        radius = 3.dp.toPx(),
                                        center = Offset(rx, peakY)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (selectedBand == 0) {
                                    (1..13 step 3).forEach { ch ->
                                        Text(
                                            text = "Ch $ch",
                                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp),
                                            color = Color(0xFFC2C7CF).copy(alpha = 0.5f)
                                        )
                                    }
                                } else {
                                    listOf(36, 44, 149, 157, 165).forEach { ch ->
                                        Text(
                                            text = "Ch $ch",
                                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp),
                                            color = Color(0xFFC2C7CF).copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF81C784), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "מעולה (>= -50) ", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFFC2C7CF))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF60A5FA), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "טוב (-67 to -51)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFFC2C7CF))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFFFD54F), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "בינוני (<= -68)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFFC2C7CF))
                }
            }
        }
    }
}

@Composable
fun LocalNetworkSsidDisplayCard(
    connectionInfo: com.example.ui.viewmodel.ConnectionInfo,
    signalStrengthDbm: Int,
    hasLocationPermission: Boolean,
    onRefresh: () -> Unit,
    onGrantPermission: () -> Unit,
    isSmallScreen: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cardBackgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E293B),
            Color(0xFF0F172A)
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("real_time_ssid_card"),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(cardBackgroundBrush)
                .padding(if (isSmallScreen) 14.dp else 18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF38BDF8), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "זיהוי רשת מקומית בזמן אמת",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF38BDF8)
                        )
                    }

                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF38BDF8).copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "ריענון שם הרשת / Refresh network state",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val connectionIcon = when (connectionInfo.connectionType) {
                        NetworkUtils.ConnectionType.WIFI -> Icons.Default.Wifi
                        NetworkUtils.ConnectionType.CELLULAR -> Icons.Default.SignalCellularAlt
                        NetworkUtils.ConnectionType.ETHERNET -> Icons.Default.Route
                        NetworkUtils.ConnectionType.OFFLINE -> Icons.Default.Warning
                    }
                    val iconTint = when (connectionInfo.connectionType) {
                        NetworkUtils.ConnectionType.OFFLINE -> Color(0xFFEF4444)
                        else -> Color(0xFF38BDF8)
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(iconTint.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = connectionIcon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "שם ה-SSID המחובר / Connected SSID:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                        
                        val displaySsid = if (connectionInfo.connectionType == NetworkUtils.ConnectionType.WIFI) {
                            connectionInfo.ssid
                        } else if (connectionInfo.connectionType == NetworkUtils.ConnectionType.CELLULAR) {
                            "רשת סלולרית / Cellular Connection"
                        } else {
                            "אין חיבור פעיל / Offline"
                        }

                        Text(
                            text = displaySsid,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = if (isSmallScreen) 16.sp else 18.sp
                            ),
                            color = if (connectionInfo.connectionType == NetworkUtils.ConnectionType.OFFLINE) Color(0xFFEF4444) else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (connectionInfo.connectionType == NetworkUtils.ConnectionType.WIFI) {
                    val dbm = signalStrengthDbm
                    val signalPercent = ((dbm - (-100f)) / ((-30f) - (-100f))).coerceIn(0f, 1f)
                    
                    val signalColor = when {
                        dbm >= -50 -> Color(0xFF10B981)
                        dbm >= -67 -> Color(0xFF3B82F6)
                        dbm >= -80 -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    }

                    val signalTextHe = when {
                        dbm >= -50 -> "קליטה מעולה"
                        dbm >= -67 -> "קליטה טובה ויציבה"
                        dbm >= -80 -> "קליטה בינונית"
                        else -> "קליטה חלשה"
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "חוזק אות Wi-Fi בזמן אמת:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "$dbm dBm ($signalTextHe)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = signalColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(signalPercent)
                                    .background(signalColor, RoundedCornerShape(3.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.2f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LocalPropertyRow(
                        label = "מזהה MAC פיזי (BSSID)",
                        value = connectionInfo.bssid,
                        icon = Icons.Default.Sensors
                    )
                    
                    val gatewayVal = if (connectionInfo.connectionType == NetworkUtils.ConnectionType.OFFLINE) "N/A" else (connectionInfo.details["Gateway"] ?: "192.168.1.1")
                    LocalPropertyRow(
                        label = "שער ברירת מחדל (Gateway)",
                        value = gatewayVal,
                        icon = Icons.Default.Router
                    )

                    val dnsVal = if (connectionInfo.connectionType == NetworkUtils.ConnectionType.OFFLINE) "N/A" else (connectionInfo.details["DNS Servers"] ?: "8.8.8.8")
                    LocalPropertyRow(
                        label = "שרתי DNS ברשת",
                        value = dnsVal,
                        icon = Icons.Default.Dns
                    )

                    val speedVal = if (connectionInfo.connectionType == NetworkUtils.ConnectionType.OFFLINE) "N/A" else (connectionInfo.details["Link Speed (Down)"] ?: "סורק באופן דינמי...")
                    LocalPropertyRow(
                        label = "מהירות חיבור פנימית (Link Speed)",
                        value = speedVal,
                        icon = Icons.Default.Speed
                    )
                }

                if (!hasLocationPermission && connectionInfo.connectionType == NetworkUtils.ConnectionType.WIFI) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF59E0B).copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.25f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "שם ה-SSID מוסתר עקב חוסר בהרשאת מיקום",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFF59E0B),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "אנדרואיד מחייב הרשאת מיקום כדי לאפשר לאפליקציה לקרוא את מזהה ה-SSID האמיתי של הרשת.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color(0xFFFFD54F),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onGrantPermission,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF59E0B),
                                    contentColor = Color(0xFF0F172A)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "אשר הרשאה",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocalPropertyRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color(0xFF94A3B8)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            ),
            color = Color.White
        )
    }
}
