package sugtao4423.lod.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import sugtao4423.lod.entity.Account

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts")
    suspend fun getAccounts(): List<Account>

    @Query("SELECT * FROM accounts WHERE screenName = :screenName")
    suspend fun findAccountByScreenName(screenName: String): Account?

    @Insert
    suspend fun insert(accountDB: Account)

    @Update
    suspend fun update(accountDB: Account)

    @Query("DELETE FROM accounts WHERE screenName = :screenName")
    suspend fun delete(screenName: String)

}
