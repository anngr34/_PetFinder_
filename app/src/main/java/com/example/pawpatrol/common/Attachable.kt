package com.example.pawpatrol.common

import java.io.Closeable

interface Attachable {

    fun attach(): Closeable
}
