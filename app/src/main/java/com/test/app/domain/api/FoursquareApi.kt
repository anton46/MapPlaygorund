package com.test.app.domain.api

import com.test.app.net.data.response.GetVenuesResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface FoursquareApi {

    companion object {
        const val VERSION = "v2"
        const val CLIENT_ID = "YSTLBY15J52XSSZ45MXLR3HQWOUE5UXFB30EFQOD5V1WTLBH"
        const val CLIENT_SECRET = "JEAGX3YN15XARHC1Z1C02QH3FZ21LW5GJG31SLC1B50U1R5Y"
        const val INTENT = "browse"
    }

    @GET("$VERSION/venues/search")
    fun searchVenues(
        @Query("client_id") clientId: String = CLIENT_ID,
        @Query("client_secret") clientSecret: String = CLIENT_SECRET,
        @Query("intent") intent: String = INTENT,
        @QueryMap queryMap: Map<String, String>
    ): Observable<GetVenuesResponse>
}
