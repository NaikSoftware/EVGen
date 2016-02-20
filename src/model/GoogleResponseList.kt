package model

/**
 * Created by naik on 20.02.16.
 */
data class GoogleResponseList<T>(
        val next_page_token: String,
        val results: T,
        val status: String)