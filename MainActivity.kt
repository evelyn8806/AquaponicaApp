package edu.uph.m24si2.aquaponica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.uph.m24si2.aquaponica.ui.theme.AQUAPONICATheme

val AppTeal = Color(0xFF0F8F86)
val AppTealDark = Color(0xFF08766F)
val AppGreen = Color(0xFF3DAA73)
val AppMint = Color(0xFFEAF8F3)
val AppBg = Color(0xFFF7FBF9)
val AppText = Color(0xFF183C38)
val AppMuted = Color(0xFF718985)
val AppLine = Color(0xFFDCEBE6)
val AppWarning = Color(0xFFE9A62B)
val AppDanger = Color(0xFFE66B62)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AQUAPONICATheme {
                AquaponicaApp()
            }
        }
    }
}

enum class Screen {
    LOGIN, SIGN_UP, MAIN
}

@Composable
fun AquaponicaApp() {
    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
    var userName by remember { mutableStateOf("User") }
    var userEmail by remember { mutableStateOf("user@email.com") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppBg
    ) {
        when (currentScreen) {
            Screen.LOGIN -> LoginScreen(
                onNavigateToSignUp = { currentScreen = Screen.SIGN_UP },
                onLoginSuccess = { emailOrUsername ->
                    if (emailOrUsername.isNotBlank()) {
                        userEmail = if (emailOrUsername.contains("@")) emailOrUsername else "$emailOrUsername@email.com"
                        userName = emailOrUsername.substringBefore("@").replaceFirstChar { it.uppercase() }
                    }
                    currentScreen = Screen.MAIN
                }
            )
            Screen.SIGN_UP -> SignUpScreen(
                onNavigateToLogin = { currentScreen = Screen.LOGIN },
                onSignUpSuccess = { name, email ->
                    if (name.isNotBlank()) userName = name
                    if (email.isNotBlank()) userEmail = email
                    currentScreen = Screen.MAIN
                }
            )
            Screen.MAIN -> MainShell(
                userName = userName,
                userEmail = userEmail,
                onLogout = { currentScreen = Screen.LOGIN }
            )
        }
    }
}

@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: (emailOrUsername: String) -> Unit
) {
    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 26.dp, vertical = 42.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Welcome Back!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = AppText
        )
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = "Monitor your aquaponic system anytime.",
            fontSize = 14.sp,
            color = AppMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = emailOrUsername,
            onValueChange = { emailOrUsername = it },
            label = { Text("Email or Username") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AppTeal) },
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = AppLine,
                focusedBorderColor = AppTeal,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AppTeal) },
            trailingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = AppLine,
                focusedBorderColor = AppTeal,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = { }) {
                Text("Forgot Password?", color = AppTeal)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onLoginSuccess(emailOrUsername) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppTeal)
        ) {
            Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account? ", color = AppMuted)
            TextButton(onClick = onNavigateToSignUp) {
                Text("Sign Up", color = AppTeal, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: (name: String, email: String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBg)
            )
        },
        containerColor = AppBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create Account",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppText
            )
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = "Start monitoring your aquaponic system.",
                fontSize = 14.sp,
                color = AppMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(25.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AppTeal) },
                singleLine = true,
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppLine,
                    focusedBorderColor = AppTeal,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AppTeal) },
                singleLine = true,
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppLine,
                    focusedBorderColor = AppTeal,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AppTeal) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppLine,
                    focusedBorderColor = AppTeal,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AppTeal) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppLine,
                    focusedBorderColor = AppTeal,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = { onSignUpSuccess(fullName, email) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppTeal)
            ) {
                Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already have an account? ", color = AppMuted)
                TextButton(onClick = onNavigateToLogin) {
                    Text("Log In", color = AppTeal, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MainShell(
    userName: String,
    userEmail: String,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showNotifications by remember { mutableStateOf(false) }

    if (showNotifications) {
        NotificationsScreen(onBack = { showNotifications = false })
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = (selectedTab == 0),
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppTeal,
                            selectedTextColor = AppTeal,
                            indicatorColor = AppMint
                        )
                    )
                    NavigationBarItem(
                        selected = (selectedTab == 1),
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Analytics, contentDescription = "Monitoring") },
                        label = { Text("Monitoring") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppTeal,
                            selectedTextColor = AppTeal,
                            indicatorColor = AppMint
                        )
                    )
                    NavigationBarItem(
                        selected = (selectedTab == 2),
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Tune, contentDescription = "Control") },
                        label = { Text("Control") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppTeal,
                            selectedTextColor = AppTeal,
                            indicatorColor = AppMint
                        )
                    )
                    NavigationBarItem(
                        selected = (selectedTab == 3),
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = "History") },
                        label = { Text("History") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppTeal,
                            selectedTextColor = AppTeal,
                            indicatorColor = AppMint
                        )
                    )
                    NavigationBarItem(
                        selected = (selectedTab == 4),
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppTeal,
                            selectedTextColor = AppTeal,
                            indicatorColor = AppMint
                        )
                    )
                }
            },
            containerColor = AppBg
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (selectedTab) {
                    0 -> DashboardScreen(userName = userName, onOpenNotifications = { showNotifications = true })
                    1 -> MonitoringScreen()
                    2 -> ControlScreen()
                    3 -> HistoryScreen()
                    4 -> ProfileScreen(userName = userName, userEmail = userEmail, onLogout = onLogout)
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(userName: String, onOpenNotifications: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hello, $userName!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Your aquaponic system is running well.",
                    fontSize = 12.sp,
                    color = AppMuted
                )
            }
            IconButton(onClick = onOpenNotifications) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = AppText)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // System Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = AppTeal)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text("System Status", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Text("Normal", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // AquaHome Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = AppMint)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("AquaHome", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppText)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Your smart aquaponic ecosystem.", fontSize = 13.sp, color = AppMuted)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "60 cm Aquarium • Tilapia + Kangkung",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTealDark
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Water Overview", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AppText)
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(icon = Icons.Outlined.WaterDrop, title = "Water Level", value = "82%", status = "Normal")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(icon = Icons.Outlined.Waves, title = "Turbidity", value = "35 NTU", status = "Good")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(icon = Icons.Outlined.Thermostat, title = "Temperature", value = "27.4°C", status = "Normal")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(icon = Icons.Outlined.Science, title = "pH", value = "7.1", status = "Normal")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("System", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AppText)
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                SystemTile(icon = Icons.Default.Water, title = "Pump", value = "ON")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                SystemTile(icon = Icons.Default.Air, title = "Aerator", value = "ON")
            }
        }
    }
}

@Composable
fun MetricCard(icon: ImageVector, title: String, value: String, status: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AppLine, AppLine)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = AppTeal, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, fontSize = 11.sp, color = AppMuted)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = AppText)
            Spacer(modifier = Modifier.height(4.dp))
            Text(status, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AppGreen)
        }
    }
}

@Composable
fun SystemTile(icon: ImageVector, title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AppLine, AppLine)))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AppMint),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AppTeal, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontSize = 11.sp, color = AppMuted)
                Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppGreen)
            }
        }
    }
}

@Composable
fun MonitoringScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Monitoring", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppText)
        Spacer(modifier = Modifier.height(3.dp))
        Text("Real-time condition of your aquaponic system.", fontSize = 12.sp, color = AppMuted)

        Spacer(modifier = Modifier.height(18.dp))

        SectionCard(title = "Water Quality", icon = Icons.Outlined.WaterDrop) {
            DetailRow(label = "Water Level", value = "82%", status = "Normal")
            DetailRow(label = "Turbidity", value = "35 NTU", status = "Good")
            DetailRow(label = "Temperature", value = "27.4°C", status = "Normal")
            DetailRow(label = "pH", value = "7.1", status = "Normal")
        }

        Spacer(modifier = Modifier.height(14.dp))

        SectionCard(title = "Plant — Kangkung", icon = Icons.Outlined.Eco) {
            DetailRow(label = "Plant Condition", value = "Healthy", status = "Good")
            DetailRow(label = "Water Circulation", value = "Active", status = "Normal")
        }

        Spacer(modifier = Modifier.height(14.dp))

        SectionCard(title = "Fish — Tilapia (Nila)", icon = Icons.Outlined.SetMeal) {
            DetailRow(label = "Fish Condition", value = "Healthy", status = "Good")
            DetailRow(label = "Feeding", value = "12:00 PM", status = "Scheduled")
            DetailRow(label = "Aerator", value = "ON", status = "Active")
        }
    }
}

@Composable
fun SectionCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AppLine, AppLine)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AppMint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = AppTeal, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, fontWeight = FontWeight.Bold, color = AppText, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, status: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = AppMuted, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text(value, fontWeight = FontWeight.Bold, color = AppText, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppMint)
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Text(status, color = AppGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        HorizontalDivider(color = AppLine)
    }
}

@Composable
fun ControlScreen() {
    var pumpOn by remember { mutableStateOf(true) }
    var aeratorOn by remember { mutableStateOf(true) }
    var pumpAuto by remember { mutableStateOf(true) }
    var aeratorAuto by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Control", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppText)
        Spacer(modifier = Modifier.height(3.dp))
        Text("Manage your aquaponic devices.", fontSize = 12.sp, color = AppMuted)

        Spacer(modifier = Modifier.height(18.dp))

        DeviceControlCard(
            title = "Water Pump",
            icon = Icons.Default.Water,
            isOn = pumpOn,
            isAuto = pumpAuto,
            onToggleOn = { pumpOn = it },
            onToggleAuto = { pumpAuto = it }
        )

        Spacer(modifier = Modifier.height(14.dp))

        DeviceControlCard(
            title = "Aerator",
            icon = Icons.Default.Air,
            isOn = aeratorOn,
            isAuto = aeratorAuto,
            onToggleOn = { aeratorOn = it },
            onToggleAuto = { aeratorAuto = it }
        )

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AppLine, AppLine)))
        ) {
            Column(modifier = Modifier.padding(17.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = AppTeal)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Fish Feeder", fontWeight = FontWeight.Bold, color = AppText, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Next feeding", color = AppMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("12:00 PM", fontWeight = FontWeight.Bold, color = AppText, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTeal)
                ) {
                    Text("Feed Now", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DeviceControlCard(
    title: String,
    icon: ImageVector,
    isOn: Boolean,
    isAuto: Boolean,
    onToggleOn: (Boolean) -> Unit,
    onToggleAuto: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AppLine, AppLine)))
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AppMint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = AppTeal)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, fontWeight = FontWeight.Bold, color = AppText, modifier = Modifier.weight(1f), fontSize = 15.sp)
                Switch(
                    checked = isOn,
                    onCheckedChange = onToggleOn,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AppTeal)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Mode", color = AppMuted, fontSize = 12.sp)
                Spacer(modifier = Modifier.weight(1f))
                FilterChip(
                    selected = isAuto,
                    onClick = { onToggleAuto(true) },
                    label = { Text("Automatic") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppTeal,
                        selectedLabelColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = !isAuto,
                    onClick = { onToggleAuto(false) },
                    label = { Text("Manual") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppTeal,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun HistoryScreen() {
    var selectedRange by remember { mutableStateOf("24H") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("History", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppText)
        Spacer(modifier = Modifier.height(3.dp))
        Text("Review sensor data over time.", fontSize = 12.sp, color = AppMuted)

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("1H", "6H", "24H", "7D").forEach { time ->
                val isSelected = (selectedRange == time)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 3.dp)
                        .height(38.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) AppTeal else Color.White)
                        .border(1.dp, if (isSelected) AppTeal else AppLine, RoundedCornerShape(14.dp))
                        .clickable { selectedRange = time },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        time,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else AppMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ChartCard(title = "Water Temperature", value = "27.4°C", icon = Icons.Outlined.Thermostat)
        Spacer(modifier = Modifier.height(14.dp))
        ChartCard(title = "Water Level", value = "82%", icon = Icons.Outlined.WaterDrop)
        Spacer(modifier = Modifier.height(14.dp))
        ChartCard(title = "Turbidity", value = "35 NTU", icon = Icons.Outlined.Waves)
    }
}

@Composable
fun ChartCard(title: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AppLine, AppLine)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = AppTeal, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.SemiBold, color = AppText, fontSize = 14.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text(value, fontWeight = FontWeight.ExtraBold, color = AppText, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(95.dp)) {
                for (i in 1..3) {
                    val y = size.height * i / 4f
                    drawLine(
                        color = AppLine,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                val path = Path()
                val points = listOf(0.0f, 0.18f, 0.12f, 0.35f, 0.28f, 0.52f, 0.43f, 0.58f, 0.50f, 0.72f, 0.64f, 0.82f, 0.76f, 0.90f)
                for (i in points.indices) {
                    val x = size.width * i / (points.size - 1)
                    val y = size.height * (0.18f + points[i] * 0.58f)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = AppTeal,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold, color = AppText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBg)
            )
        },
        containerColor = AppBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            NotificationItem(
                icon = Icons.Default.CheckCircle,
                title = "Feeding Completed",
                detail = "Fish feeding completed at 12:00 PM.",
                time = "12:00",
                isGood = true
            )
            NotificationItem(
                icon = Icons.Default.Warning,
                title = "Water Level Low",
                detail = "Water level is below the configured threshold.",
                time = "09:42",
                isGood = false
            )
            NotificationItem(
                icon = Icons.Default.Warning,
                title = "High Turbidity",
                detail = "Water turbidity is higher than the recommended level.",
                time = "08:15",
                isGood = false
            )
            NotificationItem(
                icon = Icons.Default.CheckCircle,
                title = "System Normal",
                detail = "All monitored parameters are within range.",
                time = "07:30",
                isGood = true
            )
        }
    }
}

@Composable
fun NotificationItem(icon: ImageVector, title: String, detail: String, time: String, isGood: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AppLine, AppLine)))
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isGood) AppGreen else AppWarning,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Bold, color = AppText, modifier = Modifier.weight(1f), fontSize = 14.sp)
                    Text(time, fontSize = 10.sp, color = AppMuted)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(detail, fontSize = 12.sp, color = AppMuted)
            }
        }
    }
}

@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = AppTeal)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(55.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = AppTeal, modifier = Modifier.size(30.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(userName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(userEmail, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsItem(icon = Icons.Default.Water, title = "My Aquarium", value = "AquaHome")
        SettingsItem(icon = Icons.Outlined.Restaurant, title = "Feeding Schedule", value = "12:00 PM")
        SettingsItem(icon = Icons.Outlined.WaterDrop, title = "Pump Schedule", value = "Automatic")
        SettingsItem(icon = Icons.Default.Air, title = "Aerator Schedule", value = "Automatic")
        SettingsItem(icon = Icons.Outlined.Notifications, title = "Notification Settings")
        SettingsItem(icon = Icons.Default.Tune, title = "Sensor Threshold")
        SettingsItem(icon = Icons.Outlined.Info, title = "About AQUAPONICA")

        Spacer(modifier = Modifier.height(14.dp))

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = AppDanger)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out", color = AppDanger, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, value: String? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 9.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AppLine, AppLine)))
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = AppTeal, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.SemiBold, color = AppText, fontSize = 13.sp, modifier = Modifier.weight(1f))
            if (value != null) {
                Text(value, color = AppMuted, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    AQUAPONICATheme {
        LoginScreen(onNavigateToSignUp = {}, onLoginSuccess = {})
    }
}

@Preview(showBackground = true)
@Composable
fun MainShellPreview() {
    AQUAPONICATheme {
        MainShell(userName = "User", userEmail = "user@email.com", onLogout = {})
    }
}
