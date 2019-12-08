package com.test.app.net.data.response

data class ResponseStatus(val code: Int) {
    val isSuccess
        get() = code == 200
}