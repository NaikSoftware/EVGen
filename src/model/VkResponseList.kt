package model

/**
 * Created by naik on 21.02.16.
 */
data class VkResponseList<T>(val count: Int,
                             val items: List<T>)