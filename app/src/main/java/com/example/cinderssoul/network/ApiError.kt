package com.example.cinderssoul.network

import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException

fun Throwable.toApiMessage(fallback: String = "Something went wrong."): String {
    if (this is HttpException) {
        val parsedMessage = response()?.errorBody()?.string()?.let(::parseApiErrorMessage)
        if (!parsedMessage.isNullOrBlank()) return parsedMessage

        return when (code()) {
            400 -> "Please check the information and try again."
            401 -> "Authentication failed. Please try again."
            404 -> "No matching account was found."
            409 -> "This email is already registered."
            else -> fallback
        }
    }

    return message?.takeIf { it.isNotBlank() } ?: fallback
}

private fun parseApiErrorMessage(body: String): String? {
    return runCatching {
        val json = JSONObject(body)
        val errorsMessage = json.optJSONArray("errors")?.toErrorMessage()
        errorsMessage ?: json.optString("message").takeIf { it.isNotBlank() }?.normalizeMessage()
    }.getOrNull()
}

private fun JSONArray.toErrorMessage(): String? {
    val messages = (0 until length()).mapNotNull { index ->
        optJSONObject(index)?.optString("message")?.takeIf { it.isNotBlank() }
    }
    return messages.joinToString("\n").takeIf { it.isNotBlank() }
}

private fun String.normalizeMessage(): String {
    val trimmed = trim()
    if (!trimmed.startsWith("[")) return trimmed

    return runCatching {
        JSONArray(trimmed).toErrorMessage()
    }.getOrNull() ?: trimmed
}
