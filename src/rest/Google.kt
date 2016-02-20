package rest

import model.Location
import model.LocationDetails
import java.util.*

/**
 * Created by naik on 20.02.16.
 */
class Google(val myLatitude: Float, val myLongitude: Float, val radius: Int) {

    val googleClient = GoogleClient()
    val locationTypes = "bar,cafe,library,hospital,bank,casino,church,electrician,hair_care,hardware_store,police,school,university"

    val random = Random()

    fun getRandomLocationDeatils() : LocationDetails? {

        val location = getRandomLocation() ?: return null

        val response = googleClient.mapsRepository.getPlaceDetails(
                location.placeId, Main.API_KEY).execute()

        if (response.isSuccess) {
            val googleResponse = response.body()
            if (googleResponse.status.compareTo("OK", true) == 0) {
                return googleResponse.result
            } else {
                println("Get google place error: ${googleResponse.status}")
                return null
            }
        } else {
            println("Get google place error: ${response.raw().message()}")
            return null
        }
    }

    fun getRandomLocation() : Location? {
        val response = googleClient.mapsRepository.getPlaceNear(
                "%f,%f".format(Locale.US, myLatitude, myLongitude),
                radius, locationTypes, "", Main.API_KEY).execute()

        if (response.isSuccess) {
            val googleResponse = response.body()
            if (googleResponse.status.compareTo("OK", true) == 0) {
                val results = googleResponse.results
                return results[random.nextInt(results.size)]
            } else {
                println("Get google place error: ${googleResponse.status}")
                return null
            }
        } else {
            println("Get google place error: ${response.raw().message()}")
            return null
        }
    }
}