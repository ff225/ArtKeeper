package com.example.artkeeper.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.artkeeper.data.model.Post
import com.example.artkeeper.data.model.User
import com.example.artkeeper.utils.Converters

@Database(entities = [Post::class, User::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PostRoomDatabase : RoomDatabase() {

    abstract fun postDao(): PostDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: PostRoomDatabase? = null
        fun getDatabase(context: Context): PostRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PostRoomDatabase::class.java,
                    "art_keeper_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}