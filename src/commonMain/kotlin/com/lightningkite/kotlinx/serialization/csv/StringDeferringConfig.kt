package com.lightningkite.kotlinx.serialization.csv

import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

public class StringDeferringConfig(
    public val serializersModule: SerializersModule,
    public val ignoreUnknownKeys: Boolean = false,
    public val nullMarker: String = "null",
    public val deferMarker: String = "%",
    public val deferredFormat: StringFormat = Json {
        this.serializersModule = serializersModule
        this.ignoreUnknownKeys = ignoreUnknownKeys
        this.explicitNulls = true
        this.encodeDefaults = true
        this.isLenient = true;
    }
)