package com.mauromarod.spaceflightnews.auth

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mauromarod.spaceflightnews.core.domain.model.AuthUser
import com.mauromarod.spaceflightnews.core.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics,
) : AuthRepository {

    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser?.toAuthUser()
            // Re-affirm on every session (restore or new login) so Remote Config
            // conditions on login_method are evaluated with fresh in-memory data.
            if (user != null) {
                restoreSession(user)
            } else {
                analytics.setUserProperty("login_method", null)
            }
            trySend(user)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInAnonymously(): Result<AuthUser> = runCatching {
        val result = auth.signInAnonymously().await()
        val user = result.user?.toAuthUser() ?: error("Anonymous sign-in returned null user")
        onLoginSuccess(user, method = "anonymous")
        user
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> =
        runCatching {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user?.toAuthUser() ?: error("Email sign-in returned null user")
            onLoginSuccess(user, method = "email")
            user
        }

    override suspend fun signUpWithEmail(email: String, password: String): Result<AuthUser> =
        runCatching {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user?.toAuthUser() ?: error("Sign-up returned null user")
            onLoginSuccess(user, method = "email")
            user
        }

    override fun signOut() {
        auth.signOut()
        crashlytics.setUserId("")
        analytics.setUserProperty("login_method", null)
    }

    override fun isLoggedIn(): Boolean {
        val user = auth.currentUser ?: return false
        // Called synchronously from AppNavHost before RemoteConfigLifecycleObserver.onResume()
        // fires fetchAndActivate() — guarantees the User Property is in-memory before the fetch.
        restoreSession(user.toAuthUser())
        return true
    }

    private fun onLoginSuccess(user: AuthUser, method: String) {
        crashlytics.setUserId(user.uid)
        analytics.setUserProperty("login_method", method)
    }

    private fun restoreSession(user: AuthUser) {
        crashlytics.setUserId(user.uid)
        analytics.setUserProperty("login_method", if (user.isAnonymous) "anonymous" else "email")
    }
}

private fun com.google.firebase.auth.FirebaseUser.toAuthUser() =
    AuthUser(
        uid = uid,
        email = email,
        isAnonymous = isAnonymous,
        displayName = displayName,
        photoUrl = photoUrl?.toString(),
    )
