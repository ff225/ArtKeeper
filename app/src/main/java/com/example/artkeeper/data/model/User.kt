package com.example.artkeeper.data.model

data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val nickName: String,
    val nChild: Int,
    val nameChild: List<String>,
    val nPosts: Int
)
