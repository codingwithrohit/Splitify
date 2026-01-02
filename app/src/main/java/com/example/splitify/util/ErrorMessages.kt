package com.example.splitify.util

object ErrorMessages {

    fun getFriendlyMessage(error: String): String {
        return when {
            error.contains("network", ignoreCase = true) ->
                "No internet connection. Please check your network and try again."

            error.contains("timeout", ignoreCase = true) ->
                "Request timed out. Please try again."

            error.contains("not found", ignoreCase = true) ->
                "The requested item could not be found."

            error.contains("unauthorized", ignoreCase = true) ->
                "You don't have permission to perform this action."

            error.contains("database", ignoreCase = true) ->
                "Failed to save data. Please try again."

            error.contains("null", ignoreCase = true) ->
                "Missing required information. Please try again."

            error.isEmpty() ->
                "Something went wrong. Please try again."

            else -> error  // Use the original message if it's already user-friendly
        }
    }
}