package axa.tex.drive.sdk.core


import android.content.Context
import axa.tex.drive.sdk.R
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import java.io.BufferedInputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.*

class CertificateAuthority: KoinComponentCallbacks {
    var certAuthInputStream: InputStream? = null
    var sslSocketFactory: SSLSocketFactory? = null
    constructor(appContext: Context) {
        try {
            certAuthInputStream = appContext.resources?.openRawResource(R.raw.tex_elb_ssl)
            sslSocketFactory = createSSLSocketFactory(this.certAuthInputStream!!)
        } catch (e: Exception) {
            LOGGER.error("Exception"+e.toString(), "build")
        }
    }
    companion object {
        internal val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
        private class UnifiedTrustManager @Throws(KeyStoreException::class)
        constructor(localKeyStore: KeyStore) : X509TrustManager {
            private var defaultTrustManager: X509TrustManager? = null
            private var localTrustManager: X509TrustManager? = null

            init {
                try {
                    this.defaultTrustManager = createTrustManager(null)
                    this.localTrustManager = createTrustManager(localKeyStore)
                } catch (e: NoSuchAlgorithmException) {
                    LOGGER.warn("Error ${e.message}", "constructor")
                }

            }

            @Throws(NoSuchAlgorithmException::class, KeyStoreException::class)
            private fun createTrustManager(store: KeyStore?): X509TrustManager {
                val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
                val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
                tmf.init(store)
                val trustManagers = tmf.trustManagers
                return trustManagers[0] as X509TrustManager
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                try {
                    defaultTrustManager!!.checkServerTrusted(chain, authType)
                } catch (ce: CertificateException) {
                    localTrustManager!!.checkServerTrusted(chain, authType)
                }

            }

            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                try {
                    defaultTrustManager!!.checkClientTrusted(chain, authType)
                } catch (ce: CertificateException) {
                    localTrustManager!!.checkClientTrusted(chain, authType)
                }

            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                val first = defaultTrustManager!!.acceptedIssuers
                val second = localTrustManager!!.acceptedIssuers
                val result = Arrays.copyOf(first, first.size + second.size)
                System.arraycopy(second, 0, result, first.size, second.size)
                return result
            }
        }

        fun createSSLSocketFactory(certAuthInputStream: InputStream): SSLSocketFactory? {
           try {
                // Load CAs from an InputStream
                // (could be from a resource or ByteArrayInputStream or ...)
                val cf = CertificateFactory.getInstance("X.509")
                val caInput = BufferedInputStream(certAuthInputStream)
                val ca: Certificate
                try {
                    ca = cf.generateCertificate(caInput)
                } finally {
                    caInput.close()
                }

                // Create a KeyStore containing our trusted CAs
                val keyStoreType = KeyStore.getDefaultType()
                val keyStore = KeyStore.getInstance(keyStoreType)
                keyStore.load(null, null)
                keyStore.setCertificateEntry("ca", ca)

                // Create a TrustManager that trusts the CAs in our KeyStore and system CA
                val trustManager = UnifiedTrustManager(keyStore)

                // Create an SSLContext that uses our TrustManager
                val context = SSLContext.getInstance("TLS")
                context.init(null, arrayOf<TrustManager>(trustManager), null)

                // Tell the URLConnection to use a SocketFactory from our SSLContext
                return context.socketFactory

            } catch (e: Exception) {
                LOGGER.error("Error ${e.message}", "createSSLSocketFactory")
                return null
            }
        }
    }
    fun configureSSLSocketFactory(httpsURLConnection: HttpsURLConnection) {
        try {
            httpsURLConnection.setSSLSocketFactory(sslSocketFactory)
        } catch (e: Exception) {
            LOGGER.error("Error ${e.message}", "createSSLSocketFactory")
        }
    }
}
