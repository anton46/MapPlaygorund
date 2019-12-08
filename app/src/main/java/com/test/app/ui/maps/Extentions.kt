package com.test.app.ui.maps

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

inline fun <reified VM : ViewModel> provideViewModel(
    fragmentActivity: FragmentActivity,
    crossinline provider: () -> VM
) = ViewModelProviders.of(fragmentActivity, object : ViewModelProvider.Factory {
    override fun <T1 : ViewModel> create(aClass: Class<T1>) = provider() as T1
}).get(VM::class.java)
