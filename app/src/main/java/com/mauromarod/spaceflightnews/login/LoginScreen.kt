package com.mauromarod.spaceflightnews.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mauromarod.spaceflightnews.R
import com.mauromarod.spaceflightnews.core.designsystem.SpaceBlue
import com.mauromarod.spaceflightnews.core.designsystem.SpaceDeepNavy
import com.mauromarod.spaceflightnews.core.designsystem.SpaceOnSurfaceVariant
import com.mauromarod.spaceflightnews.core.designsystem.SpaceOutline
import com.mauromarod.spaceflightnews.core.designsystem.spacing

@Composable
fun LoginScreen(
    onNavigateToNews: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var isSignUp by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is LoginUiEffect.NavigateToNews -> onNavigateToNews()
            }
        }
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
            .background(SpaceDeepNavy)
            .semantics { testTagsAsResourceId = true }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = MaterialTheme.spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.height(MaterialTheme.spacing.xLarge))

            Icon(
                imageVector = Icons.Outlined.RocketLaunch,
                contentDescription = null,
                tint = SpaceBlue,
                modifier = Modifier.size(72.dp)
            )

            Spacer(Modifier.height(MaterialTheme.spacing.medium))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = SpaceBlue,
                textAlign = TextAlign.Center,
            )

            Text(
                text = stringResource(R.string.login_app_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = SpaceOnSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(MaterialTheme.spacing.xLarge))

            LoginForm(
                isLoading = uiState is LoginUiState.Loading,
                isSignUp = isSignUp,
                onSignInWithEmail = viewModel::signInWithEmail,
                onSignUpWithEmail = viewModel::signUpWithEmail,
            )

            Spacer(Modifier.height(MaterialTheme.spacing.small))

            TextButton(
                onClick = { isSignUp = !isSignUp },
                enabled = uiState !is LoginUiState.Loading,
                modifier = Modifier.testTag(LoginTags.LOGIN_SIGNUP_BUTTON),
            ) {
                Text(
                    text = stringResource(
                        if (isSignUp) R.string.login_switch_to_signin else R.string.login_switch_to_signup
                    ),
                    color = SpaceBlue,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(Modifier.height(MaterialTheme.spacing.medium))

            HorizontalDivider(color = SpaceOutline)

            Spacer(Modifier.height(MaterialTheme.spacing.medium))

            OutlinedButton(
                onClick = viewModel::signInAnonymously,
                enabled = uiState !is LoginUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(LoginTags.GUEST_BUTTON),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SpaceOnSurfaceVariant),
            ) {
                Text(stringResource(R.string.login_continue_as_guest))
            }

            Spacer(Modifier.height(MaterialTheme.spacing.xLarge))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun LoginForm(
    isLoading: Boolean,
    isSignUp: Boolean,
    onSignInWithEmail: (email: String, password: String) -> Unit,
    onSignUpWithEmail: (email: String, password: String) -> Unit,
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
        focusedBorderColor = SpaceBlue,
        focusedLabelColor = SpaceBlue,
        cursorColor = SpaceBlue,
        unfocusedBorderColor = SpaceOutline,
        unfocusedLabelColor = SpaceOnSurfaceVariant,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
    )

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text(stringResource(R.string.login_email_label)) },
        singleLine = true,
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
                    tint = SpaceOnSurfaceVariant,
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
        colors = ButtonDefaults.buttonColors(containerColor = SpaceBlue),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = SpaceDeepNavy,
            )
        } else {
            Text(
                text = stringResource(if (isSignUp) R.string.login_create_account else R.string.login_sign_in),
                color = SpaceDeepNavy,
            )
        }
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
