package com.example.pawpatrol.animals

import com.example.pawpatrol.app.PawApplication

object AnimalsServices {

    val mockedService: AnimalsService by lazy {
        val appInstance = PawApplication.instance
        MockedAnimalsService()
    }
}
