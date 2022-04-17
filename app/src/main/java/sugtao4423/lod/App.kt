package sugtao4423.lod

import android.app.Application
import android.content.Context
import android.graphics.Typeface
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import sugtao4423.lod.db.AccountRoomDatabase
import sugtao4423.lod.entity.Account
import sugtao4423.lod.model.AccountRepository
import sugtao4423.lod.tweetlistview.TweetListAdapter
import sugtao4423.lod.usetime.UseTime
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

    private var fontAwesomeTypeface: Typeface? = null
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
    private var options: Options? = null
    private var level: Level? = null
    // Database
    private var useTime: UseTime? = null

    override fun onCreate() {
        super.onCreate()
        runBlocking { reloadAccount() }
    }

    suspend fun reloadAccount() {
        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val screenName = pref.getString(Keys.SCREEN_NAME, "")!!
        if(accountRepository.isExists(screenName)){
            account = accountRepository.findByScreenName(screenName)!!
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
                val exp = getLevel().getRandomExp()
                val isLvUp = getLevel().addExp(exp)
                ShowToast(applicationContext, R.string.param_success_tweet, exp)
                if (isLvUp) {
                    ShowToast(applicationContext, R.string.param_level_up, getLevel().getLevel())
                }
            } else {
                ShowToast(applicationContext, R.string.error_tweet)
            }
        }
    }

    fun getFontAwesomeTypeface(): Typeface {
        if (fontAwesomeTypeface == null) {
            fontAwesomeTypeface = Typeface.createFromAsset(assets, "fontawesome.ttf")
        }
        return fontAwesomeTypeface!!
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

    // options
    fun loadOption() {
        options = Options(applicationContext)
    }

    fun getOptions(): Options {
        if (options == null) {
            loadOption()
        }
        return options!!
    }

    // Level system
    fun getLevel(): Level {
        if (level == null) {
            level = Level(applicationContext)
        }
        return level!!
    }

    // UseTime
    fun getUseTime(): UseTime {
        if (useTime == null) {
            useTime = UseTime(applicationContext)
        }
        return useTime!!
    }

    fun closeUseTimeDB() {
        useTime?.dbClose()
        useTime = null
    }

}
