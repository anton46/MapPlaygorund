package com.test.app.ui.maps

import androidx.fragment.app.FragmentActivity
import com.test.app.domain.repository.IPlacesRepository
import com.test.app.net.rx.ISchedulerFactory

class MapsViewModelProvider(
    val repository: IPlacesRepository,
    val schedulerFactory: ISchedulerFactory
)

fun MapsViewModelProvider.provideViewModel(fragmentActivity: FragmentActivity) =
    provideViewModel(fragmentActivity) {
        MapsViewModel(this.repository, this.schedulerFactory)
    }