package com.example.splitify.util

import android.R.attr.data
import com.example.splitify.util.Result.Error

/**
 * A generic wrapper for operation results
 * Represents either Success or Error
 *
 * Usage:
 * when (result) {
 *     is Result.Success -> // Handle data
 *     is Result.Error -> // Handle error
 * }
 */

sealed class Result<out T>{
    data object Loading: Result<Nothing>()

    // Successful operation with data
    data class Success<out T>(val data: T): Result<T>()

    // Failed operation with error details
    data class Error(val exception: Throwable,
        val message: String = exception.message ?: "Unknown error",
        ): Result<Nothing>()

    val isSuccess: Boolean
        get() = this is Success

    val isError: Boolean
        get() = this is Error

    // Get data if success, null otherwise
//    fun getOrNull(): T? = when(this){
//        is Success -> data
//        is Error -> null
//    }
//
    // Get data if success, throw exception if error
    fun getOrThrow(): T? = when(this){
        is Success -> data
        is Error -> throw exception
        Loading -> TODO()
    }

}
// Extension function to create success result
fun<T> T.asSuccess(): Result<T> = Result.Success(this)

// Extension function to create error result
fun Throwable.asError(): Result<Nothing> = Error(this)

