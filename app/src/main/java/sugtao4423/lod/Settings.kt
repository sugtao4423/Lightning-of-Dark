package sugtao4423.lod

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.commit
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.utils.Utils
import sugtao4423.support.progressdialog.ProgressDialog
import twitter4j.TwitterException
import java.io.File
import java.text.DecimalFormat

class Settings : LoDBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.commit {
            replace(android.R.id.content, PreferencesFragment())
        }
    }

    class PreferencesFragment : PreferenceFragmentCompat() {

        private lateinit var app: App
        private lateinit var autoLoadTLInterval: Preference

        override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preference, rootKey)

            app = requireContext().applicationContext as App

            val follow2list = findPreference<Preference>("follow2list")!!
            val listAsTL = findPreference<CheckBoxPreference>("listAsTL")!!
            autoLoadTLInterval = findPreference("autoLoadTLInterval")!!
            val listSetting = findPreference<Preference>("listSetting")!!
            val clearCache = findPreference<Preference>("clearCache")!!
            setCacheSize(clearCache)

            follow2list.setOnPreferenceClickListener {
                clickFollow2List()
                false
            }

            listAsTL.apply {
                isChecked = (app.account.listAsTL > 0)
                summary = if (isChecked) app.account.listAsTL.toString() else null
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                    selectListAsTL(preference as CheckBoxPreference, newValue as Boolean)
                }
            }

            setAutoLoadTLIntervalSummary(app.account.autoLoadTLInterval)
            autoLoadTLInterval.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                clickAutoLoadTLInterval()
                false
            }

            listSetting.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(activity, Settings_List::class.java))
                false
            }

            clearCache.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO) {
                        Glide.get(requireContext()).clearDiskCache()
                    }
                    setCacheSize(it)
                    ShowToast(requireContext().applicationContext, R.string.cache_deleted)
                }
                false
            }
        }

        private fun clickFollow2List() {
            AlertDialog.Builder(requireContext()).also {
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
            CoroutineScope(Dispatchers.Main).launch {
                val progressDialog = ProgressDialog(requireContext()).apply {
                    setMessage(getString(R.string.loading))
                    isIndeterminate = false
                    setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    setCancelable(false)
                    show()
                }
                val result = withContext(Dispatchers.IO) {
                    try {
                        app.twitter.createUserList(listName, false, "user count = follow + me")
                    } catch (e: TwitterException) {
                        null
                    }
                }
                progressDialog.dismiss()
                if (result == null) {
                    ShowToast(requireContext().applicationContext, R.string.error_create_list)
                    return@launch
                }
                syncFollowList(result.id)
            }
        }

        private fun selectFollowSyncList() {
            CoroutineScope(Dispatchers.Main).launch {
                val progressDialog = ProgressDialog(requireContext()).apply {
                    setMessage(getString(R.string.loading))
                    isIndeterminate = false
                    setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    setCancelable(false)
                    show()
                }
                val result = withContext(Dispatchers.IO) {
                    try {
                        app.twitter.getUserLists(app.twitter.screenName)
                    } catch (e: TwitterException) {
                        null
                    }
                }
                progressDialog.dismiss()
                if (result == null) {
                    ShowToast(requireContext().applicationContext, R.string.error_get_list)
                    return@launch
                }
                val listNames = arrayOfNulls<String>(result.size)
                result.mapIndexed { index, userList ->
                    listNames[index] = userList.name
                }
                AlertDialog.Builder(requireContext()).apply {
                    setItems(listNames) { _, which ->
                        val selectedListId = result[which].id
                        syncFollowList(selectedListId)
                    }
                    show()
                }
            }
        }

        private fun syncFollowList(listId: Long) {
            CoroutineScope(Dispatchers.Main).launch {
                val progressDialog = ProgressDialog(requireContext()).apply {
                    setMessage(getString(R.string.loading))
                    isIndeterminate = false
                    setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    setCancelable(false)
                    show()
                }
                val result = withContext(Dispatchers.IO) {
                    try {
                        val usersInList = app.twitter.getUserListMembers(listId, 5000, -1).let {
                            val userIds = LongArray(it.size)
                            it.mapIndexed { index, user ->
                                userIds[index] = user.id
                            }
                            userIds
                        }

                        if (usersInList.isNotEmpty()) {
                            app.twitter.destroyUserListMembers(listId, usersInList)
                        }

                        val friendIds = app.twitter.getFriendsIDs(-1).iDs + app.twitter.verifyCredentials().id
                        friendIds.toList().chunked(100).map {
                            app.twitter.createUserListMembers(listId, *it.toLongArray())
                        }
                        true
                    } catch (e: TwitterException) {
                        false
                    }
                }
                progressDialog.dismiss()
                val message = if (result) R.string.success_follow2list else R.string.error_follow2list
                AlertDialog.Builder(requireContext()).apply {
                    setMessage(message)
                    setPositiveButton(R.string.ok, null)
                    show()
                }
            }
        }

        private fun selectListAsTL(preference: CheckBoxPreference, isCheck: Boolean): Boolean {
            if (!isCheck) {
                AlertDialog.Builder(requireContext()).apply {
                    setTitle(R.string.is_release)
                    setPositiveButton(R.string.ok) { _, _ ->
                        CoroutineScope(Dispatchers.Main).launch {
                            app.accountRepository.updateListAsTL(-1, app.account.screenName)
                            app.accountRepository.updateAutoLoadTLInterval(0, app.account.screenName)
                            preference.summary = null
                            setAutoLoadTLIntervalSummary(0)
                            app.reloadAccount()
                        }
                    }
                    setNegativeButton(R.string.cancel) { _, _ ->
                        preference.isChecked = true
                    }
                    show()
                }
                return true
            }

            CoroutineScope(Dispatchers.Main).launch {
                val result = withContext(Dispatchers.IO) {
                    try {
                        app.twitter.getUserLists(app.twitter.screenName)
                    } catch (e: Exception) {
                        null
                    }
                }
                if (result == null) {
                    ShowToast(requireContext().applicationContext, R.string.error_get_list)
                    return@launch
                }
                val listMap = HashMap<String, Long>()
                result.map {
                    listMap[it.name] = it.id
                }
                val listNames = listMap.keys.toTypedArray()
                AlertDialog.Builder(requireContext()).apply {
                    setTitle(R.string.choose_list_as_tl)
                    setCancelable(false)
                    setItems(listNames) { _, which ->
                        val selectedListId = listMap[listNames[which]]!!
                        CoroutineScope(Dispatchers.Main).launch {
                            app.accountRepository.updateListAsTL(selectedListId, app.account.screenName)
                            preference.summary = selectedListId.toString()
                            app.reloadAccount()
                        }
                    }
                    show()
                }
            }
            return true
        }

        private fun clickAutoLoadTLInterval() {
            val editContainer = FrameLayout(requireContext())
            val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            Utils.convertDpToPx(requireContext(), 24).let {
                params.leftMargin = it
                params.rightMargin = it
            }
            val intervalEdit = EditText(requireContext()).apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                layoutParams = params
                editContainer.addView(this)
            }

            AlertDialog.Builder(requireContext()).apply {
                setMessage(R.string.input_auto_load_interval_second)
                setView(editContainer)
                setPositiveButton(R.string.ok) { _, _ ->
                    if (intervalEdit.text.toString().isEmpty()) {
                        return@setPositiveButton
                    }
                    val isListAsTL = app.account.listAsTL > 0
                    val interval = intervalEdit.text.toString().toInt()
                    if (!isListAsTL && interval > 0 && interval < 60) {
                        ShowToast(context, R.string.error_auto_load_tl_interval)
                        return@setPositiveButton
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        app.accountRepository.updateAutoLoadTLInterval(interval, app.account.screenName)
                        setAutoLoadTLIntervalSummary(interval)
                        app.reloadAccount()
                    }
                }
                show()
            }
        }

        private fun setAutoLoadTLIntervalSummary(interval: Int) {
            val str = getString(R.string.param_setting_value_num_zero_is_disable, interval)
            autoLoadTLInterval.summary = str
        }

        private fun setCacheSize(clearCache: Preference) {
            CoroutineScope(Dispatchers.Main).launch {
                fun getDirSize(dir: File): Long {
                    var size = 0L
                    dir.listFiles().map {
                        when {
                            it == null -> return@map
                            it.isDirectory -> size += getDirSize(it)
                            it.isFile -> size += it.length()
                        }
                    }
                    return size
                }

                val result = withContext(Dispatchers.IO) {
                    DecimalFormat("#.#").let {
                        it.minimumFractionDigits = 2
                        it.maximumFractionDigits = 2
                        it.format(getDirSize(requireContext().cacheDir).toDouble() / 1024 / 1024)
                    }
                }
                clearCache.summary = getString(R.string.param_cache_num_megabyte, result)
            }
        }

    }

}
