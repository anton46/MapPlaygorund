package com.test.app.domain.repository

import com.test.app.domain.api.FoursquareApi
import com.test.app.net.data.request.SearchInfo
import com.test.app.net.data.request.toMap
import com.test.app.net.data.response.Venue
import io.reactivex.Observable

class PlacesRepository(
    private val foursquareApi: FoursquareApi
) : IPlacesRepository {

    override fun searchVenues(query: SearchInfo): Observable<List<Venue>> {
        return foursquareApi
            .searchVenues(queryMap = query.toMap())
            .flatMap { data ->
                if (data.meta.isSuccess) {
                    Observable.just(data.response.venues)
                } else {
                    Observable.error(Throwable("Request error : " + data.meta.code))
                }
            }
    }
}