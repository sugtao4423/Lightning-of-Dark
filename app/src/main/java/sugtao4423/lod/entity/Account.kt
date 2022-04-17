package sugtao4423.lod.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey @ColumnInfo(name = "screenName") val screenName: String,
    @ColumnInfo(name = "ck") val consumerKey: String,
    @ColumnInfo(name = "cs") val consumerSecret: String,
    @ColumnInfo(name = "accessToken") val accessToken: String,
    @ColumnInfo(name = "accessTokenSecret") val accessTokenSecret: String,
    @ColumnInfo(name = "listAsTL") val listAsTL: Long = -1L,
    @ColumnInfo(name = "autoLoadTLInterval") val autoLoadTLInterval: Int = 0,
    @ColumnInfo(name = "selectListIds") val selectListIds: List<Long> = listOf(),
    @ColumnInfo(name = "selectListNames") val selectListNames: List<String> = listOf(),
    @ColumnInfo(name = "appStartLoadLists") val startAppLoadLists: List<String> = listOf()
)
