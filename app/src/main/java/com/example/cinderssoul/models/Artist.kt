package com.example.cinderssoul.models

data class Artist(
    val id: Int,
    val name: String,
    val bio: String? = null,
    val avatarUrl: String? = null
)
