package axa.tex.drive.sdk.core


import android.content.Context
import axa.tex.drive.sdk.R
import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.acquisition.TripRecorderImpl
import axa.tex.drive.sdk.acquisition.collection.internal.Collector
import axa.tex.drive.sdk.acquisition.collection.internal.FixProcessor
import axa.tex.drive.sdk.acquisition.collection.internal.db.CollectionDb
import axa.tex.drive.sdk.acquisition.internal.sensor.BatterySensor
import axa.tex.drive.sdk.acquisition.internal.sensor.LocationSensor
import axa.tex.drive.sdk.acquisition.internal.sensor.MotionSensor
import axa.tex.drive.sdk.acquisition.internal.tracker.BatteryTracker
import axa.tex.drive.sdk.acquisition.internal.tracker.LocationTracker
import axa.tex.drive.sdk.acquisition.internal.tracker.MotionTracker
import axa.tex.drive.sdk.acquisition.internal.tracker.Tracker
import axa.tex.drive.sdk.acquisition.model.TexUser
import axa.tex.drive.sdk.acquisition.score.ScoreRetriever
import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.automode.internal.Automode
import axa.tex.drive.sdk.automode.internal.tracker.AutoModeTracker
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.automode.internal.tracker.TexActivityTracker
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.internal.utils.TripManager
import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module


class TexConfig {

    internal var context: Context? = null


    internal companion object {
        var config: Config? = null
        internal var user: TexUser? = null

        val logger = LoggerFactory().getLogger(this::class.java.name)
        internal fun setupKoin(context: Context) {
            val myModule = module {
                single { AutomodeHandler() }
                single { SpeedFilter() }
                single { AutoModeTracker(context) as TexActivityTracker }
                single { Automode(get()) }
                single { TripManager() }
                single { ScoreRetriever(context ) }
                single { CollectionDb(context) }
                single { FixProcessor(context) }
                single {
                    LocationTracker(
                            LocationSensor(
                                    get(),
                                    get(),
                                    context,
                                    config?.locationTrackerEnabled!!)
                    )}
                single {
                    BatteryTracker(
                            BatterySensor(
                            context,
                            config?.batteryTrackerEnabled!!)
                    )}
                single {
                    MotionTracker(
                            MotionSensor(
                            context,
                            config?.motionTrackerEnabled!!)
                    )}
                single {
                    Collector(context,
                            mutableListOf<Tracker>(
                            get<LocationTracker>(),
                            get<BatteryTracker>())
                    ) }
                single { TripRecorderImpl(context) as TripRecorder } bind TripRecorder::class
            }
            try {
                startKoin {
                    // module list
                    modules(listOf(myModule))
                }
            } catch (e: Exception) {
                logger.logger.error("Exception during setup koin", "setupKoin")
            }
        }

        fun loadAutoModeModule(context: Context) {

            val myModule = module {
                single { AutomodeHandler() }
                single { SpeedFilter() }
                single { AutoModeTracker(context) as TexActivityTracker }
                single { Automode(get()) }
            }

            try {
                startKoin {
                    // module list
                    modules(listOf(myModule))
                }
            } catch (e: Exception) {
                logger.logger.error("Exception during koin load automodule", "loadAutoModeModule")
            }
        }

        internal fun init(config: Config) {
            val logger = LoggerFactory().getLogger(this::class.java.name)
            logger.logger.info("Initializing sdk", "init")
            TexConfig.config = config
        }
    }

    private constructor(context: Context?) {
        this.context = context
    }

    class Builder {
        @JsonProperty
        private var user: TexUser? = null
        @JsonIgnore
        private var context: Context? = null
        @JsonIgnore
        private var platform: Platform = Platform.PRODUCTION
        @JsonProperty
        private var locationTrackerEnabled: Boolean = false
        @JsonProperty
        private var motionTrackerEnabled: Boolean = false
        @JsonProperty
        private var batteryTrackerEnabled: Boolean = false
        @JsonProperty
        private var appName: String = Constants.DEFAULT_APP_NAME
        @JsonProperty
        private var clientId: String
        val logger = LoggerFactory().getLogger(this::class.java.name)

        constructor(user: TexUser?, context: Context?, clientId: String) {
            logger.logger.info("Done configuring user and application context", "constructor(user: TexUser?, context: Context?)")
            this.user = user
            this.context = context
            TexConfig.user = user
            this.clientId = clientId
        }

        constructor(context: Context?, appName: String, clientId: String) {
            logger.logger.info("Configuring user and application context", "constructor(context: Context?, appName: String, clientId: String)")
            this.context = context
            this.appName = appName
            this.clientId = clientId
        }

        fun build(): TexConfig {
            logger.logger.info("Building configuration", "build")
            if (context == null) {
                logger.logger.error("No context error", "build")
            }
            if (context?.resources == null) {
                logger.logger.error("No resources error", "build")
            }
            if (context?.resources?.openRawResource(R.raw.tex_elb_ssl) == null) {
                logger.logger.error("No tex_elb_ssl error", "build")
            }
            try {
                context?.resources?.openRawResource(R.raw.tex_elb_ssl)?.let { CertificateAuthority.configureDefaultSSLSocketFactory(it) }
            } catch (e: Exception) {

                logger.logger.error("Exception"+e.toString(), "build")
            }
            logger.logger.info("Done configuring ssl certificate", "init")

            val config = Config(batteryTrackerEnabled, locationTrackerEnabled, motionTrackerEnabled, appName, clientId, platform)
            TexConfig.config = config

            logger.logger.info("Create koin module", "build")
            context?.let { setupKoin(it) }

            logger.logger.info("Done building configuration", "build")
            val texconfig = TexConfig(context)
            TexConfig.config = config
            return texconfig
        }


        fun platformHost(platform: Platform): Builder {
            logger.logger.info("Selecting platform", "build")
            this.platform = platform
            logger.logger.info("Selecting platform $platform selected", "platformHost")
            return this
        }

        fun enableLocationTracker(): Builder {
            logger.logger.info("Enabling location tracker", "enableLocationTracker")
            locationTrackerEnabled = true
            logger.logger.info("Location tracker Enabled", "enableLocationTracker")
            return this
        }

        fun disableLocationTracker(): Builder {
            logger.logger.info("Disabling location tracker", "enableLocationTracker")
            locationTrackerEnabled = false
            logger.logger.info("Location tracker disable", "enableLocationTracker")

            return this
        }

        fun enableBatteryTracker(): Builder {
            logger.logger.info("Enabling battery tracker", "enableBatteryTracker")

            batteryTrackerEnabled = true
            logger.logger.info("Battery tracker enabled", "enableBatteryTracker")
            return this
        }

        fun disableBatteryTracker(): Builder {
            logger.logger.info("Disabling battery tracker", "disableBatteryTracker")
            batteryTrackerEnabled = false
            logger.logger.info("Battery tracker disable", "disableBatteryTracker")

            return this
        }

        fun enableMotionTracker(): Builder {
            logger.logger.info("Enabling motion tracker", "enableMotionTracker")

            motionTrackerEnabled = true
            logger.logger.info("Battery tracker enabled", "enableMotionTracker")
            return this
        }

        fun disableMotionTracker(): Builder {
            logger.logger.info("Disabling motion tracker", "disableMotionTracker")

            motionTrackerEnabled = false
            logger.logger.info("Motion tracker disabled", "disableMotionTracker")

            return this
        }

        fun enableTrackers(): Builder {
            return enableBatteryTracker().enableLocationTracker()
                    .enableMotionTracker()
        }

        fun withAppName(appName: String): Builder {
            this.appName = appName
            return this
        }

        fun withClientId(clientId: String): Builder {
            this.clientId = clientId
            return this
        }
    }
}