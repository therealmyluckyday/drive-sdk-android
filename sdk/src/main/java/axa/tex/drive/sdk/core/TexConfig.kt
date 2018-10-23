package axa.tex.drive.sdk.core


import android.content.Context
import axa.tex.drive.sdk.R
import axa.tex.drive.sdk.acquisition.collection.internal.db.CollectionDb
import axa.tex.drive.sdk.core.internal.util.PlatformToHostConverter
import axa.tex.drive.sdk.acquisition.internal.tracker.BatteryTracker
import axa.tex.drive.sdk.acquisition.internal.tracker.LocationTracker
import axa.tex.drive.sdk.acquisition.internal.tracker.MotionTracker
import axa.tex.drive.sdk.acquisition.internal.tracker.Tracker
import axa.tex.drive.sdk.acquisition.internal.tracker.fake.FakeLocationTracker
import axa.tex.drive.sdk.acquisition.model.TexUser
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.koin.dsl.module.Module
import org.koin.error.AlreadyStartedException
import org.koin.standalone.StandAloneContext


class TexConfig {

    internal var context: Context? = null

    internal companion object {

        private var locationTrackerEnabled: Boolean = false
        private var motionTrackerEnabled: Boolean = false
        private var batteryTrackerEnabled: Boolean = false
        private var user: TexUser? = null



        internal fun init(context: Context, config : Config?) {

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

            SslCertificateAuthority.configureDefaultSSLSocketFactory(context.getResources().openRawResource(R.raw.tex_elb_ssl))
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

        constructor() {

        }

        constructor(user: TexUser?, context: Context?) {
            this.user = user
            this.context = context
            TexConfig.user = user
        }

        fun build(context : Context?): TexConfig {
            TexConfig.batteryTrackerEnabled = batteryTrackerEnabled;
            TexConfig.locationTrackerEnabled = locationTrackerEnabled
            TexConfig.motionTrackerEnabled = motionTrackerEnabled

            val config = Config(batteryTrackerEnabled,locationTrackerEnabled,motionTrackerEnabled)

            CollectionDb.setConfig(context, config)
            return TexConfig(context)
        }



        fun platformHost(platform: Platform): Builder {
            this.platform = platform;
            this.platformHost = PlatformToHostConverter(platform).getHost();
            return this;
        }

        fun enableLocationTracker(): Builder {
            locationTrackerEnabled = true
            return this
        }

        fun disableLocationTracker(): Builder {
            locationTrackerEnabled = false
            return this
        }

        fun enableBatteryTracker(): Builder {
            batteryTrackerEnabled = true
            return this
        }

        fun disableBatteryTracker(): Builder {
            batteryTrackerEnabled = false
            return this
        }

        fun enableMotionTracker(): Builder {
            motionTrackerEnabled = true
            return this
        }

        fun disableMotionTracker(): Builder {
            motionTrackerEnabled = false
            return this
        }
    }
}