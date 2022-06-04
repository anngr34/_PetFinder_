package com.example.pawpatrol.animals

import java.util.*
import java.util.concurrent.TimeUnit

class MockedAnimalsService : AnimalsService {

    private val fullNotes: List<MissingAnimalFullNote> by lazy {
        val currentCalendar = Calendar.getInstance()

        currentCalendar.clone()
        val currentTimeMillis = Calendar.getInstance().timeInMillis
        listOf(
            MissingAnimalFullNote(
                timeAdded = currentTimeMillis - TimeUnit.DAYS.toMillis(1),
                approximateLatitude = 49.020173,
                approximateLongitude = 40.063523,
                petName = "Rex",
                petDescription = "Very obedient german shepherd",
                attachedImageUrls = emptyList(),
            ),
            MissingAnimalFullNote(
                timeAdded = currentTimeMillis - TimeUnit.DAYS.toMillis(2) + TimeUnit.HOURS.toMillis(12),
                approximateLatitude = 49.0,
                approximateLongitude = 40.0,
                petName = "Kitty",
                petDescription = "White cat with black right ear",
                attachedImageUrls = emptyList(),
            )
        )
    }

    override fun queryAnimals(
        queryCriteria: AnimalsService.QueryCriteria
    ): List<MissingAnimalNote> {
        return fullNotes.map {
            MissingAnimalNote(
                petName = it.petName,
                shortDescription = it.petDescription,
                attachedImageUrl = it.attachedImageUrls.firstOrNull() ?: "",
            )
        }
    }
}
