package com.example.pawpatrol.activity

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.pawpatrol.R
import com.example.pawpatrol.app.AttachMainActivityComponent
import com.example.pawpatrol.navigation.DefaultNavigator
import com.example.pawpatrol.navigation.Navigator
import com.example.pawpatrol.navigation.lazyDelegate
import com.example.pawpatrol.user.UserService
import timber.log.Timber

class MainActivityComponent(
    private val activity: AppCompatActivity,
    val userService: UserService,
) {

    init {
        Timber.d("init: $this, activity: $activity, userService: $userService")
    }

    val navigator: Navigator = {
        DefaultNavigator(R.id.content_root, activity.supportFragmentManager)
    }.lazyDelegate()

    val viewModelFactory: ViewModelProvider.Factory
        get() = MainActivityViewModelFactory(userService)

    fun attachable() =
        AttachMainActivityComponent(this, activity.supportFragmentManager)

    class Builder(private val activity: MainActivity) {

        private var userService: UserService = UserService.NOOP

        fun userService(service: UserService) = apply {
            this.userService = service
        }

        fun build(): MainActivityComponent {
            return MainActivityComponent(activity, userService)
        }
    }
}
