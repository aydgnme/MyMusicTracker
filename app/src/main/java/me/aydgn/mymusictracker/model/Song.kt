package me.aydgn.mymusictracker.model

data class Song(
    val title: String,
    val artist: String,
    val album: String,
    var isFavorite: Boolean = false
)