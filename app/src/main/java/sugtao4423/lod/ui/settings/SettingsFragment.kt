package sugtao4423.lod.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import sugtao4423.lod.R
import sugtao4423.lod.ui.settingslist.ListSettingsActivity
import sugtao4423.lod.view.IntegerEditTextPreference
import twitter4j.UserList

class SettingsFragment : PreferenceFragmentCompat() {

    private val listAsTL: CheckBoxPreference by lazy { findPreference("listAsTL")!! }
    private val autoLoadTLInterval: IntegerEditTextPreference by lazy { findPreference("autoLoadTLInterval")!! }
    private val listSetting: Preference by lazy { findPreference("listSetting")!! }
    private val clearCache: Preference by lazy { findPreference("clearCache")!! }

    private val viewModel: SettingsFragmentViewModel by viewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)
        initObservers()
        initViews()
    }

    private fun initObservers() {
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
            startActivity(Intent(activity, ListSettingsActivity::class.java))
            false
        }
        clearCache.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            viewModel.clearCache()
            false
        }
    }

    private fun showSelectListAsTLDialog(list: List<UserList>) {
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
