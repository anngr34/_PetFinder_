package com.example.pawpatrol.util

import com.example.pawpatrol.data.FirebaseHolder

sealed class ImageSource {

    abstract fun toTarget(): Any

    data class Uri(val uri: android.net.Uri): ImageSource() {

        override fun toTarget(): Any {
            return uri
        }
    }

    data class Firebase(val uuid: String): ImageSource() {

        override fun toTarget(): Any {
            return FirebaseHolder.storageForImage(uuid)
        }
    }
}
