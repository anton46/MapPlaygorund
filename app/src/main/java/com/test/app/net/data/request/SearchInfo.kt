package com.test.app.net.data.request

data class SearchInfo(val query: String,
                      val northEastCoordinate: Coordinate,
                      val southWestCoordinate: Coordinate,
                      val requestDate: String) {

    data class Coordinate(val lat: Double, val lng: Double)

}

fun SearchInfo.Coordinate.combine() = "${this.lat},${this.lng}"

fun SearchInfo.toMap() = mapOf(
    "query" to this.query,
    "ne" to this.northEastCoordinate.combine(),
    "sw" to this.southWestCoordinate.combine(),
    "v" to this.requestDate
)
