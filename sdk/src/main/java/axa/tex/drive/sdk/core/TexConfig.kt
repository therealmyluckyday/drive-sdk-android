package axa.tex.drive.sdk.core


import android.content.Context
import android.os.Environment
import axa.tex.drive.sdk.BuildConfig
import axa.tex.drive.sdk.R
import axa.tex.drive.sdk.acquisition.collection.internal.db.CollectionDb
import axa.tex.drive.sdk.core.internal.util.PlatformToHostConverter
import axa.tex.drive.sdk.acquisition.internal.tracker.BatteryTracker
import axa.tex.drive.sdk.acquisition.internal.tracker.LocationTracker
import axa.tex.drive.sdk.acquisition.internal.tracker.MotionTracker
import axa.tex.drive.sdk.acquisition.internal.tracker.Tracker
import axa.tex.drive.sdk.acquisition.model.TexUser
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.orhanobut.logger.CsvFormatStrategy
import com.orhanobut.logger.DiskLogAdapter
import com.orhanobut.logger.DiskLogStrategy
import com.orhanobut.logger.Logger
import org.koin.dsl.module.Module
import org.koin.error.AlreadyStartedException
import org.koin.standalone.StandAloneContext
import java.io.File


class TexConfig {

    internal var context: Context? = null



    internal companion object {

        private var locationTrackerEnabled: Boolean = false
        private var motionTrackerEnabled: Boolean = false
        private var batteryTrackerEnabled: Boolean = false
        private var user: TexUser? = null

        private fun setupLogs(){
            val file = File(Environment.getExternalStorageDirectory(), Constants.LOG_DIR)
            if (!file.exists()) {
                file.mkdir()
            }
            Logger.addLogAdapter(DiskLogAdapter(CsvFormatStrategy.newBuilder().tag("tag").logStrategy(DiskLogStrategy(DiskLogHandler(file.absolutePath, BuildConfig.APPLICATION_ID, 500 * 1024))).build()))

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

            logger.logger.info("Configuring ssl certificate","TexConfig","init")
            SslCertificateAuthority.configureDefaultSSLSocketFactory(context.getResources().openRawResource(R.raw.tex_elb_ssl))
            logger.logger.info("Done configuring ssl certificate","TexConfig","init")

            logger.logger.info("Create koin module","TexConfig","init")
            val myModule: Module = org.koin.dsl.module.applicationContext {
                bean(LocationTracker::class.simpleName!!) { LocationTracker(context,locationTrackerEnabled) as Tracker }
                //bean(FakeLocationTracker::class.simpleName!!) { FakeLocationTracker(locationTrackerEnabled) as Tracker }
                bean(BatteryTracker::class.simpleName!!) { BatteryTracker(context, batteryTrackerEnabled) as Tracker }
                bean(MotionTracker::class.simpleName!!) { MotionTracker(context, motionTrackerEnabled) as Tracker }
                //bean { axa.tex.drive.sdk.acquisition.collection.internal.Collector(context,get(FakeLocationTracker::class.simpleName), get(BatteryTracker::class.simpleName), get(MotionTracker::class.simpleName)) } // get() will resolve service instance
                bean { axa.tex.drive.sdk.acquisition.collection.internal.Collector(context,get(LocationTracker::class.simpleName), get(BatteryTracker::class.simpleName), get(MotionTracker::class.simpleName)) }
            }
            try {
                StandAloneContext.startKoin(listOf(myModule))
            } catch (e: AlreadyStartedException) {
                e.printStackTrace()
            }
            logger.logger.info("Done create koin module","TexConfig","init")
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
        private var platform: Platform = Platform.TESTING
        @JsonProperty
        private var platformHost: String? = null;
        @JsonProperty
        private var locationTrackerEnabled: Boolean = false
        @JsonProperty
        private var motionTrackerEnabled: Boolean = false
        @JsonProperty
        private var batteryTrackerEnabled: Boolean = false

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

            TexConfig.batteryTrackerEnabled = batteryTrackerEnabled;
            TexConfig.locationTrackerEnabled = locationTrackerEnabled
            TexConfig.motionTrackerEnabled = motionTrackerEnabled

            val config = Config(batteryTrackerEnabled,locationTrackerEnabled,motionTrackerEnabled)

            CollectionDb.setConfig(context, config)

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
    }
}