package model

/**
 * Created by naik on 20.02.16.
 */
data class GoogleResponse<T>(
        val result: T,
        val status: String)