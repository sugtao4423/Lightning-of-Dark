package sugtao4423.lod.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import sugtao4423.lod.R
import sugtao4423.lod.Settings_List
import sugtao4423.lod.view.IntegerEditTextPreference
import sugtao4423.support.progressdialog.ProgressDialog
import twitter4j.ResponseList
import twitter4j.UserList

class SettingsFragment : PreferenceFragmentCompat() {

    private val follow2list: Preference by lazy { findPreference("follow2list")!! }
    private val listAsTL: CheckBoxPreference by lazy { findPreference("listAsTL")!! }
    private val autoLoadTLInterval: IntegerEditTextPreference by lazy { findPreference("autoLoadTLInterval")!! }
    private val listSetting: Preference by lazy { findPreference("listSetting")!! }
    private val clearCache: Preference by lazy { findPreference("clearCache")!! }

    private val viewModel: SettingsFragmentViewModel by viewModels()
    private val follow2ListViewModel: Follow2ListViewModel by viewModels()

    private var progressDialog: ProgressDialog? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)
        initObservers()
        initViews()
    }

    private fun initObservers() {
        follow2ListViewModel.toggleLoadingDialog.observe(this) {
            if (progressDialog != null) {
                progressDialog!!.dismiss()
                progressDialog = null
                return@observe
            }
            progressDialog = ProgressDialog(requireActivity()).apply {
                setMessage(getString(R.string.loading))
                isIndeterminate = false
                setProgressStyle(ProgressDialog.STYLE_SPINNER)
                setCancelable(false)
                show()
            }
        }
        follow2ListViewModel.showSelectSyncList.observe(this) {
            showFollowSyncListSelectDialog(it)
        }
        follow2ListViewModel.showSyncListResultMessage.observe(this) {
            showFollowSyncListResult(it)
        }

        viewModel.listAsTLData.observe(this) {
            listAsTL.isChecked = it.isChecked
            listAsTL.summary = it.summary
        }
        viewModel.onShowSelectListAsTLDialog.observe(this) {
            showSelectListAsTLDialog(it)
        }
        viewModel.autoLoadTLInterval.observe(this) {
            val str = getString(R.string.param_setting_value_num_zero_is_disable, it)
            autoLoadTLInterval.summary = str
        }
        viewModel.cacheSizeMB.observe(this) {
            clearCache.summary = getString(R.string.param_cache_num_megabyte, it)
        }
    }

    private fun initViews() {
        follow2list.setOnPreferenceClickListener {
            showFollow2ListSelectDialog()
            true
        }
        listAsTL.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                viewModel.showSelectListAsTLDialog()
            } else {
                cancelListAsTL()
            }
            true
        }
        autoLoadTLInterval.setOnPreferenceChangeListener { _, newValue ->
            viewModel.changeAutoLoadTLInterval(newValue.toString().toInt())
        }
        listSetting.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(activity, Settings_List::class.java))
            false
        }
        clearCache.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            viewModel.clearCache()
            false
        }
    }

    private fun showFollow2ListSelectDialog() {
        AlertDialog.Builder(requireActivity()).apply {
            setTitle(R.string.limit_5000_users)
            setItems(R.array.follow2list_methods) { _, which ->
                when (which) {
                    0 -> follow2ListViewModel.createFollowSyncList()
                    1 -> follow2ListViewModel.selectFollowSyncList()
                }
            }
            show()
        }
    }

    private fun showFollowSyncListSelectDialog(lists: ResponseList<UserList>) {
        val listNames = lists.map { it.name }.toTypedArray()
        AlertDialog.Builder(requireActivity()).apply {
            setItems(listNames) { _, which ->
                val selectedList = lists[which]
                follow2ListViewModel.followSyncListSelected(selectedList)
            }
            show()
        }
    }

    private fun showFollowSyncListResult(@StringRes message: Int) {
        AlertDialog.Builder(requireContext()).apply {
            setMessage(message)
            setPositiveButton(R.string.ok, null)
            show()
        }
    }

    private fun showSelectListAsTLDialog(list: ResponseList<UserList>) {
        val listNames = list.map { it.name }.toTypedArray()
        AlertDialog.Builder(requireActivity()).apply {
            setTitle(R.string.choose_list_as_tl)
            setCancelable(false)
            setItems(listNames) { _, which -> viewModel.setListAsTL(list[which]) }
            show()
        }
    }

    private fun cancelListAsTL() {
        AlertDialog.Builder(requireActivity()).apply {
            setTitle(R.string.is_release)
            setCancelable(false)
            setPositiveButton(R.string.ok) { _, _ -> viewModel.cancelListAsTL() }
            setNegativeButton(R.string.cancel) { _, _ -> viewModel.cancelListAsTLCancel() }
            show()
        }
    }

}
