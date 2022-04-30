package com.example.pawpatrol.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.example.pawpatrol.BuildConfig
import com.example.pawpatrol.common.ActivityLifecycleCallbacksAdapter
import com.example.pawpatrol.activity.MainActivity
import timber.log.Timber
import java.io.Closeable
import java.util.*

class PawApplication : Application() {

    private val associatedResources = IdentityHashMap<Any, List<Closeable>>()

    private val appComponent by lazy {
        AppComponent(this)
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacksAdapter() {

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity is MainActivity) {
                    onMainActivityCreated(activity)
                }
            }

            override fun onActivityDestroyed(activity: Activity) {
                if (activity is MainActivity) {
                    releaseAssociatedResources(activity)
                }
            }
        })
    }

    private fun onMainActivityCreated(activity: MainActivity) {
        val mainActivityComponent = appComponent
            .mainActivityComponentBuilder(activity)
            .build()

        val resources = mainActivityComponent.attachable().attach()
        associatedResources[activity] = listOf(resources)

        activity.inject(
            mainActivityComponent.navigator,
            mainActivityComponent.viewModelFactory
        )
    }

    private fun releaseAssociatedResources(token: Any) {
        associatedResources.remove(token)?.forEach {
            try {
                it.close()
            } catch (e: Exception) {
                Timber.d(e, "Failed to close resource: $it")
                // ignore exception, we have to close other resources
            }
        }
    }
}
