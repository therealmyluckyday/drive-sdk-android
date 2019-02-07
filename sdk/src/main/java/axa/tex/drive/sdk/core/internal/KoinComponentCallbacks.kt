package axa.tex.drive.sdk.core.internal

import android.content.ComponentCallbacks
import android.content.res.Configuration
import axa.tex.drive.sdk.core.logger.LoggerFactory

interface KoinComponentCallbacks : ComponentCallbacks{


    override fun onLowMemory() {
        val logger = LoggerFactory().getLogger(this::class.java.name).logger
        logger.warn("LOW MEMORY DETECTED", "onLowMemory")
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        val logger = LoggerFactory().getLogger(this::class.java.name).logger
        logger.warn("Configuration changed.", "onConfigurationChanged")
    }
}