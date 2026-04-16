package com.example.mobileapp.Presentation.add

data class CategoryModel(
    val id: Int,
    val name: String,
    val icon: Int, // R.drawable.xxx
    var isSelected: Boolean = false
)