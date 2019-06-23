package sugtao4423.lod

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AccountDB(context: Context) : SQLiteOpenHelper(context, "Accounts", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(String.format(
                "CREATE TABLE accounts(%s TEXT, %s TEXT, %s TEXT,  %s TEXT, %s TEXT, %s INTEGER, %s INTEGER, %s TEXT, %s TEXT, %s TEXT)",
                Keys.SCREEN_NAME, Keys.CK, Keys.CS, Keys.ACCESS_TOKEN, Keys.ACCESS_TOKEN_SECRET,
                Keys.LIST_AS_TIMELINE, Keys.AUTO_LOAD_TL_INTERVAL,
                Keys.SELECT_LIST_IDS, Keys.SELECT_LIST_NAMES, Keys.APP_START_LOAD_LISTS))
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

}