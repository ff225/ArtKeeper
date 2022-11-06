package com.example.artkeeper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "LoginActivity"
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUI.getInstance().signOut(this)
        val currentUser = FirebaseAuth.getInstance().currentUser
        Log.i(TAG, "${currentUser?.email}")
        if (currentUser == null) {
            createSignInIntent()
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            Toast.makeText(
                this@LoginActivity,
                "Welcome ${FirebaseAuth.getInstance().currentUser!!.email}",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }

    }


    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            Toast.makeText(this@LoginActivity, "Welcome ${user!!.email}", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            // TODO
            // Login with Firebase:
            // - errore dopo il login (risolto);
            // - impedire di uscire dalla pagina tornando al fragment precedente.
        } else {

            Toast.makeText(
                this@LoginActivity,
                "Login fallito, riprova.",
                Toast.LENGTH_LONG
            ).show()

            Log.d(TAG, "${response?.error?.errorCode}")
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }

    private fun createSignInIntent() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.TwitterBuilder().build(),
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setIsSmartLockEnabled(false)
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)

    }
}