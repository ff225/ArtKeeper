package com.example.artkeeper.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String,
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "last_name") val lastName: String,
    @ColumnInfo(name = "nickname") val nickName: String,
    @ColumnInfo(name = "num_child") val nChild: Int,
    @ColumnInfo(name = "name_child") val nameChild: List<String>?
)


data class UserOnline(
    var firstName: String? = "",
    var lastName: String? = "",
    var uid: String? = "",
    var nickName: String? = "",
    var nChild: Int? = 0,
    var nameChild: List<String>? = listOf("")
)
