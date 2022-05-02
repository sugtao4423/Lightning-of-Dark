package sugtao4423.lod.ui.settingslist

import android.os.Bundle
import androidx.fragment.app.commit
import sugtao4423.lod.ui.LoDBaseActivity

class ListSettingsActivity : LoDBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.commit {
            replace(android.R.id.content, ListSettingsFragment())
        }
    }

}
