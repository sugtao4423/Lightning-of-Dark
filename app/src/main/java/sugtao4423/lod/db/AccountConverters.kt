package sugtao4423.lod.db

import androidx.room.TypeConverter
import org.json.JSONArray
import org.json.JSONObject
import sugtao4423.lod.entity.ListSetting

class AccountConverters {

    @TypeConverter
    fun listSettingsToString(list: List<ListSetting>): String {
        val json = JSONArray()
        list.forEach {
            val item = JSONObject()
            item.put("id", it.id)
            item.put("name", it.name)
            item.put("loadOnAppStart", it.loadOnAppStart)
            json.put(item)
        }
        return json.toString()
    }

    @TypeConverter
    fun stringToListSettings(string: String): List<ListSetting> {
        val json = JSONArray(string)
        return List(json.length()) { index ->
            val item = json.getJSONObject(index)
            ListSetting(
                item.getLong("id"),
                item.getString("name"),
                item.getBoolean("loadOnAppStart"),
            )
        }
    }

}
