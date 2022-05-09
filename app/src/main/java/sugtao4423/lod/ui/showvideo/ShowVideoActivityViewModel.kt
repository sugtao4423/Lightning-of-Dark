package sugtao4423.lod.ui.showvideo

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.hadilq.liveevent.LiveEvent
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.utils.showToast

class ShowVideoActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    val isVideoOrientationSensor: Boolean
        get() = app.prefRepository.isVideoOrientationSensor

    private val _onFinish = LiveEvent<Unit>()
    val onFinish: LiveData<Unit> = _onFinish

    val exoPlayer = ExoPlayer.Builder(application).build().also {
        it.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) _onFinish.value = Unit
            }

            override fun onPlayerError(error: PlaybackException) {
                app.showToast(R.string.error_play_video)
                _onFinish.value = Unit
            }
        })
    }

    var videoUrl: String? = null
        set(value) {
            if (field != null || value == null) return

            field = value
            exoPlayer.apply {
                setMediaItem(MediaItem.fromUri(value.toUri()))
                prepare()
                play()
            }
        }

    var videoType: Int = -1
        set(value) {
            if (field != -1 || value == -1) return

            field = value
            exoPlayer.repeatMode = if (videoType == ShowVideoActivity.TYPE_GIF) {
                Player.REPEAT_MODE_ALL
            } else {
                Player.REPEAT_MODE_OFF
            }
        }

    fun pauseVideo() {
        exoPlayer.pause()
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }

}
