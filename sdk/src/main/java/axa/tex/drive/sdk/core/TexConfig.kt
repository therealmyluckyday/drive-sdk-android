package axa.tex.drive.sdk.core


import android.content.Context
import axa.tex.drive.sdk.R
import axa.tex.drive.sdk.core.internal.util.PlatformToHostConverter
import axa.tex.drive.sdk.acquisition.internal.tracker.BatteryTracker
import axa.tex.drive.sdk.acquisition.internal.tracker.MotionTracker
import axa.tex.drive.sdk.acquisition.internal.tracker.Tracker
import axa.tex.drive.sdk.acquisition.internal.tracker.fake.FakeLocationTracker
import axa.tex.drive.sdk.acquisition.model.TexUser
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



        internal fun init(context: Context) {

            SslCertificateAuthority.configureDefaultSSLSocketFactory(context.getResources().openRawResource(R.raw.tex_elb_ssl))
            val myModule: Module = org.koin.dsl.module.applicationContext {
                bean(FakeLocationTracker::class.simpleName!!) { FakeLocationTracker(context, locationTrackerEnabled) as Tracker }
                bean(BatteryTracker::class.simpleName!!) { BatteryTracker(context, batteryTrackerEnabled) as Tracker }
                bean(MotionTracker::class.simpleName!!) { MotionTracker(context, motionTrackerEnabled) as Tracker }
                bean { axa.tex.drive.sdk.acquisition.collection.internal.Collector(context,get(FakeLocationTracker::class.simpleName), get(BatteryTracker::class.simpleName), get(MotionTracker::class.simpleName)) } // get() will resolve service instance
            }
            try {
                StandAloneContext.startKoin(listOf(myModule))
            } catch (e: AlreadyStartedException) {
                e.printStackTrace()
            }
        }
    }


    private constructor(context: Context) {
        this.context = context
    }

    class Builder {

        private val user: TexUser
        private val context: Context
        private var platform: Platform = Platform.TESTING
        private var platformHost: String? = null;
        private var locationTrackerEnabled: Boolean = false
        private var motionTrackerEnabled: Boolean = false
        private var batteryTrackerEnabled: Boolean = false

        constructor(user: TexUser, context: Context) {
            this.user = user
            this.context = context
            TexConfig.user = user
        }

        fun build(): TexConfig {
            TexConfig.batteryTrackerEnabled = batteryTrackerEnabled;
            TexConfig.locationTrackerEnabled = locationTrackerEnabled
            TexConfig.motionTrackerEnabled = motionTrackerEnabled
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