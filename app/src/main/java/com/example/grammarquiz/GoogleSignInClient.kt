package com.example.grammarquiz

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class GoogleSignInClientWrapper(context: Context) {
    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    fun signOut() {
        googleSignInClient.signOut()
    }

    companion object {
        fun getSignedInAccountFromIntent(data: Intent?): Task<GoogleSignInAccount> {
            return GoogleSignIn.getSignedInAccountFromIntent(data)
        }
    }
}

class GoogleSignInContract : ActivityResultContract<Int, Task<GoogleSignInAccount>?>() {
    override fun createIntent(context: Context, input: Int): Intent {
        return GoogleSignInClientWrapper(context).getSignInIntent()
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Task<GoogleSignInAccount>? {
        return intent?.let { GoogleSignInClientWrapper.getSignedInAccountFromIntent(it) }
    }
}