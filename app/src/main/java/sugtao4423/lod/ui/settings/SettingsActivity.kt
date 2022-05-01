package sugtao4423.lod.ui.settings

import android.os.Bundle
import androidx.fragment.app.commit
import sugtao4423.lod.LoDBaseActivity

class SettingsActivity : LoDBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.commit {
            replace(android.R.id.content, SettingsFragment())
        }
    }

}
