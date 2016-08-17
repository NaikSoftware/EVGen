package rest

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import rest.repository.GoogleMaps
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by naik on 20.02.16.
 */
class GoogleClient {

    var logger = HttpLoggingInterceptor()

    init {
        logger.setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().addInterceptor(logger).build())
            .build()

    val mapsRepository = retrofit.create(GoogleMaps::class.java)
}