package com.example.cinderssoul

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cinderssoul.local.CinderDatabase
import com.example.cinderssoul.repository.SongRepository
import com.example.cinderssoul.ui.app.MusicApp
import com.example.cinderssoul.ui.theme.CindersSoulTheme

class MainActivity : ComponentActivity() {
    private val database by lazy { CinderDatabase.getInstance(applicationContext) }
    private val songRepository by lazy {
        SongRepository(musicCacheDao = database.musicCacheDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CindersSoulTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return MusicViewModel(
                                application = application as Application,
                                songRepository = songRepository,
                                musicCacheDao = database.musicCacheDao()
                            ) as T
                        }
                    }
                    MusicApp(viewModel = viewModel(factory = factory))
                }
            }
        }
    }
}