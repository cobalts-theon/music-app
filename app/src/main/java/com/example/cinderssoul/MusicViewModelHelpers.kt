package com.example.cinderssoul

import androidx.media3.common.C
import androidx.media3.common.Player
import com.example.cinderssoul.network.ApiEnvelope

internal fun Int.toUiRepeatMode(): RepeatUiMode {
    return when (this) {
        Player.REPEAT_MODE_ONE -> RepeatUiMode.ONE
        Player.REPEAT_MODE_ALL -> RepeatUiMode.ALL
        else -> RepeatUiMode.OFF
    }
}

internal fun Long.safeDuration(): Long {
    return if (this == C.TIME_UNSET || this < 0L) 0L else this
}

internal fun <T> ApiEnvelope<T>.requireData(): T {
    return data ?: throw IllegalStateException(message ?: "Backend returned no data")
}
