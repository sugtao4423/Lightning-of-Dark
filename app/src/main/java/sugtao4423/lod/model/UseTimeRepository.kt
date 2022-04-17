package sugtao4423.lod.model

import sugtao4423.lod.dao.UseTimeDao
import sugtao4423.lod.entity.UseTime
import java.text.SimpleDateFormat
import java.util.*

class UseTimeRepository(private val useTimeDao: UseTimeDao) {

    private fun todayUnixTime(): Long {
        val today = Calendar.getInstance()
        return Calendar.getInstance().let {
            it.clear()
            it.set(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
            )
            it.timeInMillis
        }
    }

    suspend fun getTotalUseTimeInMillis(): Long = useTimeDao.getTotalUseTimeInMillis()

    suspend fun getTodayUseTimeInMillis(): Long =
        useTimeDao.getOneDayUseTimeInMillis(todayUnixTime())

    suspend fun getYesterdayUseTimeInMillis(): Long =
        useTimeDao.getOneDayUseTimeInMillis(todayUnixTime() - 86400000)

    suspend fun getLastNDaysUseTimeInMillis(n: Int): Long {
        val end = todayUnixTime()
        val start = end - n * 86400000L
        return useTimeDao.getBetweenUseTimeInMillis(start, end)
    }

    suspend fun getRecordStartDate(): String {
        val startUnixTime = useTimeDao.getRecordStartUnixTime() ?: return "null"
        return SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(startUnixTime)
    }

    suspend fun save(useTime: Long) {
        val todayUseTime = useTimeDao.findByDate(todayUnixTime())
        if (todayUseTime == null) {
            useTimeDao.insert(UseTime(todayUnixTime(), useTime))
        } else {
            val newUseTime = todayUseTime.copy(use = todayUseTime.use + useTime)
            useTimeDao.update(newUseTime)
        }
    }
}
