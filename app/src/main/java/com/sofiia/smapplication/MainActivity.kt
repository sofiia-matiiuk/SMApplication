package com.sofiia.smapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.sofiia.smapplication.ui.theme.SMApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SMApplicationTheme {
                val navController = rememberNavController()
                val auth = remember { FirebaseAuth.getInstance() }

                // start destination залежить від того, чи користувач вже залогінений
                val start = if (auth.currentUser != null) "message" else "login"

                NavHost(navController = navController, startDestination = start) {
                    composable("login") {
                        LoginScreen(
                            onSignedIn = {
                                navController.navigate("message") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("message") {
                        MessageScreen(
                            onSignedOut = {
                                navController.navigate("login") {
                                    popUpTo("message") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}