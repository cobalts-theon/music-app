package com.example.cinderssoul.models

import com.google.firebase.Timestamp

data class User(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoURL: String = "",
    val createdAt: Timestamp? = null,
    val lastLogin: Timestamp? = null,
    val subscription: Subscription = Subscription(),
    val preferences: UserPreferences = UserPreferences()
)

data class Subscription(
    val type: String = "free", // "free" or "premium"
    val expiresAt: Timestamp? = null
)

data class UserPreferences(
    val theme: String = "light", // "light" or "dark"
    val quality: String = "medium", // "low", "medium", "high"
    val downloadOverWifiOnly: Boolean = true
)
