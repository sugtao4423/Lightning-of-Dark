package sugtao4423.lod.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import sugtao4423.lod.entity.UseTime

@Dao
interface UseTimeDao {

    @Query("SELECT * FROM useTime WHERE date = :unixTime")
    suspend fun findByDate(unixTime: Long): UseTime?

    @Query("SELECT SUM(use) FROM useTime")
    suspend fun getTotalUseTimeInMillis(): Long?

    @Query("SELECT use FROM useTime WHERE date = :unixTime")
    suspend fun getOneDayUseTimeInMillis(unixTime: Long): Long?

    @Query("SELECT SUM(use) FROM useTime WHERE date BETWEEN :startUnixTime AND :endUnixTime")
    suspend fun getBetweenUseTimeInMillis(startUnixTime: Long, endUnixTime: Long): Long?

    @Query("SELECT date FROM useTime LIMIT 1")
    suspend fun getRecordStartUnixTime(): Long?

    @Insert
    suspend fun insert(useTime: UseTime)

    @Update
    suspend fun update(useTime: UseTime)

}
