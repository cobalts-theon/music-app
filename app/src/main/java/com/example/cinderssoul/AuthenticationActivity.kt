package com.example.cinderssoul

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.cinderssoul.network.ApiClient
import com.example.cinderssoul.network.AuthDataDto
import com.example.cinderssoul.network.ForgotPasswordRequest
import com.example.cinderssoul.network.GoogleAuthRequest
import com.example.cinderssoul.network.LoginRequest
import com.example.cinderssoul.network.RegisterRequest
import com.example.cinderssoul.network.toApiMessage
import com.example.cinderssoul.admin.AdminActivity
import com.example.cinderssoul.ui.theme.CindersSoulTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

private enum class AuthenticationMode {
    Landing, Login, Register, ResetPassword
}

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
private val AuthCrimson = Color(0xFFE14A3B)
private val AuthFieldShape = RoundedCornerShape(28.dp)
private val AuthButtonShape = RoundedCornerShape(28.dp)
private val AuthFontFamily = FontFamily(
    Font(R.font.spotify_mix_ui_regular, FontWeight.Normal),
    Font(R.font.spotify_mix_ui_bold, FontWeight.Medium),
    Font(R.font.spotify_mix_ui_bold, FontWeight.SemiBold),
    Font(R.font.spotify_mix_ui_bold, FontWeight.Bold)
)
private val AuthTitleFontFamily = FontFamily(
    Font(R.font.spotify_mix_ui_title_bold, FontWeight.Bold),
    Font(R.font.spotify_mix_ui_title_extrabold, FontWeight.ExtraBold),
    Font(R.font.spotify_mix_ui_title_extrabold, FontWeight.Black)
)

class AuthenticationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (hasSavedAuthSession()) {
            openAuthenticatedApp(isSavedAdminSession())
            return
        }

        setContent {
            CindersSoulTheme {
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                    AuthenticationScreen(
                        onClose = { finish() },
                        onAuthenticated = { authData ->
                            saveAuthSession(authData)
                            openAuthenticatedApp(authData.user.role.equals("admin", ignoreCase = true))
                        }
                    )
                }
            }
        }
    }

    private fun hasSavedAuthSession(): Boolean {
        val prefs = getSharedPreferences(PLAYER_PREFS, Context.MODE_PRIVATE)
        val accessToken = prefs.getString(KEY_API_ACCESS_TOKEN, null)
        val refreshToken = prefs.getString(KEY_API_REFRESH_TOKEN, null)
        if (prefs.getBoolean(KEY_EXPLICIT_AUTH, false) && (!accessToken.isNullOrBlank() || !refreshToken.isNullOrBlank())) {
            ApiClient.setAccessToken(accessToken)
            return true
        }
        return false
    }

    private fun isSavedAdminSession(): Boolean {
        val prefs = getSharedPreferences(PLAYER_PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_AUTH_USER_ROLE, null).equals("admin", ignoreCase = true)
    }

    private fun openAuthenticatedApp(isAdmin: Boolean) {
        val target = if (isAdmin) AdminActivity::class.java else MainActivity::class.java
        startActivity(Intent(this, target))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun saveAuthSession(authData: AuthDataDto) {
        val user = authData.user
        val displayName = user.displayName ?: user.displayNameSnake.orEmpty()
        val avatarUrl = user.avatarUrl ?: user.avatarUrlSnake
        val createdAt = user.createdAt ?: user.createdAtCamel
        getSharedPreferences(PLAYER_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_API_ACCESS_TOKEN, authData.accessToken)
            .putString(KEY_API_REFRESH_TOKEN, authData.refreshToken)
            .putBoolean(KEY_EXPLICIT_AUTH, true)
            .putInt(KEY_AUTH_USER_ID, user.id)
            .putString(KEY_AUTH_USER_EMAIL, user.email)
            .putString(KEY_AUTH_USER_DISPLAY_NAME, displayName)
            .putString(KEY_AUTH_USER_AVATAR_URL, avatarUrl)
            .putString(KEY_AUTH_USER_ROLE, user.role ?: "user")
            .putString(KEY_AUTH_USER_CREATED_AT, createdAt)
            .apply()
        ApiClient.setAccessToken(authData.accessToken)
    }
}

@Composable
private fun AuthenticationScreen(
    onClose: () -> Unit,
    onAuthenticated: (AuthDataDto) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mode by rememberSaveable { mutableStateOf(AuthenticationMode.Landing) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }

    fun handleBack() {
        if (mode == AuthenticationMode.Landing) {
            onClose()
        } else {
            mode = AuthenticationMode.Landing
            message = null
        }
    }

    BackHandler(onBack = ::handleBack)

    fun runAuthAction(action: suspend () -> Unit) {
        scope.launch {
            isLoading = true
            message = null
            runCatching { action() }
                .onFailure { message = it.toApiMessage("Authentication failed.") }
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painterResource(id = R.drawable.authentication_background2),
                contentScale = ContentScale.FillBounds
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(58.dp))
            AuthLogoFrame()
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Cinder's Soul",
                color = Color.White,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = AuthTitleFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 40.sp,
                    lineHeight = 44.sp
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Sign in to continue listening.",
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = AuthFontFamily),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(Modifier.height(30.dp))

            if (!message.isNullOrBlank()) {
                Text(
                    text = message.orEmpty(),
                    color = AuthCrimson.copy(alpha = 0.92f),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = AuthFontFamily),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }

            when (mode) {
                AuthenticationMode.Landing -> AuthenticationLanding(
                    isLoading = isLoading,
                    onGoogle = {
                        runAuthAction {
                            val idToken = requestAuthenticationGoogleIdToken(context)
                            val response = ApiClient.apiService.googleAuth(GoogleAuthRequest(idToken))
                            onAuthenticated(response.data ?: error(response.message ?: "Google login failed."))
                        }
                    },
                    onEmail = { mode = AuthenticationMode.Register },
                    onLogin = { mode = AuthenticationMode.Login }
                )

                AuthenticationMode.Login -> AuthenticationForm(
                    mode = mode,
                    email = email,
                    password = password,
                    displayName = displayName,
                    isLoading = isLoading,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onDisplayNameChange = { displayName = it },
                    onSubmit = {
                        val validationMessage = validateLoginInput(email, password)
                        if (validationMessage != null) {
                            message = validationMessage
                        } else {
                            runAuthAction {
                                val response = ApiClient.apiService.login(
                                    LoginRequest(email = email.trim(), password = password)
                                )
                                onAuthenticated(response.data ?: error(response.message ?: "Login failed."))
                            }
                        }
                    },
                    onGoogle = {
                        runAuthAction {
                            val idToken = requestAuthenticationGoogleIdToken(context)
                            val response = ApiClient.apiService.googleAuth(GoogleAuthRequest(idToken))
                            onAuthenticated(response.data ?: error(response.message ?: "Google login failed."))
                        }
                    },
                    onForgot = { mode = AuthenticationMode.ResetPassword },
                    onSwitchMode = { mode = AuthenticationMode.Register }
                )

                AuthenticationMode.Register -> AuthenticationForm(
                    mode = mode,
                    email = email,
                    password = password,
                    displayName = displayName,
                    isLoading = isLoading,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onDisplayNameChange = { displayName = it },
                    onSubmit = {
                        val validationMessage = validateRegisterInput(email, password, displayName)
                        if (validationMessage != null) {
                            message = validationMessage
                        } else {
                            runAuthAction {
                                val response = ApiClient.apiService.register(
                                    RegisterRequest(
                                        email = email.trim(),
                                        password = password,
                                        displayName = displayName.trim()
                                    )
                                )
                                onAuthenticated(response.data ?: error(response.message ?: "Registration failed."))
                            }
                        }
                    },
                    onGoogle = {
                        runAuthAction {
                            val idToken = requestAuthenticationGoogleIdToken(context)
                            val response = ApiClient.apiService.googleAuth(GoogleAuthRequest(idToken))
                            onAuthenticated(response.data ?: error(response.message ?: "Google login failed."))
                        }
                    },
                    onForgot = { mode = AuthenticationMode.ResetPassword },
                    onSwitchMode = { mode = AuthenticationMode.Login }
                )

                AuthenticationMode.ResetPassword -> AuthenticationForm(
                    mode = mode,
                    email = email,
                    password = password,
                    displayName = displayName,
                    isLoading = isLoading,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onDisplayNameChange = { displayName = it },
                    onSubmit = {
                        val normalizedEmail = email.trim()
                        val validationMessage = validateEmailInput(normalizedEmail)
                        if (validationMessage != null) {
                            message = validationMessage
                        } else {
                            runAuthAction {
                                val response = ApiClient.apiService.requestPasswordResetOtp(
                                    ForgotPasswordRequest(email = normalizedEmail)
                                )
                                response.data ?: error(response.message ?: "Unable to send OTP.")
                                context.startActivity(
                                    OtpVerificationActivity.createIntent(
                                        context = context,
                                        email = normalizedEmail
                                    )
                                )
                            }
                        }
                    },
                    onGoogle = {
                        runAuthAction {
                            val idToken = requestAuthenticationGoogleIdToken(context)
                            val response = ApiClient.apiService.googleAuth(GoogleAuthRequest(idToken))
                            onAuthenticated(response.data ?: error(response.message ?: "Google login failed."))
                        }
                    },
                    onForgot = { mode = AuthenticationMode.Login },
                    onSwitchMode = { mode = AuthenticationMode.Login }
                )
            }
        }

        IconButton(
            onClick = ::handleBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 14.dp, top = 10.dp)
                .size(44.dp)
                .zIndex(1f)
                .background(Color.White.copy(alpha = 0.10f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun AuthLogoFrame() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(204.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.cinders_souls_logo),
            contentDescription = "Cinder's Soul",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(350.dp)
        )
    }
}

@Composable
private fun AuthenticationLanding(
    isLoading: Boolean,
    onGoogle: () -> Unit,
    onEmail: () -> Unit,
    onLogin: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AuthPrimaryButton(text = "Continue with email", enabled = !isLoading, onClick = onEmail)
        AuthOutlinedButton(
            text = "Continue with Google",
            enabled = !isLoading,
            onClick = onGoogle,
            iconResId = R.drawable.bootstrap_google
        )
        AuthSwitchLabel(
            prefix = "If you have an account,",
            actionText = " Log in",
            enabled = !isLoading,
            onClick = onLogin
        )
    }
}

@Composable
private fun AuthenticationForm(
    mode: AuthenticationMode,
    email: String,
    password: String,
    displayName: String,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoogle: () -> Unit,
    onForgot: () -> Unit,
    onSwitchMode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 360.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (mode == AuthenticationMode.Register) {
            AuthTextField(
                value = displayName,
                onValueChange = onDisplayNameChange,
                label = "Display name",
                leadingIcon = Icons.Rounded.Person
            )
        }
        AuthTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            leadingIcon = Icons.Rounded.Email
        )
        when (mode) {
            AuthenticationMode.Login,
            AuthenticationMode.Register -> {
                AuthTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = "Password",
                    leadingIcon = Icons.Rounded.Lock,
                    isPassword = true
                )
            }

            AuthenticationMode.ResetPassword -> {
                Text(
                    text = "We'll send a 6-digit OTP to this email. Enter OTP and your new password in the next screen.",
                    color = Color.White.copy(alpha = 0.76f),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = AuthFontFamily)
                )
            }

            AuthenticationMode.Landing -> Unit
        }

        AuthPrimaryButton(
            text = when (mode) {
                AuthenticationMode.Login -> "Log in"
                AuthenticationMode.Register -> "Create account"
                AuthenticationMode.ResetPassword -> "Send OTP"
                AuthenticationMode.Landing -> "Continue"
            },
            enabled = !isLoading,
            onClick = onSubmit
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(26.dp),
                color = AuthCrimson
            )
        }

        AuthOutlinedButton(text = "Continue with Google", enabled = !isLoading, onClick = onGoogle, iconResId = R.drawable.bootstrap_google)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (mode) {
                    AuthenticationMode.Register -> "If you have an account,"
                    AuthenticationMode.ResetPassword -> "Remember your password?"
                    else -> "New to Cinder's Soul?"
                },
                color = Color.White.copy(alpha = 0.62f),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = AuthFontFamily)
            )
            Text(
                text = when (mode) {
                    AuthenticationMode.Register -> " Log in"
                    AuthenticationMode.ResetPassword -> " Log in"
                    else -> " Sign up"
                },
                color = Color.White,
                fontFamily = AuthFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(enabled = !isLoading, onClick = onSwitchMode)
            )
        }

        if (mode == AuthenticationMode.Login) {
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                onClick = onForgot
            ) {
                Text(
                    "Forgot password?",
                    color = Color.White.copy(alpha = 0.86f),
                    fontFamily = AuthFontFamily
                )
            }
        }
    }
}

@Composable
private fun AuthSwitchLabel(
    prefix: String,
    actionText: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = prefix,
            color = Color.White.copy(alpha = 0.62f),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = AuthFontFamily)
        )
        Text(
            text = actionText,
            color = Color.White,
            fontFamily = AuthFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(enabled = enabled, onClick = onClick)
        )
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(label, fontFamily = AuthFontFamily) },
        shape = AuthFieldShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.08f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
            focusedBorderColor = AuthCrimson,
            unfocusedBorderColor = Color.White.copy(alpha = 0.18f),
            focusedLabelColor = AuthCrimson,
            unfocusedLabelColor = Color.White.copy(alpha = 0.62f),
            focusedLeadingIconColor = AuthCrimson,
            unfocusedLeadingIconColor = Color.White.copy(alpha = 0.62f),
            cursorColor = AuthCrimson
        ),
        leadingIcon = {
            Icon(imageVector = leadingIcon, contentDescription = null, Modifier.padding(start = 8.dp))
        },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
    )
}

@Composable
private fun AuthPrimaryButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        onClick = onClick,
        shape = AuthButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = AuthCrimson,
            contentColor = Color.White
        )
    ) {
        Text(text = text, fontFamily = AuthFontFamily, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AuthOutlinedButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    iconResId: Int? = null // Thêm tham số nhận icon resource
) {
    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(AuthButtonShape)
            .border(1.dp, Color.White.copy(alpha = 0.34f), AuthButtonShape),
        enabled = enabled,
        onClick = onClick,
        shape = AuthButtonShape,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                fontFamily = AuthFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(6.dp))
            if (iconResId != null) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 12.dp) // Khoảng cách giữa Icon và Text
                        .size(22.dp)         // Kích thước vừa vặn cho icon
                )
            }
        }
    }
}

private fun validateLoginInput(email: String, password: String): String? {
    return validateEmailInput(email)
        ?: when {
            password.isBlank() -> "Password is required."
            else -> null
        }
}

private fun validateRegisterInput(email: String, password: String, displayName: String): String? {
    return when {
        displayName.isBlank() -> "Display name is required."
        else -> validateLoginInput(email, password)
            ?: if (password.length < 6) "Password must be at least 6 characters long." else null
    }
}

private fun validateEmailInput(email: String): String? {
    val trimmedEmail = email.trim()
    return when {
        trimmedEmail.isBlank() -> "Email is required."
        !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> "Please enter a valid email address."
        else -> null
    }
}

private suspend fun requestAuthenticationGoogleIdToken(context: Context): String {
    val serverClientId = BuildConfig.GOOGLE_SERVER_CLIENT_ID
    if (serverClientId.isBlank()) {
        throw IllegalStateException("Missing Google client id.")
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(serverClientId)
        .setAutoSelectEnabled(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val result = CredentialManager.create(context).getCredential(
            context = context,
            request = request
        )
        GoogleIdTokenCredential.createFrom(result.credential.data).idToken
    } catch (error: GetCredentialException) {
        throw IllegalStateException(error.message ?: "Google sign-in was canceled.")
    }
}
