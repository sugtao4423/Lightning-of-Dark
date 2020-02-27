package sugtao4423.lod.usetime

import android.content.Context
import android.os.AsyncTask
import java.text.SimpleDateFormat
import java.util.*

class UseTime(context: Context) {

    private val db = UseTimeDB(context).writableDatabase
    private var startTime: Long = System.currentTimeMillis()

    fun dbClose() {
        db.close()
    }

    fun start() {
        startTime = System.currentTimeMillis()
    }

    fun stop() {
        val useTime = System.currentTimeMillis() - startTime
        object : AsyncTask<Unit, Unit, Unit>() {
            override fun doInBackground(vararg params: Unit?) {
                save(useTime)
            }
        }.execute()
    }

    fun getTodayUseTimeInMillis(): Int {
        val query = "SELECT use FROM useTime WHERE date = " + todayUnixTime()
        val c = db.rawQuery(query, null)
        if (!c.moveToFirst()) {
            return 0
        }
        val todayUseTime = c.getInt(0)
        c.close()
        return todayUseTime
    }

    fun getYesterdayUseTimeInMillis(): Int {
        val yesterday = todayUnixTime() - 86400000
        val query = "SELECT use FROM useTime WHERE date = $yesterday"
        val c = db.rawQuery(query, null)
        if (!c.moveToFirst()) {
            return 0
        }
        val yesterdayUseTime = c.getInt(0)
        c.close()
        return yesterdayUseTime
    }

    fun getLastNdaysUseTimeInMillis(n: Int): Long {
        val today = todayUnixTime()
        val last = today - n * 86400000L
        val query = "SELECT SUM(use) FROM useTime WHERE date BETWEEN '$last' AND '$today'"
        val c = db.rawQuery(query, null)
        if (!c.moveToFirst()) {
            return 0
        }
        val lastNdaysUseTime = c.getLong(0)
        c.close()
        return lastNdaysUseTime
    }

    fun getTotalUseTimeInMillis(): Long {
        val query = "SELECT SUM(use) FROM useTime"
        val c = db.rawQuery(query, null)
        if (!c.moveToFirst()) {
            return 0
        }
        val totalUseTime = c.getLong(0)
        c.close()
        return totalUseTime
    }

    fun getRecordStartDate(): String {
        val query = "SELECT date FROM useTime LIMIT 1"
        val c = db.rawQuery(query, null)
        if (!c.moveToFirst()) {
            return "null"
        }
        val startDate = c.getLong(0)
        c.close()
        return SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(startDate)
    }

    private fun todayUnixTime(): Long {
        val today = Calendar.getInstance()
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
        return cal.timeInMillis
    }

    private fun existsTodayRow(): Boolean {
        val query = "SELECT COUNT(*) FROM useTime WHERE date = " + todayUnixTime()
        val c = db.rawQuery(query, null)
        c.moveToFirst()
        val count = c.getInt(0)
        c.close()
        return count > 0
    }

    private fun save(useTime: Long) {
        if (existsTodayRow()) {
            updateUseTime(useTime)
        } else {
            insertUseTime(useTime)
        }
    }

    private fun insertUseTime(useTime: Long) {
        val sql = "INSERT INTO useTime VALUES (" + todayUnixTime() + ", $useTime)"
        db.execSQL(sql)
    }

    private fun updateUseTime(useTime: Long) {
        val todayDate = todayUnixTime()
        val query = "SELECT use FROM useTime WHERE date = $todayDate"
        val c = db.rawQuery(query, null)
        c.moveToFirst()
        val oldUseTime = c.getLong(0)
        c.close()
        val newUseTime = useTime + oldUseTime
        val update = "UPDATE useTime SET use = $newUseTime WHERE date = $todayDate"
        db.execSQL(update)
    }

}