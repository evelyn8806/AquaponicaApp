
package edu.uph.m24si2.aquaponica

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Set Up Your Aquarium",
            style = MaterialTheme.typography.headlineSmall
        )

        Text("Personalize your aquaponic system.")

        OutlinedTextField(
            value = aquariumName,
            onValueChange = { aquariumName = it },
            label = { Text("Aquarium Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = length,
            onValueChange = { length = it },
            label = { Text("Length (cm)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = width,
            onValueChange = { width = it },
            label = { Text("Width (cm)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Height (cm)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = fishType,
            onValueChange = { fishType = it },
            label = { Text("Fish Type") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = fishCount,
            onValueChange = { fishCount = it },
            label = { Text("Number of Fish") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = plantType,
            onValueChange = { plantType = it },
            label = { Text("Plant Type") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = concern,
            onValueChange = { concern = it },
            label = { Text("Main Concern (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (isLoading) "Saving..." else "Save Aquarium"
            )
        }
    }
}
