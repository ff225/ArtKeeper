package com.example.artkeeper.data.model

import android.net.Uri
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "post")
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @NonNull @ColumnInfo(name = "uid_user") val uidUser: String,
    @NonNull @ColumnInfo(name = "nickname") val nickName: String,
    @NonNull @ColumnInfo(name = "image_path") val imagePath: Uri,
    @NonNull @ColumnInfo(name = "number_of_likes") val nLike: Int = 0,
    @ColumnInfo(name = "sketched_by") val sketchedBy: String?,
    @ColumnInfo(name = "description") val description: String?,
    @NonNull @ColumnInfo(name = "is_uploaded") val isUploaded: Boolean = false,
    @NonNull @ColumnInfo(name = "post_timestamp") val postTimestamp: Long,
    //val idProfile: Int
)
