package com.mauromarod.spaceflightnews.core.domain.model

data class AuthUser(
    val uid: String,
    val email: String?,
    val isAnonymous: Boolean,
    val displayName: String? = null,
    val photoUrl: String? = null,
)
