package com.example.mobile_spotter.data.entities

sealed class ParsedError(val code: Int, val message: String)

// class NetworkError(code: Int, message: String) : ParsedError(code, message)
class GeneralError(code: Int, message: String) : ParsedError(code, message)
class GrpcError(code: Int, message: String) : ParsedError(code, message)

const val DEFAULT_ERROR_CODE = 0
lateinit var DEFAULT_ERROR_MESSAGE: String