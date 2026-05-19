package com.example.cinderssoul.models

data class User(
    val id: Int,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val role: String = "user",
    val createdAt: String? = null
) {
    val isAdmin: Boolean
        get() = role.equals("admin", ignoreCase = true)
}
