package com.test.app.ui.maps

import androidx.lifecycle.ViewModel
import com.test.app.domain.repository.IPlacesRepository
import com.test.app.net.data.request.SearchInfo
import com.test.app.net.data.response.Venue
import com.test.app.net.rx.ISchedulerFactory
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.*

class MapsViewModel(
    private val repository: IPlacesRepository,
    private val schedulerFactory: ISchedulerFactory
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val actions = PublishSubject.create<Action>()
    private val states = PublishSubject.create<ViewState>()
    private val restaurants = mutableSetOf<Venue>()

    fun onResume() {
        observeActionsChanged()
    }

    private fun observeActionsChanged() {
        compositeDisposable.add(
            actions.subscribe {
                when (it) {
                    is Action.LoadRestaurant -> searchRestaurantFromApi(
                        createSearchInfo(
                            it.northEastCoordinate,
                            it.southWestCoordinate
                        )
                    )
                }
            }
        )
    }

    private fun createSearchInfo(
        northEastCoordinate: SearchInfo.Coordinate,
        southWestCoordinate: SearchInfo.Coordinate
    ) = SearchInfo(
        "Restaurant",
        northEastCoordinate,
        southWestCoordinate,
        SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
    )

    fun observeStates(): Observable<ViewState> = states.hide()

    private fun searchRestaurantFromApi(searchInfo: SearchInfo) {
        states.onNext(ViewState.Loading)
        compositeDisposable.add(
            createLoadRestaurantsObservable(searchInfo)
                .observeOn(schedulerFactory.main())
                .subscribeOn(schedulerFactory.io())
                .subscribe(::onSearchResult) {
                    it.printStackTrace()
                    onError(it.message)
                }
        )
    }

    private fun createLoadRestaurantsObservable(searchInfo: SearchInfo) =
        Observable
            .just(searchInfo)
            .switchMap { repository.searchVenues(it) }

    private fun onSearchResult(venues: List<Venue>) {
        restaurants.addAll(venues)
        states.onNext(ViewState.RestaurantLoaded(venues.toSet()))
    }

    private fun onError(message: String?) {
        states.onNext(ViewState.Error(message))
    }

    fun loadRestaurants(
        northEastCoordinate: SearchInfo.Coordinate,
        southWestCoordinate: SearchInfo.Coordinate
    ) {
        if (restaurants.isNotEmpty()) {
            states.onNext(
                ViewState.RestaurantLoaded(
                    restaurants.insideViewPort(
                        northEastCoordinate,
                        southWestCoordinate
                    )
                )
            )
        }
        actions.onNext(Action.LoadRestaurant(northEastCoordinate, southWestCoordinate))
    }

    fun onDestroy() {
        compositeDisposable.clear()
    }

    private fun MutableSet<Venue>.insideViewPort(
        northEastCoordinate: SearchInfo.Coordinate,
        southWestCoordinate: SearchInfo.Coordinate
    ) = this.filter {
        it.location.lat > southWestCoordinate.lat && it.location.lat < northEastCoordinate.lat &&
                it.location.lng > southWestCoordinate.lng && it.location.lng < northEastCoordinate.lng
    }.toSet()

    sealed class ViewState {
        object Loading : ViewState()
        data class RestaurantLoaded(val restaurants: Set<Venue>) : ViewState()
        data class Error(val message: String?) : ViewState()
    }

    sealed class Action {
        data class LoadRestaurant(
            val northEastCoordinate: SearchInfo.Coordinate,
            val southWestCoordinate: SearchInfo.Coordinate
        ) : Action()
    }
}
