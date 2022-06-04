package com.example.pawpatrol.animals

data class MissingAnimalFullNote(
    val timeAdded: Long,
    val approximateLatitude: Double,
    val approximateLongitude: Double,
    val petName: String,
    val petDescription: String,
    val attachedImageUrls: List<String>,
)
