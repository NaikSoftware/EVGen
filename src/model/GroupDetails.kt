package model

import java.util.*

/**
 * Created by naik on 21.02.16.
 */
data class GroupDetails(val id: Int,
                        val name: String,
                        val description: String,
                        val start_date: Long,
                        val finish_date: Long,
                        val contacts: List<Contact>) {

    companion object {
        val default = GroupDetails(
                1234,
                "Default event",
                "An event description here",
                1436630400,
                1436662800,
                ArrayList<Contact>())
    }
}