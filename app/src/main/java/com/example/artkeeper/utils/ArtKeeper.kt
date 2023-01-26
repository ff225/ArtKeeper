package com.example.artkeeper.utils

import android.app.Application
import androidx.work.WorkManager
import com.example.artkeeper.data.PostRoomDatabase
import com.example.artkeeper.data.datasource.PostLocalDataSource
import com.example.artkeeper.data.datasource.PostRemoteDataSource
import com.example.artkeeper.data.datasource.UserLocalDataSource
import com.example.artkeeper.data.datasource.UserRemoteDataSource
import com.example.artkeeper.data.repository.PostRepository
import com.example.artkeeper.data.repository.UserRepository

class ArtKeeper : Application() {
    private val database: PostRoomDatabase by lazy { PostRoomDatabase.getDatabase(this) }
    val postRepository: PostRepository by lazy {
        PostRepository(
            PostLocalDataSource(database.postDao()),
            PostRemoteDataSource()
        )
    }
    val userRepository: UserRepository by lazy {
        UserRepository(
            UserLocalDataSource(database.userDao()),
            UserRemoteDataSource()
        )
    }
    val workManager: WorkManager by lazy { WorkManager.getInstance(this) }
}