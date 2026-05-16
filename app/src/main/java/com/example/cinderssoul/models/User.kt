package com.example.cinderssoul.models

data class User(
    val id: Int,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val createdAt: String? = null
)
