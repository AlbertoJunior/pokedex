package com.example.pokedex.data.remote.model

data class PokemonDetailedResponse(
    val name: String,
    val url: String,
    val abilities: List<Ability>,
    val moves: List<Move>,
    val height: Long,
    val locationAreaEncounters: String,
    val baseExperience: Long,
    val species: Species,
    val stats: List<Stat>,
    val sprites: Sprites,
    val types: List<Type>,
    val weight: Long
)

data class Ability(
    val name: String,
    val isHidden: Boolean,
    val slot: Long,
    val url: String,
)

data class Species(
    val name: String,
    val url: String
)

data class Move(
    val name: String,
    val url: String,
//    val versionGroupDetails: List<VersionGroupDetail>
)

//data class VersionGroupDetail(
//    val levelLearnedAt: Long,
//    val moveLearnMethod: Species,
//    val versionGroup: Species
//)

data class Sprites(
    val other: Other
)

data class Other(
    val dreamWorld: DreamWorld,
    val home: Home,
    val officialArtwork: OfficialArtwork
)

data class DreamWorld(
    val frontDefault: String,
    val frontFemale: Any? = null
)

data class Home(
    val frontDefault: String,
    val frontFemale: Any? = null,
    val frontShiny: String,
    val frontShinyFemale: Any? = null
)

data class OfficialArtwork(
    val frontDefault: String
)

data class Stat(
    val baseStat: Long,
    val effort: Long,
    val name: String,
    val url: String,
)

data class Type(
    val slot: Long,
    val name: String,
    val url: String
)
