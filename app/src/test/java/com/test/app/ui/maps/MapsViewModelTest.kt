package com.test.app.ui.maps

import com.test.app.any
import com.test.app.domain.repository.IPlacesRepository
import com.test.app.net.data.request.SearchInfo
import com.test.app.net.data.response.Location
import com.test.app.net.data.response.Venue
import com.test.app.net.rx.ISchedulerFactory
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MapsViewModelTest {

    private val testScheduler = TestScheduler()

    @Mock
    private lateinit var repository: IPlacesRepository
    @Mock
    private lateinit var schedulerFactory: ISchedulerFactory

    lateinit var viewModel: MapsViewModel

    @Before
    fun setUp() {
        `when`(schedulerFactory.io()).thenReturn(testScheduler)
        `when`(schedulerFactory.main()).thenReturn(testScheduler)

        viewModel = MapsViewModel(repository, schedulerFactory)
        viewModel.onResume()
    }

    @Test
    fun testLoadRestaurants() {
        val expectedResults = createMockResults(10, bounds = Pair(13.74, 100.54))
        `when`(repository.searchVenues(any())).thenReturn(Observable.just(expectedResults))

        val neCoordinate = SearchInfo.Coordinate(13.75, 100.55)
        val swCoordinate = SearchInfo.Coordinate(13.72, 100.52)

        val states = viewModel.observeStates().test()
        viewModel.loadRestaurants(neCoordinate, swCoordinate)

        states.assertValue(MapsViewModel.ViewState.Loading)

        testScheduler.triggerActions()

        verify(repository).searchVenues(any())
        states.assertValueAt(1, MapsViewModel.ViewState.RestaurantLoaded(expectedResults.toSet()))
        states.assertNoErrors()
    }

    @Test
    fun testLoadMoreRestaurants() {
        // Arrange Initial Load
        val initialResults = createMockResults(10, bounds = Pair(13.73, 100.53))
        `when`(repository.searchVenues(any())).thenReturn(Observable.just(initialResults))

        val initialNECoordinate = SearchInfo.Coordinate(13.75, 100.55)
        val initialSWCoordinate = SearchInfo.Coordinate(13.72, 100.52)

        val states = viewModel.observeStates().test()

        // Act Initial Load
        viewModel.loadRestaurants(initialNECoordinate, initialSWCoordinate)

        // Assert Initial Load
        states.assertValue(MapsViewModel.ViewState.Loading)

        testScheduler.triggerActions()

        verify(repository).searchVenues(any())
        states.assertValueAt(1, MapsViewModel.ViewState.RestaurantLoaded(initialResults.toSet()))

        // Arrange Load more
        val newResults = createMockResults(10, bounds = Pair(13.735, 100.535))
        val filteredResults = initialResults.filter { it.location.lat > 13.735 && it.location.lng > 100.535 }.toSet()

        `when`(repository.searchVenues(any())).thenReturn(Observable.just(newResults))

        val loadMoreNECoordinate = SearchInfo.Coordinate(13.79, 100.58)
        val loadMoreSWCoordinate = SearchInfo.Coordinate(13.735, 100.535)

        // Act Load More
        viewModel.loadRestaurants(loadMoreNECoordinate, loadMoreSWCoordinate)

        // Assert Load More
        states.assertValueAt(2, MapsViewModel.ViewState.RestaurantLoaded(filteredResults.toSet()))
        states.assertValueAt(3, MapsViewModel.ViewState.Loading)

        testScheduler.triggerActions()

        verify(repository, times(2)).searchVenues(any())
        states.assertValueAt(4, MapsViewModel.ViewState.RestaurantLoaded(newResults.toSet()))
    }

    @After
    fun flush() {
        viewModel.onDestroy()
    }

    private fun createMockResults(
        size: Int,
        accumulator: Double = 0.001,
        bounds: Pair<Double, Double>
    ) = (1..size).map {
            Venue(
                it.toString(),
                "Restaurant $it",
                Location(
                    bounds.first + (accumulator * it),
                    bounds.second + (accumulator * it),
                    listOf("City", "Province")
                )
            )
        }

}