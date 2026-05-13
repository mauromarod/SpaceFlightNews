package com.mauromarod.spaceflightnews.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LaunchRefDto(
    @Json(name = "launch_id") val launchId: String,
    @Json(name = "provider") val provider: String
)
