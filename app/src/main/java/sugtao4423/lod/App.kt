package sugtao4423.lod

import android.app.Application
import android.content.Context
import android.graphics.Typeface
import android.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.tweetlistview.TweetListAdapter
import sugtao4423.lod.usetime.UseTime
import sugtao4423.lod.utils.DBUtil
import twitter4j.StatusUpdate
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import twitter4j.conf.ConfigurationBuilder
import java.util.regex.Pattern

class App : Application() {

    private var account: Account? = null
    private var fontAwesomeTypeface: Typeface? = null
    // MainActivity
    private var twitter: Twitter? = null
    private var mentionPattern: Pattern? = null
    var autoLoadTLListener: AutoLoadTLService.AutoLoadTLListener? = null
    var latestTweetId: Long = -1
    private var lists: Array<TwitterList>? = null
    private var options: Options? = null
    private var level: Level? = null
    // Database
    private var accountDBUtil: DBUtil? = null
    private var useTime: UseTime? = null

    fun resetAccount() {
        this.account = null
        this.twitter = null
        this.mentionPattern = null
        this.lists = null
    }

    fun reloadAccountFromDB() {
        this.account = null
    }

    fun getCurrentAccount(): Account {
        if (account == null) {
            val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            account = getAccountDBUtil().getAccount(pref.getString(Keys.SCREEN_NAME, "") ?: "")
            if (account == null) {
                account = Account("", "", "", "", "")
            }
        }
        return account!!
    }

    fun haveAccount(): Boolean {
        return !(getCurrentAccount().screenName == "" || getCurrentAccount().accessToken == "" || getCurrentAccount().accessTokenSecret == "")
    }

    private fun twitterLogin() {
        val ck: String
        val cs: String
        if (getCurrentAccount().consumerKey == "") {
            ck = getString(R.string.CK)
            cs = getString(R.string.CS)
        } else {
            ck = getCurrentAccount().consumerKey
            cs = getCurrentAccount().consumerSecret
        }
        val accessToken = AccessToken(getCurrentAccount().accessToken, getCurrentAccount().accessTokenSecret)

        val conf = ConfigurationBuilder().run {
            setOAuthConsumerKey(ck)
            setOAuthConsumerSecret(cs)
            setTweetModeExtended(true)
            build()
        }
        this.twitter = TwitterFactory(conf).getInstance(accessToken)
        this.mentionPattern = Pattern.compile(".*@${getCurrentAccount().screenName}.*", Pattern.DOTALL)
    }

    fun updateStatus(status: StatusUpdate) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    getTwitter().updateStatus(status)
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

    // Twitter
    fun getTwitter(): Twitter {
        if (twitter == null) {
            twitterLogin()
        }
        return twitter!!
    }

    // mentionPattern
    fun getMentionPattern(): Pattern {
        if (mentionPattern == null) {
            twitterLogin()
        }
        return mentionPattern!!
    }

    // lists
    fun getLists(context: Context): Array<TwitterList> {
        if (lists == null) {
            val listNames = getCurrentAccount().selectListNames
            val listIds = getCurrentAccount().selectListIds
            val appStartLoadListNames = getCurrentAccount().startAppLoadLists
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

    // DBUtil
    fun getAccountDBUtil(): DBUtil {
        if (accountDBUtil == null) {
            accountDBUtil = DBUtil(applicationContext)
        }
        return accountDBUtil!!
    }

    fun closeAccountDB() {
        accountDBUtil?.dbClose()
        accountDBUtil = null
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