package sugtao4423.lod.ui.addaccount

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import sugtao4423.lod.MainActivity
import sugtao4423.lod.databinding.OauthBinding

class AddAccountActivity : AppCompatActivity() {

    private val viewModel: AddAccountActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = OauthBinding.inflate(layoutInflater).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
        }
        setContentView(binding.root)

        viewModel.onActionViewUri.observe(this) {
            startActivity(Intent(Intent.ACTION_VIEW, it))
        }
        viewModel.onFinishEvent.observe(this) {
            finish()
        }
        viewModel.onStartMainActivityEvent.observe(this) {
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        viewModel.onNewIntent(intent)
    }
}
