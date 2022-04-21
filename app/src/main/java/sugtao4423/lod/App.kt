package sugtao4423.lod

import android.app.Application
import android.graphics.Typeface
import kotlinx.coroutines.*
import sugtao4423.lod.db.AccountRoomDatabase
import sugtao4423.lod.db.UseTimeRoomDatabase
import sugtao4423.lod.entity.Account
import sugtao4423.lod.model.*
import twitter4j.StatusUpdate
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import twitter4j.conf.ConfigurationBuilder
import java.util.regex.Pattern

class App : Application() {

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
    lateinit var twitter: Twitter
        private set
    lateinit var mentionPattern: Pattern
        private set

    var autoLoadTLListener: AutoLoadTLService.AutoLoadTLListener? = null
    var latestTweetId: Long = -1

    override fun onCreate() {
        super.onCreate()
        runBlocking { reloadAccount() }
    }

    suspend fun reloadAccount() {
        if (accountRepository.isExists(prefRepository.screenName)) {
            account = accountRepository.findByScreenName(prefRepository.screenName)!!
            twitter = run {
                val ck = account.consumerKey.ifEmpty { getString(R.string.CK) }
                val cs = account.consumerSecret.ifEmpty { getString(R.string.CS) }
                val accessToken = AccessToken(account.accessToken, account.accessTokenSecret)

                val conf = ConfigurationBuilder().let {
                    it.setOAuthConsumerKey(ck)
                    it.setOAuthConsumerSecret(cs)
                    it.setTweetModeExtended(true)
                    it.build()
                }
                TwitterFactory(conf).getInstance(accessToken)
            }
            mentionPattern = Pattern.compile(".*@${account.screenName}.*", Pattern.DOTALL)
            hasAccount = true
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
                ShowToast(applicationContext, R.string.param_success_tweet, exp)
                if (isLvUp) {
                    ShowToast(
                        applicationContext,
                        R.string.param_level_up,
                        levelRepository.getLevel()
                    )
                }
            } else {
                ShowToast(applicationContext, R.string.error_tweet)
            }
        }
    }
}
