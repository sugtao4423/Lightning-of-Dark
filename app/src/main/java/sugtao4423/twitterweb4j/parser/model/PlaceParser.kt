package sugtao4423.twitterweb4j.parser.model

import org.json.JSONException
import sugtao4423.twitter4j.GeoLocation
import sugtao4423.twitter4j.Place
import sugtao4423.twitterweb4j.Json
import sugtao4423.twitterweb4j.mapList

@Throws(JSONException::class)
fun parsePlace(json: Json): Place {
    val id = json["id"].string
    val name = json["name"].stringOrNull
    val streetAddress = json["street_address"].stringOrNull
    val countryCode = json["country_code"].stringOrNull
    val country = json["country"].stringOrNull
    val placeType = json["place_type"].stringOrNull ?: json["type"].stringOrNull
    val url = json["url"].stringOrNull
    val fullName = json["full_name"].stringOrNull

    val boundingBoxType = json["bounding_box"]["type"].stringOrNull
    val boundingBoxCoordinates = json["bounding_box"]["coordinates"].orNull()?.let {
        coordinatesAsGeoLocationList(it)
    }

    val geometryType = json["geometry"]["type"].stringOrNull
    val geometryCoordinates = json["geometry"]["coordinates"].orNull()?.let {
        when (geometryType) {
            "Point" -> listOf(listOf(GeoLocation(it[1].double, it[0].double)))
            "Polygon" -> coordinatesAsGeoLocationList(it)
            else -> null
        }
    }

    val containedWithin = json["contained_within"].orNull()?.mapList { parsePlace(it) }

    return Place(
        id,
        name,
        streetAddress,
        countryCode,
        country,
        placeType,
        url,
        fullName,
        boundingBoxType,
        boundingBoxCoordinates,
        geometryType,
        geometryCoordinates,
        containedWithin,
    )
}

private fun coordinatesAsGeoLocationList(coordinates: Json): List<List<GeoLocation>> =
    coordinates.mapList { ring ->
        ring.mapList { point ->
            GeoLocation(point[1].double, point[0].double)
        }
    }
