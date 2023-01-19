package com.example.artkeeper.data.datasource

import android.util.Log
import com.example.artkeeper.data.model.User
import com.example.artkeeper.data.model.UserOnline
import com.example.artkeeper.utils.Constants.firebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRemoteDataSource(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {
    private val databaseRef =
        FirebaseDatabase.getInstance("https://artkeeper-01-default-rtdb.europe-west1.firebasedatabase.app/")

    private val dbUser = databaseRef.getReference("users")
    private val dbNickname = databaseRef.getReference("nickname")


    suspend fun insertUser(user: User) {
        withContext(dispatcher) {
            dbUser.child(user.uid).setValue(user)
            dbNickname.child(user.uid).setValue(user.nickName)
        }
    }


    suspend fun checkNickname(nickName: String): Result<Boolean> {
        return withContext(dispatcher) {
            if (dbNickname.orderByValue().equalTo(nickName).get().await().exists())
                return@withContext Result.failure(Exception("Nickname già utilizzato"))
            else
                return@withContext Result.success(true)
        }
    }


    suspend fun checkUser(): Result<Boolean> {
        Log.d("LoginFragment - UserRemoteDataSource", firebaseAuth.uid.toString())
        return withContext(dispatcher) {
            if (dbUser.orderByKey().equalTo(firebaseAuth.uid).get()
                    .await().child(firebaseAuth.uid.toString())
                    .exists()
            )
                return@withContext Result.success(true)
            else
                return@withContext Result.failure(Throwable("Utente non registrato"))
        }
    }


    /**
     * Se l'utente è presente torna l'oggetto da salvare su Room db.
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun getUser(): Result<UserOnline> {

        return withContext(dispatcher) {

            val querySnapshot =
                dbUser.orderByKey().equalTo(firebaseAuth.uid).get()
                    .await()

            val user =
                querySnapshot.child(firebaseAuth.uid.toString()).value!! as HashMap<String, *>
            Log.d("LoginFragment", user.toString())
            return@withContext Result.success(
                UserOnline(
                    user["firstName"].toString(),
                    user["lastName"].toString(),
                    user["uid"].toString(),
                    user["nickName"].toString(),
                    user["nchild"].toString().toInt(),
                    user["name_child"] as List<String>?
                )
            )
        }
    }

    suspend fun addSon(nChild: Int, nameChild: List<String>) {
        withContext(dispatcher) {
            dbUser.child(firebaseAuth.uid.toString())
                .updateChildren(mapOf("nchild" to nChild))
            dbUser.child(firebaseAuth.uid.toString())
                .updateChildren(mapOf("name_child" to nameChild))
        }
    }


    suspend fun deleteUser(): Result<Boolean> {
        return withContext(dispatcher) {
            try {
                val uid = firebaseAuth.uid
                firebaseAuth.currentUser!!.delete().await()
                dbNickname.child(uid!!).removeValue().await()
                dbUser.child(uid).removeValue().await()
                Result.success(true)
            } catch (e: FirebaseAuthRecentLoginRequiredException) {
                Log.e("UserRemoteDataSource", e.message.toString())
                Result.failure(Throwable(e.message))
            }
        }
    }
}