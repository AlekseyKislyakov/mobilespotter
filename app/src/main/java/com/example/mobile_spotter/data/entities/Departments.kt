package com.example.mobile_spotter.data.entities

import com.example.mobile_spotter.R

const val ANDROID = 3
const val IOS = 2
const val QA = 5
const val BACKEND_JAVA = 28
const val BACKEND_PHP = 25
const val BACKEND_PYTHON = 27
const val FRONTEND_JS = 24
const val FRONTEND_TCS = 26
const val SERVICE = 18
const val HEAD = 1

val departmentPriority = listOf(
        QA,
        ANDROID,
        IOS,
        HEAD,
        BACKEND_JAVA,
        BACKEND_PHP,
        BACKEND_PYTHON,
        FRONTEND_JS,
        FRONTEND_TCS,
        SERVICE
)

fun Int.recognize(): Int {
    return when(this) {
        ANDROID -> R.string.android
        IOS -> R.string.ios
        QA -> R.string.qa
        BACKEND_JAVA -> R.string.backend_java
        BACKEND_PHP -> R.string.backend_php
        BACKEND_PYTHON -> R.string.backend_python
        FRONTEND_JS -> R.string.frontend_js
        FRONTEND_TCS -> R.string.frontend_tcs
        SERVICE -> R.string.service
        else -> R.string.head
    }
}

