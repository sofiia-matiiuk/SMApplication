package com.sofiia.smapplication

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    onSignedOut: () -> Unit,
    repo: FirestoreRepository = FirestoreRepository()
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val scope = rememberCoroutineScope()

    var currentMessage by remember { mutableStateOf("") }
    var input by remember { mutableStateOf("") }

    var errorText by remember { mutableStateOf<String?>(null) }
    var infoText by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // realtime listener
    LaunchedEffect(Unit) {
        repo.listenCurrentMessage().collect { result ->
            result.onSuccess { text ->
                currentMessage = text
            }.onFailure { e ->
                errorText = e.message ?: "Помилка отримання повідомлення"
                snackbarHostState.showSnackbar(errorText!!)
            }
        }
    }

    fun send() {
        errorText = null
        infoText = null

        repo.setMessage(input) { result ->
            result.onSuccess {
                infoText = "Надіслано"
                scope.launch { snackbarHostState.showSnackbar("Надіслано") }
                input = ""
            }.onFailure { e ->
                errorText = e.message ?: "Помилка відправки"
                scope.launch { snackbarHostState.showSnackbar(errorText!!) }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Повідомлення") },
                actions = {
                    TextButton(
                        onClick = {
                            auth.signOut()
                            onSignedOut()
                        }
                    ) { Text("Sign out") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Актуальне повідомлення з Firestore:")
            Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (currentMessage.isBlank()) "(порожньо)" else currentMessage,
                    modifier = Modifier.padding(12.dp)
                )
            }

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Нове повідомлення") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { send() },
                modifier = Modifier.fillMaxWidth(),
                enabled = input.isNotBlank()
            ) {
                Text("Send")
            }
        }
    }
}