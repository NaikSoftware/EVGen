package model

import com.google.gson.annotations.SerializedName

/**
 * Created by naik on 21.02.16.
 */
data class Group(val id: Int,
                 val name: String,
                 val description: String) {

    companion object {
        val default = Group(1234, "Default event", "This is an event description")
    }
}