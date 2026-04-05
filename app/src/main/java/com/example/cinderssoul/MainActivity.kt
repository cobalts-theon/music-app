package com.example.cinderssoul

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import com.example.cinderssoul.models.Song
import com.example.cinderssoul.repository.SongRepository

class MainActivity : ComponentActivity() {
    private val songRepository = SongRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return MusicViewModel(application, songRepository) as T
                        }
                    }
                    MusicShow(viewModel = viewModel(factory = factory))
                }
            }
        }
    }
}

@Composable
fun MusicShow(
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel = viewModel()
) {
    val result by viewModel.songsState

    Box(modifier = modifier.fillMaxSize().padding(top = 32.dp)) {
        val currentResult = result
        when {
            currentResult == null -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            currentResult.isSuccess -> {
                val songs = currentResult.getOrNull() ?: emptyList()
                LazyColumn {
                    items(songs) { song ->
                        SongItem(
                            song = song,
                            onPlayClick = { viewModel.playSong(song) }
                        )
                    }
                }
            }
            currentResult.isFailure -> {
                Text(
                    text = "Error: ${currentResult.exceptionOrNull()?.message}",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun SongItem(song: Song, onPlayClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(song.coverImageUrl),
            contentDescription = "Cover Image",
            modifier = Modifier
                .size(64.dp)
                .clickable { onPlayClick() }
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(text = song.title, style = MaterialTheme.typography.titleMedium)
            Text(text = "${song.artistName} • ${song.albumName}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMusicShow() {
    val context = LocalContext.current
    // 1. Define a mock version of the repository for the preview
    val songRepository = object : SongRepository() {
        override suspend fun getAllSongs(): Result<List<Song>> {
            return Result.success(listOf(
                Song(title = "Sample Song 1", artistName = "Artist 1", albumName = "Album 1"),
                Song(title = "Sample Song 2", artistName = "Artist 2", albumName = "Album 2")
            ))
        }
    }

    // 2. Define the factory so the ViewModel can be created with the repository
    val factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MusicViewModel(context.applicationContext as Application, songRepository) as T
        }
    }

    MaterialTheme {
        Surface {
            // 3. Pass the factory to the viewModel() call
            MusicShow(viewModel = viewModel(factory = factory))
        }
    }
}
