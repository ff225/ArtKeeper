package com.example.artkeeper.data.datasource

import android.util.Log
import com.example.artkeeper.data.model.Nickname
import com.example.artkeeper.data.model.User
import com.example.artkeeper.data.model.UserOnline
import com.example.artkeeper.utils.Constants.databaseRef
import com.example.artkeeper.utils.Constants.firebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRemoteDataSource(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {
    private val TAG = javaClass.simpleName

    private val dbUser = databaseRef.getReference("users")
    private val dbNickname = databaseRef.getReference("nickname")


    suspend fun insertUser(user: User) {
        withContext(dispatcher) {

            dbUser.child(user.uid).updateChildren(
                mapOf(
                    "uid" to user.uid,
                    "firstName" to user.firstName,
                    "lastName" to user.lastName,
                    "nickName" to user.nickName,
                    "photo" to user.photo,
                    "nchild" to user.nChild,
                    "nameChild" to user.nameChild
                )
            )
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

    suspend fun insertFollowingRequest(uid: String, children: String, followers: List<String>) {
        withContext(dispatcher) {
            dbUser.child(uid).updateChildren(mapOf(children to followers))
        }
    }

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
                        user["nameChild"] as List<String>?,
                        user["pendingRequestFrom"] as List<String>?,
                        user["pendingRequestTo"] as List<String>?,
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
                .updateChildren(mapOf("nameChild" to nameChild))
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

    fun getAllNicknamePendingReq(pendingReq: List<String>): Flow<List<Nickname>> {
        return callbackFlow {
            val listener = dbNickname.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val nickNameList = mutableListOf<Nickname>()
                    for (nickname in dataSnapshot.children) {
                        if (pendingReq.contains(nickname.key)) {
                            nickNameList.add(
                                Nickname(nickname.key!!, nickname.value.toString())
                            )
                            Log.d("UserRemoteDataSource", nickname.toString())
                        }

                    }
                    trySend(nickNameList)
                }

                override fun onCancelled(error: DatabaseError) {
                    cancel()
                }
            })
            awaitClose { dbNickname.removeEventListener(listener) }
        }
    }

    fun getNickname(query: String): Flow<List<Nickname>> {
        return callbackFlow {
            val listener = dbNickname.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val nickNameList = mutableListOf<Nickname>()
                    for (nickname in dataSnapshot.children) {
                        if (nickname.value.toString().contains(query)) {
                            nickNameList.add(
                                Nickname(nickname.key!!, nickname.value.toString())
                            )
                            trySend(nickNameList)
                        }

                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    cancel()
                }
            })
            awaitClose { dbNickname.removeEventListener(listener) }
        }
    }

    fun getPendingReqFrom(): Flow<List<String>> {
        return callbackFlow {
            val pendingReqQuery = dbUser.child(firebaseAuth.uid!!)
                .child("pendingRequestFrom")
            val listener = pendingReqQuery.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    trySend(((dataSnapshot.value ?: mutableListOf<String>()) as List<String>))
                }

                override fun onCancelled(error: DatabaseError) {
                    cancel()
                }
            })
            awaitClose { pendingReqQuery.removeEventListener(listener) }
        }
    }

    fun getPendingReqTo(): Flow<List<String>> {
        return callbackFlow {
            val pendingReqQuery = dbUser.child(firebaseAuth.uid!!)
                .child("pendingRequestTo")
            val listener = pendingReqQuery.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    trySend(((dataSnapshot.value ?: mutableListOf<String>()) as List<String>))
                }

                override fun onCancelled(error: DatabaseError) {

                    cancel()
                }
            })
            awaitClose { pendingReqQuery.removeEventListener(listener) }
        }
    }

    fun getFollowers(): Flow<List<String>> {
        return callbackFlow {
            val dbUser = databaseRef.getReference("users").child(firebaseAuth.uid!!)
                .child("follower")
            val listener = dbUser.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    trySend(((dataSnapshot.value ?: mutableListOf<String>()) as List<String>))
                }

                override fun onCancelled(error: DatabaseError) {

                    cancel()
                }
            })
            awaitClose { dbUser.removeEventListener(listener) }
        }
    }

}