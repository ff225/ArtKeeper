package com.example.artkeeper.data.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "post_user",
)
data class Post(
    @PrimaryKey @ColumnInfo(name = "id_post") val idPost: String,
    @NonNull @ColumnInfo(name = "image_path") val imagePath: String,
    @ColumnInfo(name = "sketched_by") val sketchedBy: String?,
    @ColumnInfo(name = "description") val description: String?,
    @NonNull @ColumnInfo(name = "post_timestamp") val timestamp: String
)

data class PostToRemote(
    var imagePath: String = "",
    var sketchedBy: String? = null,
    var description: String? = null,
    var postTimestamp: String = ""
)


data class PostFromRemote(
    var id: String = "",
    var imagePath: String = "",
    var sketchedBy: String? = null,
    var description: String? = null,
    var postTimestamp: String = ""
)