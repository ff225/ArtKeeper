package com.example.artkeeper.utils

import android.app.Application
import androidx.work.WorkManager
import com.example.artkeeper.data.ArtKeeperRoomDatabase
import com.example.artkeeper.data.datasource.PostLocalDataSource
import com.example.artkeeper.data.datasource.PostRemoteDataSource
import com.example.artkeeper.data.datasource.UserLocalDataSource
import com.example.artkeeper.data.datasource.UserRemoteDataSource
import com.example.artkeeper.data.repository.EditImageRepositoryImpl
import com.example.artkeeper.data.repository.PostRepository
import com.example.artkeeper.data.repository.UserRepository

class ArtKeeper : Application() {
    private val database: ArtKeeperRoomDatabase by lazy { ArtKeeperRoomDatabase.getDatabase(this) }
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
    val editImageRepository: EditImageRepositoryImpl by lazy {
        EditImageRepositoryImpl(this)
    }
    val workManager: WorkManager by lazy { WorkManager.getInstance(this) }
}