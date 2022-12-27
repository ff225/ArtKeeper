package com.example.artkeeper.data.datasource

import com.example.artkeeper.data.model.User
import com.example.artkeeper.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class UserRemoteDataSource(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseRef =
        FirebaseDatabase.getInstance("https://artkeeper-01-default-rtdb.europe-west1.firebasedatabase.app/")

    private val dbUser = databaseRef.getReference("users")
    private val dbNickname = databaseRef.getReference("nickname")

    suspend fun insertUser(user: User): Resource<Boolean> {
        return Resource.Success(withContext(dispatcher) {
            async {
                dbUser.child(user.uid).setValue(user)
                dbNickname.child(user.uid).setValue(user.nickName)
            }
        }.await().isSuccessful)
    }

    suspend fun checkNickname(nickName: String): Boolean {
        return withContext(dispatcher) {
            async {
                dbNickname.orderByValue().equalTo(nickName).get().await().exists()
            }
        }.await()
    }

    suspend fun addSon(nChild: Int, nameChild: List<String>) {
        withContext(dispatcher) {
            async {
                dbUser.child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .updateChildren(mapOf("nchild" to nChild))
                dbUser.child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .updateChildren(mapOf("name_child" to nameChild))
            }
        }.await()
    }

    // TODO: Rimuovere delay
    suspend fun deleteUser() =
        coroutineScope {
            delay(5000)
            dbNickname.child(FirebaseAuth.getInstance().currentUser!!.uid).removeValue().await()
            dbUser.child(FirebaseAuth.getInstance().currentUser!!.uid).removeValue().await()
            FirebaseAuth.getInstance().currentUser!!.delete().await()
        }
}