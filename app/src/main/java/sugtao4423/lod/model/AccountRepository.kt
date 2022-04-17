package sugtao4423.lod.model

import sugtao4423.lod.dao.AccountDao
import sugtao4423.lod.entity.Account

class AccountRepository(private val accountDao: AccountDao) {

    suspend fun getAll(): List<Account> = accountDao.getAccounts()

    suspend fun findByScreenName(screenName: String): Account? =
        accountDao.findAccountByScreenName(screenName)

    suspend fun isExists(screenName: String): Boolean = findByScreenName(screenName) != null

    suspend fun insert(account: Account) = accountDao.insert(account)

    suspend fun update(account: Account) = accountDao.update(account)

    suspend fun delete(screenName: String) = accountDao.delete(screenName)

    private suspend fun updateColumn(
        screenName: String,
        listAsTL: Long? = null,
        autoLoadTLInterval: Int? = null,
        selectListIds: List<Long>? = null,
        selectListNames: List<String>? = null,
        startAppLoadLists: List<String>? = null,
    ) {
        findByScreenName(screenName)?.let {
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

    suspend fun updateListAsTL(newValue: Long, screenName: String) =
        updateColumn(screenName, listAsTL = newValue)

    suspend fun updateAutoLoadTLInterval(newValue: Int, screenName: String) =
        updateColumn(screenName, autoLoadTLInterval = newValue)

    suspend fun updateSelectListIds(newValue: List<Long>, screenName: String) =
        updateColumn(screenName, selectListIds = newValue)

    suspend fun updateSelectListNames(newValue: List<String>, screenName: String) =
        updateColumn(screenName, selectListNames = newValue)

    suspend fun updateStartAppLoadLists(newValue: List<String>, screenName: String) =
        updateColumn(screenName, startAppLoadLists = newValue)

}
