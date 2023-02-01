package com.example.artkeeper.data.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "users", primaryKeys = ["uid"])
data class User(
    @NonNull @ColumnInfo(name = "uid") val uid: String,
    @NonNull @ColumnInfo(name = "first_name") val firstName: String,
    @NonNull @ColumnInfo(name = "last_name") val lastName: String,
    @NonNull @ColumnInfo(name = "photo_url") val photo: String,
    @NonNull @ColumnInfo(name = "nickname") val nickName: String,
    @NonNull @ColumnInfo(name = "num_child") val nChild: Int,
    @ColumnInfo(name = "name_child") val nameChild: List<String>?
)

@Entity(tableName = "nickname_users", primaryKeys = ["uid"])
data class Nickname(
    @NonNull @ColumnInfo(name = "uid") val uid: String,
    @NonNull @ColumnInfo(name = "nickname") val nickName: String
)

data class UserOnline(
    var firstName: String? = "",
    var lastName: String? = "",
    var photoUser: String? = "",
    var uid: String? = "",
    var nickName: String? = "",
    var nChild: Int? = 0,
    var nameChild: List<String>? = listOf()
)