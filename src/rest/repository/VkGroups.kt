package rest.repository

import model.Group
import model.VkResponse
import model.VkResponseList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by naik on 21.02.16.
 */
interface VkGroups {

    @GET("groups.search")
    fun search(@Query("q") query: String,
               @Query("type") type: String,
               @Query("count") count: Int,
               @Query("v") apiVersion: String,
               @Query("access_token") accessToken: String) : Call<VkResponse<VkResponseList<Group>>>
}