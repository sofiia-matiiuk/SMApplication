package com.sofiia.smapplication

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun LoginScreen(
    onSignedIn: () -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    var errorText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { res ->
        isLoading = false

        if (res.resultCode != Activity.RESULT_OK) {
            errorText = "Вхід скасовано"
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(res.data)
        runCatching { task.getResult(Exception::class.java) }
            .onSuccess { account ->
                val idToken = account.idToken
                if (idToken.isNullOrBlank()) {
                    errorText = "Не вдалося отримати token"
                    return@onSuccess
                }

                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { onSignedIn() }
                    .addOnFailureListener { e -> errorText = e.message ?: "Помилка входу" }
            }
            .onFailure { e ->
                errorText = e.message ?: "Помилка входу"
            }
    }

    fun startGoogleSignIn() {
        errorText = null
        isLoading = true

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(context, gso)
        launcher.launch(client.signInIntent)
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Вхід", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { startGoogleSignIn() },
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Зачекай..." else "Sign in with Google")
            }

            if (errorText != null) {
                Spacer(Modifier.height(12.dp))
                Text(text = errorText!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}