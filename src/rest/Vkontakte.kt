package rest

import model.Group
import java.util.*

/**
 * Created by naik on 21.02.16.
 */
class Vkontakte {

    val vkontakteClient = VkontakteClient()

    fun findEvents(query: String, count: Int) : List<Group> {
        val response = vkontakteClient.groupsRepository.search(query, "event", count, "5.45", Main.VK_ACCESS_TOKEN)
                .execute()
        if (response.isSuccess) {
            val vkResponse = response.body()
            if (vkResponse.error == null) {
                return vkResponse.response.items
            } else {
                println("Find vk events error: ${vkResponse.error}")
            }
        } else {
            println("Find vk events error: ${response.raw().message()}")
        }
        return getDefaultEvents(count)
    }

    private fun getDefaultEvents(size: Int): List<Group> {
        val list = ArrayList<Group>(size)
        for (i in 0..size) {
            list.add(Group.default)
        }
        return list
    }
}