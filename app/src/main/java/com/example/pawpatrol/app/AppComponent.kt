package com.example.pawpatrol.app

import android.content.Context
import com.example.pawpatrol.activity.MainActivity
import com.example.pawpatrol.activity.MainActivityComponent
import com.example.pawpatrol.user.DefaultUserService
import com.example.pawpatrol.user.UserService
import com.example.pawpatrol.user.lazyDelegate
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class AppComponent(private val applicationContext: Context) {

    val computationExecutor by lazy {
        val threadCounter = AtomicInteger(0)
        Executors.newCachedThreadPool { task ->
            val name = "computation-${threadCounter.getAndIncrement()}"
            Thread(null, task, name)
        }
    }

    val userService: UserService = {
        DefaultUserService(applicationContext, computationExecutor)
    }.lazyDelegate()

    fun mainActivityComponentBuilder(activity: MainActivity): MainActivityComponent.Builder {
        return MainActivityComponent.Builder(activity)
            .userService(userService)
    }
}
