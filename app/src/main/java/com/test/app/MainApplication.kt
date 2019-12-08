package com.test.app

import android.app.Application
import com.test.app.di.*

open class MainApplication : Application(), HasApplicationComponent {

    private lateinit var component: PlaygroundApplicationComponent

    override fun onCreate() {
        super.onCreate()
        inject()
    }

    open fun inject() {
        component = DaggerPlaygroundApplicationComponent.builder().build()
        component.inject(this)
    }

    override fun getApplicationComponent(): ApplicationComponent = component
}