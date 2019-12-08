package com.test.app.net.retrofit

import com.test.app.net.settings.NetworkSettingsProvider
import com.test.app.net.settings.ServerEnvironment

class FoursquareApiBaseUrl(networkSettingsProvider: NetworkSettingsProvider) : AbstractBaseUrl(networkSettingsProvider) {

    override fun getHostForEnvironment(environment: ServerEnvironment?): String = when (environment) {
        ServerEnvironment.LIVE -> HOST
        else -> MOCK
    }

    override fun getPathForEnvironment(): String? = null

    companion object {
        const val HOST = "api.foursquare.com"
        const val MOCK = "localhost"
    }
}
