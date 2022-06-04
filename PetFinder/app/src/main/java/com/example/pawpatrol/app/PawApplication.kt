package com.example.pawpatrol.app

import android.app.Application
import com.example.pawpatrol.BuildConfig
import timber.log.Timber
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class PawApplication : Application() {

    companion object {

        private lateinit var _instance: PawApplication

        val instance: PawApplication
            get() = _instance
    }

    val executor: Executor by lazy {
        val threadCounter = AtomicInteger(0)
        Executors.newCachedThreadPool { task ->
            val name = "computation-${threadCounter.getAndIncrement()}"
            Thread(null, task, name)
        }
    }

    override fun onCreate() {
        super.onCreate()

        _instance = this

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
