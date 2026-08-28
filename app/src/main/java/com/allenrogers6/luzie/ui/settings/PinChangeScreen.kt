package com.allenrogers6.luzie.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.allenrogers6.luzie.data.AppPreferences
import com.allenrogers6.luzie.data.Hasher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinChangeScreen(
    preferences: AppPreferences,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var currentPin by remember {
        mutableStateOf("")
    }

    var newPin by remember {
        mutableStateOf("")
    }

    var confirmPin by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var successMessage by remember {
        mutableStateOf<String?>(null)
    }

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Change PIN")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {

            Text(
                text = "Change your Luzie PIN",
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            OutlinedTextField(
                value = currentPin,
                onValueChange = { value ->
                    if (
                        value.length <= 4 &&
                        value.all(Char::isDigit)
                    ) {
                        currentPin = value
                        errorMessage = null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Current PIN")
                },
                visualTransformation =
                    PasswordVisualTransformation(),
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                    ),
                singleLine = true,
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = newPin,
                onValueChange = { value ->
                    if (
                        value.length <= 4 &&
                        value.all(Char::isDigit)
                    ) {
                        newPin = value
                        errorMessage = null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("New PIN")
                },
                visualTransformation =
                    PasswordVisualTransformation(),
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                    ),
                singleLine = true,
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = confirmPin,
                onValueChange = { value ->
                    if (
                        value.length <= 4 &&
                        value.all(Char::isDigit)
                    ) {
                        confirmPin = value
                        errorMessage = null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Confirm new PIN")
                },
                visualTransformation =
                    PasswordVisualTransformation(),
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                    ),
                singleLine = true,
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }

            successMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled =
                    currentPin.length == 4 &&
                    newPin.length == 4 &&
                    confirmPin.length == 4,
                onClick = {
                    scope.launch {

                        val storedHash =
                            preferences.pinHash.first()

                        if (storedHash == null) {
                            errorMessage =
                                "No PIN is currently configured."
                            return@launch
                        }

                        if (
                            !Hasher.verify(
                                currentPin,
                                storedHash,
                            )
                        ) {
                            errorMessage =
                                "Current PIN is incorrect."
                            return@launch
                        }

                        if (newPin == currentPin) {
                            errorMessage =
                                "New PIN must be different."
                            return@launch
                        }

                        if (newPin != confirmPin) {
                            errorMessage =
                                "New PINs do not match."
                            return@launch
                        }

                        val newHash =
                            Hasher.hash(newPin)

                        preferences.setPinHash(
                            newHash
                        )

                        currentPin = ""
                        newPin = ""
                        confirmPin = ""

                        errorMessage = null
                        successMessage =
                            "PIN changed successfully."
                    }
                },
            ) {
                Text("Change PIN")
            }
        }
    }
}
