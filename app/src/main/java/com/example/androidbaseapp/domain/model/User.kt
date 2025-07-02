package com.example.androidbaseapp.domain.model

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String,
    val address: Address
)

data class Address(
    val street: String,
    val suite: String,
    val city: String,
    val zipcode: String
) {
    fun getFullAddress(): String {
        return "$street, $suite, $city $zipcode"
    }
}