package com.example.cinderssoul

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.cinderssoul.models.Song
import com.example.cinderssoul.repository.SongRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class MusicViewModel(application: Application, private val songRepository: SongRepository) : AndroidViewModel(application) {

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build()

    private val _songsState = mutableStateOf<Result<List<Song>>?>(null)
    val songsState: State<Result<List<Song>>?> = _songsState

    init {
        fetchSongs()
    }

    fun fetchSongs() {
        viewModelScope.launch {
            _songsState.value = songRepository.getAllSongs()
        }
    }

    fun playSong(song: Song) {
        // Kiểm tra xem bài click vào có phải bài đang load trong player không
        val isSameSong = exoPlayer.currentMediaItem?.mediaId == song.songId

        if (isSameSong) {
            // Nếu là cùng 1 bài: Chỉ cần Play hoặc Pause
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
            } else {
                exoPlayer.play()
            }
        } else {
            // Nếu là bài mới: Reset player và load bài mới
            exoPlayer.stop()
            exoPlayer.clearMediaItems()

            if (song.audioUrl.isNotEmpty()) {
                val setupAndPlay = { uri: Uri ->
                    val mediaItem = MediaItem.Builder()
                        .setUri(uri)
                        .setMediaId(song.songId) // Gán ID để lần sau check isSameSong
                        .build()
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    exoPlayer.playWhenReady = true // Tự động phát khi load xong
                }

                // Xử lý URL (HTTP hoặc Firebase)
                if (song.audioUrl.startsWith("http")) {
                    setupAndPlay(Uri.parse(song.audioUrl))
                } else {
                    val storageRef = if (song.audioUrl.startsWith("gs://")) {
                        FirebaseStorage.getInstance().getReferenceFromUrl(song.audioUrl)
                    } else {
                        FirebaseStorage.getInstance().getReference(song.audioUrl)
                    }
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        setupAndPlay(uri)
                    }.addOnFailureListener {
                        // Log lỗi nếu không lấy được URL từ Firebase
                        it.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}