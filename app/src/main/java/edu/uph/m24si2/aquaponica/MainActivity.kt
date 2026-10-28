package edu.uph.m24si2.aquaponica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import edu.uph.m24si2.aquaponica.ui.theme.AQUAPONICATheme

private val AppTeal = Color(0xFF0F8F86)
private val AppTealDark = Color(0xFF08766F)
private val AppGreen = Color(0xFF3DAA73)
private val AppMint = Color(0xFFEAF8F3)
private val AppBg = Color(0xFFF7FBF9)
private val AppText = Color(0xFF183C38)
private val AppMuted = Color(0xFF718985)
private val AppLine = Color(0xFFDCEBE6)
private val AppWarning = Color(0xFFE9A62B)
private val AppDanger = Color(0xFFE66B62)

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

private fun registerUser(
    name: String,
    email: String,
    password: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    auth.createUserWithEmailAndPassword(
        email.trim(),
        password
    ).addOnSuccessListener { result ->

        val uid = result.user?.uid

        if (uid == null) {
            onError("User ID tidak ditemukan")
            return@addOnSuccessListener
        }

        val userData = hashMapOf(
            "uid" to uid,
            "name" to name.trim(),
            "email" to email.trim(),
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("users")
            .document(uid)
            .set(userData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { error ->
                onError(
                    "Akun dibuat, tetapi profil gagal disimpan: ${error.localizedMessage}"
                )
            }

    }.addOnFailureListener { error ->
        onError(error.localizedMessage ?: "Registrasi gagal")
    }
}

private fun loginUser(
    email: String,
    password: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    FirebaseAuth.getInstance()
        .signInWithEmailAndPassword(email.trim(), password)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { error ->
            onError(error.localizedMessage ?: "Login gagal")
        }
}

private enum class Screen { LOGIN, SIGN_UP, AQUARIUM_SETUP, MAIN }

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
                onNavigateToSignUp = {
                    currentScreen = Screen.SIGN_UP
                },
                onLoginSuccess = { input ->
                    if (input.isNotBlank()) {
                        userEmail = if (input.contains("@")) {
                            input
                        } else {
                            "$input@email.com"
                        }
                        userName = input.substringBefore("@")
                            .replaceFirstChar { it.uppercase() }
                    }
                    currentScreen = Screen.MAIN
                }
            )

            Screen.SIGN_UP -> SignUpScreen(
                onNavigateToLogin = {
                    currentScreen = Screen.LOGIN
                },
                onSignUpSuccess = { name, email ->
                    if (name.isNotBlank()) userName = name
                    if (email.isNotBlank()) userEmail = email

                    currentScreen = Screen.AQUARIUM_SETUP
                }

            )

            Screen.AQUARIUM_SETUP -> {
                AquariumSetupScreen(
                    onSetupComplete = {
                        currentScreen = Screen.MAIN
                    }
                )
            }

            Screen.MAIN -> MainShell(
                userName = userName,
                userEmail = userEmail,
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    currentScreen = Screen.LOGIN
                }
            )
        }
    }
}

/* Firebase Firestore connection test */
private fun testFirestoreConnection(
    onResult: (String) -> Unit
) {
    val testData = hashMapOf(
        "status" to "connected",
        "app" to "AQUAPONICA",
        "message" to "Firebase connection test",
        "timestamp" to System.currentTimeMillis()
    )

    FirebaseFirestore.getInstance()
        .collection("connection_test")
        .document("test_001")
        .set(testData)
        .addOnSuccessListener {
            onResult("Berhasil terhubung ke Firebase!")
        }
        .addOnFailureListener { error ->
            onResult(
                "Koneksi gagal: ${
                    error.localizedMessage ?: "Error tidak diketahui"
                }"
            )
        }
}

@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

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
            leadingIcon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = AppTeal
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = AppTeal
                )
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { }) {
                Text("Forgot Password?", color = AppTeal)
            }
        }

        Button(
            onClick = {
                if (emailOrUsername.isBlank() || password.isBlank()) {
                    errorMessage = "Please enter your email and password"
                } else if (!isLoading) {
                    isLoading = true
                    errorMessage = ""

                    loginUser(
                        email = emailOrUsername,
                        password = password,
                        onSuccess = {
                            isLoading = false
                            onLoginSuccess(emailOrUsername.trim())
                        },
                        onError = { message ->
                            isLoading = false
                            errorMessage = message
                        }
                    )
                }
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTeal
            )
        ) {
            Text(
                "Log In",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        if (errorMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = errorMessage,
                color = AppDanger,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(22.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Don't have an account? ",
                color = AppMuted
            )
            TextButton(onClick = onNavigateToSignUp) {
                Text(
                    "Sign Up",
                    color = AppTeal,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: (String, String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Account",
                        color = AppText,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AppText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBg
                )
            )
        },
        containerColor = AppBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Start monitoring your aquaponic system.",
                color = AppMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = {
                    when {
                        fullName.isBlank() ||
                                email.isBlank() ||
                                password.isBlank() ||
                                confirmPassword.isBlank() -> {
                            errorMessage = "Please fill in all fields"
                        }

                        password.length < 6 -> {
                            errorMessage = "Password must be at least 6 characters"
                        }

                        password != confirmPassword -> {
                            errorMessage = "Passwords do not match"
                        }

                        !isLoading -> {
                            isLoading = true
                            errorMessage = ""

                            registerUser(
                                name = fullName,
                                email = email,
                                password = password,
                                onSuccess = {
                                    isLoading = false
                                    onSignUpSuccess(fullName.trim(), email.trim())
                                },
                                onError = { message ->
                                    isLoading = false
                                    errorMessage = message
                                }
                            )
                        }
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTeal
                )
            ) {
                Text(
                    "Sign Up",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(onClick = onNavigateToLogin) {
                Text(
                    "Already have an account? Log In",
                    color = AppTeal
                )
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
    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    var showNotifications by remember {
        mutableStateOf(false)
    }

    var showAquariumDetail by remember {
        mutableStateOf(false)
    }

    var showAquariumEdit by remember {
        mutableStateOf(false)
    }

    var selectedAquarium by remember {
        mutableStateOf<Aquarium?>(null)
    }
    var dashboardRefreshKey by remember { mutableIntStateOf(0) }
    var showAquariumSetup by remember { mutableStateOf(false) }

    if (showNotifications) {
        NotificationsScreen(
            onBack = {
                showNotifications = false
            }
        )

    } else if (showAquariumEdit && selectedAquarium != null) {

        AquariumEditScreen(
            aquarium = selectedAquarium!!,
            onBack = {
                showAquariumEdit = false
                showAquariumDetail = true
            },
            onSaveSuccess = {
                dashboardRefreshKey++
                showAquariumEdit = false
                showAquariumDetail = true
            }
        )

    } else if (showAquariumDetail) {

        AquariumDetailScreen(
            onBack = {
                showAquariumDetail = false
            },
            onEdit = { aquarium ->
                selectedAquarium = aquarium
                showAquariumDetail = false
                showAquariumEdit = true
            },
            onDeleteSuccess = {
                dashboardRefreshKey++
                showAquariumDetail = false
                selectedTab = 0
            }
        )

    } else {

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White
                ) {
                    val items = listOf(
                        "Home",
                        "Monitoring",
                        "Control",
                        "History",
                        "Profile"
                    )

                    val icons = listOf(
                        Icons.Default.Home,
                        Icons.Default.Analytics,
                        Icons.Default.Tune,
                        Icons.AutoMirrored.Filled.ShowChart,
                        Icons.Default.Person
                    )

                    items.forEachIndexed { index, label ->

                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                            },
                            icon = {
                                Icon(
                                    imageVector = icons[index],
                                    contentDescription = label
                                )
                            },
                            label = {
                                Text(label)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AppTeal,
                                selectedTextColor = AppTeal,
                                indicatorColor = AppMint
                            )
                        )
                    }
                }
            },
            containerColor = AppBg
        ) { padding ->

            Box(
                modifier = Modifier.padding(padding)
            ) {
                when (selectedTab) {

                    0 -> DashboardScreen(
                        userName = userName,
                        onOpenNotifications = {
                            showNotifications = true
                        },
                        refreshKey = dashboardRefreshKey
                    )

                    1 -> MonitoringScreen()

                    2 -> ControlScreen()

                    3 -> HistoryScreen()

                    4 -> ProfileScreen(
                        userName = userName,
                        userEmail = userEmail,
                        onLogout = onLogout,
                        onMyAquariumClick = {
                            showAquariumDetail = true
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun DashboardScreen(
    userName: String,
    onOpenNotifications: () -> Unit,
    refreshKey: Int = 0
) {
    var aquarium by remember {
        mutableStateOf<Aquarium?>(null)
    }

    var aquariumError by remember {
        mutableStateOf("")
    }

    var isAquariumLoading by remember {
        mutableStateOf(true)
    }

    val repository = remember {
        AquariumRepository()
    }

    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(refreshKey, currentUid) {
        aquarium = null
        aquariumError = ""
        isAquariumLoading = true

        if (currentUid == null) {
            aquariumError = "Silakan login terlebih dahulu."
            isAquariumLoading = false
        } else {
            repository.getAquarium(
                onSuccess = { result ->
                    aquarium = result
                    isAquariumLoading = false
                },
                onError = { error ->
                    aquariumError = error
                    isAquariumLoading = false
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Hello, $userName!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppText
                )
                Text(
                    "Your aquaponic system is running well.",
                    fontSize = 12.sp,
                    color = AppMuted
                )
            }

            IconButton(onClick = onOpenNotifications) {
                Icon(
                    Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = AppText
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppTeal
            )
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        "System Status",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    Text(
                        "Normal",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppMint
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = aquarium?.name ?: "My Aquarium",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Your smart aquaponic ecosystem.",
                    fontSize = 13.sp,
                    color = AppMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = when {
                        isAquariumLoading -> "Loading aquarium data..."

                        aquariumError.isNotEmpty() ->
                            "Gagal mengambil data: $aquariumError"

                        aquarium != null ->
                            "${aquarium!!.lengthCm.toInt()} cm Aquarium • " +
                                    "${aquarium!!.fishType} + ${aquarium!!.plantType}"

                        else -> "Aquarium belum dikonfigurasi"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTealDark
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Water Overview",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = AppText
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(
                    Icons.Outlined.WaterDrop,
                    "Water Level",
                    "82%",
                    "Normal"
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(
                    Icons.Outlined.Waves,
                    "Turbidity",
                    "35 NTU",
                    "Good"
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(
                    Icons.Outlined.Thermostat,
                    "Temperature",
                    "27.4°C",
                    "Normal"
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(
                    Icons.Outlined.Science,
                    "pH",
                    "7.1",
                    "Normal"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "System",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = AppText
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                SystemTile(Icons.Default.Water, "Pump", "ON")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                SystemTile(Icons.Default.Air, "Aerator", "ON")
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            "Firebase Connection",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = AppText
        )
        Spacer(modifier = Modifier.height(10.dp))

        FirebaseConnectionTestButton()
    }
}

@Composable
fun FirebaseConnectionTestButton() {
    var resultMessage by remember {
        mutableStateOf("Belum melakukan tes koneksi")
    }
    var isLoading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = {
                isLoading = true
                resultMessage = "Sedang menghubungkan ke Firebase..."

                testFirestoreConnection { result ->
                    resultMessage = result
                    isLoading = false
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTeal
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                if (isLoading) "Menghubungkan..."
                else "Tes Koneksi Firebase",
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            resultMessage,
            color = if (resultMessage.startsWith("Berhasil")) {
                AppGreen
            } else {
                AppMuted
            },
            fontSize = 12.sp
        )
    }
}

@Composable
fun MetricCard(
    icon: ImageVector,
    title: String,
    value: String,
    status: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = AppTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, fontSize = 11.sp, color = AppMuted)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                status,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppGreen
            )
        }
    }
}

@Composable
fun SystemTile(
    icon: ImageVector,
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = CardDefaults.outlinedCardBorder()
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
                Icon(
                    icon,
                    contentDescription = null,
                    tint = AppTeal,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(title, fontSize = 11.sp, color = AppMuted)
                Text(
                    value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppGreen
                )
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
        Text(
            "Monitoring",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppText
        )
        Text(
            "Real-time condition of your aquaponic system.",
            fontSize = 12.sp,
            color = AppMuted
        )

        Spacer(modifier = Modifier.height(18.dp))

        SectionCard("Water Quality", Icons.Outlined.WaterDrop) {
            DetailRow("Water Level", "82%", "Normal")
            DetailRow("Turbidity", "35 NTU", "Good")
            DetailRow("Temperature", "27.4°C", "Normal")
            DetailRow("pH", "7.1", "Normal")
        }

        Spacer(modifier = Modifier.height(14.dp))

        SectionCard("Plant — Kangkung", Icons.Outlined.Eco) {
            DetailRow("Plant Condition", "Healthy", "Good")
            DetailRow("Water Circulation", "Active", "Normal")
        }

        Spacer(modifier = Modifier.height(14.dp))

        SectionCard("Fish — Tilapia (Nila)", Icons.Outlined.SetMeal) {
            DetailRow("Fish Condition", "Healthy", "Good")
            DetailRow("Feeding", "12:00 PM", "Scheduled")
            DetailRow("Aerator", "ON", "Active")
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = AppTeal,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    color = AppText,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    status: String
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                color = AppMuted,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                value,
                fontWeight = FontWeight.Bold,
                color = AppText,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                status,
                color = AppGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
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
        Text(
            "Control",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppText
        )
        Text(
            "Manage your aquaponic devices.",
            fontSize = 12.sp,
            color = AppMuted
        )

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

        SectionCard("Fish Feeder", Icons.Outlined.Restaurant) {
            DetailRow("Next feeding", "12:00 PM", "Scheduled")

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTeal
                )
            ) {
                Text("Feed Now", color = Color.White)
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
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = AppTeal
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    color = AppText,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isOn,
                    onCheckedChange = onToggleOn
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Mode", color = AppMuted)
                Spacer(modifier = Modifier.weight(1f))

                FilterChip(
                    selected = isAuto,
                    onClick = { onToggleAuto(true) },
                    label = { Text("Automatic") }
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilterChip(
                    selected = !isAuto,
                    onClick = { onToggleAuto(false) },
                    label = { Text("Manual") }
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
        Text(
            "History",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppText
        )
        Text(
            "Review sensor data over time.",
            fontSize = 12.sp,
            color = AppMuted
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("1H", "6H", "24H", "7D").forEach { range ->
                FilterChip(
                    selected = selectedRange == range,
                    onClick = { selectedRange = range },
                    label = { Text(range) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ChartCard(
            "Water Temperature",
            "27.4°C",
            Icons.Outlined.Thermostat
        )

        Spacer(modifier = Modifier.height(14.dp))

        ChartCard(
            "Water Level",
            "82%",
            Icons.Outlined.WaterDrop
        )

        Spacer(modifier = Modifier.height(14.dp))

        ChartCard(
            "Turbidity",
            "35 NTU",
            Icons.Outlined.Waves
        )
    }
}

@Composable
fun ChartCard(
    title: String,
    value: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = AppTeal,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    fontWeight = FontWeight.SemiBold,
                    color = AppText,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    value,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppText
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
            ) {
                for (i in 1..3) {
                    val y = size.height * i / 4f
                    drawLine(
                        color = AppLine,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val values = listOf(
                    0.18f, 0.35f, 0.27f, 0.50f, 0.42f,
                    0.65f, 0.57f, 0.82f, 0.72f, 0.90f
                )

                val path = Path()

                values.forEachIndexed { index, point ->
                    val x = size.width * index / (values.size - 1)
                    val y = size.height * point

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
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
                title = {
                    Text(
                        "Notifications",
                        color = AppText,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AppText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBg
                )
            )
        },
        containerColor = AppBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            NotificationItem(
                Icons.Default.CheckCircle,
                "Feeding Completed",
                "Fish feeding completed at 12:00 PM.",
                "12:00",
                true
            )

            NotificationItem(
                Icons.Default.Warning,
                "Water Level Low",
                "Water level is below the configured threshold.",
                "09:42",
                false
            )

            NotificationItem(
                Icons.Default.Warning,
                "High Turbidity",
                "Water turbidity is higher than recommended.",
                "08:15",
                false
            )

            NotificationItem(
                Icons.Default.CheckCircle,
                "System Normal",
                "All monitored parameters are within range.",
                "07:30",
                true
            )
        }
    }
}

@Composable
fun NotificationItem(
    icon: ImageVector,
    title: String,
    detail: String,
    time: String,
    isGood: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = CardDefaults.outlinedCardBorder()
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
                    Text(
                        title,
                        fontWeight = FontWeight.Bold,
                        color = AppText,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        time,
                        fontSize = 10.sp,
                        color = AppMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    detail,
                    fontSize = 12.sp,
                    color = AppMuted
                )
            }
        }
    }
}

@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String,
    onLogout: () -> Unit,
    onMyAquariumClick: () -> Unit
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
            colors = CardDefaults.cardColors(
                containerColor = AppTeal
            )
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
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = AppTeal,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        userName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        userEmail,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.clickable {
                onMyAquariumClick()
            }
        ) {
            SettingsItem(
                Icons.Default.Water,
                title = "My Aquarium",
                value = "View Details"
            )
        }

        SettingsItem(
            Icons.Outlined.Restaurant,
            "Feeding Schedule",
            "12:00 PM"
        )
        SettingsItem(
            Icons.Outlined.WaterDrop,
            "Pump Schedule",
            "Automatic"
        )
        SettingsItem(
            Icons.Default.Air,
            "Aerator Schedule",
            "Automatic"
        )
        SettingsItem(
            Icons.Outlined.Notifications,
            "Notification Settings"
        )
        SettingsItem(
            Icons.Default.Tune,
            "Sensor Threshold"
        )
        SettingsItem(
            Icons.Outlined.Info,
            "About AQUAPONICA"
        )

        Spacer(modifier = Modifier.height(14.dp))

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = AppDanger
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Log Out",
                color = AppDanger,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    value: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 9.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = AppTeal,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                color = AppText,
                modifier = Modifier.weight(1f)
            )

            if (value != null) {
                Text(
                    value,
                    color = AppMuted,
                    fontSize = 12.sp
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AppMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}