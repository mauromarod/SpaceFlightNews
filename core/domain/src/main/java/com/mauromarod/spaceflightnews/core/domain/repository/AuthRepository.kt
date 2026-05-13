package com.mauromarod.spaceflightnews.core.domain.repository

import com.mauromarod.spaceflightnews.core.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<AuthUser?>
    suspend fun signInAnonymously(): Result<AuthUser>
    suspend fun signInWithEmail(email: String, password: String): Result<AuthUser>
    suspend fun signUpWithEmail(email: String, password: String): Result<AuthUser>
    fun signOut()
    fun isLoggedIn(): Boolean
}
