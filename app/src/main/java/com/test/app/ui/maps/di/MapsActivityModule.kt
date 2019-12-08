package com.test.app.ui.maps.di

import com.test.app.domain.api.FoursquareApi
import com.test.app.domain.repository.IPlacesRepository
import com.test.app.domain.repository.PlacesRepository
import com.test.app.net.rx.ISchedulerFactory
import com.test.app.ui.maps.MapsViewModel
import com.test.app.ui.maps.MapsViewModelProvider
import dagger.Module
import dagger.Provides

@Module
class MapsActivityModule {

    @Provides
    fun providesPlacesRepository(
        movieApi: FoursquareApi
    ): IPlacesRepository = PlacesRepository(movieApi)

    @Provides
    fun providesMapsViewModelProvider(repository: IPlacesRepository,
                                      schedulerFactory: ISchedulerFactory) = MapsViewModelProvider(repository, schedulerFactory)
}