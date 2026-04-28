package sugtao4423.lod.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "screen_name") val screenName: String,
    @ColumnInfo(name = "profile_image_url") val profileImageUrl: String,
    val cookie: String,
    @ColumnInfo(name = "list_as_tl") val listAsTL: Long = -1L,
    @ColumnInfo(name = "auto_load_tl_interval") val autoLoadTLInterval: Int = 0,
    @ColumnInfo(name = "select_list_ids") val selectListIds: List<Long> = listOf(),
    @ColumnInfo(name = "select_list_names") val selectListNames: List<String> = listOf(),
    @ColumnInfo(name = "start_app_load_lists") val startAppLoadLists: List<String> = listOf()
)
