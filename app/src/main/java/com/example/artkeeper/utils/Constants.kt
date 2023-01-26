package com.example.artkeeper.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object Constants {
    val regex = Regex("^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*\$")
    val firebaseAuth = FirebaseAuth.getInstance()
    val databaseRef =
        FirebaseDatabase.getInstance("https://artkeeper-01-default-rtdb.europe-west1.firebasedatabase.app/")

}