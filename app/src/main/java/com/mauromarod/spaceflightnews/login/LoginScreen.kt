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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mauromarod.spaceflightnews.R
import com.mauromarod.spaceflightnews.core.designsystem.VergeCanvas
import com.mauromarod.spaceflightnews.core.designsystem.VergeConsoleMintBorder
import com.mauromarod.spaceflightnews.core.designsystem.VergeHazardWhite
import com.mauromarod.spaceflightnews.core.designsystem.VergeJellyMint
import com.mauromarod.spaceflightnews.core.designsystem.VergeSecondaryText
import com.mauromarod.spaceflightnews.core.designsystem.VergeSurfaceSlate
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
            .background(VergeCanvas)
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

            Text(
                text = stringResource(R.string.app_name).uppercase(),
                style = MaterialTheme.typography.displayLarge,
                color = VergeJellyMint,
                textAlign = TextAlign.Center,
            )

            Text(
                text = stringResource(R.string.login_app_subtitle).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = VergeSecondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = MaterialTheme.spacing.xSmall),
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
                    ).uppercase(),
                    color = VergeJellyMint,
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            Spacer(Modifier.height(MaterialTheme.spacing.medium))

            HorizontalDivider(color = VergeHazardWhite.copy(alpha = 0.15f))

            Spacer(Modifier.height(MaterialTheme.spacing.medium))

            OutlinedButton(
                onClick = viewModel::signInAnonymously,
                enabled = uiState !is LoginUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(LoginTags.GUEST_BUTTON),
                shape = MaterialTheme.shapes.extraLarge,
                border = androidx.compose.foundation.BorderStroke(1.dp, VergeConsoleMintBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VergeSecondaryText),
            ) {
                Text(
                    text = stringResource(R.string.login_continue_as_guest).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                )
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
        focusedBorderColor = VergeJellyMint,
        focusedLabelColor = VergeJellyMint,
        cursorColor = VergeJellyMint,
        unfocusedBorderColor = VergeHazardWhite.copy(alpha = 0.4f),
        unfocusedLabelColor = VergeSecondaryText,
        focusedTextColor = VergeHazardWhite,
        unfocusedTextColor = VergeHazardWhite,
        focusedContainerColor = VergeCanvas,
        unfocusedContainerColor = VergeCanvas,
    )

    val fieldShape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)

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
                    tint = VergeSecondaryText,
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
            containerColor = VergeJellyMint,
            contentColor = VergeCanvas,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = VergeCanvas,
            )
        } else {
            Text(
                text = stringResource(if (isSignUp) R.string.login_create_account else R.string.login_sign_in).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = VergeCanvas,
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
