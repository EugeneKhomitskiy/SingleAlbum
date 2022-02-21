package com.example.singlealbum.util

fun time(seconds: Int) : String {
    val min = seconds / 60 % 60
    val sec = seconds / 1 % 60
    return String.format("%02d:%02d", min, sec)
}