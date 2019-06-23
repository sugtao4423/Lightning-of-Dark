package sugtao4423.lod.utils

import android.content.Context
import android.database.Cursor
import sugtao4423.lod.Account
import sugtao4423.lod.AccountDB
import sugtao4423.lod.Keys

class DBUtil(context: Context) {

    private val db = AccountDB(context).writableDatabase

    fun dbClose() {
        db.close()
    }

    fun getAccount(screenName: String): Account? {
        val c = db.rawQuery(String.format("SELECT * FROM accounts WHERE %s = '%s'", Keys.SCREEN_NAME, screenName), null)
        if (!c.moveToFirst()) {
            return null
        }
        val account = getAccount(c)
        c.close()
        return account
    }

    fun getAccounts(): Array<Account> {
        val c = db.rawQuery("SELECT * FROM accounts", null)
        val accountList = arrayListOf<Account>()

        var mov = c.moveToFirst()
        while (mov) {
            accountList.add(getAccount(c))
            mov = c.moveToNext()
        }
        c.close()
        return accountList.toTypedArray()
    }

    private fun getAccount(c: Cursor): Account {
        c.apply {
            val screenName = getString(0)
            val ck = getString(1)
            val cs = getString(2)
            val at = getString(3)
            val ats = getString(4)
            val listAsTL = getLong(5)
            val autoLoadTLInterval = getInt(6)
            val listIds = getString(7)
            val listNames = getString(8)
            val appLoadLists = getString(9)

            val selectListIds = if (listIds == "") {
                LongArray(0)
            } else {
                val listIdsStr = listIds.split(Regex("\\s*,\\s*"))
                val ids = LongArray(listIdsStr.size)
                listIdsStr.mapIndexed { index, s ->
                    ids[index] = s.toLong()
                }
                ids
            }

            val selectListNames = if (listNames == "") arrayOf() else listNames.split(Regex("\\s*,\\s*")).toTypedArray()
            val startAppLoadLists = if (appLoadLists == "") arrayOf() else appLoadLists.split(Regex("\\s*,\\s*")).toTypedArray()

            return Account(screenName, ck, cs, at, ats, listAsTL, autoLoadTLInterval, selectListIds, selectListNames, startAppLoadLists)
        }
    }

    fun existsAccount(screenName: String): Boolean {
        val c = db.rawQuery(String.format("SELECT COUNT(*) FROM accounts WHERE %s = '%s'", Keys.SCREEN_NAME, screenName), null)
        c.moveToFirst()
        val count = c.getInt(0)
        c.close()
        return count > 0
    }

    fun addAccount(account: Account) {
        db.execSQL(String.format("INSERT INTO accounts VALUES('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                account.screenName, account.consumerKey, account.consumerSecret,
                account.accessToken, account.accessTokenSecret, account.listAsTL, account.autoLoadTLInterval,
                account.selectListIds.joinToString(),
                account.selectListNames.joinToString(),
                account.startAppLoadLists.joinToString()))
    }

    fun deleteAccount(account: Account) {
        db.execSQL(String.format("DELETE FROM accounts WHERE %s='%s'", Keys.SCREEN_NAME, account.screenName))
    }

    fun updateListAsTL(listAsTL: Long, screenName: String) {
        db.execSQL(getUpdate1ColumnFromEq1Column(Keys.LIST_AS_TIMELINE, listAsTL, screenName))
    }

    fun updateAutoLoadTLInterval(autoLoadTLInterval: Int, screenName: String) {
        db.execSQL(getUpdate1ColumnFromEq1Column(Keys.AUTO_LOAD_TL_INTERVAL, autoLoadTLInterval, screenName))
    }

    fun updateSelectListIds(ids: String, screenName: String) {
        db.execSQL(getUpdate1ColumnFromEq1Column(Keys.SELECT_LIST_IDS, ids, screenName))
    }

    fun updateSelectListNames(names: String, screenName: String) {
        db.execSQL(getUpdate1ColumnFromEq1Column(Keys.SELECT_LIST_NAMES, names, screenName))
    }

    fun updateStartAppLoadLists(startAppLoadLists: String, screenName: String) {
        db.execSQL(getUpdate1ColumnFromEq1Column(Keys.APP_START_LOAD_LISTS, startAppLoadLists, screenName))
    }

    private fun getUpdate1ColumnFromEq1Column(updateKey: Any, update: Any, screenName: String): String {
        return String.format("UPDATE accounts SET %s = '%s' WHERE %s = '%s'", updateKey, update, Keys.SCREEN_NAME, screenName)
    }

    fun getSelectListNames(screenName: String): Array<String> {
        val c = db.rawQuery(String.format("SELECT %s FROM accounts WHERE %s = '%s'", Keys.SELECT_LIST_NAMES, Keys.SCREEN_NAME, screenName), null)
        c.moveToFirst()
        val result = c.getString(0).split(Regex("\\s*,\\s*"))
        c.close()
        return result.toTypedArray()
    }

    fun getNowStartAppLoadList(screenName: String): Array<String> {
        val c = db.rawQuery(String.format("SELECT %s FROM accounts WHERE %s = '%s'", Keys.APP_START_LOAD_LISTS, Keys.SCREEN_NAME, screenName), null)
        c.moveToNext()
        val result = c.getString(0).split(Regex("\\s*,\\s*"))
        c.close()
        return result.toTypedArray()
    }

}