package sugtao4423.lod

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.preference.CheckBoxPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.text.InputType
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import com.loopj.android.image.WebImageCache
import sugtao4423.lod.utils.Utils
import sugtao4423.support.progressdialog.ProgressDialog
import twitter4j.ResponseList
import twitter4j.TwitterException
import twitter4j.UserList
import java.text.DecimalFormat

class Settings : LoDBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, PreferencesFragment()).commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        app.loadOption()
    }

    class PreferencesFragment : PreferenceFragmentCompat() {

        private lateinit var app: App
        private lateinit var autoLoadTLInterval: Preference

        override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
            if (activity == null) {
                return
            }
            val activity = activity!!
            setPreferencesFromResource(R.xml.preference, rootKey)

            app = activity.applicationContext as App

            val follow2list = findPreference("follow2list")
            val listAsTL = findPreference("listAsTL") as CheckBoxPreference
            autoLoadTLInterval = findPreference("autoLoadTLInterval")
            val listSetting = findPreference("listSetting")
            val clearCache = findPreference("clearCache")
            setCacheSize(clearCache)

            follow2list.setOnPreferenceClickListener {
                clickFollow2List()
                false
            }

            listAsTL.apply {
                isChecked = (app.getCurrentAccount().listAsTL > 0)
                summary = if (isChecked) app.getCurrentAccount().listAsTL.toString() else null
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                    selectListAsTL(preference as CheckBoxPreference, newValue as Boolean)
                }
            }

            setAutoLoadTLIntervalSummary(app.getCurrentAccount().autoLoadTLInterval)
            autoLoadTLInterval.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                clickAutoLoadTLInterval(activity)
                false
            }

            listSetting.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(activity, Settings_List::class.java))
                false
            }

            clearCache.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                WebImageCache(activity.applicationContext).clear()
                setCacheSize(it)
                ShowToast(activity.applicationContext, R.string.cache_deleted)
                false
            }
        }

        private fun clickFollow2List() {
            AlertDialog.Builder(activity!!).also {
                it.setTitle(R.string.limit_5000_users)
                it.setItems(R.array.follow2list_methods) { _, which ->
                    when (which) {
                        0 -> createFollowSyncList()
                        1 -> selectFollowSyncList()
                    }
                }
                it.show()
            }
        }

        private fun createFollowSyncList() {
            val listName = "home_timeline"
            object : AsyncTask<Unit, Unit, UserList?>() {
                private lateinit var progressDialog: ProgressDialog

                override fun onPreExecute() {
                    progressDialog = ProgressDialog(activity!!).apply {
                        setMessage(getString(R.string.loading))
                        isIndeterminate = false
                        setProgressStyle(ProgressDialog.STYLE_SPINNER)
                        setCancelable(false)
                        show()
                    }
                }

                override fun doInBackground(vararg params: Unit?): UserList? {
                    return try {
                        app.getTwitter().createUserList(listName, false, "user count = follow + me")
                    } catch (e: TwitterException) {
                        null
                    }
                }

                override fun onPostExecute(result: UserList?) {
                    progressDialog.dismiss()
                    if (result == null) {
                        ShowToast(activity!!.applicationContext, R.string.error_create_list)
                        return
                    }
                    syncFollowList(result.id)
                }

            }.execute()
        }

        private fun selectFollowSyncList() {
            object : AsyncTask<Unit, Unit, ResponseList<UserList>?>() {
                private lateinit var progressDialog: ProgressDialog

                override fun onPreExecute() {
                    progressDialog = ProgressDialog(activity!!).apply {
                        setMessage(getString(R.string.loading))
                        isIndeterminate = false
                        setProgressStyle(ProgressDialog.STYLE_SPINNER)
                        setCancelable(false)
                        show()
                    }
                }

                override fun doInBackground(vararg params: Unit?): ResponseList<UserList>? {
                    return try {
                        app.getTwitter().getUserLists(app.getTwitter().screenName)
                    } catch (e: TwitterException) {
                        null
                    }
                }

                override fun onPostExecute(result: ResponseList<UserList>?) {
                    progressDialog.dismiss()
                    if (result == null) {
                        ShowToast(activity!!.applicationContext, R.string.error_get_list)
                        return
                    }
                    val listNames = arrayOfNulls<String>(result.size)
                    result.mapIndexed { index, userList ->
                        listNames[index] = userList.name
                    }
                    AlertDialog.Builder(activity!!).apply {
                        setItems(listNames) { _, which ->
                            val selectedListId = result[which].id
                            syncFollowList(selectedListId)
                        }
                        show()
                    }
                }
            }.execute()
        }

        private fun syncFollowList(listId: Long) {
            object : AsyncTask<Unit, Unit, Boolean>() {
                private lateinit var progressDialog: ProgressDialog

                override fun onPreExecute() {
                    progressDialog = ProgressDialog(activity!!).apply {
                        setMessage(getString(R.string.loading))
                        isIndeterminate = false
                        setProgressStyle(ProgressDialog.STYLE_SPINNER)
                        setCancelable(false)
                        show()
                    }
                }

                override fun doInBackground(vararg params: Unit?): Boolean {
                    val twitter = app.getTwitter()
                    return try {
                        val usersInList = twitter.getUserListMembers(listId, 5000, -1).let {
                            val userIds = LongArray(it.size)
                            it.mapIndexed { index, user ->
                                userIds[index] = user.id
                            }
                            userIds
                        }

                        if (usersInList.isNotEmpty()) {
                            twitter.destroyUserListMembers(listId, usersInList)
                        }

                        val friendIds = twitter.getFriendsIDs(-1).iDs + twitter.verifyCredentials().id
                        friendIds.toList().chunked(100).map {
                            twitter.createUserListMembers(listId, *it.toLongArray())
                        }
                        true
                    } catch (e: TwitterException) {
                        false
                    }
                }

                override fun onPostExecute(result: Boolean) {
                    progressDialog.dismiss()
                    val message = if (result) R.string.success_follow2list else R.string.error_follow2list
                    AlertDialog.Builder(activity!!).apply {
                        setMessage(message)
                        setPositiveButton(R.string.ok, null)
                        show()
                    }
                }
            }.execute()
        }

        private fun selectListAsTL(preference: CheckBoxPreference, isCheck: Boolean): Boolean {
            val dbUtil = app.getAccountDBUtil()
            if (!isCheck) {
                AlertDialog.Builder(activity!!).apply {
                    setTitle(R.string.is_release)
                    setPositiveButton(R.string.ok) { _, _ ->
                        dbUtil.updateListAsTL(-1, app.getCurrentAccount().screenName)
                        dbUtil.updateAutoLoadTLInterval(0, app.getCurrentAccount().screenName)
                        preference.summary = null
                        setAutoLoadTLIntervalSummary(0)
                        app.reloadAccountFromDB()
                    }
                    setNegativeButton(R.string.cancel) { _, _ ->
                        preference.isChecked = true
                    }
                    show()
                }
                return true
            }

            val listMap = HashMap<String, Long>()
            object : AsyncTask<Unit, Unit, ResponseList<UserList>?>() {

                override fun doInBackground(vararg params: Unit?): ResponseList<UserList>? {
                    return try {
                        app.getTwitter().getUserLists(app.getTwitter().screenName)
                    } catch (e: Exception) {
                        null
                    }
                }

                override fun onPostExecute(result: ResponseList<UserList>?) {
                    if (result == null) {
                        ShowToast(activity!!.applicationContext, R.string.error_get_list)
                        return
                    }
                    result.map {
                        listMap[it.name] = it.id
                    }
                    val listNames = listMap.keys.toTypedArray()
                    AlertDialog.Builder(activity!!).apply {
                        setTitle(R.string.choose_list_as_tl)
                        setCancelable(false)
                        setItems(listNames) { _, which ->
                            val selectedListId = listMap[listNames[which]]!!
                            dbUtil.updateListAsTL(selectedListId, app.getCurrentAccount().screenName)
                            preference.summary = selectedListId.toString()
                            app.reloadAccountFromDB()
                        }
                        show()
                    }
                }
            }.execute()
            return true
        }

        private fun clickAutoLoadTLInterval(context: Context) {
            val editContainer = FrameLayout(context)
            val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            Utils.convertDpToPx(context, 24).let {
                params.leftMargin = it
                params.rightMargin = it
            }
            val intervalEdit = EditText(context).apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                layoutParams = params
                editContainer.addView(this)
            }

            AlertDialog.Builder(context).apply {
                setMessage(R.string.input_auto_load_interval_second)
                setView(editContainer)
                setPositiveButton(R.string.ok) { _, _ ->
                    if (intervalEdit.text.toString().isEmpty()) {
                        return@setPositiveButton
                    }
                    val isListAsTL = app.getCurrentAccount().listAsTL > 0
                    val interval = intervalEdit.text.toString().toInt()
                    if (!isListAsTL && interval > 0 && interval < 60) {
                        ShowToast(context, R.string.error_auto_load_tl_interval, Toast.LENGTH_LONG)
                        return@setPositiveButton
                    }
                    app.getAccountDBUtil().updateAutoLoadTLInterval(interval, app.getCurrentAccount().screenName)
                    setAutoLoadTLIntervalSummary(interval)
                    app.reloadAccountFromDB()
                }
                show()
            }
        }

        private fun setAutoLoadTLIntervalSummary(interval: Int) {
            val str = getString(R.string.param_setting_value_num_zero_is_disable, interval)
            autoLoadTLInterval.summary = str
        }

        private fun setCacheSize(clearCache: Preference) {
            object : AsyncTask<Unit, Unit, String>() {
                override fun doInBackground(vararg params: Unit?): String {
                    DecimalFormat("#.#").let {
                        it.minimumFractionDigits = 2
                        it.maximumFractionDigits = 2
                        return it.format(WebImageCache(activity!!.applicationContext).getCacheSize().toDouble() / 1024 / 1024)
                    }
                }

                override fun onPostExecute(result: String?) {
                    clearCache.summary = getString(R.string.param_cache_num_megabyte, result)
                }
            }.execute()
        }

    }

}