package com.example.androidbaseapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.androidbaseapp.domain.model.Address
import com.example.androidbaseapp.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val email: String,
    val phone: String,
    val street: String,
    val suite: String,
    val city: String,
    val zipcode: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        phone = phone,
        address = Address(
            street = street,
            suite = suite,
            city = city,
            zipcode = zipcode
        )
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        email = email,
        phone = phone,
        street = address.street,
        suite = address.suite,
        city = address.city,
        zipcode = address.zipcode
    )
}