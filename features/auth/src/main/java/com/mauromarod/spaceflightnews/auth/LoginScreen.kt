package com.mauromarod.spaceflightnews.auth

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mauromarod.spaceflightnews.core.designsystem.spacing

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun LoginScreen(
    onNavigateToNews: () -> Unit,
    viewModel: LoginViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var isSignUp by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) onNavigateToNews()
    }

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Error) {
            snackbarHostState.showSnackbar(context.getString((uiState as LoginUiState.Error).messageRes))
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics { testTagsAsResourceId = true }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.systemBars)
                .imePadding()
                .padding(horizontal = MaterialTheme.spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LoginHeader(
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.xLarge)
            )

            LoginForm(
                isLoading = uiState is LoginUiState.Loading,
                isSignUp = isSignUp,
                onSignInWithEmail = viewModel::signInWithEmail,
                onSignUpWithEmail = viewModel::signUpWithEmail,
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.small),
            )

            LoginModeSwitch(
                isSignUp = isSignUp,
                isLoading = uiState is LoginUiState.Loading,
                onToggle = { isSignUp = !isSignUp },
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium),
            )

            LoginGuestButton(
                isLoading = uiState is LoginUiState.Loading,
                onSignInAnonymously = viewModel::signInAnonymously,
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Suppress("LongMethod")
@Composable
private fun LoginForm(
    isLoading: Boolean,
    isSignUp: Boolean,
    onSignInWithEmail: (email: String, password: String) -> Unit,
    onSignUpWithEmail: (email: String, password: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val onSubmit = {
        focusManager.clearFocus()
        if (isSignUp) onSignUpWithEmail(email, password) else onSignInWithEmail(email, password)
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        cursorColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTextColor = MaterialTheme.colorScheme.onBackground,
        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    )

    val fieldShape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)

    Column(modifier = modifier) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.login_email_label)) },
            singleLine = true,
            shape = fieldShape,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = fieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(LoginTags.EMAIL_FIELD),
        )

        Spacer(Modifier.height(MaterialTheme.spacing.small))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.login_password_label)) },
            singleLine = true,
            shape = fieldShape,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = stringResource(
                            if (passwordVisible) R.string.login_password_hide else R.string.login_password_show
                        ),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            colors = fieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(LoginTags.PASSWORD_FIELD),
        )

        Spacer(Modifier.height(MaterialTheme.spacing.medium))

        Button(
            onClick = { onSubmit() },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(if (isSignUp) LoginTags.SIGNUP_SUBMIT_BUTTON else LoginTags.LOGIN_SUBMIT_BUTTON),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(
                    text = stringResource(if (isSignUp) R.string.login_create_account else R.string.login_sign_in).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun LoginHeader(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
        text = stringResource(R.string.app_name).uppercase(),
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
    )

    Text(
        text = stringResource(R.string.login_app_subtitle).uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = MaterialTheme.spacing.xSmall),
    )
    }
}

@Composable
private fun LoginModeSwitch(
    isSignUp: Boolean,
    isLoading: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onToggle,
        enabled = !isLoading,
        modifier = modifier.testTag(LoginTags.LOGIN_SIGNUP_BUTTON),
    ) {
        Text(
            text = stringResource(
                if (isSignUp) R.string.login_switch_to_signin else R.string.login_switch_to_signup
            ).uppercase(),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun LoginGuestButton(
    isLoading: Boolean,
    onSignInAnonymously: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onSignInAnonymously,
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .testTag(LoginTags.GUEST_BUTTON),
        shape = MaterialTheme.shapes.extraLarge,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
    ) {
        Text(
            text = stringResource(R.string.login_continue_as_guest).uppercase(),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

object LoginTags {
    const val EMAIL_FIELD = "login_email_field"
    const val PASSWORD_FIELD = "login_password_field"
    const val LOGIN_SUBMIT_BUTTON = "login_submit_button"
    const val SIGNUP_SUBMIT_BUTTON = "signup_submit_button"
    const val GUEST_BUTTON = "login_guest_button"
    const val LOGIN_SIGNUP_BUTTON = "login_signup_button"
}
