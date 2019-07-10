package `in`.track.trackuser

import android.app.Activity
import android.os.Bundle
import android.app.Application


class ActivityLifecycle private constructor() : Application.ActivityLifecycleCallbacks {
    var isForeground = false
        private set
    private var activeActivity = 0
    private var activeActivitys: Activity? = null

    val isBackground: Boolean
        get() = !isForeground

    fun totalActiveActivity(): Int {
        return activeActivity
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle) {
        activeActivity++
    }

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        isForeground = true
        activeActivitys = activity
    }

    override fun onActivityPaused(activity: Activity) {
        isForeground = false
        activeActivitys = null
    }

    override fun onActivityStopped(activity: Activity) {


    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        activeActivity--
    }

    fun getActiveActivity(): Activity? {
        return activeActivitys
    }

    companion object {

        @get:Synchronized
        var instance: ActivityLifecycle? = null
            private set
        private var application: Application? = null

        fun init(app: Application) {
            if (instance == null) {
                application = app
                instance = ActivityLifecycle()
                app.registerActivityLifecycleCallbacks(instance)
            }
        }
    }
}