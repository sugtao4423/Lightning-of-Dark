package sugtao4423.lod

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.databinding.OauthBinding
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.auth.RequestToken
import twitter4j.conf.ConfigurationBuilder

class StartOAuth : AppCompatActivity() {

    companion object {
        const val CALLBACK_URL = "https://localhost/sugtao4423.lod/oauth"
    }

    private lateinit var binding: OauthBinding
    private lateinit var app: App
    private lateinit var ck: String
    private lateinit var cs: String

    private lateinit var twitter: Twitter
    private lateinit var rt: RequestToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = OauthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = applicationContext as App

        binding.oauthBtn.setOnClickListener { v -> clickOAuth(v) }
        binding.oauthDescriptionBtn.setOnClickListener { clickOAuthDescription() }

        getString(R.string.param_oauth_description, CALLBACK_URL).let {
            binding.oauthDescriptionBtn.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY)
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(it)
            }
        }

        binding.ckEdit.setText(app.getCurrentAccount().consumerKey)
        binding.csEdit.setText(app.getCurrentAccount().consumerSecret)
    }

    private fun clickOAuth(v: View) {
        v.isEnabled = false
        if (binding.ckEdit.text.toString().isEmpty()) {
            ck = getString(R.string.CK)
            cs = getString(R.string.CS)
        } else {
            ck = binding.ckEdit.text.toString()
            cs = binding.csEdit.text.toString()
        }

        val conf = ConfigurationBuilder().run {
            setOAuthConsumerKey(ck)
            setOAuthConsumerSecret(cs)
            build()
        }
        twitter = TwitterFactory(conf).instance

        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    rt = twitter.getOAuthRequestToken(CALLBACK_URL)
                    true
                } catch (e: TwitterException) {
                    false
                }
            }
            if (result) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(rt.authenticationURL)))
            } else {
                ShowToast(applicationContext, R.string.error_get_request_token)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent == null || intent.data == null || !intent.data!!.toString().startsWith(CALLBACK_URL)) {
            return
        }

        val verifier = intent.data!!.getQueryParameter("oauth_verifier")

        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    twitter.getOAuthAccessToken(rt, verifier)
                } catch (e: TwitterException) {
                    null
                }
            }
            if (result != null) {
                val dbUtil = app.getAccountDBUtil()
                if (dbUtil.existsAccount(result.screenName)) {
                    ShowToast(applicationContext, R.string.param_account_already_exists, result.screenName)
                    finish()
                    return@launch
                }
                PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        .edit()
                        .putString(Keys.SCREEN_NAME, result.screenName)
                        .apply()

                if (ck == getString(R.string.CK)) {
                    ck = ""
                }
                if (cs == getString(R.string.CS)) {
                    cs = ""
                }

                val account = Account(result.screenName, ck, cs, result.token, result.tokenSecret)
                dbUtil.addAccount(account)
                app.resetAccount()
                ShowToast(applicationContext, R.string.success_add_account)
                startActivity(Intent(applicationContext, MainActivity::class.java))
            } else {
                ShowToast(applicationContext, R.string.error_get_access_token)
            }
            finish()
        }
    }

    private fun clickOAuthDescription() {
        (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name), CALLBACK_URL))
        ShowToast(applicationContext, R.string.done_copy_clip_board)
    }

}