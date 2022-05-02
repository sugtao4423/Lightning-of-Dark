package sugtao4423.lod.ui

import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sugtao4423.lod.App

abstract class LoDBaseActivity : AppCompatActivity() {

    private var useTimeStartTime = System.currentTimeMillis()

    override fun onResume() {
        super.onResume()
        useTimeStartTime = System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()
        val useTimeInMillis = System.currentTimeMillis() - useTimeStartTime
        CoroutineScope(Dispatchers.Main).launch {
            (applicationContext as App).useTimeRepository.save(useTimeInMillis)
        }
    }
}
