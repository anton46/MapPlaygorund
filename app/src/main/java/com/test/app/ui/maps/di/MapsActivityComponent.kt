package com.test.app.ui.maps.di

import com.test.app.ui.di.ActivityComponent
import com.test.app.ui.maps.MapsActivity
import dagger.Subcomponent

@Subcomponent(modules = [MapsActivityModule::class])
interface MapsActivityComponent : ActivityComponent {
    fun inject(mapsActivity: MapsActivity)
}