package com.example.mobile_spotter.utils

fun combineStates(list: List<OpState?>) : OpState {
    return when {
        list.firstOrNull { it == OpState.FAILURE } != null -> {
            OpState.FAILURE
        }
        list.firstOrNull { it != OpState.SUCCESS } != null -> {
            OpState.LOADING
        }
        else -> OpState.SUCCESS
    }
}