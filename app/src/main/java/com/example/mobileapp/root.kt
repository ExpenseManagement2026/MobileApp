package com.example.mobileapp

sealed interface root {
    val route: String

    data object Search : root { override val route = "search" }
}

