package sugtao4423.lod.model

import sugtao4423.lod.dao.AccountDao
import sugtao4423.lod.entity.Account
import sugtao4423.lod.entity.ListSetting

class AccountRepository(private val accountDao: AccountDao) {

    suspend fun getAll(): List<Account> = accountDao.getAccounts()

    suspend fun findById(id: Long): Account? = accountDao.findAccountById(id)

    suspend fun isExists(id: Long): Boolean = findById(id) != null

    suspend fun insert(account: Account) = accountDao.insert(account)

    suspend fun update(account: Account) = accountDao.update(account)

    suspend fun delete(id: Long) = accountDao.delete(id)

    private suspend fun updateColumn(
        id: Long,
        listAsTL: Long? = null,
        autoLoadTLInterval: Int? = null,
        listSettings: List<ListSetting>? = null,
    ) {
        findById(id)?.let {
            val newAccount = it.copy(
                listAsTL = listAsTL ?: it.listAsTL,
                autoLoadTLInterval = autoLoadTLInterval ?: it.autoLoadTLInterval,
                listSettings = listSettings ?: it.listSettings,
            )
            update(newAccount)
        }
    }

    suspend fun updateListAsTL(newValue: Long, id: Long) =
        updateColumn(id, listAsTL = newValue)

    suspend fun updateAutoLoadTLInterval(newValue: Int, id: Long) =
        updateColumn(id, autoLoadTLInterval = newValue)

    suspend fun updateListSettings(newValue: List<ListSetting>, id: Long) =
        updateColumn(id, listSettings = newValue)

}
