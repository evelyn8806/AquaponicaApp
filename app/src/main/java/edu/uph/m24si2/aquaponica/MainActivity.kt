package edu.uph.m24si2.aquaponica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.uph.m24si2.aquaponica.ui.theme.AQUAPONICATheme
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material.icons.filled.Info

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

private val AquaPoppins = FontFamily(
    Font(R.font.poppins_light, FontWeight.Light),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

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

    val formattedEmail = if (email.contains("@")) email.trim() else "${email.trim()}@email.com"

    auth.createUserWithEmailAndPassword(
        formattedEmail,
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
            "email" to formattedEmail,
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("users")
            .document(uid)
            .set(userData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onSuccess()
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
    val formattedEmail = if (email.contains("@")) email.trim() else "${email.trim()}@email.com"
    FirebaseAuth.getInstance()
        .signInWithEmailAndPassword(formattedEmail, password)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { error ->
            onError(error.localizedMessage ?: "Login gagal")
        }
}

private enum class Screen { WELCOME, LOGIN, SIGN_UP, AQUARIUM_SETUP, MAIN }

@Composable
fun AquaponicaApp() {
    var currentScreen by remember { mutableStateOf(Screen.WELCOME) }
    var userName by remember { mutableStateOf("User") }
    var userEmail by remember { mutableStateOf("user@email.com") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppBg
    ) {
            when (currentScreen) {

                Screen.WELCOME -> WelcomeScreen(
                    onGetStarted = {
                        currentScreen = Screen.SIGN_UP
                    },
                    onLogin = {
                        currentScreen = Screen.LOGIN
                    }
                )
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
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTeal
            )
        ) {
            Text(
                if (isLoading) "Logging In..." else "Log In",
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

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val aquaDark = Color(0xFF164A43)
    val aquaMuted = Color(0xFF718985)
    val aquaBackground = Color(0xFFF5FAF7)


    Scaffold(
        containerColor = aquaBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Account",
                        color = aquaDark,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = aquaDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = aquaBackground
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "YOUR LITTLE ECOSYSTEM",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTeal,
                letterSpacing = 1.5.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Let's Grow Together.",
                fontFamily = AquaPoppins,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = aquaDark,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp
            )



            Spacer(Modifier.height(6.dp))

            Text(
                text = "Create your account and bring your\n" +
                        "aquaponic world to life.",
                fontFamily = AquaPoppins,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = aquaMuted,
                textAlign = TextAlign.Center,
                lineHeight = 21.sp
            )


            Spacer(Modifier.height(18.dp))

            BotanicalAquaSignUpIllustration(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
            )

            Spacer(Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(
                        1.dp,
                        Color(0xFFE3EFE9),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = {
                        Text(
                            "Full Name",
                            fontFamily = AquaPoppins,
                            fontSize = 12.sp
                        )
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = AquaPoppins,
                        fontSize = 14.sp,
                        color = AppText
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = botanicalAquaFieldColors()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = AquaPoppins,
                        fontSize = 14.sp,
                        color = AppText
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = botanicalAquaFieldColors()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation =
                        if (showPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                showPassword = !showPassword
                            }
                        ) {
                            Icon(
                                imageVector =
                                    if (showPassword)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                contentDescription =
                                    if (showPassword)
                                        "Hide password"
                                    else
                                        "Show password"
                            )
                        }
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = AquaPoppins,
                        fontSize = 14.sp,
                        color = AppText
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = botanicalAquaFieldColors()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                    },
                    label = { Text("Confirm Password") },
                    visualTransformation =
                        if (showConfirmPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                showConfirmPassword =
                                    !showConfirmPassword
                            }
                        ) {
                            Icon(
                                imageVector =
                                    if (showConfirmPassword)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                contentDescription =
                                    if (showConfirmPassword)
                                        "Hide password"
                                    else
                                        "Show password"
                            )
                        }
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = AquaPoppins,
                        fontSize = 14.sp,
                        color = AppText
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = botanicalAquaFieldColors()
                )

                Text(
                    text = "Use at least 6 characters for your password.",
                    fontSize = 11.sp,
                    color = aquaMuted
                )

                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = AppDanger,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = {
                        // Original validation and Firebase flow
                        when {
                            fullName.isBlank() ||
                                    email.isBlank() ||
                                    password.isBlank() ||
                                    confirmPassword.isBlank() -> {
                                errorMessage =
                                    "Please fill in all fields"
                            }

                            password.length < 6 -> {
                                errorMessage =
                                    "Password must be at least 6 characters"
                            }

                            password != confirmPassword -> {
                                errorMessage =
                                    "Passwords do not match"
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
                                        onSignUpSuccess(
                                            fullName.trim(),
                                            email.trim()
                                        )
                                    },
                                    onError = { message ->
                                        isLoading = false
                                        errorMessage = message
                                    }
                                )
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTeal
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Create My Account",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Already have an account? Log In",
                    fontFamily = AquaPoppins,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppTeal,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun botanicalAquaFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AppTeal,
        unfocusedBorderColor = Color(0xFFDCEBE6),
        focusedLabelColor = AppTeal,
        unfocusedLabelColor = Color(0xFF718985),
        cursorColor = AppTeal,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )



@Composable
private fun BotanicalAquaSignUpIllustration(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(
        label = "AquaponicAnimation"
    )

    // Animasi ikan berenang
    val swim by transition.animateFloat(
        initialValue = -8f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FishSwim"
    )

    // Animasi gelembung naik
    val bubbles by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "Bubbles"
    )

    // BARU: tanaman bergoyang lembut
    val leafSway by transition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2600,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LeafSway"
    )

    // BARU: permukaan air bergerak perlahan
    val waterWave by transition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WaterWave"
    )

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFE2F5EE))
    ) {
        val sx = size.width / 320f
        val sy = size.height / 130f

        fun pt(x: Float, y: Float) =
            Offset(x * sx, y * sy)

        // Akuarium dan air
        drawRoundRect(
            color = Color(0xFF7CD2D0).copy(alpha = 0.55f),
            topLeft = pt(20f, 51f),
            size = androidx.compose.ui.geometry.Size(
                280f * sx,
                69f * sy
            ),
            cornerRadius =
                androidx.compose.ui.geometry.CornerRadius(
                    14f * sx,
                    14f * sy
                )
        )

        // BARU: permukaan air bergelombang
        val wavePath = Path().apply {
            moveTo(22f * sx, (56f + waterWave) * sy)

            cubicTo(
                85f * sx, (53f - waterWave) * sy,
                130f * sx, (59f + waterWave) * sy,
                180f * sx, 56f * sy
            )

            cubicTo(
                225f * sx, (53f - waterWave) * sy,
                265f * sx, (59f + waterWave) * sy,
                298f * sx, (56f + waterWave) * sy
            )
        }

        drawPath(
            path = wavePath,
            color = Color(0xFF32A7A0),
            style = Stroke(width = 2f * sx)
        )

        // Tanaman dengan gerakan daun
        listOf(75f, 160f, 245f).forEachIndexed { index, x ->

            // Setiap tanaman bergerak sedikit berbeda
            val sway = leafSway *
                    if (index == 1) -0.75f else 1f

            val tipX = x + sway

            drawLine(
                color = Color(0xFF438C69),
                start = pt(x, 55f),
                end = pt(tipX, 16f),
                strokeWidth = 2.5f * sx
            )

            val leftLeaf = Path().apply {
                moveTo((tipX - 1f) * sx, 37f * sy)

                quadraticTo(
                    (tipX - 27f) * sx, 17f * sy,
                    (tipX - 19f) * sx, 10f * sy
                )

                quadraticTo(
                    (tipX + 1f) * sx, 15f * sy,
                    (tipX - 1f) * sx, 37f * sy
                )

                close()
            }

            drawPath(
                leftLeaf,
                Color(0xFF72B98B)
            )

            val rightLeaf = Path().apply {
                moveTo(tipX * sx, 32f * sy)

                quadraticTo(
                    (tipX + 28f) * sx, 11f * sy,
                    (tipX + 21f) * sx, 6f * sy
                )

                quadraticTo(
                    (tipX + 1f) * sx, 9f * sy,
                    tipX * sx, 32f * sy
                )

                close()
            }

            drawPath(
                rightLeaf,
                Color(0xFF439C72)
            )

            // Akar tanaman tetap pada posisi awal
            drawLine(
                color = Color(0xFFC6A37A),
                start = pt(x, 56f),
                end = pt(x - 4f, 76f),
                strokeWidth = 1.4f * sx
            )
        }

        // Bentuk ikan
        fun drawFish(
            x: Float,
            y: Float,
            color: Color,
            scale: Float
        ) {
            val body = Path().apply {
                moveTo(
                    (x - 17f * scale) * sx,
                    y * sy
                )

                quadraticTo(
                    x * sx,
                    (y - 12f * scale) * sy,
                    (x + 17f * scale) * sx,
                    y * sy
                )

                quadraticTo(
                    x * sx,
                    (y + 12f * scale) * sy,
                    (x - 17f * scale) * sx,
                    y * sy
                )

                close()
            }

            drawPath(body, color)

            val tail = Path().apply {
                moveTo(
                    (x - 15f * scale) * sx,
                    y * sy
                )

                lineTo(
                    (x - 28f * scale) * sx,
                    (y - 9f * scale) * sy
                )

                lineTo(
                    (x - 28f * scale) * sx,
                    (y + 9f * scale) * sy
                )

                close()
            }

            drawPath(tail, color)

            drawCircle(
                color = Color.White,
                radius = 1.7f * sx,
                center = pt(
                    x + 9f * scale,
                    y - 2f * scale
                )
            )
        }

        // Ikan pertama
        drawFish(
            x = 108f + swim,
            y = 94f,
            color = Color(0xFF178E9A),
            scale = 1f
        )

        // Ikan kedua
        drawFish(
            x = 224f - swim * 0.7f,
            y = 87f,
            color = Color(0xFFF0A36C),
            scale = 0.8f
        )

        // Gelembung naik ke permukaan
        listOf(
            Triple(145f, 108f, 3.5f),
            Triple(155f, 88f, 2.5f),
            Triple(270f, 112f, 3f)
        ).forEach { (x, y, radius) ->

            val bubbleY =
                59f + (
                        (y - 59f - bubbles * 49f + 49f) % 49f
                        )

            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = radius * sx,
                center = pt(x, bubbleY),
                style = Stroke(width = 1.4f * sx)
            )
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
    // ==========================================
    // 1. FIRESTORE - TETAP DIPERTAHANKAN
    // ==========================================

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

    // Belum ada integrasi sensor ESP32.
    // Jangan menganggap nilai contoh sebagai data nyata.
    val isDeviceConnected = false

    val hasAquarium = aquarium != null

    // ==========================================
    // 2. DASHBOARD UI
    // ==========================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        // HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Hello, $userName!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppText
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Let's take care of your little ecosystem.",
                    fontSize = 12.sp,
                    color = AppMuted
                )
            }

            IconButton(
                onClick = onOpenNotifications
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = AppText
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // ==========================================
        // 3. IoT CONNECTION STATUS
        // ==========================================

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppMint
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = AppTeal,
                        modifier = Modifier.size(26.dp)
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = if (isDeviceConnected) {
                            "Device Connected"
                        } else {
                            "Device Not Connected"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppText
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Your aquarium profile is ready. " +
                            "Connect your ESP32 to start " +
                            "monitoring real-time water conditions.",
                    fontSize = 12.sp,
                    color = AppMuted
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "IoT connection setup coming soon",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTeal
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ==========================================
        // 4. AQUARIUM PROFILE - FIRESTORE
        // ==========================================

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppMint
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = aquarium?.name ?: "My Aquarium",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppText
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Your smart aquaponic ecosystem.",
                    fontSize = 13.sp,
                    color = AppMuted
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = when {
                        isAquariumLoading -> {
                            "Loading aquarium data..."
                        }

                        aquariumError.isNotEmpty() -> {
                            "Gagal mengambil data: $aquariumError"
                        }

                        aquarium != null -> {
                            val savedAquarium = aquarium!!

                            "${savedAquarium.lengthCm.toInt()} × " +
                                    "${savedAquarium.widthCm.toInt()} × " +
                                    "${savedAquarium.heightCm.toInt()} cm • " +
                                    "${savedAquarium.fishType} + " +
                                    savedAquarium.plantType
                        }

                        else -> {
                            "Aquarium belum dikonfigurasi"
                        }
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTealDark
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ==========================================
        // 5. WATER OVERVIEW
        // ==========================================

        Text(
            text = "Water Overview",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = AppText
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Live readings will appear when sensors are connected.",
            fontSize = 12.sp,
            color = AppMuted
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                MetricCard(
                    Icons.Outlined.WaterDrop,
                    "Water Level",
                    "--",
                    "Awaiting sensor"
                )
            }

            Spacer(Modifier.width(10.dp))

            Box(
                modifier = Modifier.weight(1f)
            ) {
                MetricCard(
                    Icons.Outlined.Waves,
                    "Turbidity",
                    "--",
                    "Awaiting sensor"
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                MetricCard(
                    Icons.Outlined.Thermostat,
                    "Temperature",
                    "--",
                    "Awaiting sensor"
                )
            }

            Spacer(Modifier.width(10.dp))

            Box(
                modifier = Modifier.weight(1f)
            ) {
                MetricCard(
                    Icons.Outlined.Science,
                    "pH",
                    "--",
                    "Awaiting sensor"
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ==========================================
        // 6. SYSTEM CONTROLS STATUS
        // ==========================================

        Text(
            text = "System",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = AppText
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Equipment status will appear after IoT integration.",
            fontSize = 12.sp,
            color = AppMuted
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                SystemTile(
                    Icons.Default.Water,
                    "Pump",
                    "Unknown"
                )
            }

            Spacer(Modifier.width(10.dp))

            Box(
                modifier = Modifier.weight(1f)
            ) {
                SystemTile(
                    Icons.Default.Air,
                    "Aerator",
                    "Unknown"
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        // ==========================================
        // 7. AQUACARE ASSISTANT
        // ==========================================

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = AppTeal,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = "AquaCare Assistant",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppText
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = when {
                        isAquariumLoading -> {
                            "Preparing your aquarium guide..."
                        }

                        aquariumError.isNotEmpty() -> {
                            "We couldn't load your aquarium."
                        }

                        hasAquarium -> {
                            "Your aquarium profile is ready!"
                        }

                        else -> {
                            "Let's prepare your aquarium!"
                        }
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppText
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Before connecting your sensors, " +
                            "check that the aquarium is filled " +
                            "appropriately and all equipment " +
                            "is installed safely.",
                    fontSize = 12.sp,
                    color = AppMuted
                )

                Spacer(Modifier.height(12.dp))

                HorizontalDivider(
                    color = AppMint
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Getting Started",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppText
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "1. Prepare your aquarium and plants.",
                    fontSize = 12.sp,
                    color = AppMuted
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "2. Connect ESP32 and your sensors.",
                    fontSize = 12.sp,
                    color = AppMuted
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "3. Check water readings in Monitoring.",
                    fontSize = 12.sp,
                    color = AppMuted
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "4. Use Control only after equipment " +
                            "is connected and tested.",
                    fontSize = 12.sp,
                    color = AppMuted
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Tip: Start with the ESP32 and " +
                            "water level sensor. More sensors " +
                            "can be added later.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppTeal
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        // ==========================================
        // 8. FIREBASE CONNECTION TEST
        // ==========================================

        Text(
            text = "Firebase Connection",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = AppText
        )

        Spacer(Modifier.height(10.dp))

        FirebaseConnectionTestButton()

        Spacer(Modifier.height(20.dp))
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