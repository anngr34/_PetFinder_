package com.example.pawpatrol.user

import com.example.pawpatrol.app.PawApplication

object UserServices {

    val mockedService: UserService by lazy {
        val appInstance = PawApplication.instance
        DefaultUserService(appInstance.applicationContext, appInstance.executor)
    }
}
