package rest

import model.GroupDetails
import org.joda.time.LocalDate
import rest.repository.VkGroups
import java.sql.Date
import java.util.*

/**
 * Created by naik on 21.02.16.
 */
class Vkontakte {

    val vkontakteClient = VkontakteClient()
    val apiVersion = "5.45"

    fun findEvents(query: String, count: Int) : List<GroupDetails> {
        val response = vkontakteClient.groupsRepository.search(query, "event", count, apiVersion, Main.VK_ACCESS_TOKEN)
                .execute()
        if (response.isSuccessful) {
            val vkResponse = response.body()
            if (vkResponse.error == null) {
                val groups = vkResponse.response.items
                val ids = groups.map { group -> "${group.id}," }.reduce { s1, s2 -> s1 + s2 }.dropLast(1)
                return getByIds(ids)
            } else {
                println("Find vk events error: ${vkResponse.error}")
            }
        } else {
            println("Find vk events error: ${response.raw().message()}")
        }
        return getDefaultEvents(count)
    }

    private fun getByIds(ids: String): List<GroupDetails> {
        val response = vkontakteClient.groupsRepository.getById(ids, VkGroups.extraFields, apiVersion, Main.VK_ACCESS_TOKEN)
                .execute()
        if (response.isSuccessful) {
            val vkResponse = response.body()
            if (vkResponse.error == null) {
                return vkResponse.response
            } else {
                println("Get vk events by ids error: ${vkResponse.error}")
            }
        } else {
            println("Get vk events by ids error: ${response.raw().message()}")
        }
        return getDefaultEvents(ids.split(",").size + 1)
    }

    private fun getDefaultEvents(size: Int): List<GroupDetails> {
        val list = ArrayList<GroupDetails>(size)
        for (i in 0..size) {
            list.add(GroupDetails.default)
        }
        return list
    }

    fun convertDate(unixtime: Long) : Date {
        return Date(LocalDate(unixtime).plusYears(LocalDate().year - 1970).toDate().time)
    }
}