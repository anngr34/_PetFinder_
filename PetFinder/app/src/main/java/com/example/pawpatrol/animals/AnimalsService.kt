package com.example.pawpatrol.animals

interface AnimalsService {

    fun queryAnimals(queryCriteria: QueryCriteria): List<MissingAnimalNote>

    data class QueryCriteria(
        val latitude: Double,
        val longitude: Double,
        val distanceLimitMeters: Double,
        val sort: Sort
    ) {

        enum class Sort {
            NEAREST,
            ADDED_TIME,
        }
    }
}
