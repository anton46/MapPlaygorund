package com.test.app.di

import com.test.app.MainApplication
import com.test.app.ui.maps.di.MapsActivityComponent
import com.test.app.ui.maps.di.MapsActivityModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [NetModule::class, RxModule::class, NetworkSettingsModule::class])
interface PlaygroundApplicationComponent : ApplicationComponent {
    fun inject(mainApplication: MainApplication)

    fun add(module: MapsActivityModule): MapsActivityComponent
}
