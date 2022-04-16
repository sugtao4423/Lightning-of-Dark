package sugtao4423.lod.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import sugtao4423.lod.dao.AccountDao
import sugtao4423.lod.entity.Account

@Database(entities = [Account::class], version = 1, exportSchema = false)
abstract class AccountRoomDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile
        private var INSTANCE: AccountRoomDatabase? = null

        fun getDatabase(context: Context): AccountRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AccountRoomDatabase::class.java,
                    "Accounts"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
