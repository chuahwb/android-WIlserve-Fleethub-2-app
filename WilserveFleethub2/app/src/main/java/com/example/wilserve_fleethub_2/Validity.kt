package com.example.wilserve_fleethub_2

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Validity(
    @Json(name = "validity") val validity:Boolean?
)