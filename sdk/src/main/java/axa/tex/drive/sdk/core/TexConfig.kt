package axa.tex.drive.sdk.core


import android.content.Context
import axa.tex.drive.sdk.R
import axa.tex.drive.sdk.acquisition.SensorService
import axa.tex.drive.sdk.acquisition.SensorServiceImpl
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
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.internal.utils.TripManager
import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.android.gms.location.FusedLocationProviderClient
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module

class TexConfig {

    internal var context: Context? = null
    var texSDKRXScheduler: Scheduler? = null
    var sensorService: SensorService? = null

    internal companion object {
        var config: Config? = null
        internal var user: TexUser? = null
        
        private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
        
        internal fun setupKoin(context: Context, scheduler: Scheduler = Schedulers.single(), sensorService: SensorService) {
            val myModule = module {
                single { SpeedFilter() }
                single { Automode(sensorService, scheduler) }
                single { TripManager() }
                single { ScoreRetriever(context ) }
                single { CollectionDb(context) }
                single { FixProcessor(context) }
                single { CertificateAuthority(context) }
                single {
                    LocationTracker(
                            LocationSensor(
                                    get(),
                                    sensorService,
                                    sensorService.speedFilter(),
                                    context,
                                    if (config!=null) {config!!.locationTrackerEnabled} else {false})
                    )}
                single {
                    BatteryTracker(
                            BatterySensor(
                            context,
                                    if (config!=null) {config!!.batteryTrackerEnabled} else {false} )
                    )}
                single {
                    MotionTracker(
                            MotionSensor(
                            context,
                                    if (config!=null) {config!!.motionTrackerEnabled} else {false})
                    )}
                single {
                    Collector(context,
                            mutableListOf<Tracker>(
                            get<LocationTracker>(),
                            get<BatteryTracker>()),
                            scheduler
                    ) }
                single { TripRecorderImpl(context, sensorService, scheduler) as TripRecorder } bind TripRecorder::class
            }
            try {
                startKoin {
                    // module list
                    modules(listOf(myModule))
                }
            } catch (e: Exception) {
                LOGGER.error("Exception during setup koin: "+e, "setupKoin")
            }
        }

        fun loadAutoModeModule(context: Context) {
            val scheduler = Schedulers.single()
            val sensorService = SensorServiceImpl(context, scheduler, null)
            val myModule = module {
                single { SpeedFilter() }
                single { Automode(get(), scheduler) }
            }

            try {
                startKoin {
                    // module list
                    modules(listOf(myModule))
                }
            } catch (e: Exception) {
                LOGGER.error("Exception during koin load automodule", "loadAutoModeModule")
            }
        }

        internal fun init(config: Config) {
            LOGGER.info("Initializing sdk", "init")
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
        private val scheduler: Scheduler

        private val sensorService: SensorService

        constructor(user: TexUser?, context: Context, clientId: String, sensor: SensorService, rxScheduler: Scheduler = Schedulers.single()) {
            LOGGER.info("Done configuring user and application context", "constructor(user: TexUser?, context: Context?)")
            this.user = user
            this.context = context
            TexConfig.user = user
            this.clientId = clientId
            this.scheduler = rxScheduler
            this.sensorService = sensor
        }

        constructor(user: TexUser?, context: Context, clientId: String, rxScheduler: Scheduler = Schedulers.single()) {
            LOGGER.info("Done configuring user and application context", "constructor(user: TexUser?, context: Context?)")
            this.user = user
            this.context = context
            TexConfig.user = user
            this.clientId = clientId
            this.scheduler = rxScheduler
            this.sensorService = SensorServiceImpl(context, rxScheduler, null)
        }

        constructor(context: Context, appName: String, clientId: String, sensor: SensorService, rxScheduler: Scheduler = Schedulers.single()) {
            LOGGER.info("Configuring user and application context", "constructor(context: Context?, appName: String, clientId: String)")
            this.context = context
            this.appName = appName
            this.clientId = clientId
            this.scheduler = rxScheduler
            this.sensorService = sensor
        }

        constructor(context: Context, appName: String, clientId: String, fusedLocationClient: FusedLocationProviderClient, rxScheduler: Scheduler = Schedulers.single()) {
            LOGGER.info("Configuring user and application context", "constructor(context: Context?, appName: String, clientId: String)")
            this.context = context
            this.appName = appName
            this.clientId = clientId
            this.scheduler = rxScheduler
            this.sensorService = SensorServiceImpl(context, rxScheduler, fusedLocationClient)
        }

        fun build(): TexConfig {
            LOGGER.info("Building configuration", "build")
            if (context == null) {
                LOGGER.error("No context error", "build")
            }
            if (context?.resources == null) {
                LOGGER.error("No resources error", "build")
            }
            if (context?.resources?.openRawResource(R.raw.tex_elb_ssl) == null) {
                LOGGER.error("No tex_elb_ssl error", "build")
            }

            LOGGER.info("Done configuring ssl certificate", "init")

            val config = Config(batteryTrackerEnabled, locationTrackerEnabled, motionTrackerEnabled, appName, clientId, platform, this.scheduler)
            TexConfig.config = config
            LOGGER.info("Create koin module", "build")

            context?.let {
                setupKoin(it, scheduler, this.sensorService!!)
            }

            LOGGER.info("Done building configuration", "build")
            val texconfig = TexConfig(context)
            TexConfig.config = config
            texconfig.texSDKRXScheduler = scheduler
            texconfig.sensorService = this.sensorService
            return texconfig
        }


        fun platformHost(platform: Platform): Builder {
            LOGGER.info("Selecting platform", "build")
            this.platform = platform
            LOGGER.info("Selecting platform $platform selected", "platformHost")
            return this
        }

        fun enableLocationTracker(): Builder {
            LOGGER.info("Enabling location tracker", "enableLocationTracker")
            locationTrackerEnabled = true
            LOGGER.info("Location tracker Enabled", "enableLocationTracker")
            return this
        }

        fun disableLocationTracker(): Builder {
            LOGGER.info("Disabling location tracker", "enableLocationTracker")
            locationTrackerEnabled = false
            LOGGER.info("Location tracker disable", "enableLocationTracker")

            return this
        }

        fun enableBatteryTracker(): Builder {
            LOGGER.info("Enabling battery tracker", "enableBatteryTracker")

            batteryTrackerEnabled = true
            LOGGER.info("Battery tracker enabled", "enableBatteryTracker")
            return this
        }

        fun disableBatteryTracker(): Builder {
            LOGGER.info("Disabling battery tracker", "disableBatteryTracker")
            batteryTrackerEnabled = false
            LOGGER.info("Battery tracker disable", "disableBatteryTracker")

            return this
        }

        fun enableMotionTracker(): Builder {
            LOGGER.info("Enabling motion tracker", "enableMotionTracker")

            motionTrackerEnabled = true
            LOGGER.info("Battery tracker enabled", "enableMotionTracker")
            return this
        }

        fun disableMotionTracker(): Builder {
            LOGGER.info("Disabling motion tracker", "disableMotionTracker")

            motionTrackerEnabled = false
            LOGGER.info("Motion tracker disabled", "disableMotionTracker")

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