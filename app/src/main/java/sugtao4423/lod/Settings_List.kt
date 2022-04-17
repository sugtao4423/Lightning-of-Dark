package sugtao4423.lod

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.commit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import twitter4j.TwitterException

class Settings_List : LoDBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.commit {
            replace(android.R.id.content, PreferencesFragment())
        }
    }

    class PreferencesFragment : PreferenceFragmentCompat() {

        private lateinit var selectList: Preference
        private lateinit var startAppLoadList: Preference
        private val app by lazy { requireContext().applicationContext as App }
        private val myScreenName by lazy { app.account.screenName }

        override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preference_list, rootKey)

            selectList = findPreference("select_List")!!
            startAppLoadList = findPreference("startApp_loadList")!!

            setSummary()

            startAppLoadList.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val selectedListNames = app.account.selectListNames.toTypedArray()
                val selectedLoadList = BooleanArray(selectedListNames.size)
                val selectLoadList = arrayListOf<String>()
                val builder = AlertDialog.Builder(requireContext()).apply {
                    setTitle(R.string.choose_app_start_load_list)
                    setMultiChoiceItems(selectedListNames, selectedLoadList) { _, which, isChecked ->
                        if (isChecked) {
                            selectLoadList.add(selectedListNames[which])
                        } else {
                            selectLoadList.remove(selectedListNames[which])
                        }
                    }
                    setPositiveButton(R.string.ok) { _, _ ->
                        CoroutineScope(Dispatchers.Main).launch {
                            app.accountRepository.updateStartAppLoadLists(selectLoadList.toList(), myScreenName)
                            app.reloadAccount()
                            setSummary()
                        }
                    }
                    setNegativeButton(R.string.cancel, null)
                }
                if (selectedListNames[0] != "") {
                    builder.show()
                } else {
                    ShowToast(requireContext().applicationContext, R.string.list_not_selected)
                }
                false
            }

            selectList.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    val result = withContext(Dispatchers.IO) {
                        try {
                            app.twitter.getUserLists(myScreenName)
                        } catch (e: TwitterException) {
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

                    val listItems = listMap.keys.toTypedArray()
                    val checkedList = LinkedHashMap<String, Long>()

                    AlertDialog.Builder(requireContext()).apply {
                        setTitle(R.string.choose_list)
                        setMultiChoiceItems(listItems, BooleanArray(listItems.size)) { _, which, isChecked ->
                            if (isChecked) {
                                checkedList[listItems[which]] = listMap[listItems[which]]!!
                            } else {
                                checkedList.remove(listItems[which])
                            }
                        }
                        setPositiveButton(R.string.ok) { _, _ ->
                            val checkedListNames = checkedList.keys.toTypedArray()
                            val checkedListIds = checkedList.values.toTypedArray()

                            CoroutineScope(Dispatchers.Main).launch {
                                app.accountRepository.apply {
                                    updateSelectListNames(checkedListNames.toList(), myScreenName)
                                    updateSelectListIds(checkedListIds.toList(), myScreenName)
                                }
                                app.reloadAccount()
                                setSummary()
                            }
                        }
                        show()
                    }
                }
                false
            }
        }

        private fun setSummary() {
            selectList.summary = app.account.selectListNames.joinToString().let {
                getString(R.string.param_setting_value_str, it)
            }
            startAppLoadList.summary = app.account.startAppLoadLists.joinToString().let {
                getString(R.string.param_setting_value_str, it)
            }
        }

    }

}
