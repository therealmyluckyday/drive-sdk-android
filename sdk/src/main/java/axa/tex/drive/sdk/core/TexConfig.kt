package axa.tex.drive.sdk.core


import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import axa.tex.drive.sdk.R
import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.acquisition.TripRecorderImpl
import axa.tex.drive.sdk.acquisition.collection.internal.FixProcessor
import axa.tex.drive.sdk.acquisition.collection.internal.db.CollectionDb
import axa.tex.drive.sdk.core.internal.util.PlatformToHostConverter
import axa.tex.drive.sdk.acquisition.internal.tracker.Tracker
import axa.tex.drive.sdk.acquisition.internal.sensor.BatterySensor
import axa.tex.drive.sdk.acquisition.internal.sensor.LocationSensor
import axa.tex.drive.sdk.acquisition.internal.sensor.MotionSensor
import axa.tex.drive.sdk.acquisition.internal.tracker.BatteryTracker
import axa.tex.drive.sdk.acquisition.internal.tracker.LocationTracker
import axa.tex.drive.sdk.acquisition.internal.tracker.MotionTracker
import axa.tex.drive.sdk.acquisition.model.TexUser
import axa.tex.drive.sdk.acquisition.score.ScoreRetriever
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.koin.android.ext.android.inject
import org.koin.dsl.module.Module
import org.koin.dsl.module.module
import org.koin.error.AlreadyStartedException
import org.koin.standalone.StandAloneContext


class TexConfig {

    internal var context: Context? = null

    //internal var trackers : MutableList<Tracker>  = mutableListOf()


    internal companion object {

        private var locationTrackerEnabled: Boolean = false
        private var motionTrackerEnabled: Boolean = false
        private var batteryTrackerEnabled: Boolean = false
        internal var user: TexUser? = null
        internal var appName : String? = null//Constants.DEFAULT_APP_NAME
        internal var clientId : String? = null//Constants.DEFAULT_CLIENT_ID




        private fun setupKoin(context: Context){
            val myModule: Module = module(definition = {

                single { ScoreRetriever() }
                single { CollectionDb(context)}
                single { FixProcessor(context)}
                single(LocationTracker::class.simpleName!!) { LocationTracker(LocationSensor(context, locationTrackerEnabled)) as Tracker }
                single(BatteryTracker::class.simpleName!!) { BatteryTracker(BatterySensor(context, batteryTrackerEnabled)) as Tracker }
                single(MotionTracker::class.simpleName!!) { MotionTracker(MotionSensor(context, motionTrackerEnabled)) as Tracker }

                single { axa.tex.drive.sdk.acquisition.collection.internal.Collector(context,mutableListOf<Tracker>(get(LocationTracker::class.simpleName!!), get(BatteryTracker::class.simpleName!!), get(MotionTracker::class.simpleName!!))) }

                single { TripRecorderImpl(context) as TripRecorder}
            })
            try {
                StandAloneContext.startKoin(listOf(myModule))
            } catch (e: AlreadyStartedException) {
                e.printStackTrace()
            }

        }


        internal fun init(context: Context, config : Config?) {

            //setupLogs()

            val logger = LoggerFactory.getLogger()

            logger.logger.info("Initializing sdk","TexConfig","init")

            TexConfig.locationTrackerEnabled = if(config == null){
               false
            }else{
                config.locationTrackerEnabled
            }

            TexConfig.motionTrackerEnabled = if(config == null){
                false
            }else{
                config.motionTrackerEnabled
            }

            TexConfig.batteryTrackerEnabled = if(config == null){
                false
            }else{
                config.batteryTrackerEnabled
            }

            if(config?.appName!= null){
                TexConfig.appName = config.appName
            }

            if(config?.clientId != null){
                TexConfig.clientId= config.clientId
            }



            logger.logger.info("Create koin module","TexConfig","init")
            setupKoin(context)
            /*val myModule: Module = org.koin.dsl.module.applicationContext {

                single { CollectionDb(context)}

                single { FixProcessor()}

                single(LocationTracker::class.simpleName!!) { LocationTracker(LocationSensor(context, locationTrackerEnabled)) as Tracker }
                //bean(LocationTracker::class.simpleName!!) { LocationTracker(context,locationTrackerEnabled) as Tracker }


                single(BatteryTracker::class.simpleName!!) { BatteryTracker(BatterySensor(context, batteryTrackerEnabled)) as Tracker }
                //bean(BatteryTracker::class.simpleName!!) { BatteryTracker(context, batteryTrackerEnabled) as Tracker }


                single(MotionTracker::class.simpleName!!) { MotionTracker(MotionSensor(context, motionTrackerEnabled)) as Tracker }
                //bean(MotionTracker::class.simpleName!!) { MotionTracker(context, motionTrackerEnabled) as Tracker }




                //bean { axa.tex.drive.sdk.acquisition.collection.internal.Collector(context,get(FakeLocationTracker::class.simpleName), get(BatteryTracker::class.simpleName), get(MotionTracker::class.simpleName)) } // get() will resolve service instance
                //bean { axa.tex.drive.sdk.acquisition.collection.internal.Collector(context,get(LocationTracker::class.simpleName), get(BatteryTracker::class.simpleName), get(MotionTracker::class.simpleName)) }


                single { axa.tex.drive.sdk.acquisition.collection.internal.Collector(context,mutableListOf<Tracker>(get(LocationTracker::class.simpleName!!), get(BatteryTracker::class.simpleName!!), get(MotionTracker::class.simpleName!!))) }
            }
            try {
                StandAloneContext.startKoin(listOf(myModule))
            } catch (e: AlreadyStartedException) {
                e.printStackTrace()
            }*/
            logger.logger.info("Done create koin module","TexConfig","init")
        }



    }

    private constructor(context: Context?) {
        this.context = context
        //this.trackers = trackers
    }



    class Builder{


        @JsonProperty
        private var user: TexUser? = null
        @JsonIgnore
        private var context: Context? = null
        @JsonIgnore
        private var platform: Platform = Platform.TESTING
        @JsonProperty
        private var platformHost: String? = null;
        @JsonProperty
        private var locationTrackerEnabled: Boolean = false
        @JsonProperty
        private var motionTrackerEnabled: Boolean = false
        @JsonProperty
        private var batteryTrackerEnabled: Boolean = false
        @JsonProperty
        private var appName : String = Constants.DEFAULT_APP_NAME

        @JsonProperty
        private var clientId : String? = null




        val logger = LoggerFactory.getLogger()



        constructor() {

        }

        constructor(user: TexUser?, context: Context?) {

            logger.logger.info("Configuring user and application context","TexConfig.Builder","constructor(user: TexUser?, context: Context?)")
            this.user = user
            this.context = context
            TexConfig.user = user
            logger.logger.info("Done configuring user and application context","TexConfig.Builder","constructor(user: TexUser?, context: Context?)")

        }

        fun build(context : Context?): TexConfig {
            logger.logger.info("Building configuration","TexConfig.Builder","build")


            logger.logger.info("Configuring ssl certificate","TexConfig","init")
            context?.getResources()?.openRawResource(R.raw.tex_elb_ssl)?.let { CertificateAuthority.configureDefaultSSLSocketFactory(it) }
            logger.logger.info("Done configuring ssl certificate","TexConfig","init")



            TexConfig.batteryTrackerEnabled = batteryTrackerEnabled;
            TexConfig.locationTrackerEnabled = locationTrackerEnabled
            TexConfig.motionTrackerEnabled = motionTrackerEnabled
            TexConfig.appName = appName
            TexConfig.clientId = clientId

            context?.let { setupKoin(it) }

            val config = Config(batteryTrackerEnabled,locationTrackerEnabled,motionTrackerEnabled, appName, clientId)
            if (context != null) {

               // val collectorDb : CollectionDb by inject()

                val collectorDb = CollectionDb(context)
                collectorDb.setConfig(config)
            }



            logger.logger.info("Done building configuration","TexConfig.Builder","build")
            return TexConfig(context)
        }



        fun platformHost(platform: Platform): Builder {
            logger.logger.info("Selecting platform","TexConfig.Builder","build")
            this.platform = platform;
            this.platformHost = PlatformToHostConverter(platform).getHost();
            logger.logger.info("Selecting platform $platform selected","TexConfig.Builder","platformHost")
            return this;
        }

        fun enableLocationTracker(): Builder {
            logger.logger.info("Enabling location tracker","TexConfig.Builder","enableLocationTracker")
            locationTrackerEnabled = true
            logger.logger.info("Location tracker Enabled","TexConfig.Builder","enableLocationTracker")
            return this
        }

        fun disableLocationTracker(): Builder {
            logger.logger.info("Disabling location tracker","TexConfig.Builder","enableLocationTracker")
            locationTrackerEnabled = false
            logger.logger.info("Location tracker disable","TexConfig.Builder","enableLocationTracker")

            return this
        }

        fun enableBatteryTracker(): Builder {
            logger.logger.info("Enabling battery tracker","TexConfig.Builder","enableBatteryTracker")

            batteryTrackerEnabled = true
            logger.logger.info("Battery tracker enabled","TexConfig.Builder","enableBatteryTracker")
            return this
        }

        fun disableBatteryTracker(): Builder {
            logger.logger.info("Disabling battery tracker","TexConfig.Builder","disableBatteryTracker")
            batteryTrackerEnabled = false
            logger.logger.info("Battery tracker disable","TexConfig.Builder","disableBatteryTracker")

            return this
        }

        fun enableMotionTracker(): Builder {
            logger.logger.info("Enabling motion tracker","TexConfig.Builder","enableMotionTracker")

            motionTrackerEnabled = true
            logger.logger.info("Battery tracker enabled","TexConfig.Builder","enableMotionTracker")
            return this
        }

        fun disableMotionTracker(): Builder {
            logger.logger.info("Disabling motion tracker","TexConfig.Builder","disableMotionTracker")

            motionTrackerEnabled = false
            logger.logger.info("Motion tracker disabled","TexConfig.Builder","disableMotionTracker")

            return this
        }

        fun withAppName(appName : String): Builder{
            this.appName = appName
            return this
        }

        fun withClientId(clientId : String): Builder{
            this.clientId = clientId
            return this
        }
    }
}