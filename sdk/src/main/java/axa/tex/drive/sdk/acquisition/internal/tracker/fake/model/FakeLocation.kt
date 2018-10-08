package axa.tex.drive.sdk.acquisition.internal.tracker.fake.model


data class FakeLocation(val latitude : Double,
                        val longitude: Double,
                        val precision: Float,
                        val seed : Float,
                        val bearing : Float,
                        val altitude: Double,
                        val timestamp: Long,
                        val accuracy : Float,
                        val speed : Float,
                        val time : Long)