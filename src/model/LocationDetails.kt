package model

import com.google.gson.annotations.SerializedName

/**
 * Created by naik on 20.02.16.
 */
data class LocationDetails(@SerializedName("formatted_address") val address: String,
                           @SerializedName("formatted_phone_number") val phone: String,
                           val icon: String,
                           val name: String,
                           val vicinity: String,
                           val geometry: Geometry) {
    companion object {
        val default = LocationDetails(
                "Some address",
                "0935465432",
                "icon",
                "Some name",
                "Vicinity",
                Geometry(LatLng(31f, 51f)))
    }
}
