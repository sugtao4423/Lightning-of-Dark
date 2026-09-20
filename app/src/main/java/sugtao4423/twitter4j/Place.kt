package sugtao4423.twitter4j

data class Place(
    val id: String,
    val name: String?,
    val streetAddress: String?,
    val countryCode: String?,
    val country: String?,
    val placeType: String?,
    val url: String?,
    val fullName: String?,

    val boundingBoxType: String?,
    val boundingBoxCoordinates: List<List<GeoLocation>>?,

    val geometryType: String?,
    val geometryCoordinates: List<List<GeoLocation>>?,

    val containedWithin: List<Place>?,
) : Comparable<Place>, java.io.Serializable {

    override fun compareTo(other: Place): Int = id.compareTo(other.id)

}
