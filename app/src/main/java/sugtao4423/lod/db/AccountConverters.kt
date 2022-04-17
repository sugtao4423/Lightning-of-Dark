package sugtao4423.lod.db

import androidx.room.TypeConverter

class AccountConverters {

    @TypeConverter
    fun fromStringList(strings: List<String>): String = strings.joinToString(",")

    @TypeConverter
    fun toStringList(strings: String): List<String> =
        if (strings.isEmpty()) listOf() else strings.split(",")

    @TypeConverter
    fun fromLongList(longs: List<Long>): String = longs.joinToString(",")

    @TypeConverter
    fun toLongList(longs: String): List<Long> = toStringList(longs).map { it.toLong() }

}
