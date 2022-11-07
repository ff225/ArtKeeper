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
        Log.i(TAG, "email: ${currentUser?.email}")
        //if (currentUser == null) {
        Log.d(TAG, "start intent for login")
        createSignInIntent()
        //}
        /*else {
            Toast.makeText(
                this@LoginActivity,
                "Welcome ${FirebaseAuth.getInstance().currentUser!!.email}",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(this, MainActivity::class.java))
            //finish()
        }*/

        //finish()
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed")
        finish()
        super.onBackPressed()
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        // Login with Firebase:
        // - errore dopo il login (risolto);
        // - impedire all'utente di uscire dall'intent di login (risolto).

        Log.d(TAG, "onSignInResult")
        val response = result.idpResponse
        // L'utente cerca di tornare indietro.
        if (response?.error == null) {
            Log.d(TAG, "onSignInResult is ${response?.error}, call finish()")
            onBackPressed()
        }

        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            Log.i(TAG, "Logged!")
            val user = FirebaseAuth.getInstance().currentUser
            Toast.makeText(this@LoginActivity, "Welcome ${user!!.email}", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {

            Toast.makeText(
                this@LoginActivity,
                "Login fallito, riprova.",
                Toast.LENGTH_LONG
            ).show()

            Log.d(TAG, "${response?.error?.errorCode}")
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

        signInIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        signInLauncher.launch(signInIntent)
        Log.d(TAG, "in createSignInIntent")

    }
}