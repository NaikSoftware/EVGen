package model

import com.google.gson.annotations.SerializedName

/**
 * Created by naik on 20.02.16.
 */
data class Location(
        @SerializedName("place_id") val placeId: String,
        val name: String)