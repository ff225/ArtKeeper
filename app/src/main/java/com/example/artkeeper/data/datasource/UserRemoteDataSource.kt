package com.example.artkeeper.data.datasource

import android.util.Log
import com.example.artkeeper.data.model.Nickname
import com.example.artkeeper.data.model.User
import com.example.artkeeper.data.model.UserOnline
import com.example.artkeeper.utils.Constants.databaseRef
import com.example.artkeeper.utils.Constants.firebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRemoteDataSource(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {
    private val TAG = javaClass.simpleName

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
        Log.d(TAG, "in checkUser, ${firebaseAuth.uid.toString()}")
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

    suspend fun getAllNicknames(): Result<List<Nickname>> {
        return withContext(dispatcher) {
            val querySnapshot = dbNickname.get().await()
            val nicknamesList = mutableListOf<Nickname>()

            for (nickName in querySnapshot.children) {
                Log.d(TAG, nickName.value.toString())
                nickName.let {
                    nicknamesList.add(Nickname(nickName.key!!, nickName.value.toString()))
                }
            }
            Result.success(nicknamesList)
        }
    }

    suspend fun insertFollowingRequest(uid: String, followers: List<String>) {
        withContext(dispatcher) {
            dbUser.child(uid).updateChildren(mapOf("pendingRequest" to followers))
        }
    }

    /**
     * Se l'utente è presente torna l'oggetto da salvare su Room db.
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun getUser(uid: String): Result<UserOnline> {

        return withContext(dispatcher) {

            val querySnapshot =
                dbUser.orderByKey().equalTo(uid).get()
                    .await()

            try {
                val user =
                    querySnapshot.child(uid).value!! as HashMap<String, *>
                Log.d(TAG, "in getUser, $user")
                return@withContext Result.success(
                    UserOnline(
                        user["firstName"].toString(),
                        user["lastName"].toString(),
                        user["photo"].toString(),
                        user["uid"].toString(),
                        user["nickName"].toString(),
                        user["nchild"].toString().toInt(),
                        user["name_child"] as List<String>?,
                        user["pendingRequest"] as List<String>?,
                        user["follower"] as List<String>?
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }

        }
    }

    suspend fun addChild(nChild: Int, nameChild: List<String>) {
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
                Log.e(TAG, e.message.toString())
                Result.failure(Throwable(e.message))
            }
        }
    }
}