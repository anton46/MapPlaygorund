package com.test.app.net.data.response

data class Location(
    val lat: Double,
    val lng: Double,
    val formattedAddress: List<String>
)
