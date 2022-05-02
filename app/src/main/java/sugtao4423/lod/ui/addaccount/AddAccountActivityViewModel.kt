package sugtao4423.lod.ui.addaccount

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.text.HtmlCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.entity.Account
import sugtao4423.lod.utils.showToast
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.auth.RequestToken
import twitter4j.conf.ConfigurationBuilder

class AddAccountActivityViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val CALLBACK_URL = "https://localhost/sugtao4423.lod/oauth"
    }

    private val app = getApplication<App>()

    val consumerKey = MutableLiveData(if (app.hasAccount) app.account.consumerKey else "")
    val consumerSecret = MutableLiveData(if (app.hasAccount) app.account.consumerSecret else "")

    val callbackDescription = HtmlCompat.fromHtml(
        app.resources.getString(R.string.param_oauth_description, CALLBACK_URL),
        HtmlCompat.FROM_HTML_MODE_COMPACT
    )

    fun copyCallbackURL() {
        val clipboardManager =
            (app.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
        ClipData.newPlainText(app.resources.getString(R.string.app_name), CALLBACK_URL).let {
            clipboardManager.setPrimaryClip(it)
        }
        app.showToast(R.string.done_copy_clip_board)
    }

    val isOauthButtonEnable = MutableLiveData(true)

    private var twitter: Twitter? = null
    private var requestToken: RequestToken? = null

    private val _onActionViewUri = LiveEvent<Uri>()
    val onActionViewUri: LiveData<Uri> = _onActionViewUri

    private val _onFinishEvent = LiveEvent<Unit>()
    val onFinishEvent: LiveData<Unit> = _onFinishEvent

    private val _onStartMainActivityEvent = LiveEvent<Unit>()
    val onStartMainActivityEvent: LiveData<Unit> = _onStartMainActivityEvent

    fun startOAuth() {
        isOauthButtonEnable.value = false

        val ck = consumerKey.value!!.ifEmpty { app.resources.getString(R.string.CK) }
        val cs = consumerSecret.value!!.ifEmpty { app.resources.getString(R.string.CS) }

        val conf = ConfigurationBuilder().run {
            setOAuthConsumerKey(ck)
            setOAuthConsumerSecret(cs)
            build()
        }
        twitter = TwitterFactory(conf).instance

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { twitter!!.getOAuthRequestToken(CALLBACK_URL) }.getOrNull()
            }
            if (result == null) {
                app.showToast(R.string.error_get_request_token)
                isOauthButtonEnable.value = true
                return@launch
            }
            requestToken = result
            _onActionViewUri.value = Uri.parse(requestToken!!.authenticationURL)
        }
    }

    fun onNewIntent(intent: Intent?) {
        if (intent == null || intent.data == null ||
            !intent.data!!.toString().startsWith(CALLBACK_URL)
        ) return

        val verifier = intent.data!!.getQueryParameter("oauth_verifier")

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { twitter!!.getOAuthAccessToken(requestToken!!, verifier) }.getOrNull()
            }
            if (result == null) {
                app.showToast(R.string.error_get_access_token)
                _onFinishEvent.value = Unit
                return@launch
            }
            if (app.accountRepository.isExists(result.screenName)) {
                app.showToast(R.string.param_account_already_exists, result.screenName)
                _onFinishEvent.value = Unit
                return@launch
            }

            app.prefRepository.screenName = result.screenName

            val ck = consumerKey.value!!
            val cs = consumerSecret.value!!
            val account = Account(result.screenName, ck, cs, result.token, result.tokenSecret)
            app.accountRepository.insert(account)
            app.reloadAccount()
            app.showToast(R.string.success_add_account)
            _onStartMainActivityEvent.value = Unit
            _onFinishEvent.value = Unit
        }
    }
}
