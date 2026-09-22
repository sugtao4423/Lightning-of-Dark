package sugtao4423.lod.ui.settingslist

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.launch
import sugtao4423.lod.R
import sugtao4423.lod.utils.showToast
import sugtao4423.twitter4j.UserList

class ListSettingsFragment : PreferenceFragmentCompat() {

    private val selectList: Preference by lazy { findPreference("selectList")!! }
    private val loadOnAppStartList: Preference by lazy { findPreference("loadOnAppStartList")!! }

    private val viewModel: ListSettingsFragmentViewModel by viewModels()

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_list, rootKey)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.events.collect(::handleEvent) }
            }
        }

        selectList.setOnPreferenceClickListener {
            viewModel.getChooseListDialogData()
            true
        }
        loadOnAppStartList.setOnPreferenceClickListener {
            showLoadOnAppStartListDialog()
            true
        }
    }

    private fun render(state: ListSettingsUiState) {
        selectList.summary = state.selectListSummary
        loadOnAppStartList.summary = state.loadOnAppStartListSummary
    }

    private fun handleEvent(event: ListSettingsEvent) = when (event) {
        is ListSettingsEvent.ShowChooseListDialog -> showChooseListDialog(event.lists)
    }

    private fun showChooseListDialog(lists: List<UserList>) {
        val listNames = lists.map { it.name }.toTypedArray()
        val selectedLists = arrayListOf<UserList>()

        AlertDialog.Builder(requireActivity()).apply {
            setTitle(R.string.choose_list)
            setMultiChoiceItems(listNames, null) { _, which, isChecked ->
                val thisList = lists[which]
                if (isChecked) {
                    selectedLists.add(thisList)
                } else {
                    selectedLists.remove(thisList)
                }
            }
            setPositiveButton(R.string.ok) { _, _ ->
                viewModel.saveSelectedLists(selectedLists)
            }
            show()
        }
    }

    private fun showLoadOnAppStartListDialog() {
        val newListSettings = viewModel.listSettings.map { it.copy() }.toMutableList()
        val currentStates = newListSettings.map { it.loadOnAppStart }.toBooleanArray()
        val listNames = newListSettings.map { it.name }.toTypedArray()

        val builder = AlertDialog.Builder(requireActivity()).apply {
            setTitle(R.string.choose_app_start_load_list)
            setMultiChoiceItems(listNames, currentStates) { _, which, isChecked ->
                newListSettings[which] = newListSettings[which].copy(loadOnAppStart = isChecked)
            }
            setPositiveButton(R.string.ok) { _, _ ->
                viewModel.saveNewListSettings(newListSettings)
            }
            setNegativeButton(R.string.cancel, null)
        }

        if (listNames.isNotEmpty()) {
            builder.show()
        } else {
            requireContext().showToast(R.string.list_not_selected)
        }
    }

}
