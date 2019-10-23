package axa.tex.drive.sdk.core


import android.app.Notification
import android.content.Context
import axa.tex.drive.sdk.R
import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.acquisition.TripRecorderImpl
import axa.tex.drive.sdk.acquisition.collection.internal.FixProcessor
import axa.tex.drive.sdk.acquisition.collection.internal.db.CollectionDb
import axa.tex.drive.sdk.acquisition.collection.internal.db.queue.PersistentQueue
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
import axa.tex.drive.sdk.core.internal.util.PlatformToHostConverter
import axa.tex.drive.sdk.core.internal.utils.TripManager
import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.koin.dsl.module.Module
import org.koin.dsl.module.module
import org.koin.error.AlreadyStartedException
import org.koin.standalone.StandAloneContext


class TexConfig {

    internal var context: Context? = null


    internal companion object {
        private var locationTrackerEnabled: Boolean = true
        private var motionTrackerEnabled: Boolean = true
        private var batteryTrackerEnabled: Boolean = true
        internal var user: TexUser? = null
        internal var appName: String? = null
        internal var clientId: String? = null

        internal fun setupKoin(context: Context) {
            val myModule: Module = module(definition = {
                single { AutomodeHandler() }
                single { SpeedFilter() }
                single { AutoModeTracker(context) as TexActivityTracker }
                single { Automode(get()) }
                single { TripManager() }
                single { PersistentQueue(context) }
                single { ScoreRetriever() }
                single { CollectionDb(context) }
                single { FixProcessor(context) }
                single(LocationTracker::class.simpleName!!) { LocationTracker(LocationSensor(get(), get(), context, locationTrackerEnabled)) as Tracker }
                single(BatteryTracker::class.simpleName!!) { BatteryTracker(BatterySensor(context, batteryTrackerEnabled)) as Tracker }
                single(MotionTracker::class.simpleName!!) { MotionTracker(MotionSensor(context, motionTrackerEnabled)) as Tracker }
                single { axa.tex.drive.sdk.acquisition.collection.internal.Collector(context, mutableListOf<Tracker>(get(LocationTracker::class.simpleName!!), get(BatteryTracker::class.simpleName!!))) }
                single { TripRecorderImpl(context) as TripRecorder }
            })
            try {
                StandAloneContext.startKoin(listOf(myModule))
            } catch (e: AlreadyStartedException) {
                e.printStackTrace()
            }

        }


        fun loadAutoModeModule(context: Context) {

            val myModule: Module = module(definition = {
                single { AutomodeHandler() }
                single { SpeedFilter() }
                single { AutoModeTracker(context) as TexActivityTracker }
                single { Automode(get()) }
            })

            try {
                StandAloneContext.startKoin(listOf(myModule))
            } catch (e: AlreadyStartedException) {
                e.printStackTrace()
            }

        }


        internal fun init(context: Context, config: Config?) {
            val logger = LoggerFactory().getLogger(this::class.java.name)

            logger.logger.info("Initializing sdk", "init")

            TexConfig.locationTrackerEnabled = if (config == null) {
                false
            } else {
                config.locationTrackerEnabled
            }

            TexConfig.motionTrackerEnabled = if (config == null) {
                false
            } else {
                config.motionTrackerEnabled
            }

            TexConfig.batteryTrackerEnabled = if (config == null) {
                false
            } else {
                config.batteryTrackerEnabled
            }

            if (config?.appName != null) {
                TexConfig.appName = config.appName
            }

            if (config?.clientId != null) {
                TexConfig.clientId = config.clientId
            }

            logger.logger.info("Create koin module", "init")
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
        private var platformHost: String? = null;
        @JsonProperty
        private var locationTrackerEnabled: Boolean = false
        @JsonProperty
        private var motionTrackerEnabled: Boolean = false
        @JsonProperty
        private var batteryTrackerEnabled: Boolean = false
        @JsonProperty
        private var appName: String = Constants.DEFAULT_APP_NAME
        @JsonProperty
        private var clientId: String? = null
        private var customNotification: Notification? = null
        val logger = LoggerFactory().getLogger(this::class.java.name)

        constructor() {
        }

        constructor(user: TexUser?, context: Context?) {
            logger.logger.info("Configuring user and application context", "constructor(user: TexUser?, context: Context?)")
            this.user = user
            this.context = context
            TexConfig.user = user
            logger.logger.info("Done configuring user and application context", "constructor(user: TexUser?, context: Context?)")
        }

        constructor(context: Context?, appName: String, clientId: String) {
            logger.logger.info("Configuring user and application context", "constructor(context: Context?, appName: String, clientId: String)")
            this.context = context
            this.appName = appName
            this.clientId = clientId
        }

        fun build(): TexConfig {
            logger.logger.info("Building configuration", "build")
            logger.logger.info("Configuring ssl certificate", "init")
            context?.getResources()?.openRawResource(R.raw.tex_elb_ssl)?.let { CertificateAuthority.configureDefaultSSLSocketFactory(it) }
            logger.logger.info("Done configuring ssl certificate", "init")

            TexConfig.batteryTrackerEnabled = batteryTrackerEnabled;
            TexConfig.locationTrackerEnabled = locationTrackerEnabled
            TexConfig.motionTrackerEnabled = motionTrackerEnabled
            TexConfig.appName = appName
            TexConfig.clientId = clientId

            context?.let { setupKoin(it) }

            val config = Config(batteryTrackerEnabled, locationTrackerEnabled, motionTrackerEnabled, appName, clientId, platform)
            if (context != null) {
                val collectorDb = CollectionDb(context)
                collectorDb.setConfig(config)
            }

            logger.logger.info("Done building configuration", "build")
            return TexConfig(context)
        }


        fun platformHost(platform: Platform): Builder {
            logger.logger.info("Selecting platform", "build")
            this.platform = platform;
            this.platformHost = PlatformToHostConverter(platform).getHost();
            logger.logger.info("Selecting platform $platform selected", "platformHost")
            return this;
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