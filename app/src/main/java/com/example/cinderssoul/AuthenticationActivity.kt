package com.example.cinderssoul

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.cinderssoul.network.ApiClient
import com.example.cinderssoul.network.AuthDataDto
import com.example.cinderssoul.network.ForgotPasswordRequest
import com.example.cinderssoul.network.GoogleAuthRequest
import com.example.cinderssoul.network.LoginRequest
import com.example.cinderssoul.network.RegisterRequest
import com.example.cinderssoul.network.ResetPasswordRequest
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
private val AuthCrimson = Color(0xFFC0392B)
private val AuthFieldShape = RoundedCornerShape(24.dp)

class AuthenticationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (hasSavedAuthSession()) {
            openMainApp()
            return
        }

        setContent {
            CindersSoulTheme(darkTheme = true) {
                Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {
                    AuthenticationScreen(
                        onClose = { finish() },
                        onAuthenticated = { authData ->
                            saveAuthSession(authData)
                            openMainApp()
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

    private fun openMainApp() {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun saveAuthSession(authData: AuthDataDto) {
        getSharedPreferences(PLAYER_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_API_ACCESS_TOKEN, authData.accessToken)
            .putString(KEY_API_REFRESH_TOKEN, authData.refreshToken)
            .putBoolean(KEY_EXPLICIT_AUTH, true)
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
    var otp by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
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
                .onFailure { message = it.message ?: "Authentication failed." }
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0509),
                        Color(0xFF080102),
                        Color.Black,
                    )
                )
            )
    ) {
        IconButton(
            onClick = ::handleBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 65.dp)
                .background(AuthCrimson.copy(alpha = 0.18f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(42.dp))
            AuthLogoFrame()
            Spacer(Modifier.height(22.dp))
            Text(
                text = "Cinder's Soul",
                color = Color.White,
                fontSize = 38.sp,
                lineHeight = 42.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Sign in to continue listening.",
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(Modifier.height(30.dp))

            if (!message.isNullOrBlank()) {
                Text(
                    text = message.orEmpty(),
                    color = AuthCrimson.copy(alpha = 0.92f),
                    style = MaterialTheme.typography.bodySmall,
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
                    otp = otp,
                    newPassword = newPassword,
                    isLoading = isLoading,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onDisplayNameChange = { displayName = it },
                    onOtpChange = { otp = it },
                    onNewPasswordChange = { newPassword = it },
                    onSubmit = {
                        runAuthAction {
                            val response = ApiClient.apiService.login(
                                LoginRequest(email = email.trim(), password = password)
                            )
                            onAuthenticated(response.data ?: error(response.message ?: "Login failed."))
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
                    onSwitchMode = { mode = AuthenticationMode.Register },
                    onSendOtp = {}
                )

                AuthenticationMode.Register -> AuthenticationForm(
                    mode = mode,
                    email = email,
                    password = password,
                    displayName = displayName,
                    otp = otp,
                    newPassword = newPassword,
                    isLoading = isLoading,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onDisplayNameChange = { displayName = it },
                    onOtpChange = { otp = it },
                    onNewPasswordChange = { newPassword = it },
                    onSubmit = {
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
                    },
                    onGoogle = {
                        runAuthAction {
                            val idToken = requestAuthenticationGoogleIdToken(context)
                            val response = ApiClient.apiService.googleAuth(GoogleAuthRequest(idToken))
                            onAuthenticated(response.data ?: error(response.message ?: "Google login failed."))
                        }
                    },
                    onForgot = { mode = AuthenticationMode.ResetPassword },
                    onSwitchMode = { mode = AuthenticationMode.Login },
                    onSendOtp = {}
                )

                AuthenticationMode.ResetPassword -> AuthenticationForm(
                    mode = mode,
                    email = email,
                    password = password,
                    displayName = displayName,
                    otp = otp,
                    newPassword = newPassword,
                    isLoading = isLoading,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onDisplayNameChange = { displayName = it },
                    onOtpChange = { otp = it },
                    onNewPasswordChange = { newPassword = it },
                    onSubmit = {
                        runAuthAction {
                            val response = ApiClient.apiService.resetPassword(
                                ResetPasswordRequest(
                                    email = email.trim(),
                                    otp = otp.trim(),
                                    newPassword = newPassword
                                )
                            )
                            onAuthenticated(response.data ?: error(response.message ?: "Reset failed."))
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
                    onSwitchMode = { mode = AuthenticationMode.Login },
                    onSendOtp = {
                        runAuthAction {
                            val response = ApiClient.apiService.requestPasswordResetOtp(
                                ForgotPasswordRequest(email = email.trim())
                            )
                            val data = response.data ?: error(response.message ?: "Unable to send OTP.")
                            message = data.devOtp?.let { "OTP sent. Dev code: $it" } ?: "OTP sent."
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AuthLogoFrame() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(238.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.cinders_souls_logo),
            contentDescription = "Cinder's Soul",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(238.dp)
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
        AuthOutlinedButton(text = "Continue with Google", enabled = !isLoading, onClick = onGoogle)
        TextButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            onClick = onLogin
        ) {
            Text("Log in", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AuthenticationForm(
    mode: AuthenticationMode,
    email: String,
    password: String,
    displayName: String,
    otp: String,
    newPassword: String,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoogle: () -> Unit,
    onForgot: () -> Unit,
    onSwitchMode: () -> Unit,
    onSendOtp: () -> Unit
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
                AuthTextField(
                    value = otp,
                    onValueChange = onOtpChange,
                    label = "OTP",
                    leadingIcon = Icons.Rounded.Lock,
                    isPassword = true
                )
                AuthTextField(
                    value = newPassword,
                    onValueChange = onNewPasswordChange,
                    label = "New password",
                    leadingIcon = Icons.Rounded.Lock,
                    isPassword = true
                )
            }

            AuthenticationMode.Landing -> Unit
        }

        if (mode == AuthenticationMode.ResetPassword) {
            AuthOutlinedButton(text = "Send OTP", enabled = !isLoading, onClick = onSendOtp)
        }

        AuthPrimaryButton(
            text = when (mode) {
                AuthenticationMode.Login -> "Log in"
                AuthenticationMode.Register -> "Create account"
                AuthenticationMode.ResetPassword -> "Reset password"
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

        AuthOutlinedButton(text = "Continue with Google", enabled = !isLoading, onClick = onGoogle)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (mode) {
                    AuthenticationMode.Register -> "Already have an account?"
                    AuthenticationMode.ResetPassword -> "Remember your password?"
                    else -> "New to Cinder's Soul?"
                },
                color = Color.White.copy(alpha = 0.62f),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = when (mode) {
                    AuthenticationMode.Register -> " Log in"
                    AuthenticationMode.ResetPassword -> " Log in"
                    else -> " Sign up"
                },
                color = Color.White,
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
                Text("Forgot password?", color = Color.White.copy(alpha = 0.86f))
            }
        }
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
        label = { Text(label) },
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
            Icon(imageVector = leadingIcon, contentDescription = null)
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
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = AuthCrimson,
            contentColor = Color.White
        )
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AuthOutlinedButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.42f), CircleShape),
        enabled = enabled,
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White
        )
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
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
