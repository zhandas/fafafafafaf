package com.example.mealjsonexample

object Graph {
    val mainScreen: Screen = Screen("MainScreen")
    val secondScreen: Screen = Screen("SecondScreen")
    val lastScreen:Screen=Screen("MealDetailScreen")
}

data class Screen(
    val route: String,
)