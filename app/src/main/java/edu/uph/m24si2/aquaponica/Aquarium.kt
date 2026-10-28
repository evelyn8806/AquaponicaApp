package edu.uph.m24si2.aquaponica

data class Aquarium(
    val id: String = "",
    val ownerUid: String = "",
    val name: String = "",
    val lengthCm: Double = 0.0,
    val widthCm: Double = 0.0,
    val heightCm: Double = 0.0,
    val fishType: String = "",
    val fishCount: Int = 0,
    val plantType: String = "",
    val userConcern: String = "",
    val feedingTimes: List<String> = emptyList()
)