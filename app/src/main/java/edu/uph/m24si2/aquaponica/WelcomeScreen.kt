package edu.uph.m24si2.aquaponica

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.uph.m24si2.aquaponica.ui.theme.AQUAPONICATheme
import kotlin.math.PI
import kotlin.math.sin

// =====================================
// AQUAPONICA COLORS
// =====================================

private val WelcomeTeal = Color(0xFF0F8F86)
private val WelcomeDark = Color(0xFF164A43)
private val WelcomeMint = Color(0xFFEAF8F3)
private val WelcomeGreen = Color(0xFF3DAA73)
private val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

// =====================================
// WELCOME SCREEN
// =====================================

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit
) {
    val infiniteTransition =
        rememberInfiniteTransition(label = "aquarium")

    // Animasi ikan
    val fishMovement by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 6500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "fishMovement"
    )

    // Animasi gelembung
    val bubbleMovement by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubbleMovement"
    )

    // Animasi tanaman
    val plantMovement by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "plantMovement"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF0FFF9),
                            Color(0xFFDDF5EC),
                            Color(0xFFB9E5D8)
                        )
                    )
                )
        ) {
            AnimatedLeafBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(
                    horizontal = 24.dp,
                    vertical = 12.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.weight(0.4f))

            // =====================================
            // LOGO AQUAPONICA
            // =====================================

            Image(
                painter = painterResource(
                    id = R.drawable.aquaponica_logo
                ),
                contentDescription = "AQUAPONICA Logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "Smart Aquaponics for a Better Tomorrow",
                fontSize = 12.sp,
                color = WelcomeDark.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // =====================================
            // ANIMATED AQUAPONICS
            // =====================================

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFFE2FAF4),
                                Color(0xFFB8EAE3),
                                Color(0xFF78C9C4)
                            )
                        ),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = WelcomeTeal.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .padding(10.dp)
            ) {

                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawAquarium(
                        fishMovement = fishMovement,
                        bubbleMovement = bubbleMovement,
                        plantMovement = plantMovement
                    )
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 2.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.82f)
                ) {
                }
            }

            Spacer(Modifier.height(24.dp))

            // =====================================
            // HEADLINE
            // =====================================

            Text(
                text = "Grow · Nature · Smarter.",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 23.sp,
                lineHeight = 36.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF164A43)
            )


            Spacer(Modifier.height(10.dp))

            Text(
                text = "Monitor your fish, plants, and water\nin one smart ecosystem.",
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF648C83)
            )

            Spacer(Modifier.height(20.dp))

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(9.dp),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                FeaturePill("🐟", "Fish")
                FeaturePill("🌱", "Plants")
                FeaturePill("💧", "Water")
            }

            Spacer(Modifier.weight(0.5f))

            // =====================================
            // GET STARTED
            // =====================================

            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WelcomeTeal
                )
            ) {
                Text(
                    text = "Get Started  →",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(11.dp))

            // =====================================
            // LOGIN BUTTON
            // =====================================

            OutlinedButton(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    WelcomeTeal
                )
            ) {
                Text(
                    text = "I Already Have an Account",
                    color = WelcomeTeal,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}
}
@Composable
fun AnimatedLeafBackground(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(
        label = "leafAnimation"
    )

    val sway by transition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 6000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leafSway"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val leafColor = androidx.compose.ui.graphics.Color(
            0xFF438D70
        ).copy(alpha = 0.10f)

        fun drawLeaf(
            center: Offset,
            radius: Float,
            rotation: Float
        ) {
            rotate(
                degrees = rotation + sway,
                pivot = center
            ) {
                val path = Path().apply {
                    moveTo(
                        center.x,
                        center.y - radius
                    )

                    quadraticTo(
                        center.x + radius,
                        center.y,
                        center.x,
                        center.y + radius
                    )

                    quadraticTo(
                        center.x - radius,
                        center.y,
                        center.x,
                        center.y - radius
                    )

                    close()
                }

                drawPath(
                    path = path,
                    color = leafColor
                )
            }
        }

        drawLeaf(
            center = Offset(
                size.width * 0.02f,
                size.height * 0.05f
            ),
            radius = size.width * 0.42f,
            rotation = -35f
        )

        drawLeaf(
            center = Offset(
                size.width * 0.93f,
                size.height * 0.30f
            ),
            radius = size.width * 0.15f,
            rotation = 35f
        )

        drawLeaf(
            center = Offset(
                size.width * 0.08f,
                size.height * 0.83f
            ),
            radius = size.width * 0.18f,
            rotation = 25f
        )
    }
}
// =====================================
// FEATURE PILL
// =====================================

@Composable
private fun FeaturePill(
    emoji: String,
    title: String
) {
    Surface(
        color = WelcomeMint,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 18.dp,
                vertical = 10.dp
            ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = 19.sp
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                color = WelcomeDark,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// =====================================
// DRAW AQUAPONICS SYSTEM
// =====================================

private fun DrawScope.drawAquarium(
    fishMovement: Float,
    bubbleMovement: Float,
    plantMovement: Float
) {
    val w = size.width
    val h = size.height

    val tankTop = h * 0.43f
    val tankBottom = h * 0.97f
    val waterTop = h * 0.51f

    val stroke =
        (w * 0.007f).coerceAtLeast(2f)

    // =====================================
    // AQUARIUM GLASS
    // =====================================

    drawRoundRect(
        color = Color(0xFFECFFFC).copy(alpha = 0.55f),
        topLeft = Offset(w * 0.04f, tankTop),
        size = Size(
            w * 0.92f,
            tankBottom - tankTop
        ),
        cornerRadius = CornerRadius(w * 0.035f)
    )

    // =====================================
    // WATER
    // =====================================

    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(
                Color(0xFFBCEEE9),
                Color(0xFF5BC1BA)
            ),
            startY = waterTop,
            endY = tankBottom
        ),
        topLeft = Offset(w * 0.055f, waterTop),
        size = Size(
            w * 0.89f,
            tankBottom - waterTop
        ),
        cornerRadius = CornerRadius(w * 0.025f)
    )

    // =====================================
    // MOVING WATER SURFACE
    // =====================================

    val wave = Path().apply {
        for (i in 0..100) {
            val x =
                w * (0.055f + 0.89f * i / 100f)

            val y = waterTop +
                    sin(
                        i * 0.13f +
                                fishMovement * 2f * PI
                    ).toFloat() * h * 0.009f

            if (i == 0) {
                moveTo(x, y)
            } else {
                lineTo(x, y)
            }
        }
    }

    drawPath(
        path = wave,
        color = Color.White.copy(alpha = 0.8f),
        style = Stroke(stroke)
    )

    // =====================================
    // SAND
    // =====================================

    drawRoundRect(
        color = Color(0xFFE3E9BD),
        topLeft = Offset(
            w * 0.055f,
            h * 0.91f
        ),
        size = Size(
            w * 0.89f,
            h * 0.06f
        ),
        cornerRadius = CornerRadius(w * 0.02f)
    )

    // =====================================
    // PLANT ROOTS
    // =====================================

    listOf(
        0.20f,
        0.34f,
        0.50f,
        0.66f,
        0.80f
    ).forEachIndexed { i, pos ->

        val rootX = w * pos

        val root = Path().apply {
            moveTo(
                rootX,
                h * 0.40f
            )

            quadraticBezierTo(
                rootX +
                        (if (i % 2 == 0) 1 else -1) *
                        w * 0.025f,
                h * 0.49f,
                rootX +
                        sin(
                            plantMovement * 0.12f + i
                        ).toFloat() * w * 0.015f,
                h * 0.56f
            )
        }

        drawPath(
            path = root,
            color = Color(0xFFC0A77A),
            style = Stroke(stroke * 0.65f)
        )
    }

    // =====================================
    // WATER CIRCULATION PIPE
    // =====================================

    val pipe = Path().apply {
        moveTo(
            w * 0.88f,
            h * 0.82f
        )

        lineTo(
            w * 0.97f,
            h * 0.82f
        )

        lineTo(
            w * 0.97f,
            h * 0.34f
        )

        lineTo(
            w * 0.85f,
            h * 0.34f
        )
    }

    drawPath(
        path = pipe,
        color = Color(0xFF388F89),
        style = Stroke(
            width = stroke * 2.2f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    drawLine(
        color = Color(0xFFEBFFFA),
        start = Offset(
            w * 0.97f,
            h * 0.64f
        ),
        end = Offset(
            w * 0.97f,
            h * 0.72f
        ),
        strokeWidth = stroke * 0.7f
    )

    // =====================================
    // FIVE KANGKUNG PLANTS
    // =====================================

    listOf(
        0.20f,
        0.34f,
        0.50f,
        0.66f,
        0.80f
    ).forEachIndexed { index, pos ->

        drawKangkung(
            base = Offset(
                w * pos,
                h * 0.35f
            ),
            height = h *
                    (if (index % 2 == 0) 0.16f else 0.13f),
            sway = plantMovement *
                    w * 0.0025f *
                    (if (index % 2 == 0) 1f else -1f),
            unit = w * 0.012f
        )
    }

    // =====================================
    // PLANT GROW BED
    // =====================================

    drawRoundRect(
        color = Color(0xFF4F927A),
        topLeft = Offset(
            w * 0.11f,
            h * 0.34f
        ),
        size = Size(
            w * 0.78f,
            h * 0.075f
        ),
        cornerRadius = CornerRadius(w * 0.025f)
    )

    drawRoundRect(
        color = Color(0xFFE4F1E7),
        topLeft = Offset(
            w * 0.10f,
            h * 0.33f
        ),
        size = Size(
            w * 0.80f,
            h * 0.025f
        ),
        cornerRadius = CornerRadius(w * 0.012f)
    )

    // =====================================
    // AQUATIC PLANTS
    // =====================================

    listOf(
        0.13f,
        0.25f,
        0.75f
    ).forEachIndexed { index, pos ->

        val x = w * pos
        val y = h * 0.91f

        val top = y -
                h * (0.12f + index * 0.025f)

        drawLine(
            color = Color(0xFF258D68),
            start = Offset(x, y),
            end = Offset(
                x + plantMovement,
                top
            ),
            strokeWidth = stroke * 1.5f
        )

        repeat(3) { leaf ->

            val leafY =
                y - (y - top) * (leaf + 1) / 4f

            drawOval(
                color = if (leaf % 2 == 0)
                    WelcomeGreen
                else
                    Color(0xFF6DC78A),
                topLeft = Offset(
                    x +
                            (if (leaf % 2 == 0) -1 else 1) *
                            w * 0.012f,
                    leafY
                ),
                size = Size(
                    w * 0.037f,
                    h * 0.052f
                )
            )
        }
    }

    // =====================================
    // FOUR ANIMATED FISH
    // =====================================

    val fishScale =
        (w / 350f).coerceIn(0.45f, 1.3f)

    // Fish 1
    drawFish(
        center = Offset(
            w * (0.16f + fishMovement * 0.56f),
            h * 0.65f +
                    sin(
                        fishMovement * 2f * PI
                    ).toFloat() * h * 0.02f
        ),
        scale = 0.72f * fishScale,
        bodyColor = Color(0xFF0F8F86),
        facingRight = true
    )

    // Fish 2
    drawFish(
        center = Offset(
            w * (0.82f - fishMovement * 0.51f),
            h * 0.79f +
                    sin(
                        fishMovement * 2f * PI + 1f
                    ).toFloat() * h * 0.018f
        ),
        scale = 0.53f * fishScale,
        bodyColor = Color(0xFFF0A75D),
        facingRight = false
    )

    // Fish 3
    drawFish(
        center = Offset(
            w * (0.72f - fishMovement * 0.38f),
            h * 0.61f +
                    sin(
                        fishMovement * 2f * PI + 2f
                    ).toFloat() * h * 0.012f
        ),
        scale = 0.39f * fishScale,
        bodyColor = Color(0xFF278CB2),
        facingRight = false
    )

    // Fish 4
    drawFish(
        center = Offset(
            w * (0.25f + fishMovement * 0.32f),
            h * 0.85f +
                    sin(
                        fishMovement * 2f * PI + 3f
                    ).toFloat() * h * 0.012f
        ),
        scale = 0.32f * fishScale,
        bodyColor = Color(0xFF3EAF89),
        facingRight = true
    )

    // =====================================
    // ANIMATED BUBBLES
    // =====================================

    repeat(12) { index ->

        val progress =
            (bubbleMovement + index / 12f) % 1f

        val x =
            w * (0.15f + (index % 6) * 0.12f)

        val y =
            h * (0.89f - progress * 0.35f)

        drawCircle(
            color = Color.White.copy(alpha = 0.75f),
            radius = stroke *
                    (0.65f + index % 3 * 0.3f),
            center = Offset(
                x +
                        sin(
                            progress * 9f + index
                        ).toFloat() * w * 0.012f,
                y
            ),
            style = Stroke(
                width = stroke * 0.45f
            )
        )
    }

    // =====================================
    // FRONT GLASS BORDER
    // =====================================

    drawRoundRect(
        color = Color(0xFF4EAAA4).copy(alpha = 0.8f),
        topLeft = Offset(
            w * 0.04f,
            tankTop
        ),
        size = Size(
            w * 0.92f,
            tankBottom - tankTop
        ),
        cornerRadius = CornerRadius(w * 0.035f),
        style = Stroke(width = stroke * 0.9f)
    )
}

// =====================================
// DRAW KANGKUNG
// =====================================

private fun DrawScope.drawKangkung(
    base: Offset,
    height: Float,
    sway: Float,
    unit: Float
) {
    val tip = Offset(
        base.x + sway,
        base.y - height
    )

    val stem = Path().apply {
        moveTo(base.x, base.y)

        quadraticBezierTo(
            base.x + sway * 0.4f,
            base.y - height * 0.5f,
            tip.x,
            tip.y
        )
    }

    drawPath(
        path = stem,
        color = Color(0xFF277F58),
        style = Stroke(
            width = unit * 0.45f,
            cap = StrokeCap.Round
        )
    )

    repeat(3) { i ->

        val t = 0.30f + i * 0.23f

        val x = base.x + sway * t
        val y = base.y - height * t

        val direction =
            if (i % 2 == 0) -1f else 1f

        val leaf = Path().apply {
            moveTo(x, y)

            quadraticBezierTo(
                x + direction * unit * 2.2f,
                y - unit * 3f,
                x + direction * unit * 3.2f,
                y - unit * 5.4f
            )

            quadraticBezierTo(
                x - direction * unit * 0.4f,
                y - unit * 4.1f,
                x,
                y
            )
        }

        drawPath(
            path = leaf,
            color = if (i % 2 == 0)
                WelcomeGreen
            else
                Color(0xFF54BE83)
        )
    }

    drawOval(
        color = Color(0xFF2F9B67),
        topLeft = Offset(
            tip.x - unit * 0.75f,
            tip.y - unit * 2.5f
        ),
        size = Size(
            unit * 1.5f,
            unit * 3.5f
        )
    )
}

// =====================================
// DRAW FISH
// =====================================

private fun DrawScope.drawFish(
    center: Offset,
    scale: Float,
    bodyColor: Color,
    facingRight: Boolean
) {
    rotate(
        degrees = if (facingRight) 0f else 180f,
        pivot = center
    ) {

        val bodyWidth = 88f * scale
        val bodyHeight = 48f * scale

        // Fish tail
        val tail = Path().apply {
            moveTo(
                center.x - bodyWidth * 0.36f,
                center.y
            )

            lineTo(
                center.x - bodyWidth * 0.85f,
                center.y - bodyHeight * 0.7f
            )

            lineTo(
                center.x - bodyWidth * 0.85f,
                center.y + bodyHeight * 0.7f
            )

            close()
        }

        drawPath(
            path = tail,
            color = bodyColor.copy(alpha = 0.85f)
        )

        // Fish body
        drawOval(
            color = bodyColor,
            topLeft = Offset(
                center.x - bodyWidth / 2f,
                center.y - bodyHeight / 2f
            ),
            size = Size(
                bodyWidth,
                bodyHeight
            )
        )

        // Fish fin
        val fin = Path().apply {
            moveTo(
                center.x - 12f * scale,
                center.y - 17f * scale
            )

            lineTo(
                center.x - 2f * scale,
                center.y - 37f * scale
            )

            lineTo(
                center.x + 22f * scale,
                center.y - 17f * scale
            )

            close()
        }

        drawPath(
            path = fin,
            color = bodyColor.copy(alpha = 0.75f)
        )

        // Fish eye
        drawCircle(
            color = Color.White,
            radius = 6f * scale,
            center = Offset(
                center.x + 23f * scale,
                center.y - 7f * scale
            )
        )

        drawCircle(
            color = WelcomeDark,
            radius = 2.8f * scale,
            center = Offset(
                center.x + 25f * scale,
                center.y - 7f * scale
            )
        )

        // Fish body detail
        drawLine(
            color = Color.White.copy(alpha = 0.45f),
            start = Offset(
                center.x - 10f * scale,
                center.y - 13f * scale
            ),
            end = Offset(
                center.x + 4f * scale,
                center.y + 13f * scale
            ),
            strokeWidth = 3f * scale
        )
    }
}

// =====================================
// PREVIEW
// =====================================

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    AQUAPONICATheme {
        WelcomeScreen(
            onGetStarted = {},
            onLogin = {}
        )
    }
}

