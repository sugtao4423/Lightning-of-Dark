package sugtao4423.lod.ui.settingslist

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import sugtao4423.lod.R
import sugtao4423.lod.utils.showToast
import twitter4j.ResponseList
import twitter4j.UserList

class ListSettingsFragment : PreferenceFragmentCompat() {

    private val selectList: Preference by lazy { findPreference("selectList")!! }
    private val startAppLoadList: Preference by lazy { findPreference("startAppLoadList")!! }

    private val viewModel: ListSettingsFragmentViewModel by viewModels()

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_list, rootKey)

        viewModel.preferenceSummary.observe(this) {
            selectList.summary = it.selectListSummary
            startAppLoadList.summary = it.startAppLoadListSummary
        }
        viewModel.showChooseListDialog.observe(this) {
            showChooseListDialog(it)
        }

        selectList.setOnPreferenceClickListener {
            viewModel.getChooseListDialogData()
            true
        }
        startAppLoadList.setOnPreferenceClickListener {
            showStartAppLoadChooseListDialog()
            true
        }
    }

    private fun showChooseListDialog(lists: ResponseList<UserList>) {
        val listNames = lists.map { it.name }.toTypedArray()
        val selectedLists = arrayListOf<UserList>()

        AlertDialog.Builder(requireActivity()).apply {
            setTitle(R.string.choose_list)
            setMultiChoiceItems(listNames, BooleanArray(lists.size)) { _, which, isChecked ->
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

    private fun showStartAppLoadChooseListDialog() {
        val selectedListNames = viewModel.selectedListNames.toTypedArray()
        val selectedStartAppLoadLists = arrayListOf<String>()

        val builder = AlertDialog.Builder(requireActivity()).apply {
            setTitle(R.string.choose_app_start_load_list)
            setMultiChoiceItems(
                selectedListNames,
                BooleanArray(selectedListNames.size)
            ) { _, which, isChecked ->
                val thisListName = selectedListNames[which]
                if (isChecked) {
                    selectedStartAppLoadLists.add(thisListName)
                } else {
                    selectedStartAppLoadLists.remove(thisListName)
                }
            }
            setPositiveButton(R.string.ok) { _, _ ->
                viewModel.saveStartAppLoadLists(selectedStartAppLoadLists)
            }
            setNegativeButton(R.string.cancel, null)
        }

        if (selectedListNames.isNotEmpty()) {
            builder.show()
        } else {
            requireContext().showToast(R.string.list_not_selected)
        }
    }

}
