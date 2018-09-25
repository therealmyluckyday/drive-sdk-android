package axa.tex.drive.sdk.acquisition.model

open class Fix {
    private val timestamp: Long

    constructor(timestamp: Long) {
        this.timestamp = timestamp
    }

    fun timestamp() : Long{
        return timestamp
    }
}