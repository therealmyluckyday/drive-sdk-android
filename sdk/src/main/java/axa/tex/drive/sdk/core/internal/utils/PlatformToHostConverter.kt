package axa.tex.drive.sdk.core.internal.utils

import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.Platform.*


internal class PlatformToHostConverter(platform: Platform) {

    private var host: String = ""


    init {

        when (platform) {
            PRODUCTION ->
                this.host = "https://gw.tex.dil.services/v2.0"
            PREPROD ->
                this.host = "https://gw-preprod.tex.dil.services/v2.0"
            TESTING ->
                this.host = "https://gw-uat.tex.dil.services/v2.0"
        }
    }

    fun getHost(): String? {
        return host
    }

}
