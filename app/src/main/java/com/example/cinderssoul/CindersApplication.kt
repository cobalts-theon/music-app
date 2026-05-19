package com.example.cinderssoul

import android.app.Application

/**
 * Custom Application class for Cinder's Soul app.
 * Coil3 has built-in support for animated images including GIFs, so no additional
 * configuration is needed. Just extend Application to allow for future customization.
 */
class CindersApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Coil3 automatically supports animated GIFs and WebP
        // No additional configuration needed
    }
}
