package axa.tex.drive.sdk.core.internal.util

import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.Platform.PRODUCTION
import axa.tex.drive.sdk.core.Platform.PREPROD
import axa.tex.drive.sdk.core.Platform.TESTING


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

    fun getHost() : String?{
        return host;
    }

}
