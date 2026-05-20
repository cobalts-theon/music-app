package com.example.cinderssoul.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cinderssoul.AuthenticationActivity
import com.example.cinderssoul.models.Album
import com.example.cinderssoul.models.Artist
import com.example.cinderssoul.models.Song
import com.example.cinderssoul.models.User
import com.example.cinderssoul.network.ApiClient
import com.example.cinderssoul.ui.app.AppleMusicRed
import com.example.cinderssoul.ui.components.CoverImage
import com.example.cinderssoul.ui.components.LibraryRowDivider
import com.example.cinderssoul.ui.components.itemCountText
import com.example.cinderssoul.ui.theme.CindersSoulTheme

private val AdminCardShape = RoundedCornerShape(18.dp)
private val AdminCardBorder = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
private val AdminTextFieldShape = RoundedCornerShape(28.dp)
private const val PLAYER_PREFS = "player_state"
private const val KEY_API_ACCESS_TOKEN = "api_access_token"
private const val KEY_API_REFRESH_TOKEN = "api_refresh_token"
private const val KEY_EXPLICIT_AUTH = "explicit_auth"
private const val KEY_AUTH_USER_ID = "auth_user_id"
private const val KEY_AUTH_USER_EMAIL = "auth_user_email"
private const val KEY_AUTH_USER_DISPLAY_NAME = "auth_user_display_name"
private const val KEY_AUTH_USER_AVATAR_URL = "auth_user_avatar_url"
private const val KEY_AUTH_USER_ROLE = "auth_user_role"
private const val KEY_AUTH_USER_CREATED_AT = "auth_user_created_at"

class AdminActivity : ComponentActivity() {
    private val viewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CindersSoulTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AdminApp(viewModel = viewModel, onLogout = ::logout)
                }
            }
        }
    }

    private fun logout() {
        getSharedPreferences(PLAYER_PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_API_ACCESS_TOKEN)
            .remove(KEY_API_REFRESH_TOKEN)
            .remove(KEY_EXPLICIT_AUTH)
            .remove(KEY_AUTH_USER_ID)
            .remove(KEY_AUTH_USER_EMAIL)
            .remove(KEY_AUTH_USER_DISPLAY_NAME)
            .remove(KEY_AUTH_USER_AVATAR_URL)
            .remove(KEY_AUTH_USER_ROLE)
            .remove(KEY_AUTH_USER_CREATED_AT)
            .apply()
        ApiClient.setAccessToken(null)
        startActivity(
            Intent(this, AuthenticationActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        )
        finish()
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AdminApp(viewModel: AdminViewModel, onLogout: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val searchQuery = state.searchQuery.trim()
    val filteredSongs = state.songs.filter { it.matchesAdminSearch(searchQuery) }
    val filteredArtists = state.artists.filter { it.matchesAdminSearch(searchQuery) }
    val filteredAlbums = state.albums.filter { it.matchesAdminSearch(searchQuery) }
    val filteredUsers = state.users.filter { it.matchesAdminSearch(searchQuery) }
    val selectedArtist = state.artistDetailId?.let { id -> state.artists.firstOrNull { it.id == id } }
    val selectedAlbum = state.albumDetailId?.let { id -> state.albums.firstOrNull { it.id == id } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin") },
                navigationIcon = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Log out")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = AppleMusicRed,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            AdminSectionBar(
                activeSection = state.activeSection,
                onSectionSelected = viewModel::setSection
            )
            if (state.activeSection != AdminSection.Dashboard) {
                AdminSearchField(
                    query = state.searchQuery,
                    sectionTitle = state.activeSection.title,
                    onQueryChange = viewModel::updateSearchQuery,
                    onClear = { viewModel.updateSearchQuery("") }
                )
            }

            if (state.isLoading && state.songs.isEmpty() && state.artists.isEmpty() && state.albums.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.errorMessage?.let { message ->
                        item { AdminMessage(text = message, isError = true) }
                    }
                    state.statusMessage?.let { message ->
                        item { AdminMessage(text = message, isError = false) }
                    }
                    if (state.isSaving) {
                        item { AdminSavingRow() }
                    }

                    when (state.activeSection) {
                        AdminSection.Dashboard -> item {
                            AdminDashboard(state = state)
                        }
                        AdminSection.Songs -> {
                            item {
                                AdminSectionHeader(
                                    title = "Songs",
                                    countText = adminCountText(filteredSongs.size, state.songs.size, "song", searchQuery),
                                    onAdd = viewModel::openNewSong
                                )
                            }
                            if (filteredSongs.isEmpty()) {
                                item { AdminEmptySearchResult(searchQuery) }
                            }
                            items(filteredSongs, key = { it.id }) { song ->
                                AdminSongRow(
                                    song = song,
                                    onEdit = { viewModel.editSong(song) },
                                    onDelete = { viewModel.deleteSong(song) }
                                )
                            }
                        }
                        AdminSection.Artists -> {
                            item {
                                AdminSectionHeader(
                                    title = "Artists",
                                    countText = adminCountText(filteredArtists.size, state.artists.size, "artist", searchQuery),
                                    onAdd = viewModel::openNewArtist
                                )
                            }
                            if (filteredArtists.isEmpty()) {
                                item { AdminEmptySearchResult(searchQuery) }
                            }
                            items(filteredArtists, key = { it.id }) { artist ->
                                AdminArtistRow(
                                    artist = artist,
                                    songsCount = state.songs.count { it.artistId == artist.id },
                                    onDetails = { viewModel.openArtistDetails(artist) },
                                    onEdit = { viewModel.editArtist(artist) },
                                    onDelete = { viewModel.deleteArtist(artist) }
                                )
                            }
                        }
                        AdminSection.Albums -> {
                            item {
                                AdminSectionHeader(
                                    title = "Albums",
                                    countText = adminCountText(filteredAlbums.size, state.albums.size, "album", searchQuery),
                                    onAdd = viewModel::openNewAlbum
                                )
                            }
                            if (filteredAlbums.isEmpty()) {
                                item { AdminEmptySearchResult(searchQuery) }
                            }
                            items(filteredAlbums, key = { it.id }) { album ->
                                AdminAlbumRow(
                                    album = album,
                                    songsCount = state.songs.count { it.albumId == album.id },
                                    onDetails = { viewModel.openAlbumDetails(album) },
                                    onEdit = { viewModel.editAlbum(album) },
                                    onDelete = { viewModel.deleteAlbum(album) }
                                )
                            }
                        }
                        AdminSection.Users -> {
                            item {
                                AdminSectionHeader(
                                    title = "Users",
                                    countText = adminCountText(filteredUsers.size, state.users.size, "user", searchQuery),
                                    onAdd = viewModel::openNewUser
                                )
                            }
                            if (filteredUsers.isEmpty()) {
                                item { AdminEmptySearchResult(searchQuery) }
                            }
                            items(filteredUsers, key = { it.id }) { user ->
                                AdminUserRow(
                                    user = user,
                                    onEdit = { viewModel.editUser(user) },
                                    onDelete = { viewModel.deleteUser(user) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedArtist?.let { artist ->
        AdminArtistDetailDialog(
            artist = artist,
            songs = state.songs.filter { it.artistId == artist.id },
            isSaving = state.isSaving,
            statusMessage = state.statusMessage,
            errorMessage = state.errorMessage,
            onDismiss = viewModel::closeArtistDetails,
            onEditArtist = { viewModel.editArtist(artist) },
            onDeleteArtist = { viewModel.deleteArtist(artist) },
            onAddSong = { viewModel.openNewSongForArtist(artist) },
            onEditSong = viewModel::editSong,
            onDeleteSong = viewModel::deleteSong
        )
    }
    selectedAlbum?.let { album ->
        AdminAlbumDetailDialog(
            album = album,
            songs = state.songs.filter { it.albumId == album.id },
            isSaving = state.isSaving,
            statusMessage = state.statusMessage,
            errorMessage = state.errorMessage,
            onDismiss = viewModel::closeAlbumDetails,
            onEditAlbum = { viewModel.editAlbum(album) },
            onDeleteAlbum = { viewModel.deleteAlbum(album) },
            onAddSong = { viewModel.openNewSongForAlbum(album) },
            onEditSong = viewModel::editSong,
            onDeleteSong = viewModel::deleteSong
        )
    }

    state.songForm?.let { form ->
        SongFormDialog(
            form = form,
            isSaving = state.isSaving,
            onChange = viewModel::updateSongForm,
            onDismiss = viewModel::closeSongForm,
            onSave = viewModel::saveSong
        )
    }
    state.artistForm?.let { form ->
        ArtistFormDialog(
            form = form,
            isSaving = state.isSaving,
            onChange = viewModel::updateArtistForm,
            onDismiss = viewModel::closeArtistForm,
            onSave = viewModel::saveArtist
        )
    }
    state.albumForm?.let { form ->
        AlbumFormDialog(
            form = form,
            isSaving = state.isSaving,
            onChange = viewModel::updateAlbumForm,
            onDismiss = viewModel::closeAlbumForm,
            onSave = viewModel::saveAlbum
        )
    }
    state.userForm?.let { form ->
        UserFormDialog(
            form = form,
            isSaving = state.isSaving,
            onChange = viewModel::updateUserForm,
            onDismiss = viewModel::closeUserForm,
            onSave = viewModel::saveUser
        )
    }
}

@Composable
private fun AdminSectionBar(activeSection: AdminSection, onSectionSelected: (AdminSection) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(AdminSection.entries) { section ->
            FilterChip(
                selected = activeSection == section,
                onClick = { onSectionSelected(section) },
                label = { Text(section.title, maxLines = 1) },
                leadingIcon = {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun AdminSearchField(
    query: String,
    sectionTitle: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Search $sectionTitle") },
        singleLine = true,
        leadingIcon = {
            Icon(Icons.Rounded.Search, contentDescription = null)
        },
        trailingIcon = if (query.isNotBlank()) {
            {
                IconButton(onClick = onClear) {
                    Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                }
            }
        } else {
            null
        },
        shape = AdminTextFieldShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppleMusicRed,
            focusedLabelColor = AppleMusicRed,
            cursorColor = AppleMusicRed,
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.56f)
        )
    )
}

private val AdminSection.icon: ImageVector
    get() = when (this) {
        AdminSection.Dashboard -> Icons.Rounded.Dashboard
        AdminSection.Songs -> Icons.Rounded.MusicNote
        AdminSection.Artists -> Icons.Rounded.AccountCircle
        AdminSection.Albums -> Icons.Rounded.Album
        AdminSection.Users -> Icons.Rounded.Groups
    }

@Composable
private fun AdminDashboard(state: AdminUiState) {
    val catalogTotal = state.summary.songsCount + state.summary.artistsCount + state.summary.albumsCount

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AdminDashboardSummaryCard(
            catalogTotal = catalogTotal,
            playlistsCount = state.summary.playlistsCount,
            usersCount = state.summary.usersCount
        )
        AdminCatalogChart(state = state)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminStatCard(
                title = "Songs",
                value = state.summary.songsCount.toString(),
                icon = Icons.Rounded.MusicNote,
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = "Artists",
                value = state.summary.artistsCount.toString(),
                icon = Icons.Rounded.AccountCircle,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminStatCard(
                title = "Albums",
                value = state.summary.albumsCount.toString(),
                icon = Icons.Rounded.Album,
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = "Users",
                value = state.summary.usersCount.toString(),
                icon = Icons.Rounded.Groups,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminStatCard(
                title = "Playlists",
                value = state.summary.playlistsCount.toString(),
                icon = Icons.Rounded.LibraryMusic,
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = "Catalog",
                value = catalogTotal.toString(),
                icon = Icons.Rounded.AdminPanelSettings,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AdminDashboardSummaryCard(catalogTotal: Int, playlistsCount: Int, usersCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AdminCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = AdminCardBorder
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.AdminPanelSettings,
                    contentDescription = null,
                    tint = AppleMusicRed,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Catalog Control",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$catalogTotal catalog items - $playlistsCount playlists - $usersCount users",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(118.dp),
        shape = AdminCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = AdminCardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, tint = AppleMusicRed, modifier = Modifier.size(28.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AdminCatalogChart(state: AdminUiState) {
    val chartItems = listOf(
        "Songs" to state.summary.songsCount,
        "Artists" to state.summary.artistsCount,
        "Albums" to state.summary.albumsCount,
        "Users" to state.summary.usersCount,
        "Playlists" to state.summary.playlistsCount
    )
    val total = chartItems.sumOf { it.second }.coerceAtLeast(0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AdminCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = AdminCardBorder
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Catalog Chart",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Box(
                    modifier = Modifier.size(158.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(130.dp)) {
                        val strokeWidth = 22.dp.toPx()
                        drawArc(
                            color = Color.White.copy(alpha = 0.10f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        if (total > 0) {
                            var startAngle = -90f
                            chartItems.forEachIndexed { index, item ->
                                val sweepAngle = (item.second.toFloat() / total.toFloat()) * 360f
                                if (sweepAngle > 0f) {
                                    drawArc(
                                        color = ChartColors[index % ChartColors.size],
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                    startAngle += sweepAngle
                                }
                            }
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = total.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    chartItems.forEachIndexed { index, item ->
                        AdminChartLegendItem(
                            label = item.first,
                            value = item.second,
                            color = ChartColors[index % ChartColors.size]
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminChartLegendItem(label: String, value: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
        )
    }
}

private val ChartColors = listOf(
    AppleMusicRed,
    Color(0xFF7CDBA6),
    Color(0xFF6EA8FE),
    Color(0xFFE0B95C),
    Color(0xFFB78CFF)
)

private fun adminCountText(visibleCount: Int, totalCount: Int, singular: String, query: String): String {
    if (query.isBlank()) {
        return itemCountText(totalCount, singular)
    }
    return "${itemCountText(visibleCount, singular)} found from ${itemCountText(totalCount, singular)}"
}

private fun Song.matchesAdminSearch(query: String): Boolean =
    query.isBlank() ||
        id.toString().adminContains(query) ||
        title.adminContains(query) ||
        artistName.adminContains(query) ||
        albumTitle.adminContains(query) ||
        genre.adminContains(query)

private fun Artist.matchesAdminSearch(query: String): Boolean =
    query.isBlank() ||
        id.toString().adminContains(query) ||
        name.adminContains(query) ||
        bio.adminContains(query)

private fun Album.matchesAdminSearch(query: String): Boolean =
    query.isBlank() ||
        id.toString().adminContains(query) ||
        title.adminContains(query) ||
        artist?.name.adminContains(query) ||
        releaseDate.adminContains(query)

private fun User.matchesAdminSearch(query: String): Boolean =
    query.isBlank() ||
        id.toString().adminContains(query) ||
        displayName.adminContains(query) ||
        email.adminContains(query) ||
        role.adminContains(query)

private fun String?.adminContains(query: String): Boolean =
    this?.contains(query, ignoreCase = true) == true

@Composable
private fun AdminMessage(text: String, isError: Boolean) {
    Text(
        text = text,
        color = if (isError) AppleMusicRed else MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun AdminSavingRow() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        Spacer(Modifier.width(8.dp))
        Text("Saving", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun AdminEmptySearchResult(query: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AdminCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = AdminCardBorder
    ) {
        Text(
            text = if (query.isBlank()) "No records." else "No results for \"$query\"",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AdminSectionHeader(title: String, countText: String, onAdd: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        AdminSectionTitle(title = title, countText = countText, modifier = Modifier.weight(1f))
        Button(onClick = onAdd) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add")
        }
    }
}

@Composable
private fun AdminSectionTitle(title: String, countText: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(countText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AdminSongRow(song: Song, onEdit: () -> Unit, onDelete: () -> Unit) {
    AdminMediaRow(
        title = song.title,
        subtitle = "${song.artistName} - ${song.genre ?: "No genre"}",
        imageUrl = song.coverUrl,
        onEdit = onEdit,
        onDelete = onDelete
    )
}

@Composable
private fun AdminArtistRow(
    artist: Artist,
    songsCount: Int,
    onDetails: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AdminMediaRow(
        title = artist.name,
        subtitle = itemCountText(songsCount, "song"),
        imageUrl = artist.avatarUrl,
        onDetails = onDetails,
        onEdit = onEdit,
        onDelete = onDelete
    )
}

@Composable
private fun AdminAlbumRow(
    album: Album,
    songsCount: Int,
    onDetails: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AdminMediaRow(
        title = album.title,
        subtitle = "${album.artist?.name ?: "Unknown artist"} - ${itemCountText(songsCount, "song")}",
        imageUrl = album.coverUrl,
        onDetails = onDetails,
        onEdit = onEdit,
        onDelete = onDelete
    )
}

@Composable
private fun AdminUserRow(user: User, onEdit: () -> Unit, onDelete: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoverImage(imageUrl = user.avatarUrl, title = user.displayName, modifier = Modifier.size(52.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.displayName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Text(user.role, color = if (user.isAdmin) AppleMusicRed else MaterialTheme.colorScheme.onSurfaceVariant)
            IconButton(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = AppleMusicRed)
            }
        }
        LibraryRowDivider(modifier = Modifier.padding(start = 64.dp))
    }
}

@Composable
private fun AdminMediaRow(
    title: String,
    subtitle: String,
    imageUrl: String?,
    onDetails: (() -> Unit)? = null,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoverImage(imageUrl = imageUrl, title = title, modifier = Modifier.size(56.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            onDetails?.let { details ->
                IconButton(onClick = details) {
                    Icon(Icons.Rounded.LibraryMusic, contentDescription = "Manage songs")
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = AppleMusicRed)
            }
        }
        LibraryRowDivider(modifier = Modifier.padding(start = 68.dp))
    }
}

@Composable
private fun AdminArtistDetailDialog(
    artist: Artist,
    songs: List<Song>,
    isSaving: Boolean,
    statusMessage: String?,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onEditArtist: () -> Unit,
    onDeleteArtist: () -> Unit,
    onAddSong: () -> Unit,
    onEditSong: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(artist.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminDetailHero(
                    imageUrl = artist.avatarUrl,
                    title = artist.name,
                    subtitle = itemCountText(songs.size, "song"),
                    body = artist.bio.orEmpty().ifBlank { "No bio." }
                )
                AdminDetailMessages(statusMessage = statusMessage, errorMessage = errorMessage)
                AdminDetailActionRow(
                    isSaving = isSaving,
                    onAddSong = onAddSong,
                    onEdit = onEditArtist,
                    onDelete = onDeleteArtist
                )
                AdminDetailSongList(
                    title = "Songs by artist",
                    emptyText = "No songs for this artist.",
                    songs = songs,
                    onEditSong = onEditSong,
                    onDeleteSong = onDeleteSong
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun AdminAlbumDetailDialog(
    album: Album,
    songs: List<Song>,
    isSaving: Boolean,
    statusMessage: String?,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onEditAlbum: () -> Unit,
    onDeleteAlbum: () -> Unit,
    onAddSong: () -> Unit,
    onEditSong: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit
) {
    val artistName = album.artist?.name ?: "Unknown artist"
    val releaseText = album.releaseDate.orEmpty().ifBlank { "No release date." }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(album.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminDetailHero(
                    imageUrl = album.coverUrl,
                    title = album.title,
                    subtitle = "$artistName - ${itemCountText(songs.size, "song")}",
                    body = releaseText
                )
                AdminDetailMessages(statusMessage = statusMessage, errorMessage = errorMessage)
                AdminDetailActionRow(
                    isSaving = isSaving,
                    onAddSong = onAddSong,
                    onEdit = onEditAlbum,
                    onDelete = onDeleteAlbum
                )
                AdminDetailSongList(
                    title = "Album tracks",
                    emptyText = "No songs in this album.",
                    songs = songs,
                    onEditSong = onEditSong,
                    onDeleteSong = onDeleteSong
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun AdminDetailHero(imageUrl: String?, title: String, subtitle: String, body: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CoverImage(imageUrl = imageUrl, title = title, modifier = Modifier.size(64.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AdminDetailMessages(statusMessage: String?, errorMessage: String?) {
    errorMessage?.let { message ->
        AdminMessage(text = message, isError = true)
    }
    statusMessage?.let { message ->
        AdminMessage(text = message, isError = false)
    }
}

@Composable
private fun AdminDetailActionRow(
    isSaving: Boolean,
    onAddSong: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onAddSong,
            enabled = !isSaving,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
        }
        OutlinedButton(
            onClick = onEdit,
            enabled = !isSaving,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        OutlinedButton(
            onClick = onDelete,
            enabled = !isSaving,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Rounded.Delete, contentDescription = null, tint = AppleMusicRed, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun AdminDetailSongList(
    title: String,
    emptyText: String,
    songs: List<Song>,
    onEditSong: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (songs.isEmpty()) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            songs.forEach { song ->
                AdminDetailSongRow(
                    song = song,
                    onEdit = { onEditSong(song) },
                    onDelete = { onDeleteSong(song) }
                )
            }
        }
    }
}

@Composable
private fun AdminDetailSongRow(song: Song, onEdit: () -> Unit, onDelete: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoverImage(imageUrl = song.coverUrl, title = song.title, modifier = Modifier.size(44.dp))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(song.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = "${song.albumTitle} - ${song.genre ?: "No genre"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            OutlinedButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)) {
                Icon(Icons.Rounded.Edit, contentDescription = "Edit song", modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = onDelete, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete song", tint = AppleMusicRed, modifier = Modifier.size(18.dp))
            }
        }
        LibraryRowDivider(modifier = Modifier.padding(start = 54.dp))
    }
}

@Composable
private fun SongFormDialog(
    form: AdminSongForm,
    isSaving: Boolean,
    onChange: ((AdminSongForm) -> AdminSongForm) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AdminFormDialog(
        title = if (form.isEditing) "Edit Song" else "New Song",
        isSaving = isSaving,
        onDismiss = onDismiss,
        onSave = onSave
    ) {
        AdminTextField("Title", form.title) { value -> onChange { it.copy(title = value) } }
        AdminTextField("Artist ID", form.artistId, keyboardType = KeyboardType.Number, enabled = !form.isEditing) { value ->
            onChange { it.copy(artistId = value) }
        }
        AdminTextField("Album ID", form.albumId, keyboardType = KeyboardType.Number) { value ->
            onChange { it.copy(albumId = value) }
        }
        AdminTextField("Duration", form.duration, keyboardType = KeyboardType.Number) { value ->
            onChange { it.copy(duration = value) }
        }
        AdminTextField("Audio URL", form.audioUrl) { value -> onChange { it.copy(audioUrl = value) } }
        AdminTextField("Cover URL", form.coverUrl) { value -> onChange { it.copy(coverUrl = value) } }
        AdminTextField("Genre", form.genre) { value -> onChange { it.copy(genre = value) } }
        AdminTextField("Lyrics", form.lyrics, minLines = 3) { value -> onChange { it.copy(lyrics = value) } }
    }
}

@Composable
private fun ArtistFormDialog(
    form: AdminArtistForm,
    isSaving: Boolean,
    onChange: ((AdminArtistForm) -> AdminArtistForm) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AdminFormDialog(
        title = if (form.isEditing) "Edit Artist" else "New Artist",
        isSaving = isSaving,
        onDismiss = onDismiss,
        onSave = onSave
    ) {
        AdminTextField("Name", form.name) { value -> onChange { it.copy(name = value) } }
        AdminTextField("Bio", form.bio, minLines = 3) { value -> onChange { it.copy(bio = value) } }
        AdminTextField("Avatar URL", form.avatarUrl) { value -> onChange { it.copy(avatarUrl = value) } }
    }
}

@Composable
private fun AlbumFormDialog(
    form: AdminAlbumForm,
    isSaving: Boolean,
    onChange: ((AdminAlbumForm) -> AdminAlbumForm) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AdminFormDialog(
        title = if (form.isEditing) "Edit Album" else "New Album",
        isSaving = isSaving,
        onDismiss = onDismiss,
        onSave = onSave
    ) {
        AdminTextField("Title", form.title) { value -> onChange { it.copy(title = value) } }
        AdminTextField("Artist ID", form.artistId, keyboardType = KeyboardType.Number, enabled = !form.isEditing) { value ->
            onChange { it.copy(artistId = value) }
        }
        AdminTextField("Cover URL", form.coverUrl) { value -> onChange { it.copy(coverUrl = value) } }
        AdminTextField("Release Date", form.releaseDate) { value -> onChange { it.copy(releaseDate = value) } }
    }
}

@Composable
private fun UserFormDialog(
    form: AdminUserForm,
    isSaving: Boolean,
    onChange: ((AdminUserForm) -> AdminUserForm) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AdminFormDialog(
        title = if (form.isEditing) "Edit User" else "New User",
        isSaving = isSaving,
        onDismiss = onDismiss,
        onSave = onSave
    ) {
        AdminTextField("Email", form.email) { value -> onChange { it.copy(email = value) } }
        AdminTextField("Display name", form.displayName) { value -> onChange { it.copy(displayName = value) } }
        AdminTextField("Avatar URL", form.avatarUrl) { value -> onChange { it.copy(avatarUrl = value) } }
        AdminTextField("Role", form.role) { value -> onChange { it.copy(role = value) } }
        AdminTextField(
            label = if (form.isEditing) "New password (optional)" else "Password",
            value = form.password,
            keyboardType = KeyboardType.Password
        ) { value ->
            onChange { it.copy(password = value) }
        }
    }
}

@Composable
private fun AdminFormDialog(
    title: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content
            )
        },
        confirmButton = {
            Button(onClick = onSave, enabled = !isSaving) {
                Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AdminTextField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    minLines: Int = 1,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        shape = AdminTextFieldShape,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppleMusicRed,
            focusedLabelColor = AppleMusicRed,
            cursorColor = AppleMusicRed,
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.56f)
        )
    )
}
