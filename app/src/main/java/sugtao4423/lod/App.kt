package sugtao4423.lod

import android.app.Application
import android.content.Context
import android.graphics.Typeface
import kotlinx.coroutines.*
import sugtao4423.lod.db.AccountRoomDatabase
import sugtao4423.lod.db.UseTimeRoomDatabase
import sugtao4423.lod.entity.Account
import sugtao4423.lod.model.AccountRepository
import sugtao4423.lod.model.LevelRepository
import sugtao4423.lod.model.PrefRepository
import sugtao4423.lod.model.UseTimeRepository
import sugtao4423.lod.tweetlistview.TweetListAdapter
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

    val fontAwesomeTypeface: Typeface by lazy {
        Typeface.createFromAsset(assets, "fontawesome.ttf")
    }

    // MainActivity
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
    private var lists: Array<TwitterList>? = null

    override fun onCreate() {
        super.onCreate()
        runBlocking { reloadAccount() }
    }

    suspend fun reloadAccount() {
        if(accountRepository.isExists(prefRepository.screenName)){
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
        } else {
            account = Account("", "", "", "", "")
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
                    ShowToast(applicationContext, R.string.param_level_up, levelRepository.getLevel())
                }
            } else {
                ShowToast(applicationContext, R.string.error_tweet)
            }
        }
    }

    /*
     * +-+-+-+-+-+-+-+-+-+-+-+-+
     * |M|a|i|n|A|c|t|i|v|i|t|y|
     * +-+-+-+-+-+-+-+-+-+-+-+-+
     */

    // lists
    fun getLists(context: Context): Array<TwitterList> {
        if (lists == null) {
            val listNames = account.selectListNames
            val listIds = account.selectListIds
            val appStartLoadListNames = account.startAppLoadLists
            val result = arrayOfNulls<TwitterList>(listNames.size)
            for (i in listNames.indices) {
                val adapter = TweetListAdapter(context)
                val listName = listNames[i]
                val listId = listIds[i]
                val isAppStartLoad = appStartLoadListNames.contains(listName)
                result[i] = TwitterList(adapter, false, listName, listId, isAppStartLoad)
            }
            lists = result.requireNoNulls()
        }
        return lists!!
    }
}
