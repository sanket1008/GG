package com.unc.gearupvr.model

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import com.unc.gearupvr.components.video_player.VideoPlayerActivity
import com.unc.gearupvr.components.video_player.VrVideoActivity
import java.util.*


class GearupApp : Application() {
    companion object {
        var ctx: Context? = null
        var deviceId: String? = null

        const val SHARED_PREFERENCES: String = "MyPref"
        const val IS_ANALYTICS_SUBMITTED: String = "IsAnalyticsSubmitted"
        const val NAV_TITLE: String = "navigationTitle"
        const val SSL_ERROR_CODE = 500

        /* hash map key should be BuildConfig.FLAVOR
        * the USERNAME and PASSWORD is used in ApiRequest.authenticate function*/
        val USERNAME = hashMapOf<String, String>(
            "uncDev" to "rdc3-r8q5",
            "uncQa" to "device_user",
            "uncUat" to "device_user",
            "uncProd" to "ewbs-yzbg",
            "mississippiDev" to "mississippi",
            "mississippiQa" to "mississippi",
            "mississippiUat" to "mississippi",
            "mississippiProd" to "0rer-vcea"
        )
        val PASSWORD = hashMapOf<String, String>(
            "uncDev" to "o4zey1yeukm2bnk9",
            "uncQa" to "gearup@123",
            "uncUat" to "gearup@123",
            "uncProd" to "xkfhb9jybc0ucbdc",
            "mississippiDev" to "gearup@123",
            "mississippiQa" to "gearup@123",
            "mississippiUat" to "gearup@123",
            "mississippiProd" to "cm2nucolok512e39"
        )

    }

    override fun onCreate() {
        super.onCreate()
        ctx = applicationContext

        //adding listener to block the orientation change
        registerActivityLifecycleCallbacks(ActivityLifecycleAdapter())
// TODO
//        (ctx?.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE))?.let { sharedPref ->
//            deviceId = sharedPref.getString("deviceId", null)
//            if (deviceId == null) {
//                deviceId = UUID.randomUUID().toString()
//                val editor = sharedPref.edit()
//                editor.putString("deviceId", deviceId)
//                editor.apply()
//            }
//        }
    }

    inner class ActivityLifecycleAdapter : ActivityLifecycleCallbacks {
        override fun onActivityPaused(p0: Activity) {

        }

        override fun onActivityStarted(p0: Activity) {

        }

        override fun onActivityDestroyed(p0: Activity) {

        }

        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {

        }

        override fun onActivityStopped(p0: Activity) {

        }

        override fun onActivityCreated(p0: Activity, p1: Bundle?) {
            //force app to be in portrait mode only

            if (p0 is VideoPlayerActivity || p0 is VrVideoActivity) {
                p0.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                p0.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }

        override fun onActivityResumed(p0: Activity) {

        }
    }
}
