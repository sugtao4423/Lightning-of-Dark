package sugtao4423.lod.model

import sugtao4423.lod.dao.AccountDao
import sugtao4423.lod.entity.Account

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
        selectListIds: List<Long>? = null,
        selectListNames: List<String>? = null,
        startAppLoadLists: List<String>? = null,
    ) {
        findById(id)?.let {
            val newAccount = it.copy(
                listAsTL = listAsTL ?: it.listAsTL,
                autoLoadTLInterval = autoLoadTLInterval ?: it.autoLoadTLInterval,
                selectListIds = selectListIds ?: it.selectListIds,
                selectListNames = selectListNames ?: it.selectListNames,
                startAppLoadLists = startAppLoadLists ?: it.startAppLoadLists,
            )
            update(newAccount)
        }
    }

    suspend fun updateListAsTL(newValue: Long, id: Long) =
        updateColumn(id, listAsTL = newValue)

    suspend fun updateAutoLoadTLInterval(newValue: Int, id: Long) =
        updateColumn(id, autoLoadTLInterval = newValue)

    suspend fun updateSelectListIds(newValue: List<Long>, id: Long) =
        updateColumn(id, selectListIds = newValue)

    suspend fun updateSelectListNames(newValue: List<String>, id: Long) =
        updateColumn(id, selectListNames = newValue)

    suspend fun updateStartAppLoadLists(newValue: List<String>, id: Long) =
        updateColumn(id, startAppLoadLists = newValue)

}
