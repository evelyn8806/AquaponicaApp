
package edu.uph.m24si2.aquaponica

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AquariumDetailScreen(
    onBack: () -> Unit,
    onEdit: (Aquarium) -> Unit,
    onDeleteSuccess: () -> Unit
) {
    val repository = remember { AquariumRepository() }

    var aquarium by remember {
        mutableStateOf<Aquarium?>(null)
    }

    var isLoading by remember { mutableStateOf(true) }
    var isDeleting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repository.getAquarium(
            onSuccess = { result ->
                aquarium = result
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back")
        }

        Text(
            text = "My Aquarium",
            style = MaterialTheme.typography.headlineSmall
        )

        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            aquarium == null -> {
                Text(
                    text = if (errorMessage.isNotEmpty()) {
                        errorMessage
                    } else {
                        "No aquarium found."
                    }
                )
            }

            else -> {
                val data = aquarium!!

                Text(
                    text = data.name,
                    style = MaterialTheme.typography.titleLarge
                )

                HorizontalDivider()

                AquariumDetailRow("Length", "${data.lengthCm} cm")
                AquariumDetailRow("Width", "${data.widthCm} cm")
                AquariumDetailRow("Height", "${data.heightCm} cm")
                AquariumDetailRow("Fish Type", data.fishType)
                AquariumDetailRow(
                    "Fish Count",
                    data.fishCount.toString()
                )
                AquariumDetailRow("Plant Type", data.plantType)
                AquariumDetailRow(
                    "Main Concern",
                    data.userConcern.ifBlank { "-" }
                )
                AquariumDetailRow(
                    "Feeding Times",
                    data.feedingTimes.joinToString()
                        .ifBlank { "-" }
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { onEdit(data) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isDeleting
                ) {
                    Text("Edit Aquarium")
                }

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isDeleting,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete Aquarium")
                }

                if (isDeleting) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) {
                    showDeleteDialog = false
                }
            },
            title = {
                Text("Delete Aquarium?")
            },
            text = {
                Text(
                    "Are you sure you want to delete this aquarium? " +
                            "This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        val data = aquarium

                        if (data != null) {
                            isDeleting = true
                            errorMessage = ""

                            repository.deleteAquarium(
                                aquarium = data,
                                onSuccess = {
                                    isDeleting = false
                                    showDeleteDialog = false
                                    aquarium = null
                                    onDeleteSuccess()
                                },
                                onError = { error ->
                                    isDeleting = false
                                    showDeleteDialog = false
                                    errorMessage = error
                                }
                            )
                        }
                    }
                ) {
                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AquariumDetailRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
