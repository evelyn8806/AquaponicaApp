package edu.uph.m24si2.aquaponica

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SetupTeal = Color(0xFF0F8F86)
private val SetupDark = Color(0xFF164A43)
private val SetupMuted = Color(0xFF718985)
private val SetupBg = Color(0xFFF5FAF7)
private val SetupBorder = Color(0xFFDCEBE6)

private val SetupPoppins = FontFamily(
    Font(R.font.poppins_light, FontWeight.Light),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

@Composable
fun AquariumSetupScreen(
    onSetupComplete: () -> Unit
) {
    var aquariumName by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var fishType by remember { mutableStateOf("") }
    var fishCount by remember { mutableStateOf("") }
    var plantType by remember { mutableStateOf("") }
    var concern by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val repository = remember { AquariumRepository() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SetupBg)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "YOUR LITTLE ECOSYSTEM",
            fontFamily = SetupPoppins,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = SetupTeal,
            letterSpacing = 1.3.sp
        )

        Text(
            text = "Build Your Little\nEcosystem.",
            fontFamily = SetupPoppins,
            fontSize = 25.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
            color = SetupDark
        )

        Text(
            text = "Tell us about your aquarium to get started.",
            fontFamily = SetupPoppins,
            fontSize = 13.sp,
            color = SetupMuted
        )

        // Decorative header illustration.
        // No Firebase or repository logic is changed.
        SetupAquariumBanner()

        SetupSectionCard(
            title = "01 · Aquarium Details"
        ) {
            SetupInput(
                value = aquariumName,
                onValueChange = { aquariumName = it },
                label = "Aquarium Name"
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SetupInput(
                        value = length,
                        onValueChange = { length = it },
                        label = "Length",
                        numeric = true
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    SetupInput(
                        value = width,
                        onValueChange = { width = it },
                        label = "Width",
                        numeric = true
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    SetupInput(
                        value = height,
                        onValueChange = { height = it },
                        label = "Height",
                        numeric = true
                    )
                }
            }

            Text(
                text = "Dimensions in centimeters (cm)",
                fontSize = 11.sp,
                fontFamily = SetupPoppins,
                color = SetupMuted
            )
        }

        SetupSectionCard(
            title = "02 · Fish & Plants"
        ) {
            SetupInput(
                value = fishType,
                onValueChange = { fishType = it },
                label = "Fish Type"
            )

            SetupInput(
                value = fishCount,
                onValueChange = { fishCount = it },
                label = "Number of Fish",
                numeric = true,
                integerOnly = true
            )

            SetupInput(
                value = plantType,
                onValueChange = { plantType = it },
                label = "Plant Type"
            )
        }

        SetupSectionCard(
            title = "03 · Personal Preferences"
        ) {
            SetupInput(
                value = concern,
                onValueChange = { concern = it },
                label = "Main Concern (Optional)"
            )
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontFamily = SetupPoppins,
                fontSize = 12.sp
            )
        }

        Button(
            onClick = {
                val lengthValue = length.toDoubleOrNull()
                val widthValue = width.toDoubleOrNull()
                val heightValue = height.toDoubleOrNull()
                val fishCountValue = fishCount.toIntOrNull()

                if (
                    aquariumName.isBlank() ||
                    fishType.isBlank() ||
                    plantType.isBlank() ||
                    lengthValue == null ||
                    widthValue == null ||
                    heightValue == null ||
                    fishCountValue == null ||
                    lengthValue <= 0 ||
                    widthValue <= 0 ||
                    heightValue <= 0 ||
                    fishCountValue <= 0
                ) {
                    errorMessage =
                        "Please complete all required fields correctly"
                } else {
                    isLoading = true
                    errorMessage = ""

                    val aquarium = Aquarium(
                        name = aquariumName.trim(),
                        lengthCm = lengthValue,
                        widthCm = widthValue,
                        heightCm = heightValue,
                        fishType = fishType.trim(),
                        fishCount = fishCountValue,
                        plantType = plantType.trim(),
                        userConcern = concern.trim()
                    )

                    repository.saveAquarium(
                        aquarium = aquarium,
                        onSuccess = {
                            isLoading = false
                            onSetupComplete()
                        },
                        onError = { error ->
                            isLoading = false
                            errorMessage = error
                        }
                    )
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SetupTeal
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
                    text = "Create My Aquarium",
                    fontFamily = SetupPoppins,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SetupSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = SetupBorder,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            fontFamily = SetupPoppins,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = SetupDark
        )

        content()
    }
}

@Composable
private fun SetupInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    numeric: Boolean = false,
    integerOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontFamily = SetupPoppins,
                fontSize = 11.sp
            )
        },
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = SetupPoppins,
            fontSize = 13.sp,
            color = SetupDark
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = when {
                integerOnly -> KeyboardType.Number
                numeric -> KeyboardType.Decimal
                else -> KeyboardType.Text
            }
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SetupTeal,
            unfocusedBorderColor = SetupBorder,
            focusedLabelColor = SetupTeal,
            unfocusedLabelColor = SetupMuted,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            cursorColor = SetupTeal
        )
    )
}

@Composable
private fun SetupAquariumBanner() {
    // Reuses the existing AQUAPONICA logo resource
    // without changing any data or navigation.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE0F4ED)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🐟  🌱  💧",
            fontSize = 34.sp,
            textAlign = TextAlign.Center
        )
    }
}
