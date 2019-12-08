package com.test.app.domain.proxy

import com.test.app.domain.api.FoursquareApi
import com.test.app.net.data.response.GetVenuesResponse
import com.test.app.net.data.response.Venues
import com.test.app.net.provider.ApiProvider
import com.test.app.net.provider.ApiProxy
import io.reactivex.Observable
import io.reactivex.Scheduler

class FoursquareApiProxy(
    apiProvider: ApiProvider<FoursquareApi>,
    scheduler: Scheduler
) : ApiProxy<FoursquareApi>(apiProvider, scheduler), FoursquareApi {

    override fun searchVenues(
        clientId: String,
        clientSecret: String,
        intent: String,
        queryMap: Map<String, String>
    ): Observable<GetVenuesResponse> {
        return getApiInterface().searchVenues(clientId, clientSecret, intent, queryMap)
    }
}
