package edu.uph.m24si2.aquaponica

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AquariumEditScreen(
    aquarium: Aquarium,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val repository = remember { AquariumRepository() }

    var name by remember(aquarium.id) {
        mutableStateOf(aquarium.name)
    }

    var fishType by remember(aquarium.id) {
        mutableStateOf(aquarium.fishType)
    }

    var plantType by remember(aquarium.id) {
        mutableStateOf(aquarium.plantType)
    }

    var length by remember(aquarium.id) {
        mutableStateOf(aquarium.lengthCm.toString())
    }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Aquarium") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Aquarium Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = fishType,
                onValueChange = { fishType = it },
                label = { Text("Fish Type") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = plantType,
                onValueChange = { plantType = it },
                label = { Text("Plant Type") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = length,
                onValueChange = { length = it },
                label = { Text("Aquarium Length (cm)") },
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    val lengthValue = length.toDoubleOrNull()

                    when {
                        name.isBlank() ||
                                fishType.isBlank() ||
                                plantType.isBlank() -> {
                            errorMessage = "Please complete all fields"
                        }

                        lengthValue == null || lengthValue <= 0 -> {
                            errorMessage = "Invalid aquarium length"
                        }

                        else -> {
                            isSaving = true
                            errorMessage = ""

                            val updatedAquarium = aquarium.copy(
                                name = name.trim(),
                                fishType = fishType.trim(),
                                plantType = plantType.trim(),
                                lengthCm = lengthValue
                            )

                            repository.updateAquarium(
                                aquarium = updatedAquarium,
                                onSuccess = {
                                    isSaving = false
                                    onSaveSuccess()
                                },
                                onError = { error ->
                                    isSaving = false
                                    errorMessage = error
                                }
                            )
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isSaving) "Saving..."
                    else "Save Changes"
                )
            }
        }
    }
}
