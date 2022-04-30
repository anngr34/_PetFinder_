package com.example.pawpatrol.mocks

enum class TestUsers(
    val id: String,
    val username: String,
    val password: String,
    val token: String,
    val email: String,
    val phoneNumber: String?,
) {
    ALWAYS_AUTHORIZED_USER(
        "1",
        "user1",
        "",
        "sezame",
        "user1@paw.com",
        "+38099*******"
    ),
    NOT_EXISTING_USER(
        "",
        "user2",
        "",
        "",
        "",
        ""
    ),
    USER_WITH_PASS(
        "3",
        "user3",
        "1234abcd",
        "abracadabra",
        "user3@paw.com",
        "+38095*******"
    ),
}