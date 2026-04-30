package sugtao4423.lod

import android.app.Application
import android.graphics.Typeface
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import sugtao4423.lod.db.AccountRoomDatabase
import sugtao4423.lod.db.UseTimeRoomDatabase
import sugtao4423.lod.entity.Account
import sugtao4423.lod.model.AccountRepository
import sugtao4423.lod.model.FileDownloader
import sugtao4423.lod.model.LevelRepository
import sugtao4423.lod.model.PrefRepository
import sugtao4423.lod.model.UseTimeRepository
import sugtao4423.lod.service.AutoLoadTLService
import sugtao4423.lod.utils.showToast
import sugtao4423.twitterweb4j.TwitterWeb4j
import twitter4j.StatusUpdate
import twitter4j.TwitterException
import java.util.regex.Pattern

class App : Application() {

    companion object {
        const val DEFAULT_TWEET_COUNT = 50
    }

    private val accountDatabase by lazy { AccountRoomDatabase.getDatabase(this) }
    val accountRepository by lazy { AccountRepository(accountDatabase.accountDao()) }
    private val useTimeDatabase by lazy { UseTimeRoomDatabase.getDatabase(this) }
    val useTimeRepository by lazy { UseTimeRepository(useTimeDatabase.useTimeDao()) }

    val prefRepository by lazy { PrefRepository(this) }
    val levelRepository by lazy { LevelRepository(prefRepository) }

    val fileDownloader by lazy { FileDownloader(this) }

    val fontAwesomeTypeface: Typeface by lazy {
        Typeface.createFromAsset(assets, "fontawesome.ttf")
    }

    var hasAccount = false
        private set
    lateinit var account: Account
        private set
    lateinit var twitter: TwitterWeb4j
        private set
    lateinit var mentionPattern: Pattern
        private set

    var autoLoadTLListener: AutoLoadTLService.AutoLoadTLListener? = null
    var cursorTop: String? = null

    override fun onCreate() {
        super.onCreate()
        runBlocking { reloadAccount() }
    }

    suspend fun reloadAccount() {
        if (accountRepository.isExists(prefRepository.accountId)) {
            account = accountRepository.findById(prefRepository.accountId)!!
            twitter = TwitterWeb4j(account.cookie)
            loadClientTransaction()
            mentionPattern = Pattern.compile(".*@${account.screenName}.*", Pattern.DOTALL)
            hasAccount = true
        }
    }

    private fun loadClientTransaction() = CoroutineScope(Dispatchers.Main).launch {
        withContext(Dispatchers.IO) {
            runCatching { twitter.loadClientTransaction() }
        }.onFailure {
            Toast.makeText(
                applicationContext, "Failed to load client transaction data.", Toast.LENGTH_LONG
            ).show()
        }
    }

    fun updateStatus(status: StatusUpdate) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    twitter.updateStatus(status)
                } catch (e: TwitterException) {
                    null
                }
            }
            if (result != null) {
                val exp = levelRepository.getRandomExp()
                val isLvUp = levelRepository.addExp(exp)
                showToast(R.string.param_success_tweet, exp)
                if (isLvUp) {
                    showToast(R.string.param_level_up, levelRepository.getLevel())
                }
            } else {
                showToast(R.string.error_tweet)
            }
        }
    }
}
