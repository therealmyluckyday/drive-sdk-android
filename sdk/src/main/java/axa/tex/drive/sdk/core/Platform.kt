package axa.tex.drive.sdk.core

enum class Platform () {
    PRODUCTION,
    TESTING,
    INTEGRATION,
    PREPROD;

    fun generateUrl(isAPIV2: Boolean): String {
        if (isAPIV2) {
            when (this) {
                PRODUCTION ->
                    return "https://mobile-sink.youdrive.next.dil.services"
                PREPROD ->
                    return "https://mobile-sink.youdrive-pp.next.dil.services"
                TESTING ->
                    return "https://mobile-sink.youdrive-dev.next.dil.services/mobile"
                INTEGRATION ->
                    return "https://mobile-sink.youdrive-uat.next.dil.services"
            }
        } else {
            when (this) {
                PRODUCTION ->
                    return "https://gw.tex.dil.services/v2.0"
                PREPROD ->
                    return "https://gw-preprod.tex.dil.services/v2.0"
                TESTING ->
                    return "https://gw-uat.tex.dil.services/v2.0"
                INTEGRATION ->
                    return "https://gw-int.tex.dil.services/v2.0"
            }
        }
    }
}