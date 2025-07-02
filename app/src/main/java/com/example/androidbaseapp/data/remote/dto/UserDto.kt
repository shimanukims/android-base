package com.example.androidbaseapp.data.remote.dto

import com.example.androidbaseapp.domain.model.Address
import com.example.androidbaseapp.domain.model.User
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String,
    val address: AddressDto
)

@Serializable
data class AddressDto(
    val street: String,
    val suite: String,
    val city: String,
    val zipcode: String
)

fun UserDto.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        phone = phone,
        address = address.toDomain()
    )
}

fun AddressDto.toDomain(): Address {
    return Address(
        street = street,
        suite = suite,
        city = city,
        zipcode = zipcode
    )
}