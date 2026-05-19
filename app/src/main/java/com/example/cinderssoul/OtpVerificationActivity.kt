package com.example.cinderssoul

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cinderssoul.network.ApiClient
import com.example.cinderssoul.network.AuthDataDto
import com.example.cinderssoul.network.ForgotPasswordRequest
import com.example.cinderssoul.network.ResetPasswordRequest
import com.example.cinderssoul.network.toApiMessage
import com.example.cinderssoul.admin.AdminActivity
import com.example.cinderssoul.ui.theme.CindersSoulTheme
import kotlinx.coroutines.launch

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

private val OtpCrimson = Color(0xFFE14A3B)
private val OtpFieldShape = RoundedCornerShape(28.dp)
private val OtpButtonShape = RoundedCornerShape(28.dp)
private val OtpFontFamily = FontFamily(
    Font(R.font.spotify_mix_ui_regular, FontWeight.Normal),
    Font(R.font.spotify_mix_ui_bold, FontWeight.Medium),
    Font(R.font.spotify_mix_ui_bold, FontWeight.SemiBold),
    Font(R.font.spotify_mix_ui_bold, FontWeight.Bold)
)
private val OtpTitleFontFamily = FontFamily(
    Font(R.font.spotify_mix_ui_title_bold, FontWeight.Bold),
    Font(R.font.spotify_mix_ui_title_extrabold, FontWeight.ExtraBold),
    Font(R.font.spotify_mix_ui_title_extrabold, FontWeight.Black)
)

class OtpVerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val email = intent.getStringExtra(EXTRA_EMAIL)?.trim().orEmpty()
        if (email.isBlank()) {
            finish()
            return
        }

        setContent {
            CindersSoulTheme(darkTheme = true) {
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                    OtpVerificationScreen(
                        email = email,
                        onBack = { finish() },
                        onAuthenticated = { authData ->
                            saveAuthSession(authData)
                            openAuthenticatedApp(authData.user.role.equals("admin", ignoreCase = true))
                        }
                    )
                }
            }
        }
    }

    private fun openAuthenticatedApp(isAdmin: Boolean) {
        val target = if (isAdmin) AdminActivity::class.java else MainActivity::class.java
        val intent = Intent(this, target).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
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

    companion object {
        private const val EXTRA_EMAIL = "extra_email"

        fun createIntent(context: Context, email: String): Intent {
            return Intent(context, OtpVerificationActivity::class.java)
                .putExtra(EXTRA_EMAIL, email)
        }
    }
}

@Composable
private fun OtpVerificationScreen(
    email: String,
    onBack: () -> Unit,
    onAuthenticated: (AuthDataDto) -> Unit
) {
    val scope = rememberCoroutineScope()
    var otp by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }

    BackHandler(onBack = onBack)

    fun runOtpAction(action: suspend () -> Unit) {
        scope.launch {
            isLoading = true
            message = null
            runCatching { action() }
                .onFailure { message = it.toApiMessage("Unable to verify OTP.") }
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2A0809),
                        Color(0xFF10080A),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(64.dp))
            Text(
                text = "Verify OTP",
                color = Color.White,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = OtpTitleFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp
                )
            )
            Text(
                text = "OTP sent to $email",
                color = Color.White.copy(alpha = 0.78f),
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = OtpFontFamily)
            )

            if (!message.isNullOrBlank()) {
                Text(
                    text = message.orEmpty(),
                    color = OtpCrimson.copy(alpha = 0.92f),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = OtpFontFamily),
                    textAlign = TextAlign.Start
                )
            }

            OtpTextField(
                value = otp,
                onValueChange = { otp = it },
                label = "OTP",
                isPassword = true
            )
            OtpTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "New password",
                isPassword = true
            )

            OtpPrimaryButton(
                text = "Reset password",
                enabled = !isLoading,
                onClick = {
                    val validationMessage = validateOtpResetInput(otp, newPassword)
                    if (validationMessage != null) {
                        message = validationMessage
                    } else {
                        runOtpAction {
                            val response = ApiClient.apiService.resetPassword(
                                ResetPasswordRequest(
                                    email = email,
                                    otp = otp.trim(),
                                    newPassword = newPassword
                                )
                            )
                            onAuthenticated(response.data ?: error(response.message ?: "Reset failed."))
                        }
                    }
                }
            )

            OtpOutlinedButton(
                text = "Resend OTP",
                enabled = !isLoading,
                onClick = {
                    runOtpAction {
                        val response = ApiClient.apiService.requestPasswordResetOtp(
                            ForgotPasswordRequest(email = email)
                        )
                        response.data ?: error(response.message ?: "Unable to send OTP.")
                        message = "OTP sent again to your email."
                    }
                }
            )

            Text(
                text = "Back to login",
                color = Color.White.copy(alpha = 0.86f),
                fontFamily = OtpFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(enabled = !isLoading, onClick = onBack)
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(26.dp),
                    color = OtpCrimson
                )
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 14.dp, top = 10.dp)
                .size(44.dp)
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
private fun OtpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(label, fontFamily = OtpFontFamily) },
        shape = OtpFieldShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.08f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
            focusedBorderColor = OtpCrimson,
            unfocusedBorderColor = Color.White.copy(alpha = 0.18f),
            focusedLabelColor = OtpCrimson,
            unfocusedLabelColor = Color.White.copy(alpha = 0.62f),
            focusedLeadingIconColor = OtpCrimson,
            unfocusedLeadingIconColor = Color.White.copy(alpha = 0.62f),
            cursorColor = OtpCrimson
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                modifier = Modifier.padding(start = 8.dp)
            )
        },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
    )
}

@Composable
private fun OtpPrimaryButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        onClick = onClick,
        shape = OtpButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = OtpCrimson,
            contentColor = Color.White
        )
    ) {
        Text(text = text, fontFamily = OtpFontFamily, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun OtpOutlinedButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        onClick = onClick,
        shape = OtpButtonShape,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
    ) {
        Text(text = text, fontFamily = OtpFontFamily, fontWeight = FontWeight.Bold)
    }
}

private fun validateOtpResetInput(otp: String, newPassword: String): String? {
    return when {
        otp.isBlank() -> "OTP is required."
        !otp.trim().matches(Regex("^\\d{6}$")) -> "OTP must be 6 digits."
        newPassword.isBlank() -> "New password is required."
        newPassword.length < 6 -> "New password must be at least 6 characters long."
        else -> null
    }
}
