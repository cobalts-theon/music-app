package com.example.cinderssoul.ui.dialogs

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import coil3.compose.AsyncImage
import com.example.cinderssoul.BuildConfig
import com.example.cinderssoul.models.Playlist
import com.example.cinderssoul.models.Song
import com.example.cinderssoul.models.User
import com.example.cinderssoul.ui.app.AuthDialogMode
import com.example.cinderssoul.ui.components.CoverImage
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

private val DialogTextFieldShape = RoundedCornerShape(28.dp)

@Composable
internal fun AccountAuthDialog(
    user: User?,
    isLoading: Boolean,
    message: String?,
    onDismiss: () -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: (email: String, password: String, displayName: String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onRequestResetOtp: (email: String) -> Unit,
    onResetPassword: (email: String, otp: String, newPassword: String) -> Unit,
    onUpdateProfile: (displayName: String?) -> Unit,
    onChangeAvatar: () -> Unit,
    onLogout: () -> Unit
) {
    var mode by remember(user?.id) {
        mutableStateOf(if (user == null) AuthDialogMode.SignIn else AuthDialogMode.Profile)
    }
    var email by remember { mutableStateOf(user?.email.orEmpty()) }
    var password by remember { mutableStateOf("") }
    var displayName by remember(user?.id) { mutableStateOf(user?.displayName.orEmpty()) }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    LaunchedEffect(user?.id) {
        mode = if (user == null) AuthDialogMode.SignIn else AuthDialogMode.Profile
        email = user?.email.orEmpty()
        displayName = user?.displayName.orEmpty()
        password = ""
        otp = ""
        newPassword = ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (mode) {
                    AuthDialogMode.SignIn -> "Sign In"
                    AuthDialogMode.Register -> "Create Account"
                    AuthDialogMode.ForgotPassword -> "Reset Password"
                    AuthDialogMode.Profile -> "Profile"
                }
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!message.isNullOrBlank()) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                when (mode) {
                    AuthDialogMode.SignIn -> {
                        AuthEmailField(email = email, onEmailChange = { email = it })
                        AuthPasswordField(value = password, label = "Password", onChange = { password = it })
                        TextButton(
                            enabled = !isLoading,
                            onClick = { mode = AuthDialogMode.ForgotPassword }
                        ) {
                            Text("Forgot password")
                        }
                    }

                    AuthDialogMode.Register -> {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = displayName,
                            onValueChange = { displayName = it },
                            singleLine = true,
                            label = { Text("Display name") },
                            shape = DialogTextFieldShape
                        )
                        AuthEmailField(email = email, onEmailChange = { email = it })
                        AuthPasswordField(value = password, label = "Password", onChange = { password = it })
                    }

                    AuthDialogMode.ForgotPassword -> {
                        AuthEmailField(email = email, onEmailChange = { email = it })
                        AuthPasswordField(value = otp, label = "OTP", onChange = { otp = it })
                        AuthPasswordField(value = newPassword, label = "New password", onChange = { newPassword = it })
                    }

                    AuthDialogMode.Profile -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (user?.avatarUrl.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AccountCircle,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.size(58.dp)
                                    )
                                }
                            } else {
                                AsyncImage(
                                    model = user?.avatarUrl,
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user?.email.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                TextButton(enabled = !isLoading, onClick = onChangeAvatar) {
                                    Text("Change photo")
                                }
                            }
                        }
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = displayName,
                            onValueChange = { displayName = it },
                            singleLine = true,
                            label = { Text("Display name") },
                            shape = DialogTextFieldShape
                        )
                    }
                }
            }
        },
        confirmButton = {
            when (mode) {
                AuthDialogMode.SignIn -> {
                    TextButton(
                        enabled = !isLoading,
                        onClick = { onLogin(email, password) }
                    ) {
                        Text(if (isLoading) "Signing in..." else "Sign In")
                    }
                }
                AuthDialogMode.Register -> {
                    TextButton(
                        enabled = !isLoading,
                        onClick = { onRegister(email, password, displayName) }
                    ) {
                        Text(if (isLoading) "Creating..." else "Create")
                    }
                }
                AuthDialogMode.ForgotPassword -> {
                    TextButton(
                        enabled = !isLoading,
                        onClick = { onResetPassword(email, otp, newPassword) }
                    ) {
                        Text(if (isLoading) "Resetting..." else "Reset")
                    }
                }
                AuthDialogMode.Profile -> {
                    TextButton(
                        enabled = !isLoading,
                        onClick = { onUpdateProfile(displayName) }
                    ) {
                        Text(if (isLoading) "Saving..." else "Save")
                    }
                }
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (mode) {
                    AuthDialogMode.SignIn -> {
                        TextButton(enabled = !isLoading, onClick = { mode = AuthDialogMode.Register }) {
                            Text("Register")
                        }
                        TextButton(enabled = !isLoading, onClick = onGoogleSignIn) {
                            Text("Google")
                        }
                    }
                    AuthDialogMode.Register -> {
                        TextButton(enabled = !isLoading, onClick = { mode = AuthDialogMode.SignIn }) {
                            Text("Sign In")
                        }
                        TextButton(enabled = !isLoading, onClick = onGoogleSignIn) {
                            Text("Google")
                        }
                    }
                    AuthDialogMode.ForgotPassword -> {
                        TextButton(enabled = !isLoading, onClick = { onRequestResetOtp(email) }) {
                            Text("Send OTP")
                        }
                        TextButton(enabled = !isLoading, onClick = { mode = AuthDialogMode.SignIn }) {
                            Text("Back")
                        }
                    }
                    AuthDialogMode.Profile -> {
                        TextButton(enabled = !isLoading, onClick = onLogout) {
                            Text("Logout")
                        }
                    }
                }
            }
        }
    )
}

@Composable
internal fun AuthEmailField(email: String, onEmailChange: (String) -> Unit) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = email,
        onValueChange = onEmailChange,
        singleLine = true,
        label = { Text("Email") },
        shape = DialogTextFieldShape
    )
}

@Composable
internal fun AuthPasswordField(value: String, label: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onChange,
        singleLine = true,
        label = { Text(label) },
        shape = DialogTextFieldShape,
        visualTransformation = PasswordVisualTransformation()
    )
}

internal suspend fun requestGoogleIdToken(context: Context): Result<String> = runCatching {
    val serverClientId = BuildConfig.GOOGLE_SERVER_CLIENT_ID
    if (serverClientId.isBlank()) {
        throw IllegalStateException("Missing GOOGLE_SERVER_CLIENT_ID.")
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(serverClientId)
        .setAutoSelectEnabled(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    val result = CredentialManager.create(context).getCredential(
        context = context,
        request = request
    )

    GoogleIdTokenCredential.createFrom(result.credential.data).idToken
}.recoverCatching { error ->
    if (error is GetCredentialException) {
        throw IllegalStateException(error.message ?: "Google sign-in was canceled.")
    }
    throw error
}

@Composable
internal fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String?, imageUri: Uri?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Playlist") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CoverImage(
                        imageUrl = selectedImageUri?.toString(),
                        title = name.ifBlank { "Playlist cover" },
                        modifier = Modifier.size(68.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    TextButton(onClick = { imageLauncher.launch("image/*") }) {
                        Text("Choose image")
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text("Playlist name") },
                    shape = DialogTextFieldShape
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    shape = DialogTextFieldShape,
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name, description.ifBlank { null }, selectedImageUri) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
internal fun AddSongToPlaylistDialog(
    song: Song,
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onCreateNew: () -> Unit,
    onAdd: (playlistId: Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Playlist") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (playlists.isEmpty()) {
                    Text(
                        text = "No playlist available. Create a new playlist first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        playlists.forEach { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAdd(playlist.id) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CoverImage(
                                    imageUrl = playlist.coverUrl,
                                    title = playlist.name,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = playlist.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${playlist.songs.size} tracks",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCreateNew) {
                Text("New playlist")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
internal fun AddSongsToPlaylistDialog(
    playlist: Playlist,
    songs: List<Song>,
    onDismiss: () -> Unit,
    onAddSong: (Song) -> Unit
) {
    var query by remember(playlist.id) { mutableStateOf("") }
    val existingSongIds = playlist.songs.map { it.id }.toSet()
    val normalizedQuery = query.trim().lowercase()
    val availableSongs = songs
        .filterNot { it.id in existingSongIds }
        .filter { song ->
            normalizedQuery.isBlank() ||
                song.title.lowercase().contains(normalizedQuery) ||
                song.artistName.lowercase().contains(normalizedQuery) ||
                song.albumTitle.lowercase().contains(normalizedQuery)
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add songs",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    label = { Text("Search songs") },
                    shape = DialogTextFieldShape,
                    leadingIcon = {
                        Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    }
                )

                if (availableSongs.isEmpty()) {
                    Text(
                        text = if (query.isBlank()) {
                            "All songs are already in this playlist."
                        } else {
                            "No songs found."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        availableSongs.forEach { song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAddSong(song) }
                                    .padding(horizontal = 8.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CoverImage(
                                    imageUrl = song.coverUrl,
                                    title = song.title,
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${song.artistName} • ${song.albumTitle}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
internal fun EditPlaylistDialog(
    playlist: Playlist,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String?, imageUri: Uri?) -> Unit
) {
    var name by remember(playlist.id) { mutableStateOf(playlist.name) }
    var description by remember(playlist.id) { mutableStateOf(playlist.description.orEmpty()) }
    var selectedImageUri by remember(playlist.id) { mutableStateOf<Uri?>(null) }
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Playlist") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CoverImage(
                        imageUrl = selectedImageUri?.toString() ?: playlist.coverUrl,
                        title = name.ifBlank { playlist.name },
                        modifier = Modifier.size(76.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    TextButton(onClick = { imageLauncher.launch("image/*") }) {
                        Text("Change image")
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text("Playlist name") },
                    shape = DialogTextFieldShape
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    shape = DialogTextFieldShape,
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, description.ifBlank { null }, selectedImageUri) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
