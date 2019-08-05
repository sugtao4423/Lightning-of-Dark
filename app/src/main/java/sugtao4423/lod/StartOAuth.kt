package sugtao4423.lod

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import twitter4j.auth.RequestToken
import twitter4j.conf.ConfigurationBuilder

class StartOAuth : AppCompatActivity() {

    companion object {
        const val CALLBACK_URL = "https://localhost/sugtao4423.lod/oauth"
    }

    private lateinit var app: App
    private lateinit var customCK: EditText
    private lateinit var customCS: EditText
    private lateinit var ck: String
    private lateinit var cs: String

    private lateinit var twitter: Twitter
    private lateinit var rt: RequestToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.oauth)
        app = applicationContext as App

        val description = findViewById<TextView>(R.id.oauthDescription)
        getString(R.string.param_oauth_description, CALLBACK_URL).let {
            description.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY)
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(it)
            }
        }

        customCK = findViewById(R.id.editCk)
        customCS = findViewById(R.id.editCs)

        customCK.setText(app.getCurrentAccount().consumerKey)
        customCS.setText(app.getCurrentAccount().consumerSecret)
    }

    fun clickOAuth(v: View) {
        v.isEnabled = false
        if (customCK.text.toString().isEmpty()) {
            ck = getString(R.string.CK)
            cs = getString(R.string.CS)
        } else {
            ck = customCK.text.toString()
            cs = customCS.text.toString()
        }

        val conf = ConfigurationBuilder().run {
            setOAuthConsumerKey(ck)
            setOAuthConsumerSecret(cs)
            build()
        }
        twitter = TwitterFactory(conf).instance

        object : AsyncTask<Unit, Unit, Boolean>() {

            override fun doInBackground(vararg params: Unit?): Boolean {
                return try {
                    rt = twitter.getOAuthRequestToken(CALLBACK_URL)
                    true
                } catch (e: TwitterException) {
                    false
                }
            }

            override fun onPostExecute(result: Boolean) {
                if (result) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(rt.authenticationURL)))
                } else {
                    ShowToast(applicationContext, R.string.error_get_request_token)
                }
            }
        }.execute()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent == null || intent.data == null || !intent.data!!.toString().startsWith(CALLBACK_URL)) {
            return
        }

        val verifier = intent.data!!.getQueryParameter("oauth_verifier")

        object : AsyncTask<Unit, Unit, AccessToken?>() {

            override fun doInBackground(vararg params: Unit?): AccessToken? {
                return try {
                    twitter.getOAuthAccessToken(rt, verifier)
                } catch (e: TwitterException) {
                    null
                }
            }

            override fun onPostExecute(result: AccessToken?) {
                if (result != null) {
                    val dbUtil = app.getAccountDBUtil()
                    if (dbUtil.existsAccount(result.screenName)) {
                        val toast = getString(R.string.param_account_already_exists, result.screenName)
                        ShowToast(applicationContext, toast, Toast.LENGTH_LONG)
                        finish()
                        return
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
        }.execute()
    }

    fun clickOAuthDescription(@Suppress("UNUSED_PARAMETER") v: View) {
        (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip = ClipData.newPlainText(getString(R.string.app_name), CALLBACK_URL)
        ShowToast(applicationContext, R.string.done_copy_clip_board)
    }

}