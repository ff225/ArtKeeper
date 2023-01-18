package com.example.artkeeper.data.datasource

import android.util.Log
import com.example.artkeeper.data.model.User
import com.example.artkeeper.data.model.UserOnline
import com.example.artkeeper.utils.Constants.firebaseAuth
import com.example.artkeeper.utils.Resource
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.database.FirebaseDatabase
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
    @Suppress("UNCHECKED_CAST")
    suspend fun getUser(): UserOnline {
        //var user: UserOnline
        Log.d(
            "LoginFragment - UserRemoteDataSource - GetUser",
            firebaseAuth.uid.toString()
        )
        return withContext(dispatcher) {
            async {
                //var userOnline:UserOnline? = null
                val querySnapshot =
                    dbUser.orderByKey().equalTo(firebaseAuth.uid).get()
                        .await()

                val user =
                    querySnapshot.child(firebaseAuth.uid.toString()).value!! as HashMap<String, *>
                Log.d("LoginFragment", user.toString())
                UserOnline(
                    user["firstName"].toString(),
                    user["lastName"].toString(),
                    user["uid"].toString(),
                    user["nickName"].toString(),
                    user["nchild"].toString().toInt(),
                    user["name_child"] as List<String>?
                )
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

    suspend fun deleteUser(): Result<Boolean> {
        Log.d("LoginFragment - UserRemoteDataSource - deleteUser", firebaseAuth.uid.toString())

        return withContext(dispatcher) {
            try {
                val uid = firebaseAuth.uid
                firebaseAuth.currentUser!!.delete().await()
                dbNickname.child(uid!!).removeValue().await()
                dbUser.child(uid).removeValue().await()
                /* firebaseAuth.currentUser!!.delete().addOnCompleteListener {
                     if (it.isSuccessful) {
                         dbNickname.child(uid!!).removeValue()
                         dbUser.child(uid).removeValue()
                     }
                 }.await()*/
                Result.success(true)
                //firebaseAuth.currentUser!!.delete().await()
            } catch (e: FirebaseAuthRecentLoginRequiredException) {
                Log.e("UserRemoteDataSource", e.message.toString())
                Result.failure(Throwable(e.message))
            }
        }
    }
}