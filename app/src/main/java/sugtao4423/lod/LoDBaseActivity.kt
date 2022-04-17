package sugtao4423.lod

import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class LoDBaseActivity : AppCompatActivity() {

    protected val app by lazy { applicationContext as App }
    private var useTimeStartTime = System.currentTimeMillis()

    override fun onResume() {
        super.onResume()
        useTimeStartTime = System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()
        val useTimeInMillis = System.currentTimeMillis() - useTimeStartTime
        CoroutineScope(Dispatchers.Main).launch {
            app.useTimeRepository.save(useTimeInMillis)
        }
    }
}
