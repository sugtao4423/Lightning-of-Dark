package sugtao4423.lod

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class LoDBaseActivity : AppCompatActivity() {

    protected lateinit var app: App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = applicationContext as App
    }

    override fun onResume() {
        super.onResume()
        app.getUseTime().start()
    }

    override fun onPause() {
        super.onPause()
        app.getUseTime().stop()
    }

}