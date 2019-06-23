package sugtao4423.lod

import android.app.AlertDialog
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import sugtao4423.lod.utils.DBUtil
import twitter4j.ResponseList
import twitter4j.TwitterException
import twitter4j.UserList

class Settings_List : LoDBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, PreferencesFragment()).commit()
    }

    class PreferencesFragment : PreferenceFragmentCompat() {

        private lateinit var selectList: Preference
        private lateinit var startAppLoadList: Preference
        private lateinit var dbUtil: DBUtil
        private lateinit var myScreenName: String
        private lateinit var app: App

        override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
            if (activity == null) {
                return
            }
            val activity = activity!!
            setPreferencesFromResource(R.xml.preference_list, rootKey)

            selectList = findPreference("select_List")
            startAppLoadList = findPreference("startApp_loadList")

            app = activity.applicationContext as App
            dbUtil = app.getAccountDBUtil()
            myScreenName = app.getCurrentAccount().screenName

            setSummary()

            startAppLoadList.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val selectedListNames = dbUtil.getSelectListNames(myScreenName)
                val selectedLoadList = BooleanArray(selectedListNames.size)
                val selectLoadList = arrayListOf<String>()
                val builder = AlertDialog.Builder(activity).apply {
                    setTitle(R.string.choose_app_start_load_list)
                    setMultiChoiceItems(selectedListNames, selectedLoadList) { _, which, isChecked ->
                        if (isChecked) {
                            selectLoadList.add(selectedListNames[which])
                        } else {
                            selectLoadList.remove(selectedListNames[which])
                        }
                    }
                    setPositiveButton(R.string.ok) { _, _ ->
                        dbUtil.updateStartAppLoadLists(selectLoadList.joinToString(), myScreenName)
                        app.resetAccount()
                        setSummary()
                    }
                    setNegativeButton(R.string.cancel, null)
                }
                if (selectedListNames[0] != "") {
                    builder.show()
                } else {
                    ShowToast(activity.applicationContext, R.string.list_not_selected)
                }
                false
            }

            selectList.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                object : AsyncTask<Unit, Unit, ResponseList<UserList>?>() {

                    override fun doInBackground(vararg params: Unit?): ResponseList<UserList>? {
                        return try {
                            app.getTwitter().getUserLists(myScreenName)
                        } catch (e: TwitterException) {
                            null
                        }
                    }

                    override fun onPostExecute(result: ResponseList<UserList>?) {
                        if (result == null) {
                            ShowToast(activity.applicationContext, R.string.error_get_list)
                            return
                        }
                        val listMap = HashMap<String, Long>()
                        result.map {
                            listMap[it.name] = it.id
                        }

                        val listItems = listMap.keys.toTypedArray()
                        val checkedList = LinkedHashMap<String, Long>()

                        AlertDialog.Builder(activity).apply {
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

                                dbUtil.updateSelectListNames(checkedListNames.joinToString(), myScreenName)
                                dbUtil.updateSelectListIds(checkedListIds.joinToString(), myScreenName)
                                app.resetAccount()
                                setSummary()
                            }
                            show()
                        }
                    }
                }.execute()
                false
            }
        }

        private fun setSummary() {
            dbUtil.getSelectListNames(myScreenName).let {
                selectList.summary = getString(R.string.param_setting_value_str, it.joinToString())
            }
            dbUtil.getNowStartAppLoadList(myScreenName).let {
                startAppLoadList.summary = getString(R.string.param_setting_value_str, it.joinToString())
            }
        }

    }

}