package com.example.artkeeper.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String,
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "last_name")val lastName: String,
    @ColumnInfo(name = "nickname")val nickName: String,
    @ColumnInfo(name = "num_child")val nChild: Int,
    //TODO risolvere problema legato all'array di stringhe name_child
    @ColumnInfo(name = "name_child") val nameChild: String
)
