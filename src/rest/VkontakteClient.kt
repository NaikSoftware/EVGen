package rest

import rest.repository.VkGroups
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by naik on 21.02.16.
 */
class VkontakteClient {

    val retrofit = Retrofit.Builder()
            .baseUrl("https://api.vk.com/method/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val groupsRepository = retrofit.create(VkGroups::class.java)
}