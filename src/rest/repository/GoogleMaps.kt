package rest.repository

import model.GoogleResponse
import model.GoogleResponseList
import model.Location
import model.LocationDetails
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by naik on 20.02.16.
 */
interface GoogleMaps {

    @GET("maps/api/place/nearbysearch/json")
    fun getPlaceNear(@Query("location") lonLat: String,
                     @Query("radius") radius: Int,
                     @Query("types") types: String,
                     @Query("name") name: String,
                     @Query("key") apiKey: String) : Call<GoogleResponseList<List<Location>>>

    @GET("maps/api/place/details/json")
    fun getPlaceDetails(@Query("placeid") placeId: String,
                        @Query("key") apiKey: String) : Call<GoogleResponse<LocationDetails>>
}