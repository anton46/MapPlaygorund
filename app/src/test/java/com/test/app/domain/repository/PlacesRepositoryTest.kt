package com.test.app.domain.repository

import com.test.app.domain.api.FoursquareApi
import com.test.app.net.data.request.SearchInfo
import com.test.app.net.data.request.toMap
import com.test.app.net.data.response.*
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PlacesRepositoryTest {

    @Mock
    lateinit var api: FoursquareApi

    lateinit var repository: PlacesRepository

    @Before
    fun setUp() {
        repository = PlacesRepository(api)
    }

    @Test
    fun testLoadRestaurantSuccess() {
        val restaurants = listOf(
            Venue(
                "1",
                "Restaurant 1",
                Location(
                    10.0,
                    100.0,
                    listOf("City", "Province")
                )
            )
        )

        val searchInfo = SearchInfo(
            "Restaurant",
            SearchInfo.Coordinate(10.0, 100.0),
            SearchInfo.Coordinate(20.0, 200.0),
            "20101010"
        )

        `when`(api.searchVenues(queryMap = searchInfo.toMap())).thenReturn(
            Observable.just(
                GetVenuesResponse(
                    ResponseStatus(200),
                    Venues(restaurants)
                )
            )
        )

        val subscriber = repository.searchVenues(searchInfo).test()

        verify(api).searchVenues(queryMap = searchInfo.toMap())

        subscriber.assertValue(restaurants)
        subscriber.assertComplete()
    }

    @Test
    fun testLoadRestaurantError() {
        val searchInfo = SearchInfo(
            "Restaurant",
            SearchInfo.Coordinate(10.0, 100.0),
            SearchInfo.Coordinate(20.0, 200.0),
            "20101010"
        )

        `when`(api.searchVenues(queryMap = searchInfo.toMap())).thenReturn(
            Observable.just(
                GetVenuesResponse(
                    ResponseStatus(500),
                    Venues(listOf())
                )
            )
        )

        val subscriber = repository.searchVenues(searchInfo).test()

        verify(api).searchVenues(queryMap = searchInfo.toMap())

        subscriber.assertErrorMessage("Request error : 500")
        subscriber.errors()
    }
}