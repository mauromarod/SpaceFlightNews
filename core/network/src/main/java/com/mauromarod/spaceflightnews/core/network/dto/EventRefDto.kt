package com.mauromarod.spaceflightnews.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EventRefDto(
    @Json(name = "event_id") val eventId: Int,
    @Json(name = "provider") val provider: String
)
