package sugtao4423.lod.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.launch
import sugtao4423.lod.R
import sugtao4423.lod.ui.settingslist.ListSettingsActivity
import sugtao4423.lod.view.IntegerEditTextPreference
import sugtao4423.twitter4j.UserList

class SettingsFragment : PreferenceFragmentCompat() {

    private val listAsTL: CheckBoxPreference by lazy { findPreference("listAsTL")!! }
    private val autoLoadTLInterval: IntegerEditTextPreference by lazy { findPreference("autoLoadTLInterval")!! }
    private val listSetting: Preference by lazy { findPreference("listSetting")!! }
    private val clearCache: Preference by lazy { findPreference("clearCache")!! }

    private val viewModel: SettingsFragmentViewModel by viewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.events.collect(::handleEvent) }
            }
        }

        initViews()
    }

    private fun render(state: SettingsUiState) {
        listAsTL.isChecked = state.listAsTLChecked
        listAsTL.summary = state.listAsTLSummary

        autoLoadTLInterval.summary =
            getString(R.string.param_setting_value_num_zero_is_disable, state.autoLoadTLInterval)

        clearCache.summary = getString(R.string.param_cache_size, state.cacheSize)
    }

    private fun handleEvent(event: SettingsEvent) = when (event) {
        is SettingsEvent.ShowSelectListAsTLDialog -> showSelectListAsTLDialog(event.userLists)
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
