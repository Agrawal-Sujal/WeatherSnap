package com.trackzio.weathersnap.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberDebouncedClick(
    debounceTime: Long = 800L,
    onClick: () -> Unit
): () -> Unit {
    val clickState = remember { object { var lastTime = 0L } }
    return {
        val currentTime = System.currentTimeMillis()
        if (currentTime - clickState.lastTime > debounceTime) {
            clickState.lastTime = currentTime
            onClick()
        }
    }
}

@Composable
fun <T> rememberDebouncedClickParam(
    debounceTime: Long = 800L,
    onClick: (T) -> Unit
): (T) -> Unit {
    val clickState = remember { object { var lastTime = 0L } }
    return { param ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - clickState.lastTime > debounceTime) {
            clickState.lastTime = currentTime
            onClick(param)
        }
    }
}
