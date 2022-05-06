package sugtao4423.lod.ui.showvideo

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hadilq.liveevent.LiveEvent
import sugtao4423.lod.App

class ShowVideoActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    val isVideoOrientationSensor: Boolean
        get() = app.prefRepository.isVideoOrientationSensor

    private val _onStopProgressDialog = MutableLiveData<Unit>()
    val onStopProgressDialog: LiveData<Unit> = _onStopProgressDialog

    private val _onFinish = LiveEvent<Unit>()
    val onFinish: LiveData<Unit> = _onFinish

    var videoUrl: String? = null
    var videoType: Int = -1

    private var isVideoError = false

    fun onVideoPrepared(mediaPlayer: MediaPlayer) {
        _onStopProgressDialog.value = Unit
        mediaPlayer.start()
    }

    fun onVideoError(): Boolean {
        isVideoError = true
        return false
    }

    fun onVideoComplete(mediaPlayer: MediaPlayer) {
        if (isVideoError) {
            _onFinish.value = Unit
            return
        }
        if (videoType == ShowVideoActivity.TYPE_GIF) {
            mediaPlayer.apply {
                seekTo(0)
                start()
            }
        } else {
            _onFinish.value = Unit
        }
    }

}
