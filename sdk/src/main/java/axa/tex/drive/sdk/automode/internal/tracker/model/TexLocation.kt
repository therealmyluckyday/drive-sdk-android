package axa.tex.drive.sdk.automode.internal.tracker.model


data class TexLocation(val latitude : Float,
                       val longitude : Float,
                       val accuracy : Float,
                       val speed: Float,
                       val bearing : Float)