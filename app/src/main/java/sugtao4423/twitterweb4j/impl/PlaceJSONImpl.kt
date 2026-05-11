package sugtao4423.twitterweb4j.impl

import sugtao4423.twitterweb4j.Json
import twitter4j.GeoLocation
import twitter4j.Place
import twitter4j.RateLimitStatus

data class PlaceJSONImpl(@Transient val json: Json) : Place, java.io.Serializable {

    private val name = json["name"].stringOrNull
    private val streetAddress = json["street_address"].stringOrNull
    private val countryCode = json["country_code"].stringOrNull
    private val id = json["id"].string
    private val country = json["country"].stringOrNull
    private val placeType = json["place_type"].stringOrNull ?: json["type"].stringOrNull
    private val url = json["url"].stringOrNull
    private val fullName = json["full_name"].stringOrNull

    private val boundingBoxType = json["bounding_box"]["type"].stringOrNull
    private val boundingBoxCoordinates =
        json["bounding_box"]["coordinates"].orNull()?.let { coordinatesAsGeoLocationArray(it) }

    private val geometryType = json["geometry"]["type"].stringOrNull
    private val geometryCoordinates = json["geometry"]["coordinates"].orNull()?.let {
        when (geometryType) {
            "Point" -> arrayOf(arrayOf(GeoLocation(it[1].double, it[0].double)))
            "Polygon" -> coordinatesAsGeoLocationArray(it)
            else -> null
        }
    }

    private val containedWithin: Array<Place>? = json["contained_within"].orNull()?.let {
        Array(it.size) { i -> PlaceJSONImpl(it[i]) }
    }

    private fun coordinatesAsGeoLocationArray(coordinates: Json): Array<Array<GeoLocation>> =
        Array(coordinates.size) { i ->
            Array(coordinates[i].size) { j ->
                GeoLocation(coordinates[i][j][1].double, coordinates[i][j][0].double)
            }
        }

    override fun getName(): String? = name
    override fun getStreetAddress(): String? = streetAddress
    override fun getCountryCode(): String? = countryCode
    override fun getId(): String = id
    override fun getCountry(): String? = country
    override fun getPlaceType(): String? = placeType
    override fun getURL(): String? = url
    override fun getFullName(): String? = fullName

    override fun getBoundingBoxType(): String? = boundingBoxType
    override fun getBoundingBoxCoordinates(): Array<Array<GeoLocation>>? = boundingBoxCoordinates

    override fun getGeometryType(): String? = geometryType
    override fun getGeometryCoordinates(): Array<Array<GeoLocation>>? = geometryCoordinates

    override fun getContainedWithIn(): Array<Place>? = containedWithin

    override fun compareTo(other: Place): Int = id.compareTo(other.id)

    /** **Not implemented** */
    override fun getRateLimitStatus(): RateLimitStatus? = null

    /** **Not implemented** */
    override fun getAccessLevel(): Int = -1

}
