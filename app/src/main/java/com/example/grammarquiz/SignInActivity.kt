package com.example.grammarquiz

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException

class SignInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SignInScreen()
                }
            }
        }
    }
}

@Composable
fun SignInScreen() {
    val context = LocalContext.current
    var user by remember { mutableStateOf<GoogleSignInAccount?>(null) }

    val signInLauncher = rememberLauncherForActivityResult(contract = GoogleSignInContract()) { task ->
        try {
            val account = task?.getResult(ApiException::class.java)
            user = account
        } catch (e: ApiException) {
            Toast.makeText(context, "Sign in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (user == null) {
            Button(onClick = { signInLauncher.launch(0) }) {
                Text("Sign in with Google")
            }
        } else {
            Text("Welcome, ${user?.displayName}")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                GoogleSignInClientWrapper(context).signOut()
                user = null
            }) {
                Text("Sign out")
            }
        }
    }
}