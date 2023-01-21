package com.example.artkeeper.utils

import com.google.firebase.auth.FirebaseAuth

object Constants {
    val regex = Regex("^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*\$")
    val firebaseAuth = FirebaseAuth.getInstance()
}