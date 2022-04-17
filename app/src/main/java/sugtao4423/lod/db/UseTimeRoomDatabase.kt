package sugtao4423.lod.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import sugtao4423.lod.dao.UseTimeDao
import sugtao4423.lod.entity.UseTime

@Database(entities = [UseTime::class], version = 1, exportSchema = false)
abstract class UseTimeRoomDatabase : RoomDatabase() {

    abstract fun useTimeDao(): UseTimeDao

    companion object {
        @Volatile
        private var INSTANCE: UseTimeRoomDatabase? = null

        fun getDatabase(context: Context): UseTimeRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UseTimeRoomDatabase::class.java,
                    "UseTime"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
