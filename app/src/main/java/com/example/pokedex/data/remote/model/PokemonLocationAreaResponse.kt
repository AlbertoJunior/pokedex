package com.example.pokedex.data.remote.model

import com.google.gson.annotations.SerializedName

data class LocationAreaEncounterElement(
    @SerializedName(value = "locationArea", alternate = ["location_area"])
    val locationArea: NameUrlObject? = null,
    @SerializedName(value = "versionDetails", alternate = ["version_details"])
    val versionDetails: List<VersionDetail>? = null
)

data class VersionDetail(
    @SerializedName(value = "encounterDetails", alternate = ["encounter_details"])
    val encounterDetails: List<EncounterDetail>? = null,
    @SerializedName(value = "maxChance", alternate = ["max_chance"])
    val maxChance: Long? = null,
    val version: NameUrlObject? = null
)

data class EncounterDetail(
    val chance: Long? = null,
    val method: NameUrlObject? = null,
    @SerializedName(value = "minLevel", alternate = ["min_level"])
    val minLevel: Long? = null,
    @SerializedName(value = "maxLevel", alternate = ["max_level"])
    val maxLevel: Long? = null
)