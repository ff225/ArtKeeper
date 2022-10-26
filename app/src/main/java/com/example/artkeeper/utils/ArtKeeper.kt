package com.example.artkeeper.utils

import android.app.Application
import com.example.artkeeper.data.PostRoomDatabase

class ArtKeeper : Application() {
    val database: PostRoomDatabase by lazy { PostRoomDatabase.getDatabase(this) }
}