package com.test.app

import org.junit.Assert
import org.mockito.Mockito

fun <T> any(): T = Mockito.any<T>()

infix fun Any?.shouldEqual(theOther: Any?) = Assert.assertEquals(theOther, this)