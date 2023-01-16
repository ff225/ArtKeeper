package com.example.artkeeper.data.datasource

import android.util.Log
import com.example.artkeeper.data.model.User
import com.example.artkeeper.data.model.UserOnline
import com.example.artkeeper.utils.Constants.firebaseAuth
import com.example.artkeeper.utils.Resource
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRemoteDataSource(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {
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

    /**
     * Verifica la presenza dell'utente su RealTime Database
     *
     * @return true se l'utente è registrato
     */
    suspend fun checkUser(): Boolean {
        Log.d("LoginFragment - UserRemoteDataSource", firebaseAuth.uid.toString())
        return withContext(dispatcher) {
            async {
                dbUser.orderByKey().equalTo(firebaseAuth.uid).get()
                    .await().child(firebaseAuth.uid.toString())
                    .exists()
            }
        }.await()
    }

    /**
     * Se l'utente è presente copia il valore su Room db.
     */
    suspend fun getUser(): UserOnline {
        //var user: UserOnline
        Log.d(
            "LoginFragment - UserRemoteDataSource - GetUser",
            firebaseAuth.uid.toString()
        )
        return withContext(dispatcher) {
            async {
                val querySnapshot =
                    dbUser.orderByKey().equalTo(firebaseAuth.uid).get()
                        .await()
                querySnapshot.child(firebaseAuth.uid.toString()).getValue<UserOnline>()!!
            }
        }.await()
    }


    suspend fun addSon(nChild: Int, nameChild: List<String>) {
        withContext(dispatcher) {
            async {
                dbUser.child(firebaseAuth.uid.toString())
                    .updateChildren(mapOf("nchild" to nChild))
                dbUser.child(firebaseAuth.uid.toString())
                    .updateChildren(mapOf("name_child" to nameChild))
            }
        }.await()
    }

    suspend fun deleteUser() {
        Log.d("LoginFragment - UserRemoteDataSource - deleteUser", firebaseAuth.uid.toString())
        withContext(dispatcher) {
            try {
                val uid = firebaseAuth.uid
                firebaseAuth.currentUser?.delete()?.await()
                dbNickname.child(uid!!).removeValue().await()
                dbUser.child(uid).removeValue().await()
            } catch (e: Exception) {
                Log.e("UserRemoteDataSource", e.message.toString())
            }
        }
    }
}