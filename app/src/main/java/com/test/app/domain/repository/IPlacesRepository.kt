package com.test.app.domain.repository

import com.test.app.net.data.request.SearchInfo
import com.test.app.net.data.response.Venue
import io.reactivex.Observable

interface IPlacesRepository {
    fun searchVenues(query: SearchInfo): Observable<List<Venue>>
}
