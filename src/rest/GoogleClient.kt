package rest

import rest.repository.GoogleMaps
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by naik on 20.02.16.
 */
class GoogleClient {

    val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val mapsRepository = retrofit.create(GoogleMaps::class.java)
}